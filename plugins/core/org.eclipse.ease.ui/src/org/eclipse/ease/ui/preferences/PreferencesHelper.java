/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/package org.eclipse.ease.ui.preferences;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.repository.IEntry;
import org.eclipse.ease.ui.repository.IRepositoryFactory;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Helper methods to access script storage preferences.
 */
public final class PreferencesHelper {

	/**
	 * Only static methods available, do not create instance.
	 */
	@Deprecated
	private PreferencesHelper() {
	}

	/**
	 * Get the default location to store recorded/imported scripts to. If no path was defined by the user, a default path within the .metadata workspace folder
	 * is returned. As the user might change the default path also invalid entries might be returned.
	 * 
	 * @return path to default script storage location
	 */
	public static String getScriptStorageLocation() {
		String location = getUserScriptStorageLocation();
		if (location != null)
			return location;

		return getDefaultScriptStorageLocation();
	}

	/**
	 * Get the storage location for recorded/imported scripts as set by the user. If the user did not explicitely set a location, <code>null</code> is returned
	 * 
	 * @return user provided storage location or <code>null</code>
	 */
	public static String getUserScriptStorageLocation() {
		final StringBuilder location = new StringBuilder();

		final IEclipsePreferences rootNode = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		try {
			rootNode.accept(new IPreferenceNodeVisitor() {

				@Override
				public boolean visit(final IEclipsePreferences node) throws BackingStoreException {
					if (rootNode.equals(node))
						return true;

					else {
						if (node.getBoolean(IPreferenceConstants.SCRIPT_STORAGE_DEFAULT, false))
							location.replace(0, location.length(), node.get(IPreferenceConstants.SCRIPT_STORAGE_LOCATION, ""));

						return false;
					}
				}
			});
		} catch (BackingStoreException e) {
		}

		if (location.length() > 0)
			return location.toString();

		return null;
	}

	/**
	 * Get the default location to store recorded/imported scripts to. Returns the hard-coded default location within the workspace/.metadata folder.
	 * 
	 * @return path to default script storage location
	 */
	public static String getDefaultScriptStorageLocation() {
		return "file:///" + Activator.getDefault().getStateLocation().append("recordedScripts").toString();
	}

	/**
	 * Returns a collection of script locations as stored in the preferences. Converts preference data to {@link IEntry} elements.
	 * 
	 * @return all configured script locations
	 */
	public static Collection<IEntry> getLocations() {
		final Collection<IEntry> locations = new HashSet<IEntry>();

		final IEclipsePreferences rootNode = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		try {
			rootNode.accept(new IPreferenceNodeVisitor() {

				@Override
				public boolean visit(final IEclipsePreferences node) throws BackingStoreException {
					if (rootNode.equals(node))
						return true;

					else {
						IEntry entry = IRepositoryFactory.eINSTANCE.createEntry();
						entry.setLocation(node.get(IPreferenceConstants.SCRIPT_STORAGE_LOCATION, ""));
						entry.setDefault(node.getBoolean(IPreferenceConstants.SCRIPT_STORAGE_DEFAULT, false));
						entry.setRecursive(node.getBoolean(IPreferenceConstants.SCRIPT_STORAGE_RECURSIVE, true));

						locations.add(entry);

						return false;
					}
				}
			});
		} catch (BackingStoreException e) {
			// we were not able to load any locations, display empty view
		}

		return locations;
	}

	/**
	 * Add a script storage location to the preferences.
	 * 
	 * @param entry
	 *            location to add
	 */
	public static void addLocation(final IEntry entry) {
		String path = entry.getLocation().replace('/', '|');
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID + "/" + path);
		node.put(IPreferenceConstants.SCRIPT_STORAGE_LOCATION, entry.getLocation());
		node.putBoolean(IPreferenceConstants.SCRIPT_STORAGE_DEFAULT, entry.isDefault());
		node.putBoolean(IPreferenceConstants.SCRIPT_STORAGE_RECURSIVE, entry.isRecursive());
	}

	/**
	 * Add a script storage location to the preferences.
	 * 
	 * @param location
	 *            location of storage
	 * @param defaultLocation
	 *            whether this location should be used as default location for scripts
	 * @param recursive
	 *            if location should be parsed recursively
	 */
	public static void addLocation(final String location, final boolean defaultLocation, final boolean recursive) {
		IEntry entry = IRepositoryFactory.eINSTANCE.createEntry();
		entry.setLocation(location);
		entry.setRecursive(recursive);
		entry.setDefault(defaultLocation);

		addLocation(entry);

	}

	/**
	 * Remove all script locations from the preferences.
	 * 
	 * @throws BackingStoreException
	 *             on invalid preferences access
	 */
	public static void clearLocations() throws BackingStoreException {
		// remove existing child nodes
		final IEclipsePreferences rootNode = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		rootNode.accept(new IPreferenceNodeVisitor() {

			@Override
			public boolean visit(final IEclipsePreferences node) throws BackingStoreException {
				if (rootNode.equals(node))
					return true;

				else {
					node.removeNode();
					return false;
				}
			}
		});
	}
}
