/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.debug.ITracingConstant;
import org.eclipse.ease.debug.Tracer;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * The Environment provides base functions for all script interpreters. It is automatically loaded by any interpreter upon startup.
 */
public class EnvironmentModule extends AbstractEnvironment {

	public static final String MODULE_NAME = "/System/Environment";

	public static final String MODULE_PREFIX = "__MOD_";

	public EnvironmentModule() {
	}

	/**
	 * Creates wrapper functions for a given java instance. Searches for members and methods annotated with {@link WrapToScript} and creates wrapping code in
	 * the target script language. A method named &lt;instance&gt;.myMethod() will be made available by calling myMethod().
	 *
	 * @param toBeWrapped
	 *            instance to be wrapped
	 */
	@Override
	@WrapToScript
	public void wrap(final Object toBeWrapped) {
		// register new variable in script engine
		final String identifier = getScriptEngine().getSaveVariableName(getWrappedVariableName(toBeWrapped));

		final boolean reloaded = getScriptEngine().hasVariable(identifier);
		getScriptEngine().setVariable(identifier, toBeWrapped);

		// FIXME move to script engine
		if (ITracingConstant.ENVIRONEMENT_MODULE_WRAPPER_TRACING) {
			Tracer.logInfo("[Environment Module] Add variable to engine :\n " + toBeWrapped.toString() + " with value" + toBeWrapped);
		}

		// create function wrappers
		createWrappers(toBeWrapped, identifier, reloaded);

		// notify listeners
		fireModuleEvent(toBeWrapped, reloaded ? IModuleListener.RELOADED : IModuleListener.LOADED);
	}

	public static final String getWrappedVariableName(final Object toBeWrapped) {
		return (MODULE_PREFIX + toBeWrapped.getClass().getName()).replace('.', '_');
	}

	/**
	 * Create JavaScript wrapper functions for autoload methods. Adds code of following style: <code>function {name} (a, b, c, ...) {
	 * __module.{name}(a, b, c, ...);
	 * }</code>
	 *
	 * @param instance
	 *            module instance to create wrappers for
	 * @param reload
	 *            flag indicating that the module was already loaded
	 */
	private void createWrappers(final Object instance, final String identifier, final boolean reload) {
		// script code to inject
		final StringBuilder scriptCode = new StringBuilder();

		// create wrappers for methods
		for (final Method method : ModuleHelper.getMethods(instance.getClass())) {
			final String code = getWrapper().createFunctionWrapper(this, identifier, method);

			if ((code != null) && !code.isEmpty()) {
				scriptCode.append(code);
				scriptCode.append('\n');
			}
		}

		// create wrappers for static fields
		if (!reload) {
			// this is only done upon initial loading as we try to create constants here
			for (final Field field : ModuleHelper.getFields(instance.getClass())) {
				try {

					// only wrap if field is not already declared
					if (!getScriptEngine().hasVariable(getWrapper().getSaveVariableName(field.getName()))) {
						final String code = getWrapper().createStaticFieldWrapper(this, identifier, field);

						if ((code != null) && !code.isEmpty()) {
							scriptCode.append(code);
							scriptCode.append('\n');
						}
					} else {
						Logger.logWarning("Skipped wrapping of field \"" + field.getName() + "\" (module \"" + instance.getClass().getName()
								+ "\") as variable is already declared.");
					}

				} catch (final IllegalArgumentException e) {
					Logger.logError("Could not wrap field \"" + field.getName() + " \" of module \"" + instance.getClass() + "\".");
				}
			}
		}

		// execute code
		final String codeToInject = scriptCode.toString();
		// FIXME move log to script engine
		if (ITracingConstant.ENVIRONEMENT_MODULE_WRAPPER_TRACING) {
			Tracer.logInfo("[Environement Module] Injecting code:\n" + codeToInject);
		}

		getScriptEngine().inject(new Script("Wrapper(" + instance.getClass().getSimpleName() + ")", codeToInject));
	}

	/**
	 * Execute script code. This method executes script code directly in the running interpreter. Execution is done in the same thread as the caller thread.
	 *
	 * @param data
	 *            code to be interpreted
	 * @return result of code execution
	 */
	@WrapToScript
	public final Object execute(final Object data) {
		return getScriptEngine().inject(data);
	}

	/**
	 * Terminates script execution immediately. Code following this command will not be executed anymore.
	 *
	 * @param value
	 *            return code
	 */
	@WrapToScript
	public final void exit(final @ScriptParameter(defaultValue = ScriptParameter.NULL) Object value) {
		throw new ExitException(value);
	}

