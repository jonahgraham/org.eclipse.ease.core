/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.helpgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

public class ModuleDoclet extends Doclet {

	public static void main(final String[] args) {

		String[] javadocargs = { "-sourcepath", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform/src",
				"-root", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform", "-doclet",
				ModuleDoclet.class.getName(), "-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin", "org.eclipse.ease.modules.platform" };
		com.sun.tools.javadoc.Main.execute(javadocargs);
	}

	private class Overview implements Comparable<Overview> {
		private final String fTitle;
		private final String fLinkID;
		private final String fDescription;

		public Overview(final String title, final String linkID, final String description) {
			fTitle = title;
			fLinkID = linkID;
			fDescription = description;
		}

		@Override
		public int compareTo(final Overview arg0) {
			return fTitle.compareTo(arg0.fTitle);
		}
	};

	private static final String QUALIFIED_WRAP_TO_SCRIPT = "org.eclipse.ease.modules.WrapToScript";
	private static final String WRAP_TO_SCRIPT = "WrapToScript";
	private static final String LINE_DELIMITER = "\n";

	private static final String OPTION_PROJECT_ROOT = "-root";
	private static final String OPTION_DOCLETPATH = "-docletpath";

	public static boolean start(final RootDoc root) {
		final ModuleDoclet doclet = new ModuleDoclet();
		return doclet.process(root);
	}

	public static int optionLength(final String option) {
		if (OPTION_PROJECT_ROOT.equals(option))
			return 2;

		return Doclet.optionLength(option);
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	private File getDocletPath() {
		return fDocletPath;
	}

	private Map<String, IMemento> mLookupTable;
	private File fDocletPath;

	private boolean process(final RootDoc root) {

		String[][] options = root.options();
		for (String[] option : options) {
			if (OPTION_DOCLETPATH.equals(option[0]))
				fDocletPath = new File(option[1]);
		}

		final ClassDoc[] classes = root.classes();

		// write to output file
		for (final String[] option : options) {
			if (OPTION_PROJECT_ROOT.equals(option[0])) {
				try {
					// get project location
					File rootFolder = new File(option[1]);

					// create lookup table with module data
					createModuleLookupTable(rootFolder);

					// create HTML help files
					if (createHTMLFiles(rootFolder, classes)) {
						// some files were created, update tocs, ...

						// create TOC file
						createTOCFile(rootFolder);

						// update plugin.xml
						updatePluginXML(rootFolder);

						// update MANIFEST.MF
						updateManifest(rootFolder);

						// update build.properties
						updateBuildProperties(rootFolder);
					}
				} catch (Exception e) {
					// TODO handle this exception (but for now, at least know it happened)
					throw new RuntimeException(e);

				}
			}
		}

		return true;
	}

	private File getChild(final File folder, final String name) {
		// if the folder exists, it needs to be a directory
		// if it does not exist, it will be created by the writeFile() method
		if ((folder.isDirectory()) || (!folder.exists()))
			return new File(folder.getPath() + File.separator + name);

		return null;
	}

	private void updateManifest(final File rootFolder) throws IOException {
		final File manifestFile = getChild(getChild(rootFolder, "META-INF"), "MANIFEST.MF");

		Manifest manifest = new Manifest();
		manifest.read(new FileInputStream(manifestFile));

		Attributes mainAttributes = manifest.getMainAttributes();
		String require = mainAttributes.getValue("Require-Bundle");

		if ((require == null) || (require.isEmpty()))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.6.0,4.0.0)\"");

