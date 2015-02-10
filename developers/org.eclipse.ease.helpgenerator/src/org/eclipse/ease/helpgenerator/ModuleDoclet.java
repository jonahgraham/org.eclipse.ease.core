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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

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
import com.sun.javadoc.Tag;

public class ModuleDoclet extends Doclet {

	public static void main(final String[] args) {

		String[] javadocargs = { "-sourcepath", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform/src",
				"-root", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform", "-doclet",
				ModuleDoclet.class.getName(), "-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin", "-apiLinks",
				"java.*|http://docs.oracle.com/javase/8/docs/api", "org.eclipse.ease.modules.platform" };
		com.sun.tools.javadoc.Main.execute(javadocargs);

		String[] javadocargs2 = { "-sourcepath", "/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease/src", "-root",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease", "-doclet", ModuleDoclet.class.getName(), "-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin", "-apiLinks",
				"java.*|http://docs.oracle.com/javase/8/docs/api", "org.eclipse.ease.modules" };
		com.sun.tools.javadoc.Main.execute(javadocargs2);
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

	private static final String WRAP_TO_SCRIPT = "WrapToScript";
	private static final String QUALIFIED_WRAP_TO_SCRIPT = "org.eclipse.ease.modules." + WRAP_TO_SCRIPT;
	private static final String LINE_DELIMITER = "\n";

	private static final String OPTION_PROJECT_ROOT = "-root";
	private static final String OPTION_DOCLETPATH = "-docletpath";
	private static final Object OPTION_API_LINKS = "-apiLinks";

	public static boolean start(final RootDoc root) {
		final ModuleDoclet doclet = new ModuleDoclet();
		return doclet.process(root);
	}

	public static int optionLength(final String option) {
		if (OPTION_PROJECT_ROOT.equals(option))
			return 2;

		if (OPTION_API_LINKS.equals(option))
			return 2;

		return Doclet.optionLength(option);
	}

	public static boolean validOptions(final String options[][], final DocErrorReporter reporter) {
		return true;
	}

	private File getDocletPath() {
		return fDocletPath;
	}

	/** Maps module.class.name to module definition XML memento. */
	private Map<String, IMemento> fModuleNodes;
	private File fDocletPath;
	private File fRootFolder = null;
	private final Collection<IMemento> fCategoryNodes = new HashSet<IMemento>();
	private final Map<Pattern, String> fExternalAPIDocs = new HashMap<Pattern, String>();

	private boolean process(final RootDoc root) {

		// parse options
		String[][] options = root.options();
		for (String[] option : options) {
			if (OPTION_DOCLETPATH.equals(option[0]))
				fDocletPath = new File(option[1]);

			else if (OPTION_PROJECT_ROOT.equals(option[0]))
				fRootFolder = new File(option[1]);

			else if (OPTION_API_LINKS.equals(option[0])) {
				for (String entry : option[1].split(";")) {
					String[] tokens = entry.trim().split("\\|");
					if (tokens.length == 2)
						fExternalAPIDocs.put(Pattern.compile(tokens[0]), tokens[1] + (tokens[1].endsWith("/") ? "" : "/"));
				}
			}
		}

		final ClassDoc[] classes = root.classes();

		// write to output file
		if (fRootFolder != null) {
			try {
				// create lookup table with module data
				createModuleLookupTable();

				// create HTML help files
				boolean created = createHTMLFiles(classes);

				// create category TOCs
				created |= createCategories();

				if (created) {
					// some files were created, update project, ...

					// create module TOC files
					Set<String> tocFiles = createModuleTOCFiles();

					// update plugin.xml
					updatePluginXML(fRootFolder, tocFiles);

					// update MANIFEST.MF
					updateManifest(fRootFolder);

					// update build.properties
					updateBuildProperties(fRootFolder);
				}
			} catch (Exception e) {
				// TODO handle this exception (but for now, at least know it happened)
				throw new RuntimeException(e);

			}

			return true;
		}

		return false;
	}

	private boolean createCategories() throws IOException {
		boolean created = false;

		for (IMemento node : fCategoryNodes) {
			XMLMemento memento = XMLMemento.createWriteRoot("toc");
			memento.putString("label", node.getString("name"));
			memento.putString("link_to", createCategoryLink(node.getString("parent")));

			IMemento topicNode = memento.createChild("topic");
			topicNode.putString("label", node.getString("name"));
			topicNode.putBoolean("sort", true);
			topicNode.createChild("anchor").putString("id", "modules_anchor");

			File targetFile = getChild(getChild(fRootFolder, "help"), createCategoryFileName(node.getString("id")));
			writeFile(targetFile, memento.toString());
			created = true;
		}

		return created;
	}

	private static String extractCategoryName(final String categoryId) {
		if (categoryId != null) {
			int index = categoryId.indexOf(".category.");
			if (index != -1)
				return categoryId.substring(index + ".category.".length());
		}

		return null;
	}

	private static String createCategoryLink(final String categoryId) {
		String pluginID = "org.eclipse.ease.help";
		if (categoryId != null) {
			int index = categoryId.indexOf(".category.");
			if (index != -1)
				pluginID = categoryId.substring(0, index);
		}

		return "../" + pluginID + "/help/" + createCategoryFileName(categoryId) + "#modules_anchor";
	}

	private static String createCategoryFileName(final String categoryId) {
		String category = extractCategoryName(categoryId);
		return (category != null) ? "category_" + category + ".xml" : "scripting_book.xml";
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
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"");

		else if (!require.contains("org.eclipse.help"))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"," + require);

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

