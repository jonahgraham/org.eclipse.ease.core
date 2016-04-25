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
package org.eclipse.ease.ui.scripts.repository;

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.emf.common.util.EMap;

/**
 * <!-- begin-user-doc --> A representation of the model object ' <em><b>Script</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.ease.ui.scripts.repository.IScript#getTimestamp <em>Timestamp</em>}</li>
 * <li>{@link org.eclipse.ease.ui.scripts.repository.IScript#getEntry <em>Entry</em>}</li>
 * <li>{@link org.eclipse.ease.ui.scripts.repository.IScript#getScriptParameters <em>Script Parameters</em>}</li>
 * <li>{@link org.eclipse.ease.ui.scripts.repository.IScript#getUserParameters <em>User Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ease.ui.scripts.repository.IRepositoryPackage#getScript()
 * @model
 * @generated
 */
public interface IScript extends IRawLocation {
	/**
	 * Returns the value of the '<em><b>Timestamp</b></em>' attribute. The default value is <code>"-1"</code>. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Timestamp</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Timestamp</em>' attribute.
	 * @see #setTimestamp(long)
	 * @see org.eclipse.ease.ui.scripts.repository.IRepositoryPackage#getScript_Timestamp()
	 * @model default="-1" required="true"
	 * @generated
	 */
	long getTimestamp();

	/**
	 * Sets the value of the ' {@link org.eclipse.ease.ui.scripts.repository.IScript#getTimestamp <em>Timestamp</em>}' attribute. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 *
	 * @param value
	 *            the new value of the '<em>Timestamp</em>' attribute.
	 * @see #getTimestamp()
	 * @generated
	 */
	void setTimestamp(long value);

	/**
	 * Returns the value of the '<em><b>Entry</b></em>' container reference. It is bidirectional and its opposite is '
	 * {@link org.eclipse.ease.ui.scripts.repository.IScriptLocation#getScripts <em>Scripts</em>}'. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Entry</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Entry</em>' container reference.
	 * @see #setEntry(IScriptLocation)
	 * @see org.eclipse.ease.ui.scripts.repository.IRepositoryPackage#getScript_Entry()
	 * @see org.eclipse.ease.ui.scripts.repository.IScriptLocation#getScripts
	 * @model opposite="scripts" required="true" transient="false"
	 * @generated
	 */
	IScriptLocation getEntry();

	/**
	 * Sets the value of the '{@link org.eclipse.ease.ui.scripts.repository.IScript#getEntry <em>Entry</em>}' container reference. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Entry</em>' container reference.
	 * @see #getEntry()
	 * @generated
	 */
	void setEntry(IScriptLocation value);

	/**
	 * Returns the value of the '<em><b>Script Parameters</b></em>' map. The key is of type {@link java.lang.String}, and the value is of type
	 * {@link java.lang.String}, <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Script Parameters</em>' map isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Script Parameters</em>' map.
	 * @see org.eclipse.ease.ui.scripts.repository.IRepositoryPackage#getScript_ScriptParameters()
	 * @model mapType="org.eclipse.ease.ui.scripts.repository.ParameterMap<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	EMap<String, String> getScriptParameters();

	/**
	 * Returns the value of the '<em><b>User Parameters</b></em>' map. The key is of type {@link java.lang.String}, and the value is of type
	 * {@link java.lang.String}, <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>User Parameters</em>' map isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>User Parameters</em>' map.
	 * @see org.eclipse.ease.ui.scripts.repository.IRepositoryPackage#getScript_UserParameters()
	 * @model mapType="org.eclipse.ease.ui.scripts.repository.ParameterMap<org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString>"
	 * @generated
	 */
	EMap<String, String> getUserParameters();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	void run();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	String getName();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation" dataType="org.eclipse.ease.ui.scripts.repository.Path"
	 * @generated
	 */
	IPath getPath();

	/**
	 * Get all parameters stored for this script. Merges script parameters and user defined parameters. User parameters have higher relevance.
	 * 
	 * @generated NOT
	 */
	Map<String, String> getParameters();

	/**
	 *
	 * @generated NOT
	 */
	ScriptType getType();

	/**
	 * Verify if this is a remote script. Remote scripts are not stored on a local file system.
	 * 
	 * @generated NOT
	 */
	boolean isRemote();

} // IScript
