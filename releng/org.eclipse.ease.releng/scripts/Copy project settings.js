loadModule("/System/Resources");

sourceFile = getFile("workspace://org.eclipse.ease/.settings/org.eclipse.jdt.ui.prefs");

for each (project in getWorkspace().getProjects()) {
	if ((project.getName().startsWith("org.eclipse.ease.")) && (project.hasNature("org.eclipse.pde.PluginNature"))) {
		
		copyFile(sourceFile, "workspace://" + project.getName() + "/" + sourceFile.getProjectRelativePath());
		print("Copy file to " + project.getName());
	}
}