	/**
	 * Include and execute a script file. Quite similar to eval(Object) a source file is opened and its content is executed. Multiple sources are available:
	 * "workspace://" opens a file relative to the workspace root, "project://" opens a file relative to the current project, "file://" opens a file from the
	 * file system. All other types of URIs are supported too (like http:// ...). You may also use absolute and relative paths as defined by your local file
	 * system.
	 *
	 * @param filename
	 *            name of file to be included
	 * @return result of include operation
	 * @throws Throwable
	 */
	@WrapToScript
	public final Object include(final String filename) {
		final Object file = ResourceTools.resolveFile(filename, getScriptEngine().getExecutedFile(), true);
		if (file != null)
			return getScriptEngine().inject(file);

		else {

			// maybe this is a URI
			try {
				final URL url = new URL(filename);
				return getScriptEngine().inject(url.openStream());

			} catch (final MalformedURLException e) {
			} catch (final IOException e) {
			}
		}

		// giving up
		throw new RuntimeException("Cannot locate '" + filename + "'");
	}

	/**
	 * Get the current script engine instance.
	 *
	 * @return {@link IScriptEngine} instance
	 */
	@WrapToScript
	@Override
	public IScriptEngine getScriptEngine() {

		final IScriptEngine engine = super.getScriptEngine();
		if (engine == null) {
			final Job currentJob = Job.getJobManager().currentJob();
			if (currentJob instanceof IScriptEngine)
				return (IScriptEngine) currentJob;
		}

		return engine;
	}

	/**
	 * Get the generic script wrapper registered for this script engine.
	 *
	 * @return script wrapper
	 */
	private IModuleWrapper getWrapper() {
		// use the static access method for the service as we might run without UI (workbench would not be available)
		return ScriptService.getService().getModuleWrapper(getScriptEngine().getDescription().getID());
	}

	/**
	 * Add a jar file to the classpath. Contents of the jar can be accessed right after loading. <i>location</i> can be an URI, a path, a File or an IFile
	 * instance.
	 *
	 * @param location
	 *            URI, Path, File or IFile
	 * @throws MalformedURLException
	 *             invalid URL detected
	 */
	@WrapToScript
	public void loadJar(Object location) throws MalformedURLException {
		if (!(location instanceof URL)) {
			// try to resolve workspace URIs
			Object file = ResourceTools.resolveFile(location.toString(), getScriptEngine().getExecutedFile(), true);

			if (file instanceof IFile)
				file = ((IFile) file).getFullPath().toFile();

			if (file instanceof File)
				location = ((File) file).toURI().toURL();

			else
				location = new URL(location.toString());
		}

		if (location instanceof URL)
			getScriptEngine().registerJar((URL) location);
	}

	/**
	 * Open help page on addressed topic. If the given topic matches a method or field from a loaded module, the definition will be opened. If the topic is
	 * unknown, a search in the whole eclipse help will be launched.
	 *
	 * @param topic
	 *            help topic to open (typically a function name)
	 */
	@WrapToScript
	public void help(@ScriptParameter(defaultValue = ScriptParameter.NULL) final String topic) {

		if (PlatformUI.isWorkbenchRunning()) {
			if (topic != null) {
				for (final Object module : getModules()) {
					final ModuleDefinition definition = ModuleDefinition.getDefinition(module);
					if (definition != null) {
						// look for matching method
						for (final Method method : definition.getMethods()) {
							if (matchesMethod(method, topic)) {
								// method found, display help

								final String link = definition.getHelpLocation(method.getName());
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(link);
									}
								});

								// done
								return;
							}
						}

						for (final Field field : definition.getFields()) {
							if (matchesField(field, topic)) {
								// field found, display help

								final String link = definition.getHelpLocation(field.getName());
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(link);
									}
								});

								// done
								return;
							}
						}
					}
				}

				// nothing found, start a search in help
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						PlatformUI.getWorkbench().getHelpSystem().search(topic);
					}
				});
			} else {
				// no topic provided, show main help page
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						PlatformUI.getWorkbench().getHelpSystem().displayHelp();
					}
				});
			}
		}
	}

	/**
	 * Verify that a given name matches the field name or one of its aliases.
	 *
	 * @param field
	 *            field to query
	 * @param name
	 *            name to match
	 * @return <code>true</code> on match
	 */
	private boolean matchesField(final Field field, final String name) {
		if (name.equalsIgnoreCase(field.getName()))
			return true;

		final WrapToScript wrapAnnotation = field.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String alias : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (name.equalsIgnoreCase(alias.trim()))
					return true;
		}

		return false;
	}

	/**
	 * Verify that a given name matches the method name or one of its aliases.
	 *
	 * @param method
	 *            method to query
	 * @param name
	 *            name to match
	 * @return <code>true</code> on match
	 */
	private boolean matchesMethod(final Method method, final String name) {
		if (name.equalsIgnoreCase(method.getName()))
			return true;

		final WrapToScript wrapAnnotation = method.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String alias : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (name.equalsIgnoreCase(alias.trim()))
					return true;
		}

		return false;
	}
}
