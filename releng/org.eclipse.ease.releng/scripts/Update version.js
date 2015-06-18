loadModule('/System/Resources');
loadModule('/System/UI');

newVersion = showInputDialog("Please enter to version number to upgrade to", "1.0.0" , "Version request");

// update manifest files
for each (file in findFiles("MANIFEST.MF")) {
	if (file.getProject().getName().startsWith("org.eclipse.ease")) {
		content = readFile(file);
		content = content.replaceFirst("Bundle-Version: \\d+.\\d+.\\d+.qualifier", "Bundle-Version: " + newVersion + ".qualifier");
		
		writeFile(file, content)
	}
}

// update features
for each (file in findFiles("feature.xml")) {
	if (file.getProject().getName().startsWith("org.eclipse.ease")) {
		content = readFile(file);
		content = content.replaceFirst('version=\\"\\d+.\\d+.\\d+.qualifier\\"', 'version="' + newVersion + '.qualifier"');

		writeFile(file, content)
	}
}

// update poms
for each (file in findFiles("pom.xml")) {
	if (file.getProject().getName().startsWith("org.eclipse.ease")) {
		content = readFile(file);
		content = content.replaceFirst('<version>\\d+.\\d+.\\d+-SNAPSHOT</version>', '<version>' + newVersion + '-SNAPSHOT</version>');
		
		writeFile(file, content)
	}
}

// update p2 definitions
for each (file in findFiles("category.xml")) {
	if (file.getProject().getName().startsWith("org.eclipse.ease")) {
		content = readFile(file);
		content = content.replaceAll('\\d+.\\d+.\\d+.qualifier', newVersion + '.qualifier');
		
		writeFile(file, content)
	}
}

showMessageDialog("Remember to update the target platform version in o.e.e.releng/pom.xml");