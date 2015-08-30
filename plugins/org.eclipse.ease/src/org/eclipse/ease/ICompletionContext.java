/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

import java.util.Collection;
import java.util.Map;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.ScriptType;

/**
 * Interface for completion context. This context helps ICompletionProvider to simplify completion proposal calculation. Stores information about given input,
 * filter for part of interest, and Source stack for part of interest.
 *
 * @author Martin Kloesch
 */
public interface ICompletionContext {

	public enum Type {
		UNKNOWN, NONE, STATIC_CLASS, CLASS_INSTANCE, PACKAGE, STRING_LITERAL
	};

	String getOriginalCode();

	String getProcessedCode();

	String getFilter();

	Class<? extends Object> getReferredClazz();

	/**
	 * Get the base resource of the context. Typically holds a reference to the file open in an editor
	 *
	 * @return base resource or <code>null</code>
	 */
	Object getResource();

	/**
	 * Get the running script engine. Only works for live engines like a shell.
	 *
	 * @return script engine or <code>null</code>
	 */
	IScriptEngine getScriptEngine();

	ScriptType getScriptType();

	/**
	 * Get a list of loaded modules.
	 *
	 * @return loaded modules
	 */
	Collection<ModuleDefinition> getLoadedModules();

	/**
	 * Get a list of included resource. Returns a map of resource objects -> resource content.
	 *
	 * @return map of included resources
	 */
	Map<Object, String> getIncludedResources();

	public Type getType();

	int getOffset();

	int getSelectionRange();

	/**
	 * Returns the package for PACKAGE types.
	 *
	 * @return package name
	 */
	String getPackage();

	/**
	 * Get the caller method for string literals. On STRING_LITERAL types this value denotes the calling method. The whole context of the caller is passed as a
	 * value. Eg. "new java.lang.String"
	 *
	 * @return calling method
	 */
	String getCaller();

	/**
	 * Get the index of the parameter for string literals. On STRING_LITERAL types this value indicates which parameter we are looking at: 0 for the first, 1
	 * for the second, ...
	 *
	 * @return parameter offset for string literals
	 */
	int getParameterOffset();

}