loadModule('/System/Resources');

path = showFolderSelectionDialog(null, "Select Project", "Select project to copy settings from");

if (path != null) {
       templateProject = getProject(path.substring(12));
       settings = templateProject.getFolder(".settings")
      
       if (settings != null) {
             // settings folder found
             files = findFiles("*", settings)
             for each (project in getWorkspace().getProjects()) {
                   
                    if (project.isOpen()) {
                           print("Update project: " + project.getName())
                   
                           // do not update settings for feature projects
                           if (project.getFile("feature.xml") == null)
                                  continue;
                   
                           if (project.getFolder(".settings") != null) {
                                  // the target contains settings
                                  for each (file in files) {
                                        print("\tcopy " + file.getName())
                                        copyFile(file, project.getFolder(".settings").getFile(file.getName()));
                                  }
                           }
                    }
             }
       }
}
