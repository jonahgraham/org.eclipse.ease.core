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
package org.eclipse.ease.ui.scripts.repository.impl;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Map;

 import org.eclipse.ease.Logger;
 import org.eclipse.ease.service.ScriptType;
 import org.eclipse.ease.tools.ResourceTools;
 import org.eclipse.ease.ui.repository.IRepositoryFactory;
 import org.eclipse.ease.ui.repository.IScript;
 import org.eclipse.ease.ui.repository.IScriptLocation;

 public class FileSystemParser extends InputStreamParser {

	 public FileSystemParser(final RepositoryService repositoryService) {
		 super(repositoryService);
	 }

	 public void parse(final File file, final IScriptLocation entry) {
		 if (file.isDirectory()) {
			 // containment, parse children
			 for (File child : file.listFiles()) {
				 if ((child.isFile()) || (entry.isRecursive()))
					 parse(child, entry);
			 }

		 } else {
			 // try to locate registered script
			 String location = file.toURI().toString();
			 IScript script = getScriptByLocation(location);

			 try {
				 if (script == null) {
					 // new script detected
					 ScriptType scriptType = ResourceTools.getScriptType(file);
					 if (scriptType != null) {
						 script = IRepositoryFactory.eINSTANCE.createScript();
						 script.setEntry(entry);
						 script.setLocation(location);

						 Map<String, String> parameters = extractParameters(scriptType, new FileInputStream(file));
						 script.getScriptParameters().clear();
						 script.getScriptParameters().putAll(parameters);

						 script.setTimestamp(file.lastModified());

						 getRepositoryService().addScript(script);
					 }

				 } else if (script.getTimestamp() != file.lastModified()) {
					 // script needs updating
					 ScriptType scriptType = ResourceTools.getScriptType(file);
					 Map<String, String> parameters = extractParameters(scriptType, new FileInputStream(file));

					 script.setTimestamp(file.lastModified());

					 getRepositoryService().updateScript(script, parameters);

				 } else
					 // script is up to date
					 script.setUpdatePending(false);

			 } catch (FileNotFoundException e) {
				 // cannot find file
				 Logger.logError("Cannot locate script file: " + file, e);
			 }
		 }
	 }
 }