	private void updatePluginXML(final File rootFolder, final Collection<String> tocs) throws Exception {
		HashSet<String> toDo = new HashSet<String>(tocs);

		File pluginFile = getChild(rootFolder, "plugin.xml");
		XMLMemento memento = XMLMemento.createReadRoot(new InputStreamReader(new FileInputStream(pluginFile)));
		for (IMemento extensionNode : memento.getChildren("extension")) {
			String extensionPoint = extensionNode.getString("point");
			if ("org.eclipse.help.toc".equals(extensionPoint)) {
				// a help topic is already registered
				for (IMemento tocNode : extensionNode.getChildren("toc")) {
					String tocLocation = tocNode.getString("file");
					if (tocLocation.length() > 5)
						toDo.remove(tocLocation.substring(5));
				}
			}
		}

		for (String fileLocation : toDo) {
			// some TOCs not registered yet
			IMemento extensionNode = memento.createChild("extension");
			extensionNode.putString("point", "org.eclipse.help.toc");
			IMemento tocNode = extensionNode.createChild("toc");
			tocNode.putString("file", "help/" + fileLocation);
			tocNode.putBoolean("primary", false);

		}

		if (!toDo.isEmpty())
			// we had to modify the file
			writeFile(pluginFile, memento.toString().replace("&#x0A;", "\n"));
	}

	private Set<String> createModuleTOCFiles() throws IOException {
		Map<String, IMemento> tocDefinitions = new HashMap<String, IMemento>();

		// create categories
		for (IMemento categoryDefinition : fCategoryNodes) {
			XMLMemento memento = XMLMemento.createWriteRoot("toc");
			memento.putString("label", categoryDefinition.getString("name"));
			memento.putString("link_to", createCategoryLink(categoryDefinition.getString("parent")));

			IMemento topicNode = memento.createChild("topic");
			topicNode.putString("label", categoryDefinition.getString("name"));
			topicNode.putBoolean("sort", true);

			topicNode.createChild("anchor").putString("id", "modules_anchor");
			tocDefinitions.put(createCategoryFileName(categoryDefinition.getString("id")), memento);
		}

		// create modules
		if (!fModuleNodes.isEmpty()) {

			for (IMemento moduleDefinition : fModuleNodes.values()) {
				String categoryID = moduleDefinition.getString("category");
				String fileName = createCategoryFileName(categoryID).replace("category_", "modules_");

				IMemento memento;
				if (tocDefinitions.containsKey(fileName))
					memento = tocDefinitions.get(fileName);

				else {
					memento = XMLMemento.createWriteRoot("toc");
					memento.putString("label", "Modules");
					memento.putString("link_to", createCategoryLink(categoryID));

					tocDefinitions.put(fileName, memento);
				}

				IMemento topicNode = memento.createChild("topic");
				topicNode.putString("href", "help/" + createHTMLFileName(moduleDefinition));
				topicNode.putString("label", moduleDefinition.getString("name"));
			}
		}

		for (Entry<String, IMemento> entry : tocDefinitions.entrySet()) {
			File targetFile = getChild(getChild(fRootFolder, "help"), entry.getKey());
			writeFile(targetFile, entry.getValue().toString());
		}

		return tocDefinitions.keySet();
	}

