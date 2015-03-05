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

import java.io.BufferedReader;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;

public class ModuleDoclet extends Doclet {

	public static void main(final String[] args) {

		final String[] javadocargs = {
				"-sourcepath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform/src",
				"-root",
				"/data/develop/workspaces/EASE/org.eclipse.ease.modules/plugins/org.eclipse.ease.modules.platform",
				"-doclet",
				ModuleDoclet.class.getName(),
				"-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin",

				"-linkOffline",
				"http://docs.oracle.com/javase/8/docs/api",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease.help/package-lists/java8",

				"-linkOffline",
				"../../platform/isv",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease.help/package-lists/eclipse-luna",

				"org.eclipse.ease.modules.platform"

		};
		com.sun.tools.javadoc.Main.execute(javadocargs);

		final String[] javadocargs2 = { "-sourcepath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease/src", "-root",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease", "-doclet",
				ModuleDoclet.class.getName(), "-docletpath",
				"/data/develop/workspaces/EASE/org.eclipse.ease.core/developers/org.eclipse.ease.helpgenerator/bin",
				"-apiLinks", "java.*|http://docs.oracle.com/javase/8/docs/api", "org.eclipse.ease.modules",
				"-linkOffline", "http://docs.oracle.com/javase/8/docs/api",
		"/data/develop/workspaces/EASE/org.eclipse.ease.core/plugins/org.eclipse.ease.help/package-lists/java8" };

		// com.sun.tools.javadoc.Main.execute(javadocargs2);
	}

	private static final String OPTION_PROJECT_ROOT = "-root";
	private static final String OPTION_DOCLETPATH = "-docletpath";
	private static final Object OPTION_LINK = "-link";
	private static final Object OPTION_LINK_OFFLINE = "-linkOffline";

	public static boolean start(final RootDoc root) {
		final ModuleDoclet doclet = new ModuleDoclet();
		return doclet.process(root);
	}

	public static int optionLength(final String option) {
		if (OPTION_PROJECT_ROOT.equals(option))
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

	/** Maps module.class.name to module definition XML memento. */
	private Map<String, IMemento> fModuleNodes;
	private File fRootFolder = null;
	private final Collection<IMemento> fCategoryNodes = new HashSet<IMemento>();

	private LinkProvider fLinkProvider;

	private boolean process(final RootDoc root) {

		fLinkProvider = new LinkProvider();

		// parse options
		final String[][] options = root.options();
		for (final String[] option : options) {

			if (OPTION_PROJECT_ROOT.equals(option[0]))
				fRootFolder = new File(option[1]);

			else if (OPTION_LINK.equals(option[0])) {
				try {
					fLinkProvider.registerAddress(option[1], parsePackages(new URL(option[1]).openStream()));
				} catch (final MalformedURLException e) {
					System.out.println("Error: cannot parse external URL " + option[1]);
				} catch (final IOException e) {
					System.out.println("Error: cannot read from " + option[1]);

				}

			} else if (OPTION_LINK_OFFLINE.equals(option[0])) {
				try {
					fLinkProvider.registerAddress(option[1], parsePackages(new FileInputStream(option[2]
							+ File.separator + "package-list")));
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

	private static Collection<String> parsePackages(final InputStream inputStream) {
		final Collection<String> packages = new HashSet<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			String line = reader.readLine();
			while (line != null) {
				packages.add(line);
				line = reader.readLine();
			}
		} catch (IOException e) {
			// could not read, ignore
		}

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

			final File targetFile = getChild(getChild(fRootFolder, "help"),
					createCategoryFileName(node.getString("id")));
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

	public static String createHTMLFileName(final String moduleID) {
		return "module_" + escape(moduleID) + ".html";
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
				String content = new HTMLWriter(clazz, fLinkProvider, fModuleNodes.get(clazz.qualifiedName())
						.getChildren("dependency")).createContents(fModuleNodes.get(clazz.qualifiedName()).getString(
						"name"));

				// write document
				final File targetFile = getChild(getChild(fRootFolder, "help"),
						createHTMLFileName(fModuleNodes.get(clazz.qualifiedName()).getString("id")));
				writeFile(targetFile, content);
				createdFiles = true;
			}
		}

		return createdFiles;
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
}
