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
/**
 */
package org.eclipse.ease.ui.scripts.repository.util;

import java.util.Map;

import org.eclipse.ease.ui.scripts.repository.IRepositoryPackage;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.util.XMLProcessor;

/**
 * This class contains helper methods to serialize and deserialize XML documents <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class RepositoryXMLProcessor extends XMLProcessor {

	/**
	 * Public constructor to instantiate the helper. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public RepositoryXMLProcessor() {
		super((EPackage.Registry.INSTANCE));
		IRepositoryPackage.eINSTANCE.eClass();
	}

	/**
	 * Register for "*" and "xml" file extensions the RepositoryResourceFactoryImpl factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected Map<String, Resource.Factory> getRegistrations() {
		if (registrations == null) {
			super.getRegistrations();
			registrations.put(XML_EXTENSION, new RepositoryResourceFactoryImpl());
			registrations.put(STAR_EXTENSION, new RepositoryResourceFactoryImpl());
		}
		return registrations;
	}

} // RepositoryXMLProcessor