	private static String createHTMLFileName(final IMemento moduleDefinition) {
		return "module_" + escape(moduleDefinition.getString("id")) + ".html";
	}

	private static void addLine(final StringBuffer buffer, final Object text) {
		buffer.append(text).append(LINE_DELIMITER);
	}

	private static void addText(final StringBuffer buffer, final Object text) {
		buffer.append(text);
	}

	private boolean createHTMLFiles(final ClassDoc[] classes) throws IOException {
		boolean createdFiles = false;

		for (final ClassDoc clazz : classes) {

			// only add classes which are registered in our modules lookup table
			if (fModuleNodes.containsKey(clazz.qualifiedName())) {
				// class found to create help for
				StringBuffer buffer = new StringBuffer();
				File headerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "header.txt");
				addLine(buffer, readResourceFile(headerFile));

				// header
				addText(buffer, "\t<h1>");
				addText(buffer, fModuleNodes.get(clazz.qualifiedName()).getString("name"));
				addLine(buffer, " Module</h1>");

				// class description
				addText(buffer, "\t<p>");
				final String classComment = clazz.commentText();
				if (classComment != null)
					addText(buffer, clazz.commentText());

				addLine(buffer, "</p>");

				// constants
				addLine(buffer, createConstantsSection(clazz));

				// function overview
				addLine(buffer, "\t<h2>Method Overview</h2>");
				addLine(buffer, "\t<table class=\"functions\">");
				addLine(buffer, "\t\t<tr>");
				addLine(buffer, "\t\t\t<th>Method</th>");
				addLine(buffer, "\t\t\t<th>Description</th>");
				addLine(buffer, "\t\t</tr>");

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
					addLine(buffer, "\t\t<tr>");
					addLine(buffer, "\t\t\t<td><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
					addLine(buffer, "\t\t\t<td>" + getFirstSentence(entry.fDescription) + "</td>");
					addLine(buffer, "\t\t</tr>");
				}

				addLine(buffer, "\t</table>");
				addLine(buffer, "");

				// function details
				addLine(buffer, "\t<h2>Methods</h2>");

				List<MethodDoc> methods = new ArrayList<MethodDoc>(Arrays.asList(clazz.methods()));
				Collections.sort(methods, new Comparator<MethodDoc>() {

					@Override
					public int compare(final MethodDoc o1, final MethodDoc o2) {
						return o1.name().compareTo(o2.name());
					}
				});

				for (final MethodDoc method : methods) {
					if (isExported(method)) {
						addLine(buffer, "\t<h3><a id=\"" + method.name() + "\">" + method.name() + "</a></h3>");

						// synopsis
						addText(buffer, "\t<p class=\"synopsis\">");
						addText(buffer, createClassText(method.returnType().qualifiedTypeName()));
						addText(buffer, " ");
						addText(buffer, method.name());
						addText(buffer, "(");
						for (Parameter parameter : method.parameters()) {
							addText(buffer, createClassText(parameter.type().qualifiedTypeName()));
							addText(buffer, " ");
							addText(buffer, parameter.name());
							addText(buffer, ", ");
						}
						if (method.parameters().length > 0)
							buffer.delete(buffer.length() - 2, buffer.length());

						addText(buffer, ")");
						addLine(buffer, "</p>");

						// main description
						addLine(buffer, "\t<p class=\"description\">" + method.commentText() + "</p>");

						Collection<String> aliases = getFunctionAliases(method);
						if (!aliases.isEmpty()) {
							addLine(buffer, "\t<p class=\"synonyms\">");

							for (String alias : aliases)
								addText(buffer, alias + " ");

							addLine(buffer, "</p>");
						}

						if (method.parameters().length > 0) {
							File parameterHeaderFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "parameters_header.txt");
							addLine(buffer, readResourceFile(parameterHeaderFile));
							for (Parameter parameter : method.parameters()) {
								addLine(buffer, "\t\t<tr>");
								addLine(buffer, "\t\t\t<td>" + parameter.name() + "</td>");
								addLine(buffer, "\t\t\t<td>" + findComment(method, parameter.name()) + "</td>");
								// TODO add default value
								addLine(buffer, "\t\t</tr>");
							}
							addLine(buffer, "\t</table>");
						}

						if (!"void".equals(method.returnType().qualifiedTypeName())) {
							addText(buffer, "\t<p class=\"return\"><em>Returns:</em>");
							addText(buffer, createClassText(method.returnType().qualifiedTypeName()));

							Tag[] tags = method.tags("return");
							if (tags.length > 0) {
								addText(buffer, " ... ");
								addText(buffer, tags[0].text());
							}

							addLine(buffer, "</p>");
						}
					}
				}

				File footerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "footer.txt");
				addLine(buffer, readResourceFile(footerFile));

