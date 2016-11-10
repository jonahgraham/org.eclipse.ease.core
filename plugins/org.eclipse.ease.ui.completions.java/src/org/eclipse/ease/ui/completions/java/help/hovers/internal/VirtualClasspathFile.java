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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author christian
 *
 */
public class VirtualClasspathFile implements IFile {

	@Override
	public void accept(final IResourceProxyVisitor visitor, final int memberFlags) throws CoreException {
		throw new RuntimeException("accept not implemented");
	}

	@Override
	public void accept(final IResourceProxyVisitor visitor, final int depth, final int memberFlags) throws CoreException {
		throw new RuntimeException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor) throws CoreException {
		throw new RuntimeException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor, final int depth, final boolean includePhantoms) throws CoreException {
		throw new RuntimeException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor, final int depth, final int memberFlags) throws CoreException {
		throw new RuntimeException("accept not implemented");
	}

	@Override
	public void clearHistory(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("clearHistory not implemented");
	}

	@Override
	public void copy(final IPath destination, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("copy not implemented");
	}

	@Override
	public void copy(final IPath destination, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("copy not implemented");
	}

	@Override
	public void copy(final IProjectDescription description, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("copy not implemented");
	}

	@Override
	public void copy(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("copy not implemented");
	}

	@Override
	public IMarker createMarker(final String type) throws CoreException {
		throw new RuntimeException("createMarker not implemented");
	}

	@Override
	public IResourceProxy createProxy() {
		throw new RuntimeException("createProxy not implemented");
	}

	@Override
	public void delete(final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("delete not implemented");
	}

	@Override
	public void delete(final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("delete not implemented");
	}

	@Override
	public void deleteMarkers(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		throw new RuntimeException("deleteMarkers not implemented");
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public IMarker findMarker(final long id) throws CoreException {
		throw new RuntimeException("findMarker not implemented");
	}

	@Override
	public IMarker[] findMarkers(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		throw new RuntimeException("findMarkers not implemented");
	}

	@Override
	public int findMaxProblemSeverity(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		throw new RuntimeException("findMaxProblemSeverity not implemented");
	}

	@Override
	public String getFileExtension() {
		throw new RuntimeException("getFileExtension not implemented");
	}

	@Override
	public long getLocalTimeStamp() {
		throw new RuntimeException("getLocalTimeStamp not implemented");
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
	public IMarker getMarker(final long id) {
		throw new RuntimeException("getMarker not implemented");
	}

	@Override
	public long getModificationStamp() {
		throw new RuntimeException("getModificationStamp not implemented");
	}

	@Override
	public IPathVariableManager getPathVariableManager() {
		throw new RuntimeException("getPathVariableManager not implemented");
	}

	@Override
	public IContainer getParent() {
		throw new RuntimeException("getParent not implemented");
	}

	@Override
	public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
		throw new RuntimeException("getPersistentProperties not implemented");
	}

	@Override
	public String getPersistentProperty(final QualifiedName key) throws CoreException {
		throw new RuntimeException("getPersistentProperty not implemented");
	}

	@Override
	public IProject getProject() {
		throw new RuntimeException("getProject not implemented");
	}

	@Override
	public IPath getProjectRelativePath() {
		throw new RuntimeException("getProjectRelativePath not implemented");
	}

	@Override
	public IPath getRawLocation() {
		throw new RuntimeException("getRawLocation not implemented");
	}

	@Override
	public URI getRawLocationURI() {
		throw new RuntimeException("getRawLocationURI not implemented");
	}

	@Override
	public ResourceAttributes getResourceAttributes() {
		throw new RuntimeException("getResourceAttributes not implemented");
	}

	@Override
	public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
		throw new RuntimeException("getSessionProperties not implemented");
	}

	@Override
	public Object getSessionProperty(final QualifiedName key) throws CoreException {
		throw new RuntimeException("getSessionProperty not implemented");
	}

	@Override
	public int getType() {
		throw new RuntimeException("getType not implemented");
	}

	@Override
	public IWorkspace getWorkspace() {
		throw new RuntimeException("getWorkspace not implemented");
	}

	@Override
	public boolean isAccessible() {
		throw new RuntimeException("isAccessible not implemented");
	}

	@Override
	public boolean isDerived() {
		throw new RuntimeException("isDerived not implemented");
	}

	@Override
	public boolean isDerived(final int options) {
		throw new RuntimeException("isDerived not implemented");
	}

	@Override
	public boolean isHidden() {
		throw new RuntimeException("isHidden not implemented");
	}

	@Override
	public boolean isHidden(final int options) {
		throw new RuntimeException("isHidden not implemented");
	}

	@Override
	public boolean isLinked() {
		throw new RuntimeException("isLinked not implemented");
	}

	@Override
	public boolean isVirtual() {
		throw new RuntimeException("isVirtual not implemented");
	}

	@Override
	public boolean isLinked(final int options) {
		throw new RuntimeException("isLinked not implemented");
	}

	@Override
	public boolean isLocal(final int depth) {
		throw new RuntimeException("isLocal not implemented");
	}

	@Override
	public boolean isPhantom() {
		throw new RuntimeException("isPhantom not implemented");
	}

	@Override
	public boolean isSynchronized(final int depth) {
		throw new RuntimeException("isSynchronized not implemented");
	}

	@Override
	public boolean isTeamPrivateMember() {
		throw new RuntimeException("isTeamPrivateMember not implemented");
	}

