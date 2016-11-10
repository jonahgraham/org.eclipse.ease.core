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
import java.nio.charset.Charset;
import java.util.Map;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author christian
 *
 */
public class VirtualProject implements IProject {

	@Override
	public boolean exists(final IPath path) {
		throw new RuntimeException("exists not implemented");
	}

	@Override
	public IResource findMember(final String path) {
		throw new RuntimeException("findMember not implemented");
	}

	@Override
	public IResource findMember(final String path, final boolean includePhantoms) {
		throw new RuntimeException("findMember not implemented");
	}

	@Override
	public IResource findMember(final IPath path) {
		throw new RuntimeException("findMember not implemented");
	}

	@Override
	public IResource findMember(final IPath path, final boolean includePhantoms) {
		throw new RuntimeException("findMember not implemented");
	}

	@Override
	public String getDefaultCharset() throws CoreException {
		// FIXME needs preferences lookup for default settings
		return Charset.defaultCharset().name();
	}

	@Override
	public String getDefaultCharset(final boolean checkImplicit) throws CoreException {
		throw new RuntimeException("getDefaultCharset not implemented");
	}

	@Override
	public IFile getFile(final IPath path) {
		throw new RuntimeException("getFile not implemented");
	}

	@Override
	public IFolder getFolder(final IPath path) {
		throw new RuntimeException("getFolder not implemented");
	}

	@Override
	public IResource[] members() throws CoreException {
		throw new RuntimeException("members not implemented");
	}

	@Override
	public IResource[] members(final boolean includePhantoms) throws CoreException {
		throw new RuntimeException("members not implemented");
	}

	@Override
	public IResource[] members(final int memberFlags) throws CoreException {
		throw new RuntimeException("members not implemented");
	}

	@Override
	public IFile[] findDeletedMembersWithHistory(final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("findDeletedMembersWithHistory not implemented");
	}

	@Override
	public void setDefaultCharset(final String charset) throws CoreException {
		throw new RuntimeException("setDefaultCharset not implemented");
	}

	@Override
	public void setDefaultCharset(final String charset, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setDefaultCharset not implemented");
	}

	@Override
	public IResourceFilterDescription createFilter(final int type, final FileInfoMatcherDescription matcherDescription, final int updateFlags,
			final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("createFilter not implemented");
	}

	@Override
	public IResourceFilterDescription[] getFilters() throws CoreException {
		throw new RuntimeException("getFilters not implemented");
	}

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
		throw new RuntimeException("exists not implemented");
	}

	@Override
	public IMarker findMarker(final long id) throws CoreException {
		throw new RuntimeException("findMarker not implemented");
	}

	@Override
	public IMarker[] findMarkers(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		return new IMarker[0];
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
	public IPath getFullPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getFullPath();
	}

	@Override
	public long getLocalTimeStamp() {
		throw new RuntimeException("getLocalTimeStamp not implemented");
	}

	@Override
	public IPath getLocation() {
		return null;
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
	public String getName() {
		return "EASE Java Help Project (virtual)";
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
		return PROJECT;
	}

	@Override
	public IWorkspace getWorkspace() {
		throw new RuntimeException("getWorkspace not implemented");
	}

	@Override
	public boolean isAccessible() {
		return true;
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
	public boolean isReadOnly() {
		throw new RuntimeException("isReadOnly not implemented");
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
	public void build(final int kind, final String builderName, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("build not implemented");
	}

	@Override
	public void build(final int kind, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("build not implemented");
	}

	@Override
	public void build(final IBuildConfiguration config, final int kind, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("build not implemented");
	}

	@Override
	public void close(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("close not implemented");
	}

	@Override
	public void create(final IProjectDescription description, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("create not implemented");
	}

	@Override
	public void create(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("create not implemented");
	}

	@Override
	public void create(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("create not implemented");
	}

	@Override
	public void delete(final boolean deleteContent, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("delete not implemented");
	}

	@Override
	public IBuildConfiguration getActiveBuildConfig() throws CoreException {
		throw new RuntimeException("getActiveBuildConfig not implemented");
	}

	@Override
	public IBuildConfiguration getBuildConfig(final String configName) throws CoreException {
		throw new RuntimeException("getBuildConfig not implemented");
	}

	@Override
	public IBuildConfiguration[] getBuildConfigs() throws CoreException {
		throw new RuntimeException("getBuildConfigs not implemented");
	}

	@Override
	public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
		throw new RuntimeException("getContentTypeMatcher not implemented");
	}

	@Override
	public IProjectDescription getDescription() throws CoreException {
		return new VirtualProjectDescription(this);
	}

	@Override
	public IFile getFile(final String name) {
		if (".classpath".equals(name))
			return new VirtualClasspathFile();

		throw new RuntimeException("getFile not implemented");
	}

	@Override
	public IFolder getFolder(final String name) {
		throw new RuntimeException("getFolder not implemented");
	}

	@Override
	public IProjectNature getNature(final String natureId) throws CoreException {
		throw new RuntimeException("getNature not implemented");
	}

	@Override
	public IPath getPluginWorkingLocation(final IPluginDescriptor plugin) {
		throw new RuntimeException("getPluginWorkingLocation not implemented");
	}

	@Override
	public IPath getWorkingLocation(final String id) {
		return null;
	}

	@Override
	public IProject[] getReferencedProjects() throws CoreException {
		throw new RuntimeException("getReferencedProjects not implemented");
	}

	@Override
	public IProject[] getReferencingProjects() {
		throw new RuntimeException("getReferencingProjects not implemented");
	}

	@Override
	public IBuildConfiguration[] getReferencedBuildConfigs(final String configName, final boolean includeMissing) throws CoreException {
		throw new RuntimeException("getReferencedBuildConfigs not implemented");
	}

	@Override
	public boolean hasBuildConfig(final String configName) throws CoreException {
		throw new RuntimeException("hasBuildConfig not implemented");
	}

	@Override
	public boolean hasNature(final String natureId) throws CoreException {
		return ("org.eclipse.jdt.core.javanature".equals(natureId));
	}

	@Override
	public boolean isNatureEnabled(final String natureId) throws CoreException {
		throw new RuntimeException("isNatureEnabled not implemented");
	}

	@Override
	public boolean isOpen() {
		throw new RuntimeException("isOpen not implemented");
	}

	@Override
	public void loadSnapshot(final int options, final URI snapshotLocation, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("loadSnapshot not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("move not implemented");
	}

	@Override
	public void open(final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("open not implemented");
	}

	@Override
	public void open(final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("open not implemented");
	}

	@Override
	public void saveSnapshot(final int options, final URI snapshotLocation, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("saveSnapshot not implemented");
	}

	@Override
	public void setDescription(final IProjectDescription description, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setDescription not implemented");
	}

	@Override
	public void setDescription(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("setDescription not implemented");
	}

	// Bug 517300: No @Override because this is a new method on IProject in Eclipse 4.7
	// Note that Auto-cleanup will re-add the @Override
	public void clearCachedDynamicReferences() {
		throw new RuntimeException("clearCachedDynamicReferences not implemented");
	}
}