				// write document
				File targetFile = getChild(getChild(fRootFolder, "help"), createHTMLFileName(fModuleNodes.get(clazz.qualifiedName())));
				writeFile(targetFile, buffer.toString());
				createdFiles = true;
			}
		}

		return createdFiles;
	}

	private static String getFirstSentence(final String description) {
		int pos = description.indexOf('.');

		return (pos > 0) ? description.substring(0, pos + 1) : description;
	}

	private Object createClassText(final String qualifiedName) {
		for (Entry<Pattern, String> entry : fExternalAPIDocs.entrySet()) {
			if (entry.getKey().matcher(qualifiedName).matches())
				return "<a href=\"" + entry.getValue() + qualifiedName.replace('.', '/') + ".html\" title=\"" + qualifiedName + "\">"
				+ qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1) + "</a>";
		}

		return qualifiedName;
	}

	private StringBuffer createConstantsSection(final ClassDoc clazz) {
		StringBuffer buffer = new StringBuffer();
		HashMap<String, String> constants = new HashMap<String, String>();
		for (final FieldDoc field : clazz.fields()) {
			if (isExported(field))
				constants.put(field.name(), field.commentText());
		}

		if (!constants.isEmpty()) {
			addLine(buffer, "");
			addLine(buffer, "\t<h2>Constants</h2>");
			addLine(buffer, "\t<table class=\"constants\">");
			addLine(buffer, "\t\t<tr>");
			addLine(buffer, "\t\t\t<th>Constant</th>");
			addLine(buffer, "\t\t\t<th>Description</th>");
			addLine(buffer, "\t\t</tr>");

			for (Entry<String, String> entry : constants.entrySet()) {
				addLine(buffer, "\t\t<tr>");
				addLine(buffer, "\t\t\t<td>" + entry.getKey() + "</td>");
				addLine(buffer, "\t\t\t<td>" + entry.getValue() + "</td>");
				addLine(buffer, "\t\t</tr>");
			}

			addLine(buffer, "\t</table>");
			addLine(buffer, "");
		}

		return buffer;
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

	private static String escape(final String data) {
		return data.replace(' ', '_').toLowerCase();
	}

	private void createModuleLookupTable() {
		fModuleNodes = new HashMap<String, IMemento>();

		// read plugin.xml
		final File pluginXML = getChild(fRootFolder, "plugin.xml");

		try {
			final IMemento root = XMLMemento.createReadRoot(new InputStreamReader(new FileInputStream(pluginXML)));
			for (final IMemento extensionNode : root.getChildren("extension")) {
				if ("org.eclipse.ease.modules".equals(extensionNode.getString("point"))) {
					for (final IMemento instanceNode : extensionNode.getChildren("module"))
						fModuleNodes.put(instanceNode.getString("class"), instanceNode);

					for (final IMemento instanceNode : extensionNode.getChildren("category"))
						fCategoryNodes.add(instanceNode);
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

		stream.close();
		return buffer;
	}
}
