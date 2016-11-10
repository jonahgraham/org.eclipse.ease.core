/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completions.java.help.hovers.internal;

import java.net.URI;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IPath;

/**
 * @author christian
 *
 */
public class VirtualProjectDescription implements IProjectDescription {

	/**
	 * @param dummyProject
	 */
	public VirtualProjectDescription(final VirtualProject dummyProject) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBuildConfiguration[] getBuildConfigReferences(final String configName) {
		throw new RuntimeException("getBuildConfigReferences not implemented");
	}

	@Override
	public ICommand[] getBuildSpec() {
		throw new RuntimeException("getBuildSpec not implemented");
	}

	@Override
	public String getComment() {
		throw new RuntimeException("getComment not implemented");
	}

	@Override
	public IProject[] getDynamicReferences() {
		return new IProject[0];
	}

	@Override
	public IPath getLocation() {
		throw new RuntimeException("getLocation not implemented");
	}

	@Override
	public URI getLocationURI() {
		throw new RuntimeException("getLocationURI not implemented");
	}

	@Override
	public String getName() {
		throw new RuntimeException("getName not implemented");
	}

	@Override
	public String[] getNatureIds() {
		throw new RuntimeException("getNatureIds not implemented");
	}

	@Override
	public IProject[] getReferencedProjects() {
		throw new RuntimeException("getReferencedProjects not implemented");
	}

	@Override
	public boolean hasNature(final String natureId) {
		throw new RuntimeException("hasNature not implemented");
	}

	@Override
	public ICommand newCommand() {
		throw new RuntimeException("newCommand not implemented");
	}

	@Override
	public void setActiveBuildConfig(final String configName) {
		throw new RuntimeException("setActiveBuildConfig not implemented");
	}

	@Override
	public void setBuildConfigs(final String[] configNames) {
		throw new RuntimeException("setBuildConfigs not implemented");
	}

	@Override
	public void setBuildConfigReferences(final String configName, final IBuildConfiguration[] references) {
		throw new RuntimeException("setBuildConfigReferences not implemented");
	}

	@Override
	public void setBuildSpec(final ICommand[] buildSpec) {
		throw new RuntimeException("setBuildSpec not implemented");
	}

	@Override
	public void setComment(final String comment) {
		throw new RuntimeException("setComment not implemented");
	}

	@Override
	public void setDynamicReferences(final IProject[] projects) {
		throw new RuntimeException("setDynamicReferences not implemented");
	}

	@Override
	public void setLocation(final IPath location) {
		throw new RuntimeException("setLocation not implemented");
	}

	@Override
	public void setLocationURI(final URI location) {
		throw new RuntimeException("setLocationURI not implemented");
	}

	@Override
	public void setName(final String projectName) {
		throw new RuntimeException("setName not implemented");
	}

	@Override
	public void setNatureIds(final String[] natures) {
		throw new RuntimeException("setNatureIds not implemented");
	}

	@Override
	public void setReferencedProjects(final IProject[] projects) {
		throw new RuntimeException("setReferencedProjects not implemented");
	}

}
