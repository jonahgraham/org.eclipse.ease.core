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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
import java.util.regex.Matcher;
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

		final String[] javadocargs = { "-sourcepath", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform/src",
				"-root", "/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform", "-doclet",
				ModuleDoclet.class.getName(), "-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin", "-apiLinks",
				"java.*|http://docs.oracle.com/javase/8/docs/api", "org.eclipse.ease.modules.platform" };
		com.sun.tools.javadoc.Main.execute(javadocargs);

		final String[] javadocargs2 = { "-sourcepath", "/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease/src", "-root",
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
	private static final Object SCRIPT_PARAMETER = "ScriptParameter";
	private static final Object QUALIFIED_SCRIPT_PARAMETER = "org.eclipse.ease.modules." + SCRIPT_PARAMETER;
	private static final String LINE_DELIMITER = "\n";

	private static final String OPTION_PROJECT_ROOT = "-root";
	private static final String OPTION_DOCLETPATH = "-docletpath";
	private static final Object OPTION_API_LINKS = "-apiLinks";
	private static final Object OPTION_LINK = "-link";
	private static final Object OPTION_LINK_OFFLINE = "-linkOffline";

	public static boolean start(final RootDoc root) {
		final ModuleDoclet doclet = new ModuleDoclet();
		return doclet.process(root);
	}

	public static int optionLength(final String option) {
		if (OPTION_PROJECT_ROOT.equals(option))
			return 2;

		if (OPTION_API_LINKS.equals(option))
			return 2;

		if (OPTION_LINK.equals(option))
			return 2;

		if (OPTION_LINK_OFFLINE.equals(option))
			return 3;

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

	/** Maps (URL to use) -> Collection of package names. */
	private final Map<URL, Collection<String>> fExternalDocs = new HashMap<URL, Collection<String>>();

	private boolean process(final RootDoc root) {

		// parse options
		final String[][] options = root.options();
		for (final String[] option : options) {
			if (OPTION_DOCLETPATH.equals(option[0]))
				fDocletPath = new File(option[1]);

			else if (OPTION_PROJECT_ROOT.equals(option[0]))
				fRootFolder = new File(option[1]);

			else if (OPTION_API_LINKS.equals(option[0])) {
				for (final String entry : option[1].split(";")) {
					final String[] tokens = entry.trim().split("\\|");
					if (tokens.length == 2)
						fExternalAPIDocs.put(Pattern.compile(tokens[0]), tokens[1] + (tokens[1].endsWith("/") ? "" : "/"));
				}
			} else if (OPTION_LINK.equals(option[0])) {
				try {
					fExternalDocs.put(new URL(option[1]), parsePackages(new URL(option[1]).openStream()));
				} catch (final MalformedURLException e) {
					System.out.println("Error: cannot parse external URL " + option[1]);
				} catch (final IOException e) {
					System.out.println("Error: cannot read from " + option[1]);

				}

			} else if (OPTION_LINK_OFFLINE.equals(option[0])) {
				try {
					fExternalDocs.put(new URL(option[1]), parsePackages(new FileInputStream(option[2])));
				} catch (final MalformedURLException e) {
					System.out.println("Error: cannot parse external URL " + option[1]);
				} catch (final FileNotFoundException e) {
					System.out.println("Error: cannot read from " + option[2]);
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
					final Set<String> tocFiles = createModuleTOCFiles();

					// update plugin.xml
					updatePluginXML(fRootFolder, tocFiles);

					// update MANIFEST.MF
					updateManifest(fRootFolder);

					// update build.properties
					updateBuildProperties(fRootFolder);
				}
			} catch (final Exception e) {
				// TODO handle this exception (but for now, at least know it
				// happened)
				throw new RuntimeException(e);

			}

			return true;
		}

		return false;
	}

	private static Collection<String> parsePackages(InputStream fileInputStream) {
		final Collection<String> packages = new HashSet<String>();
		return packages;
	}

	private boolean createCategories() throws IOException {
		boolean created = false;

		for (final IMemento node : fCategoryNodes) {
			final XMLMemento memento = XMLMemento.createWriteRoot("toc");
			memento.putString("label", node.getString("name"));
			memento.putString("link_to", createCategoryLink(node.getString("parent")));

			final IMemento topicNode = memento.createChild("topic");
			topicNode.putString("label", node.getString("name"));
			topicNode.putBoolean("sort", true);
			topicNode.createChild("anchor").putString("id", "modules_anchor");

			final File targetFile = getChild(getChild(fRootFolder, "help"), createCategoryFileName(node.getString("id")));
			writeFile(targetFile, memento.toString());
			created = true;
		}

		return created;
	}

	private static String extractCategoryName(final String categoryId) {
		if (categoryId != null) {
			final int index = categoryId.indexOf(".category.");
			if (index != -1)
				return categoryId.substring(index + ".category.".length());
		}

		return null;
	}

	private static String createCategoryLink(final String categoryId) {
		String pluginID = "org.eclipse.ease.help";
		if (categoryId != null) {
			final int index = categoryId.indexOf(".category.");
			if (index != -1)
				pluginID = categoryId.substring(0, index);
		}

		return "../" + pluginID + "/help/" + createCategoryFileName(categoryId) + "#modules_anchor";
	}

	private static String createCategoryFileName(final String categoryId) {
		final String category = extractCategoryName(categoryId);
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

		final Manifest manifest = new Manifest();
		manifest.read(new FileInputStream(manifestFile));

		final Attributes mainAttributes = manifest.getMainAttributes();
		final String require = mainAttributes.getValue("Require-Bundle");

		if ((require == null) || (require.isEmpty()))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"");

		else if (!require.contains("org.eclipse.help"))
			mainAttributes.putValue("Require-Bundle", "org.eclipse.help;bundle-version=\"[3.5.0,4.0.0)\"," + require);

		else
			// manifest contains reference to org.eclipse.help, bail out
			return;

		final FileOutputStream out = new FileOutputStream(manifestFile);
		manifest.write(out);
		out.close();
	}

	private void updateBuildProperties(final File rootFolder) throws IOException {
		final File buildFile = getChild(rootFolder, "build.properties");

		final Properties properties = new Properties();
		properties.load(new FileInputStream(buildFile));
		final String property = properties.getProperty("bin.includes");
		if (!property.contains("help/")) {
			if (property.trim().isEmpty())
				properties.setProperty("bin.includes", "help/");
			else
				properties.setProperty("bin.includes", "help/," + property.trim());

			final FileOutputStream out = new FileOutputStream(buildFile);
			properties.store(out, "");
			out.close();
		}
	}

	private void updatePluginXML(final File rootFolder, final Collection<String> tocs) throws Exception {
		final HashSet<String> toDo = new HashSet<String>(tocs);

		final File pluginFile = getChild(rootFolder, "plugin.xml");
		final XMLMemento memento = XMLMemento.createReadRoot(new InputStreamReader(new FileInputStream(pluginFile)));
		for (final IMemento extensionNode : memento.getChildren("extension")) {
			final String extensionPoint = extensionNode.getString("point");
			if ("org.eclipse.help.toc".equals(extensionPoint)) {
				// a help topic is already registered
				for (final IMemento tocNode : extensionNode.getChildren("toc")) {
					final String tocLocation = tocNode.getString("file");
					if (tocLocation.length() > 5)
						toDo.remove(tocLocation.substring(5));
				}
			}
		}

		for (final String fileLocation : toDo) {
			// some TOCs not registered yet
			final IMemento extensionNode = memento.createChild("extension");
			extensionNode.putString("point", "org.eclipse.help.toc");
			final IMemento tocNode = extensionNode.createChild("toc");
			tocNode.putString("file", "help/" + fileLocation);
			tocNode.putBoolean("primary", false);

		}

		if (!toDo.isEmpty())
			// we had to modify the file
			writeFile(pluginFile, memento.toString().replace("&#x0A;", "\n"));
	}

	private Set<String> createModuleTOCFiles() throws IOException {
		final Map<String, IMemento> tocDefinitions = new HashMap<String, IMemento>();

		// create categories
		for (final IMemento categoryDefinition : fCategoryNodes) {
			final XMLMemento memento = XMLMemento.createWriteRoot("toc");
			memento.putString("label", categoryDefinition.getString("name"));
			memento.putString("link_to", createCategoryLink(categoryDefinition.getString("parent")));

			final IMemento topicNode = memento.createChild("topic");
			topicNode.putString("label", categoryDefinition.getString("name"));
			topicNode.putBoolean("sort", true);

			topicNode.createChild("anchor").putString("id", "modules_anchor");
			tocDefinitions.put(createCategoryFileName(categoryDefinition.getString("id")), memento);
		}

		// create modules
		if (!fModuleNodes.isEmpty()) {

			for (final IMemento moduleDefinition : fModuleNodes.values()) {
				final String categoryID = moduleDefinition.getString("category");
				final String fileName = createCategoryFileName(categoryID).replace("category_", "modules_");

				IMemento memento;
				if (tocDefinitions.containsKey(fileName))
					memento = tocDefinitions.get(fileName);

				else {
					memento = XMLMemento.createWriteRoot("toc");
					memento.putString("label", "Modules");
					memento.putString("link_to", createCategoryLink(categoryID));

					tocDefinitions.put(fileName, memento);
				}

				final IMemento topicNode = memento.createChild("topic");
				topicNode.putString("href", "help/" + createHTMLFileName(moduleDefinition.getString("id")));
				topicNode.putString("label", moduleDefinition.getString("name"));
			}
		}

		for (final Entry<String, IMemento> entry : tocDefinitions.entrySet()) {
			final File targetFile = getChild(getChild(fRootFolder, "help"), entry.getKey());
			writeFile(targetFile, entry.getValue().toString());
		}

		return tocDefinitions.keySet();
	}

	private static String createHTMLFileName(final String moduleID) {
		return "module_" + escape(moduleID) + ".html";
	}

	private static void addLine(final StringBuffer buffer, final Object text) {
		buffer.append(text).append(LINE_DELIMITER);
	}

	private static void addText(final StringBuffer buffer, final Object text) {
		buffer.append(text);
	}

	/**
	 * Create HTML help pages for module classes.
	 *
	 * @param classes
	 * @return
	 * @throws IOException
	 */
	private boolean createHTMLFiles(final ClassDoc[] classes) throws IOException {
		boolean createdFiles = false;

		for (final ClassDoc clazz : classes) {

			// only add classes which are registered in our modules lookup table
			if (fModuleNodes.containsKey(clazz.qualifiedName())) {
				// class found to create help for
				final StringBuffer buffer = new StringBuffer();
				final File headerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "header.txt");
				addLine(buffer, readResourceFile(headerFile));

				// header
				addText(buffer, "\t<h1>");
				addText(buffer, fModuleNodes.get(clazz.qualifiedName()).getString("name"));
				addLine(buffer, " Module</h1>");

				// class description
				addText(buffer, "\t<p>");
				final String classComment = clazz.commentText();
				if (classComment != null)
					addText(buffer, insertLinks(clazz, clazz.commentText()));

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

				final List<Overview> overview = new ArrayList<Overview>();
				for (final MethodDoc method : clazz.methods()) {
					if (isExported(method)) {
						overview.add(new Overview(method.name(), method.name(), method.commentText()));
						for (final String alias : getFunctionAliases(method))
							overview.add(new Overview(alias, method.name(), "Alias for <a href=\"#" + method.name() + "\">" + method.name() + "</a>."));
					}
				}
				Collections.sort(overview);

				for (final Overview entry : overview) {
					addLine(buffer, "\t\t<tr>");
					addLine(buffer, "\t\t\t<td><a href=\"#" + entry.fLinkID + "\">" + entry.fTitle + "</a>()</td>");
					addLine(buffer, "\t\t\t<td>" + insertLinks(clazz, getFirstSentence(entry.fDescription)) + "</td>");
					addLine(buffer, "\t\t</tr>");
				}

				addLine(buffer, "\t</table>");
				addLine(buffer, "");

				// function details
				addLine(buffer, "\t<h2>Methods</h2>");

				final List<MethodDoc> methods = new ArrayList<MethodDoc>(Arrays.asList(clazz.methods()));
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
						for (final Parameter parameter : method.parameters()) {
							final AnnotationDesc parameterAnnotation = getScriptParameterAnnotation(parameter);
							if (parameterAnnotation != null)
								addText(buffer, "[");

							addText(buffer, createClassText(parameter.type().qualifiedTypeName()));
							addText(buffer, " ");
							addText(buffer, parameter.name());
							if (parameterAnnotation != null)
								addText(buffer, "]");

							addText(buffer, ", ");
						}
						if (method.parameters().length > 0)
							buffer.delete(buffer.length() - 2, buffer.length());

						addText(buffer, ")");
						addLine(buffer, "</p>");

						// main description
						addLine(buffer, "\t<p class=\"description\">" + insertLinks(clazz, method.commentText()) + "</p>");

						final Collection<String> aliases = getFunctionAliases(method);
						if (!aliases.isEmpty()) {
							addLine(buffer, "\t<p class=\"synonyms\">");

							for (final String alias : aliases)
								addText(buffer, alias + " ");

							addLine(buffer, "</p>");
						}

						if (method.parameters().length > 0) {
							final File parameterHeaderFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "parameters_header.txt");
							addLine(buffer, readResourceFile(parameterHeaderFile));
							for (final Parameter parameter : method.parameters()) {
								addLine(buffer, "\t\t<tr>");
								addLine(buffer, "\t\t\t<td>" + parameter.name() + "</td>");
								addLine(buffer, "\t\t\t<td>" + createClassText(parameter.type().qualifiedTypeName()) + "</td>");
								addText(buffer, "\t\t\t<td>" + findComment(method, parameter.name()));

								final AnnotationDesc parameterAnnotation = getScriptParameterAnnotation(parameter);
								if (parameterAnnotation != null) {
									addText(buffer, "<br /><b>Optional:</b> defaults to &lt;<i>");
									for (final ElementValuePair pair : parameterAnnotation.elementValues()) {
										if ("org.eclipse.ease.modules.ScriptParameter.defaultValue()".equals(pair.element().toString())) {
											String defaultValue = pair.value().toString();

											if (!String.class.getName().equals(parameter.type().qualifiedTypeName()))
												defaultValue = defaultValue.substring(1, defaultValue.length() - 1);

											if (defaultValue.contains("org.eclipse.ease.modules.ScriptParameter.null"))
												addText(buffer, "null");

											else
												addText(buffer, defaultValue);
										}
									}
									addText(buffer, "</i>&gt;.");
								}
								addLine(buffer, "</td>");
								addLine(buffer, "\t\t</tr>");
							}
							addLine(buffer, "\t</table>");
						}

						if (!"void".equals(method.returnType().qualifiedTypeName())) {
							addText(buffer, "\t<p class=\"return\"><em>Returns:</em>");
							addText(buffer, createClassText(method.returnType().qualifiedTypeName()));

							final Tag[] tags = method.tags("return");
							if (tags.length > 0) {
								addText(buffer, " ... ");
								addText(buffer, tags[0].text());
							}

							addLine(buffer, "</p>");
						}
					}
				}

				final File footerFile = getChild(getChild(getChild(getDocletPath(), ".."), "templates"), "footer.txt");
				addLine(buffer, readResourceFile(footerFile));
				// write document
				final File targetFile = getChild(getChild(fRootFolder, "help"), createHTMLFileName(fModuleNodes.get(clazz.qualifiedName()).getString("id")));
				writeFile(targetFile, buffer.toString());
				createdFiles = true;
			}
		}

		return createdFiles;
	}

	/** Pattern to detect a link token. */
	private static final Pattern PATTERN_LINK = Pattern.compile("\\{@(link|module)\\s+(.*?)\\}", Pattern.DOTALL);

	/** Pattern to parse a link. */
	private static final Pattern PATTERN_INNER_LINK = Pattern.compile("(\\w+(?:\\.\\w+)*)?(?:#(\\w+)\\((.*?)\\))?");

	private static String insertLinks(ClassDoc clazz, String text) {

		final StringBuilder output = new StringBuilder();
		int startPos = 0;
		final Matcher matcher = PATTERN_LINK.matcher(text);

		while (matcher.find()) {
			output.append(text.substring(startPos, matcher.start()));
			startPos = matcher.end();

			final Matcher linkMatcher = PATTERN_INNER_LINK.matcher(matcher.group(2).replace('\r', ' ').replace('\n', ' '));
			if (linkMatcher.matches()) {
				// group 1 = class
				// group 2 = method (optional)
				// group 3 = params (without paranthesis)

				if ("link".equals(matcher.group(1))) {
					// link to java API
					if (linkMatcher.group(1) == null) {
						// link to same document
						// FIXME not correct
					} else {
						// external document
						// TODO fix path
						final String findClass = findClass(linkMatcher.group(1), clazz);
						System.out.println("searching " + linkMatcher.group(1) + ", found " + findClass);
					}

				} else if ("module".equals(matcher.group(1))) {
					// link to a scripting module
					if (linkMatcher.group(1) == null) {
						// link to same document
						output.append("<a href=\"#" + linkMatcher.group(2) + "\">" + linkMatcher.group(2) + "()</a>");
					} else {
						// external document
						final String plugin = linkMatcher.group(1).substring(0, linkMatcher.group(1).lastIndexOf('.'));
						output.append("<a href=\"../../" + plugin + "/help/" + createHTMLFileName(linkMatcher.group(1)) + "#" + linkMatcher.group(2) + "\">"
								+ linkMatcher.group(2) + "()</a>");
					}
				}
			}
		}

		if (startPos == 0)
			return text;

		output.append(text.substring(startPos));

		return output.toString();
	}

	private static String findClass(String name, ClassDoc baseClass) {
		for (final ClassDoc doc : baseClass.importedClasses()) {
			if (doc.toString().endsWith(name))
				return doc.toString();
		}

		final ClassDoc target = baseClass.findClass(name);
		return (target != null) ? target.toString() : null;
	}

	private static String getFirstSentence(final String description) {
		final int pos = description.indexOf('.');

		return (pos > 0) ? description.substring(0, pos + 1) : description;
	}

	private Object createClassText(final String qualifiedName) {
		for (final Entry<Pattern, String> entry : fExternalAPIDocs.entrySet()) {
			if (entry.getKey().matcher(qualifiedName).matches())
				return "<a href=\"" + entry.getValue() + qualifiedName.replace('.', '/') + ".html\" title=\"" + qualifiedName + "\">"
				+ qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1) + "</a>";
		}

		return qualifiedName;
	}

	private StringBuffer createConstantsSection(final ClassDoc clazz) {
		final StringBuffer buffer = new StringBuffer();
		final HashMap<String, String> constants = new HashMap<String, String>();
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

			for (final Entry<String, String> entry : constants.entrySet()) {
				addLine(buffer, "\t\t<tr>");
				addLine(buffer, "\t\t\t<td><a id=\"" + entry.getKey() + "\">" + entry.getKey() + "</a></td>");
				addLine(buffer, "\t\t\t<td>" + entry.getValue() + "</td>");
				addLine(buffer, "\t\t</tr>");
			}

			addLine(buffer, "\t</table>");
			addLine(buffer, "");
		}

		return buffer;
	}

	private Collection<String> getFunctionAliases(final MethodDoc method) {
		final Collection<String> aliases = new HashSet<String>();
		final AnnotationDesc annotation = getWrapAnnotation(method);
		if (annotation != null) {
			for (final ElementValuePair pair : annotation.elementValues()) {
				if ("alias".equals(pair.element().name())) {
					String candidates = pair.value().toString();
					candidates = candidates.substring(1, candidates.length() - 1);
					for (final String token : candidates.split("[,;]")) {
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

		final FileWriter writer = new FileWriter(file);
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

	private static AnnotationDesc getScriptParameterAnnotation(final Parameter parameter) {
		for (final AnnotationDesc annotation : parameter.annotations()) {
			if (isScriptParameterAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static boolean isScriptParameterAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()))
				|| (SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()));
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