		else if (!require.contains("org.eclipse.help"))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.6.0,4.0.0)\"," + require);

		else
			// manifest contains reference to org.eclipse.help, bail out
			return;

		FileOutputStream out = new FileOutputStream(manifestFile);
		manifest.write(out);
		out.close();
	}

	private void updateBuildProperties(final File rootFolder) throws IOException {
		File buildFile = getChild(rootFolder, "build.properties");

		final Properties properties = new Properties();
		properties.load(new FileInputStream(buildFile));
		final String property = properties.getProperty("bin.includes");
		if (!property.contains("help/")) {
			if (property.trim().isEmpty())
				properties.setProperty("bin.includes", "help/");
			else
				properties.setProperty("bin.includes", "help/," + property.trim());

			FileOutputStream out = new FileOutputStream(buildFile);
			properties.store(out, "");
			out.close();
		}
	}

	private void updatePluginXML(final File rootFolder) throws Exception {
		File pluginFile = getChild(rootFolder, "plugin.xml");
		XMLMemento memento = XMLMemento.createReadRoot(new InputStreamReader(new FileInputStream(pluginFile)));
		for (IMemento extensionNode : memento.getChildren("extension")) {
			String extensionPoint = extensionNode.getString("point");
			if ("org.eclipse.help.toc".equals(extensionPoint)) {
				// a help topic is registered
				for (IMemento tocNode : extensionNode.getChildren("toc")) {
					if ("help/modules_toc.xml".equals(tocNode.getString("file")))
						// already registered
						return;
				}
			}
		}

		// modules TOC node not registered yet
		IMemento extensionNode = memento.createChild("extension");
		extensionNode.putString("point", "org.eclipse.help.toc");
		IMemento tocNode = extensionNode.createChild("toc");
		tocNode.putString("file", "help/modules_toc.xml");
		tocNode.putBoolean("primary", false);

		writeFile(pluginFile, memento.toString().replace("&#x0A;", "\n"));
	}

	private void createTOCFile(final File rootFolder) throws IOException {
		XMLMemento memento = XMLMemento.createWriteRoot("toc");
		memento.putString("label", "Modules");
		memento.putString("link_to", "../org.eclipse.ease/help/scripting_book.xml#modules_anchor");
		for (IMemento moduleDefinition : mLookupTable.values()) {
			IMemento topicNode = memento.createChild("topic");
			topicNode.putString("href", "help/module_" + escape(moduleDefinition.getString("name")) + ".html");
			topicNode.putString("label", moduleDefinition.getString("name"));
		}
		File targetFile = getChild(getChild(rootFolder, "help"), "modules_toc.xml");
		writeFile(targetFile, memento.toString());
	}

	private boolean createHTMLFiles(final File rootFolder, final ClassDoc[] classes) throws IOException {
		boolean createdFiles = false;

		for (final ClassDoc clazz : classes) {

			// only add classes which are registered in our modules lookup table
			if (mLookupTable.containsKey(clazz.qualifiedName())) {
				// class found to create help for
				StringBuffer buffer = new StringBuffer();
				File headerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "header.txt");
				buffer.append(readResourceFile(headerFile));

				// header
				buffer.append("\t<h1>Module ");
				buffer.append(mLookupTable.get(clazz.qualifiedName()).getString("name"));
				buffer.append("</h1>");
				buffer.append(LINE_DELIMITER);

				// class description
				buffer.append("\t<p>");
				final String classComment = clazz.commentText();
				if ((classComment != null) && (!classComment.isEmpty()))
					buffer.append(clazz.commentText());

				buffer.append("</p>");
				buffer.append(LINE_DELIMITER);

				// constants
				buffer.append(createConstantsSection(clazz));

				// function overview
				buffer.append(LINE_DELIMITER);
				buffer.append("\t<h2>Function Overview</h2>");
				buffer.append(LINE_DELIMITER);
				buffer.append("\t<table class=\"functions\">");
				buffer.append(LINE_DELIMITER);

				List<Overview> overview = new ArrayList<Overview>();
				for (final MethodDoc method : clazz.methods()) {
					if (isExported(method)) {
						overview.add(new Overview(method.name(), method.name(), method.commentText()));
						for (String alias : getFunctionAliases(method))
							overview.add(new Overview(alias, method.name(), "Alias for <a href=\"#" + method.name() + "\">" + method.name() + "</a>."));
					}
				}
				Collections.sort(overview);

				for (Overview entry : overview) {
					buffer.append("\t\t<tr>");
					buffer.append(LINE_DELIMITER);
					buffer.append("\t\t\t<th><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a></th>");
					buffer.append(LINE_DELIMITER);
					buffer.append("\t\t\t<td>" + entry.fDescription + "</td>");
					buffer.append(LINE_DELIMITER);
					buffer.append("\t\t</tr>");
					buffer.append(LINE_DELIMITER);
				}

				buffer.append("\t</table>");
				buffer.append(LINE_DELIMITER);
				buffer.append(LINE_DELIMITER);

				buffer.append("\t<h2>Functions</h2>");
				buffer.append(LINE_DELIMITER);

				for (final MethodDoc method : clazz.methods()) {
					if (isExported(method)) {
						buffer.append(LINE_DELIMITER);
						buffer.append("\t<h3><a id=\"" + method.name() + "\">" + method.name() + "</a></h3>");
						buffer.append(LINE_DELIMITER);

						buffer.append("\t<p class=\"synopsis\">");
						buffer.append(method.returnType().qualifiedTypeName());
						buffer.append(" ");
						buffer.append(method.name());
						buffer.append("(");
						for (Parameter parameter : method.parameters()) {
							buffer.append(parameter.type().qualifiedTypeName());
							buffer.append(" ");
							buffer.append(parameter.name());
							buffer.append(", ");
						}
						if (method.parameters().length > 0)
							buffer.delete(buffer.length() - 2, buffer.length());

						buffer.append(")");
						buffer.append("</p>");
						buffer.append(LINE_DELIMITER);

						buffer.append("\t<p class=\"description\">" + method.commentText() + "</p>");
						buffer.append(LINE_DELIMITER);

						Collection<String> aliases = getFunctionAliases(method);
						if (!aliases.isEmpty()) {
							buffer.append("\t<p class=\"synonyms\">");

							for (String alias : aliases)
								buffer.append(alias).append(" ");

							buffer.append("</p>");
							buffer.append(LINE_DELIMITER);
						}

						if (method.parameters().length > 0) {
							File parameterHeaderFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "parameters_header.txt");
							buffer.append(readResourceFile(parameterHeaderFile));
							for (Parameter parameter : method.parameters()) {
								buffer.append("\t\t<tr>");
								buffer.append(LINE_DELIMITER);
								buffer.append("\t\t\t<td>" + parameter.name() + "</td>");
								buffer.append(LINE_DELIMITER);
								buffer.append("\t\t\t<td>" + createLink(parameter.type().qualifiedTypeName()) + "</td>");
								buffer.append(LINE_DELIMITER);
								buffer.append("\t\t\t<td>" + findComment(method, parameter.name()) + "</td>");
								buffer.append(LINE_DELIMITER);
								// TODO add default value
								buffer.append("\t\t</tr>");
								buffer.append(LINE_DELIMITER);
							}
							buffer.append("\t</table>");
							buffer.append(LINE_DELIMITER);
						}

						if (!"void".equals(method.returnType().qualifiedTypeName())) {
							buffer.append("\t<p class=\"return\">");
							buffer.append(createLink(method.returnType().qualifiedTypeName()));
							// TODO add return type description
							buffer.append(" ... </p>");
							buffer.append(LINE_DELIMITER);
						}
					}
				}

				File footerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "footer.txt");
				buffer.append(readResourceFile(footerFile));

				// write document
				File targetFile = getChild(getChild(rootFolder, "help"), "module_"
						+ escape(mLookupTable.get(clazz.qualifiedName()).getString("name") + ".html"));
				writeFile(targetFile, buffer.toString());
				createdFiles = true;
			}
		}

		return createdFiles;
	}

	private StringBuffer createConstantsSection(final ClassDoc clazz) {
		StringBuffer buffer = new StringBuffer();
		HashMap<String, String> constants = new HashMap<String, String>();
		for (final FieldDoc field : clazz.fields()) {
			if (isExported(field))
				constants.put(field.name(), field.commentText());
		}

		if (!constants.isEmpty()) {
			buffer.append(LINE_DELIMITER);
			buffer.append("\t<h2>Constants</h2>");
			buffer.append(LINE_DELIMITER);
			buffer.append("\t<table class=\"constants\">");
			buffer.append(LINE_DELIMITER);

			for (Entry<String, String> entry : constants.entrySet()) {
				buffer.append("\t\t<tr>");
				buffer.append(LINE_DELIMITER);
				buffer.append("\t\t\t<th>" + entry.getKey() + "</th>");
				buffer.append(LINE_DELIMITER);
				buffer.append("\t\t\t<td>" + entry.getValue() + "</td>");
				buffer.append(LINE_DELIMITER);
				buffer.append("\t\t</tr>");
				buffer.append(LINE_DELIMITER);
			}

			buffer.append("\t</table>");
			buffer.append(LINE_DELIMITER);
			buffer.append(LINE_DELIMITER);
		}

		return buffer;
	}

	private String createLink(final String qualifiedTypeName) {
		if (qualifiedTypeName.startsWith("java.")) {
			String target = "http://docs.oracle.com/javase/7/docs/api/" + qualifiedTypeName.replace('.', '/') + ".html";

			return "<a href=\"" + target + "\">" + qualifiedTypeName + "</a>";
		}
		return qualifiedTypeName;
	}

	private Collection<String> getFunctionAliases(final MethodDoc method) {
		Collection<String> aliases = new HashSet<String>();
		AnnotationDesc annotation = getWrapAnnotation(method);
		if (annotation != null) {
			for (ElementValuePair pair : annotation.elementValues()) {
				if ("alias".equals(pair.element().name())) {
					String candidates = pair.value().toString();
					candidates = candidates.substring(1, candidates.length() - 1);
					for (String token : candidates.split("[,;]")) {
						if (!token.trim().isEmpty())
							aliases.add(token.trim());
					}
				}
			}
		}

		return aliases;
	}

	private static void writeFile(final File file, final String data) throws IOException {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		// save data to file
		if (!file.exists())
			file.createNewFile();

		FileWriter writer = new FileWriter(file);
		writer.write(data);
		writer.close();
	}

	private String escape(final String data) {
		return data.replace(' ', '_').toLowerCase();
	}

	private void createModuleLookupTable(final File projectRoot) {
		mLookupTable = new HashMap<String, IMemento>();

		// read plugin.xml
		final File pluginXML = getChild(projectRoot, "plugin.xml");

		try {
			final IMemento root = XMLMemento.createReadRoot(new InputStreamReader(new FileInputStream(pluginXML)));
			for (final IMemento extensionNode : root.getChildren("extension")) {
				if ("org.eclipse.ease.modules".equals(extensionNode.getString("point"))) {
					for (final IMemento instanceNode : extensionNode.getChildren("module"))
						mLookupTable.put(instanceNode.getString("class"), instanceNode);
				}
			}
		} catch (final Exception e) {
		}
	}

	private static String findComment(final MethodDoc method, final String name) {

		for (final ParamTag paramTags : method.paramTags()) {
			if (name.equals(paramTags.parameterName()))
				return paramTags.parameterComment();
		}

		return "";
	}

	private static boolean isExported(final FieldDoc field) {
		for (final AnnotationDesc annotation : field.annotations()) {
			if (isWrapToScriptAnnotation(annotation))
				return true;
		}

		return false;
	}

	private static boolean isExported(final MethodDoc method) {
		return getWrapAnnotation(method) != null;
	}

	private static AnnotationDesc getWrapAnnotation(final MethodDoc method) {
		for (final AnnotationDesc annotation : method.annotations()) {
			if (isWrapToScriptAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static boolean isWrapToScriptAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()))
				|| (WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()));
	}

	private static StringBuffer readResourceFile(final File file) throws IOException {
		final StringBuffer buffer = new StringBuffer();
		final InputStream stream = new FileInputStream(file);
		while (stream.available() > 0)
			buffer.append((char) stream.read());

		return buffer;
	}
}