	@Override
	public boolean isTeamPrivateMember(final int options) {
		throw new RuntimeException("isTeamPrivateMember not implemented");
	}

	@Override
	public void move(final IPath destination, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void move(final IPath destination, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final boolean force, final boolean keepHistory, final IProgressMonitor monitor)
			throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void refreshLocal(final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("refreshLocal not implemented");
	}

	@Override
	public void revertModificationStamp(final long value) throws CoreException {
		throw new RuntimeException("revertModificationStamp not implemented");
	}

	@Override
	public void setDerived(final boolean isDerived) throws CoreException {
		throw new RuntimeException("setDerived not implemented");
	}

	@Override
	public void setDerived(final boolean isDerived, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setDerived not implemented");
	}

	@Override
	public void setHidden(final boolean isHidden) throws CoreException {
		throw new RuntimeException("setHidden not implemented");
	}

	@Override
	public void setLocal(final boolean flag, final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setLocal not implemented");
	}

	@Override
	public long setLocalTimeStamp(final long value) throws CoreException {
		throw new RuntimeException("setLocalTimeStamp not implemented");
	}

	@Override
	public void setPersistentProperty(final QualifiedName key, final String value) throws CoreException {
		throw new RuntimeException("setPersistentProperty not implemented");
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		throw new RuntimeException("setReadOnly not implemented");
	}

	@Override
	public void setResourceAttributes(final ResourceAttributes attributes) throws CoreException {
		throw new RuntimeException("setResourceAttributes not implemented");
	}

	@Override
	public void setSessionProperty(final QualifiedName key, final Object value) throws CoreException {
		throw new RuntimeException("setSessionProperty not implemented");
	}

	@Override
	public void setTeamPrivateMember(final boolean isTeamPrivate) throws CoreException {
		throw new RuntimeException("setTeamPrivateMember not implemented");
	}

	@Override
	public void touch(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("touch not implemented");
	}

	@Override
	public Object getAdapter(final Class adapter) {
		throw new RuntimeException("getAdapter not implemented");
	}

	@Override
	public boolean contains(final ISchedulingRule rule) {
		throw new RuntimeException("contains not implemented");
	}

	@Override
	public boolean isConflicting(final ISchedulingRule rule) {
		throw new RuntimeException("isConflicting not implemented");
	}

	@Override
	public void appendContents(final InputStream source, final boolean force, final boolean keepHistory, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("appendContents not implemented");
	}

	@Override
	public void appendContents(final InputStream source, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("appendContents not implemented");
	}

	@Override
	public void create(final InputStream source, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("create not implemented");
	}

	@Override
	public void create(final InputStream source, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("create not implemented");
	}

	@Override
	public void createLink(final IPath localLocation, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("createLink not implemented");
	}

	@Override
	public void createLink(final URI location, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("createLink not implemented");
	}

	@Override
	public void delete(final boolean force, final boolean keepHistory, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("delete not implemented");
	}

	@Override
	public String getCharset() throws CoreException {
		throw new RuntimeException("getCharset not implemented");
	}

	@Override
	public String getCharset(final boolean checkImplicit) throws CoreException {
		throw new RuntimeException("getCharset not implemented");
	}

	@Override
	public String getCharsetFor(final Reader reader) throws CoreException {
		throw new RuntimeException("getCharsetFor not implemented");
	}

	@Override
	public IContentDescription getContentDescription() throws CoreException {
		throw new RuntimeException("getContentDescription not implemented");
	}

	@Override
	public InputStream getContents() throws CoreException {
		throw new RuntimeException("getContents not implemented");
	}

	@Override
	public InputStream getContents(final boolean force) throws CoreException {
		return new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<classpath>\n"
				+ "	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8\"/>\n"
				+ "	<classpathentry kind=\"lib\" path=\"/usr/local/bin/eclipse/plugins/org.eclipse.core.resources_3.10.0.v20150423-0755.jar\">\n"
				+ "		<attributes>\n"
				+ "			<attribute name=\"javadoc_location\" value=\"http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.isv/reference/api\"/>\n"
				+ "		</attributes>\n" + "	</classpathentry>\n" + "</classpath>\n" + "").getBytes());
	}

	@Override
	public int getEncoding() throws CoreException {
		throw new RuntimeException("getEncoding not implemented");
	}

	@Override
	public IPath getFullPath() {
		throw new RuntimeException("getFullPath not implemented");
	}

	@Override
	public IFileState[] getHistory(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("getHistory not implemented");
	}

	@Override
	public String getName() {
		throw new RuntimeException("getName not implemented");
	}

	@Override
	public boolean isReadOnly() {
		throw new RuntimeException("isReadOnly not implemented");
	}

	@Override
	public void move(final IPath destination, final boolean force, final boolean keepHistory, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void setCharset(final String newCharset) throws CoreException {
		throw new RuntimeException("setCharset not implemented");
	}

	@Override
	public void setCharset(final String newCharset, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setCharset not implemented");
	}

	@Override
	public void setContents(final InputStream source, final boolean force, final boolean keepHistory, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setContents not implemented");
	}

	@Override
	public void setContents(final IFileState source, final boolean force, final boolean keepHistory, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setContents not implemented");
	}

	@Override
	public void setContents(final InputStream source, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setContents not implemented");
	}

	@Override
	public void setContents(final IFileState source, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setContents not implemented");
	}
}
