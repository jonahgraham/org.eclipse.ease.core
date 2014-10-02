/**
 *   Copyright (c) 2013 Atos
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *
 *   Contributors:
 *       Arthur Daussy - initial implementation
 */
package org.eclipse.ease.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ease.Logger;
import org.eclipse.ease.storedscript.notification.IStoredScriptListener;
import org.eclipse.ease.storedscript.service.IStoredScriptService;
import org.eclipse.ease.storedscript.storedscript.IStoredScript;
import org.eclipse.ease.storedscript.storedscript.ScriptMetadata;
import org.eclipse.ease.storedscript.storedscript.StoredScriptRegistry;
import org.eclipse.ease.storedscript.storedscript.StoredscriptPackage;
import org.eclipse.ease.ui.metadata.IUIMetadata;
import org.eclipse.ease.ui.metadata.UIMetadataUtils;
import org.eclipse.ease.ui.scriptuigraph.Node;
import org.eclipse.ease.ui.scriptuigraph.Root;
import org.eclipse.ease.ui.scriptuigraph.ScriptuigraphFactory;
import org.eclipse.ease.ui.scriptuigraph.StoredScriptUI;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class ScriptGraphService implements IStoredScriptListener {

	private static class SingletonHolder {
		public static ScriptGraphService INSTANCE = new ScriptGraphService();
	}

	public static ScriptGraphService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private Root fRoot = null;

	private ResourceSet fResourceSet;

	/**
	 * @return the resourceSet
	 */
	public ResourceSet getResourceSet() {
		if (fResourceSet == null) {
			init();
		}
		return fResourceSet;
	}

	public Root getScriptGraph() {
		if (fRoot == null) {
			init();
		}
		return fRoot;
	}

	protected void init() {

		IStoredScriptService.INSTANCE.addListener(this);
		fResourceSet = new ResourceSetImpl();
		ResourceImpl r = new ResourceImpl();
		fResourceSet.getResources().add(r);
		fRoot = ScriptuigraphFactory.eINSTANCE.createRoot();
		r.getContents().add(fRoot);
		Set<IStoredScript> storeScripts = IStoredScriptService.INSTANCE.getStoredScript();
		for (IStoredScript s : storeScripts) {
			addUIScript(s);
		}
	}

	public Node getNodeFromFragment(final String fragment) {
		Resource resource = getGraphResource();
		if (resource != null) {
			EObject eObject = resource.getEObject(fragment);
			if (eObject instanceof Node) {
				return (Node) eObject;
			}
		}
		return null;
	}

	public String getNodeFragment(final Node n) {
		return getGraphResource().getURIFragment(n);
	}

	protected Resource getGraphResource() {
		return fResourceSet.getResources().get(0);
	}

	private final Map<IStoredScript, StoredScriptUI> map = new HashMap<IStoredScript, StoredScriptUI>();

	@Override
	public void scriptChange(final Notification scriptNotif) {
		Object notifier = scriptNotif.getNotifier();
		if (notifier instanceof StoredScriptRegistry) {
			if (StoredscriptPackage.Literals.STORED_SCRIPT_REGISTRY__SCRIPTS.equals(scriptNotif.getFeature())) {
				if (Notification.ADD == scriptNotif.getEventType()) {
					addUIScript((IStoredScript) scriptNotif.getNewValue());
				} else if (Notification.REMOVE == scriptNotif.getEventType()) {
					removeUIScript((IStoredScript) scriptNotif.getOldValue());
				} else if (Notification.REMOVE_MANY == scriptNotif.getEventType()) {
					Collection<IStoredScript> toRemove = (Collection<IStoredScript>) scriptNotif.getOldValue();
					for (IStoredScript script : toRemove) {
						removeUIScript(script);
					}

				} else if (Notification.ADD_MANY == scriptNotif.getEventType()) {
					Collection<IStoredScript> toRemove = (Collection<IStoredScript>) scriptNotif.getOldValue();
					for (IStoredScript script : toRemove) {
						addUIScript(script);
					}

				}
			}
		} else if (notifier instanceof ScriptMetadata) {
			ScriptMetadata notif = (ScriptMetadata) notifier;
			if (StoredscriptPackage.Literals.SCRIPT_METADATA__VALUE.equals(scriptNotif.getFeature())) {
				if (Notification.SET == scriptNotif.getEventType()) {
					IStoredScript script = notif.getScript();
					if (IUIMetadata.MENU_METADATA.equals(notif.getKey())) {
						removeUIScript(script);
						addUIScript(script);
					}
				}
			}
		}
	}

	private void removeUIScript(final IStoredScript script) {
		StoredScriptUI node2 = map.get(script);
		if (node2 == null) {
			Logger.logError("No UI element for " + script.getUri());
			return;
		}
		getScriptGraph().removeScript(node2);
		map.remove(script);
	}

	private void addUIScript(final IStoredScript script) {
		if (script == null) {
			Logger.logError("Script not found in registry");
		}
		List<String> menus = UIMetadataUtils.getMenu(script);
		BasicEList<String> path = new BasicEList<String>(menus);
		StoredScriptUI node = getScriptGraph().addScript(path);
		node.setScript(script);
		node.setName(path.get(path.size() - 1));
		map.put(script, node);
	}
}
