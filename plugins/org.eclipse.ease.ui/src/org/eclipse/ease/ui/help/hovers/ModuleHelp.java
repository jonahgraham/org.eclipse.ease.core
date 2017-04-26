/*******************************************************************************
 * Copyright (c) 2015 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *     Christian Pontesegger - adaptions to parse improved HTML help files
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesTools;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

public class ModuleHelp {

	private static final Map<String, String> CACHED_IMAGES = new HashMap<>();

	/**
	 * When we need to add images to HTML sites we need to copy them over to the file system.
	 *
	 * @param bundlePath
	 *            path within org.eclipse.ease.ui plugin
	 * @return file system path
	 */
	private static String getImageLocation(String bundlePath) {

		if (!CACHED_IMAGES.containsKey(bundlePath)) {
			final InputStream input = Activator.getResource(bundlePath);
			if (input != null) {
				try {
					final File tempFile = File.createTempFile("EASE_image", "png");
					tempFile.deleteOnExit();
					final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));

					final InputStream inputStream = new BufferedInputStream(input);
					final byte[] buffer = new byte[1024];

					int bytes = inputStream.read(buffer);
					while (bytes != -1) {
						outputStream.write(buffer, 0, bytes);
						bytes = inputStream.read(buffer);
					}

					inputStream.close();
					outputStream.close();

					CACHED_IMAGES.put(bundlePath, tempFile.toURI().toString());
				} catch (final FileNotFoundException e) {
					Logger.error(Activator.PLUGIN_ID, "Cannot find image file for help hover", e);
					return null;

				} catch (final IOException e) {
					Logger.error(Activator.PLUGIN_ID, "Cannot create image file for help hover", e);
					return null;
				}
			}
		}

		return CACHED_IMAGES.get(bundlePath);
	}

	/**
	 * Retrieve help page for a given module definition.
	 *
	 * @param definition
	 *            module definition to fetch help for
	 * @return help content (HTML body node)
	 */
	private static IMemento getHelpContent(final ModuleDefinition definition) {

		if (definition != null) {
			final String helpLocation = definition.getHelpLocation(null);
			final URL url = PlatformUI.getWorkbench().getHelpSystem().resolve(helpLocation, true);
			try {
				final IMemento rootNode = XMLMemento.createReadRoot(new InputStreamReader(url.openStream(), "UTF-8"));
				return rootNode.getChild("body");
			} catch (final Exception e) {
				Logger.error(Activator.PLUGIN_ID, "Cannot find the module help content ", e);
			}
		}

		return null;
	}

	/**
	 * Retrieve help content for module definition.
	 *
	 * @param definition
	 *            module definition to fetch help for
	 * @return help content
	 */
	public static String getModuleHelpTip(final ModuleDefinition definition) {

		final IMemento bodyNode = getHelpContent(definition);
		if (bodyNode != null) {

			final StringBuffer helpContent = new StringBuffer();
			for (final IMemento node : bodyNode.getChildren()) {
				if ("module".equals(node.getString("class"))) {
					for (final IMemento contentNode : node.getChildren()) {
						if ("description".equals(contentNode.getString("class"))) {
							final String content = getNodeContent(contentNode);
							if ((content != null) && (!content.isEmpty()))
								helpContent.append(content);
						}
					}
				}
			}

			if (helpContent.length() > 0) {
				final StringBuffer help = new StringBuffer();
				HTMLPrinter.addSmallHeader(help, getImageAndLabel(getImageLocation("icons/eobj16/module.png"), definition.getName()));
				help.append("<br />"); //$NON-NLS-1$
				help.append(helpContent);

				return help.toString();
			}
		}

		return null;
	}

	public static String getImageAndLabel(String imageSrcPath, String label) {
		final StringBuffer buf = new StringBuffer();
		final int imageWidth = 16;
		final int imageHeight = 16;
		final int labelLeft = 20;
		final int labelTop = 2;

		buf.append("<div style='word-wrap: break-word; position: relative; "); //$NON-NLS-1$

		if (imageSrcPath != null) {
			buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("padding-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		buf.append("'>"); //$NON-NLS-1$
		if (imageSrcPath != null) {
			final StringBuffer imageStyle = new StringBuffer("border:none; position: absolute; "); //$NON-NLS-1$
			imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("left: ").append(-labelLeft - 1).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$

			// hack for broken transparent PNG support in IE 6, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=223900 :
			buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
			final String tooltip = ""; //$NON-NLS-1$
			buf.append("<span ").append(tooltip).append("style=\"").append(imageStyle). //$NON-NLS-1$ //$NON-NLS-2$
					append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageSrcPath).append("')\"></span>\n"); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$

			buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
			buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
			buf.append("<img ").append(tooltip).append("style='").append(imageStyle).append("' src='").append(imageSrcPath).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			buf.append("<![endif]-->\n"); //$NON-NLS-1$
		}

		buf.append(label);

		buf.append("</div>"); //$NON-NLS-1$
		return buf.toString();
	}

	/**
	 * Creates a link with the given URI and label text.
	 *
	 * @param uri
	 *            the URI
	 * @param label
	 *            the label
	 * @return the HTML link
	 * @since 3.6
	 */
	public static String createLink(String uri, String label) {
		return "<a class='header' href='" + uri + "'>" + label + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Retrieve help content for module method.
	 *
	 * @param method
	 *            module method to fetch help for
	 * @return help content
	 */
	public static String getMethodHelpTip(final Method method) {

		// FIXME do not use getDeclaringMethod, see bug 502854
		final IMemento bodyNode = getHelpContent(ModulesTools.getDeclaringModule(method));
		if (bodyNode != null) {

			for (final IMemento node : bodyNode.getChildren("div")) {
				if ((method.getName().equals(node.getString("data-method"))) && ("command".equals(node.getString("class")))) {
					// method found

					final StringBuffer helpContent = new StringBuffer();

					HTMLPrinter.addSmallHeader(helpContent, getImageAndLabel(getImageLocation("icons/eobj16/function.png"), createSynopsis(method)));
					helpContent.append("<br />"); //$NON-NLS-1$

					// method description
					for (final IMemento contentNode : node.getChildren()) {
						if ("description".equals(contentNode.getString("class"))) {
							helpContent.append("<p>");
							helpContent.append(getNodeContent(contentNode));
							helpContent.append("</p>");
						}
					}

					// method parameters

					if ((method.getParameters().length > 0)) {
						final Map<String, String> parameterDescription = extractDescriptions(node, "parameters", "data-parameter");

						helpContent.append("<dl>");
						if (method.getParameters().length > 0) {
							helpContent.append("<dt>Parameters:</dt>");
							for (final Parameter parameter : method.getParameters()) {
								helpContent.append("<dd>");
								helpContent.append("<b>");
								if (parameter.isAnnotationPresent(ScriptParameter.class))
									helpContent.append("<i>");

								helpContent.append(parameter.getName());
								if (parameter.isAnnotationPresent(ScriptParameter.class))
									helpContent.append("</i>");

								helpContent.append("</b> ");
								if (parameterDescription.containsKey(parameter.getName()))
									helpContent.append(parameterDescription.get(parameter.getName()));

								helpContent.append("</dd>");
							}
							helpContent.append("</dl>");
						}
					}

					// return value
					final String returnValueDescription = extractReturnValueDescription(node);
					if (returnValueDescription != null) {
						helpContent.append("<dl>");
						helpContent.append("<dt>Returns:</dt>");
						helpContent.append("<dd>");
						helpContent.append(returnValueDescription);
						helpContent.append("</dd>");
						helpContent.append("</dl>");
					}

					// exceptions
					if (method.getExceptionTypes().length > 0) {
						final Map<String, String> exceptionDescription = extractDescriptions(node, "exceptions", "data-exception");

						helpContent.append("<dl>");
						helpContent.append("<dt>Throws:</dt>");

						for (final Class<?> exceptionType : method.getExceptionTypes()) {
							helpContent.append("<dd>");
							helpContent.append("<b>").append(createLink("some location", exceptionType.getSimpleName())).append("</b>");

							if (exceptionDescription.containsKey(exceptionType.getSimpleName()))
								helpContent.append(" - ").append(exceptionDescription.get(exceptionType.getSimpleName()));

							else if (exceptionDescription.containsKey(exceptionType.getName()))
								helpContent.append(" - ").append(exceptionDescription.get(exceptionType.getName()));

							helpContent.append("</dd>");
						}

						helpContent.append("</dl>");
					}

					// examples
					final Map<String, String> examples = extractExamples(node);
					if (!examples.isEmpty()) {
						helpContent.append("<dl>");
						helpContent.append("<dt>Examples:</dt>");

						for (final Entry<String, String> example : examples.entrySet()) {
							helpContent.append("<dd><div class=\"code\">");
							helpContent.append(example.getKey());
							helpContent.append("</div><div class=\"description\">");
							helpContent.append(example.getValue());
							helpContent.append("</div></dd>");
						}

						helpContent.append("</dl>");
					}

					return helpContent.toString();
				}
			}
		}

		return null;
	}

	private static String extractReturnValueDescription(IMemento methodNode) {
		for (final IMemento node : methodNode.getChildren()) {
			if ("return".equals(node.getString("class")))
				return getNodeContent(node);
		}

		return null;
	}

	private static Map<String, String> extractDescriptions(IMemento methodNode, String type, String keyAttribute) {
		final Map<String, String> parameters = new HashMap<>();

		for (final IMemento node : methodNode.getChildren()) {
			if (type.equals(node.getString("class"))) {
				// parameter node found

				final List<IMemento> candidates = new ArrayList<>();
				candidates.addAll(Arrays.asList(node.getChildren()));

				int argumentCounter = 0;
				while (!candidates.isEmpty()) {
					final IMemento candidate = candidates.remove(0);
					if ("description".equals(candidate.getString("class"))) {
						final String parameterName = candidate.getString(keyAttribute);
						parameters.put(parameterName, getNodeContent(candidate));

						// have a copy with the generic argument name in case reflection cannot find them for the method
						parameters.put("arg" + argumentCounter, getNodeContent(candidate));
						argumentCounter++;

					} else
						candidates.addAll(0, Arrays.asList(candidate.getChildren()));
				}
			}
		}

		return parameters;
	}

	private static Map<String, String> extractExamples(IMemento methodNode) {
		final Map<String, String> examples = new HashMap<>();

		for (final IMemento node : methodNode.getChildren()) {
			if ("examples".equals(node.getString("class"))) {
				// parameter node found

				String key = null;
				for (final IMemento child : node.getChildren()) {
					if (key == null)
						key = getNodeContent(child);

					else {
						examples.put(key, getNodeContent(child));
						key = null;
					}
				}
			}
		}

		return examples;
	}

	public static String getNodeContent(IMemento node) {
		final String candidate = node.toString();
		int startPos = candidate.indexOf("<" + node.getType());
		if (startPos != -1)
			startPos = candidate.indexOf('>', startPos);

		final int endPos = candidate.lastIndexOf("<");

		if ((startPos != -1) && (endPos != -1) && (startPos < endPos))
			return candidate.substring(startPos + 1, endPos);

		return (node.getTextData() != null) ? node.getTextData() : "";
	}

	/**
	 * @param method
	 * @return
	 */
	private static String createSynopsis(Method method) {
		final StringBuilder builder = new StringBuilder();

		final Class<?> returnType = method.getReturnType();
		if (Void.TYPE.equals(returnType))
			builder.append("void");
		else
			builder.append(createLink("some location", returnType.getSimpleName()));

		builder.append(' ');
		builder.append(method.getName());

		builder.append('(');
		for (final Parameter parameter : method.getParameters()) {
			if (parameter.isAnnotationPresent(ScriptParameter.class))
				builder.append('[');

			builder.append(createLink("some location", parameter.getType().getSimpleName()));
			builder.append(' ');
			builder.append(parameter.getName());

			if (parameter.isAnnotationPresent(ScriptParameter.class))
				builder.append(']');

			builder.append(", ");
		}
		if (method.getParameterCount() > 0)
			builder.delete(builder.length() - 2, builder.length());

		builder.append(')');

		return builder.toString();
	}

	/**
	 * Retrieve help content for module constants.
	 *
	 * @param field
	 *            module field to fetch help for
	 * @return help content
	 */
	public static String getConstantHelpTip(final Field field) {

		final IMemento bodyNode = getHelpContent(ModulesTools.getDeclaringModule(field));
		if (bodyNode != null) {
			for (final IMemento node : bodyNode.getChildren("table")) {
				if ("constants".equals(node.getString("class"))) {
					final List<IMemento> candidates = new ArrayList<>();
					candidates.addAll(Arrays.asList(node.getChildren()));

					while (!candidates.isEmpty()) {
						final IMemento candidate = candidates.remove(0);
						if (field.getName().equals(candidate.getString("data-field"))) {
							// constant found

							final StringBuffer helpContent = new StringBuffer();

							HTMLPrinter.addSmallHeader(helpContent, getImageAndLabel(getImageLocation("icons/eobj16/field.png"), field.getName()));
							helpContent.append("<br />"); //$NON-NLS-1$
							helpContent.append(getNodeContent(candidate));

							return helpContent.toString();

						} else
							candidates.addAll(Arrays.asList(candidate.getChildren()));
					}

					break;
				}
			}
		}

		return null;
	}
}
