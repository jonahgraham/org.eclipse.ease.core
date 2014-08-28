/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.utils;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;


public class EffectiveScriptGenerator implements IExecutionListener {

	private IFile file;


	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		if(ENGINE_START == status) {
			//create a file
			IProject targetProject = getGenrationProject();
			if(targetProject != null) {
				String timeStamp = getCurrentTime();
				String fileName = timeStamp;
				file = targetProject.getFile(fileName);
				if(!file.exists()) {
					try {
						file.create(new ByteArrayInputStream("".getBytes()), true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		} else if(ENGINE_END == status) {
			//close file
		} else if(SCRIPT_INJECTION_START == status) {
			//Write injected code
			if(script != null) {
				if(file != null) {
					try {
						String comment = "\n\n// " + getCurrentTime() + " from " + script.getTitle() + "\n\n";
						file.appendContents(new ByteArrayInputStream(comment.getBytes()), true, true, new NullProgressMonitor());
						file.appendContents(script.getCodeStream(), true, true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if(SCRIPT_INJECTION_END == status) {
			//close stream
		}

	}


	private String getCurrentTime() {
		return new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss").format(Calendar.getInstance().getTime());
	}


	protected IProject getGenrationProject() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("ScriptDebug_EffectiveScriptGeneration");
		if(!project.exists()) {
			try {
				project.create(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
				Logger.logError("Unable to create Projet to hold effective script generation");
				return null;
			}

		}
		if(!project.isOpen()) {
			try {
				project.open(new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
		return project;
	}

}
