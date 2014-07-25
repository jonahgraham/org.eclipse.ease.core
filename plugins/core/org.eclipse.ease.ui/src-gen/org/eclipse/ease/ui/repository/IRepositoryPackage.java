/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************//**
 */
package org.eclipse.ease.ui.repository;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.ease.ui.repository.IRepositoryFactory
 * @model kind="package"
 * @generated
 */
public interface IRepositoryPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "repository";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "repository";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "repository";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IRepositoryPackage eINSTANCE = org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.ease.ui.repository.impl.LocationImpl <em>Location</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ease.ui.repository.impl.LocationImpl
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getLocation()
	 * @generated
	 */
	int LOCATION = 1;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__LOCATION = 0;

	/**
	 * The feature id for the '<em><b>Update Pending</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION__UPDATE_PENDING = 1;

	/**
	 * The number of structural features of the '<em>Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION_FEATURE_COUNT = 2;

	/**
	 * The operation id for the '<em>Get Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION___GET_CONTENT = 0;

	/**
	 * The operation id for the '<em>Get Input Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION___GET_INPUT_STREAM = 1;

	/**
	 * The number of operations of the '<em>Location</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LOCATION_OPERATION_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.ease.ui.repository.impl.ScriptImpl <em>Script</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ease.ui.repository.impl.ScriptImpl
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getScript()
	 * @generated
	 */
	int SCRIPT = 0;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__LOCATION = LOCATION__LOCATION;

	/**
	 * The feature id for the '<em><b>Update Pending</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__UPDATE_PENDING = LOCATION__UPDATE_PENDING;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__TIMESTAMP = LOCATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Entry</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__ENTRY = LOCATION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Script Parameters</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__SCRIPT_PARAMETERS = LOCATION_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>User Parameters</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT__USER_PARAMETERS = LOCATION_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Script</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT_FEATURE_COUNT = LOCATION_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>Get Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT___GET_CONTENT = LOCATION___GET_CONTENT;

	/**
	 * The operation id for the '<em>Get Input Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT___GET_INPUT_STREAM = LOCATION___GET_INPUT_STREAM;

	/**
	 * The operation id for the '<em>Run</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT___RUN = LOCATION_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT___GET_NAME = LOCATION_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Path</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT___GET_PATH = LOCATION_OPERATION_COUNT + 2;

	/**
	 * The number of operations of the '<em>Script</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCRIPT_OPERATION_COUNT = LOCATION_OPERATION_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.ease.ui.repository.impl.RepositoryImpl <em>Repository</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryImpl
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getRepository()
	 * @generated
	 */
	int REPOSITORY = 2;

	/**
	 * The feature id for the '<em><b>Entries</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY__ENTRIES = 0;

	/**
	 * The number of structural features of the '<em>Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.ease.ui.repository.impl.EntryImpl <em>Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ease.ui.repository.impl.EntryImpl
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getEntry()
	 * @generated
	 */
	int ENTRY = 3;

	/**
	 * The feature id for the '<em><b>Location</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY__LOCATION = LOCATION__LOCATION;

	/**
	 * The feature id for the '<em><b>Update Pending</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY__UPDATE_PENDING = LOCATION__UPDATE_PENDING;

	/**
	 * The feature id for the '<em><b>Recursive</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY__RECURSIVE = LOCATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Default</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY__DEFAULT = LOCATION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Scripts</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY__SCRIPTS = LOCATION_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY_FEATURE_COUNT = LOCATION_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY___GET_CONTENT = LOCATION___GET_CONTENT;

	/**
	 * The operation id for the '<em>Get Input Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY___GET_INPUT_STREAM = LOCATION___GET_INPUT_STREAM;

	/**
	 * The number of operations of the '<em>Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ENTRY_OPERATION_COUNT = LOCATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.ease.ui.repository.impl.ParameterMapImpl <em>Parameter Map</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ease.ui.repository.impl.ParameterMapImpl
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getParameterMap()
	 * @generated
	 */
	int PARAMETER_MAP = 4;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_MAP__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_MAP__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Parameter Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_MAP_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Parameter Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_MAP_OPERATION_COUNT = 0;


	/**
	 * The meta object id for the '<em>Path</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.core.runtime.IPath
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getPath()
	 * @generated
	 */
	int PATH = 5;


	/**
	 * The meta object id for the '<em>Input Stream</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.InputStream
	 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getInputStream()
	 * @generated
	 */
	int INPUT_STREAM = 6;


	/**
	 * Returns the meta object for class '{@link org.eclipse.ease.ui.repository.IScript <em>Script</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Script</em>'.
	 * @see org.eclipse.ease.ui.repository.IScript
	 * @generated
	 */
	EClass getScript();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ease.ui.repository.IScript#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.ease.ui.repository.IScript#getTimestamp()
	 * @see #getScript()
	 * @generated
	 */
	EAttribute getScript_Timestamp();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.ease.ui.repository.IScript#getEntry <em>Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Entry</em>'.
	 * @see org.eclipse.ease.ui.repository.IScript#getEntry()
	 * @see #getScript()
	 * @generated
	 */
	EReference getScript_Entry();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.ease.ui.repository.IScript#getScriptParameters <em>Script Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Script Parameters</em>'.
	 * @see org.eclipse.ease.ui.repository.IScript#getScriptParameters()
	 * @see #getScript()
	 * @generated
	 */
	EReference getScript_ScriptParameters();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.ease.ui.repository.IScript#getUserParameters <em>User Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>User Parameters</em>'.
	 * @see org.eclipse.ease.ui.repository.IScript#getUserParameters()
	 * @see #getScript()
	 * @generated
	 */
	EReference getScript_UserParameters();

	/**
	 * Returns the meta object for the '{@link org.eclipse.ease.ui.repository.IScript#run() <em>Run</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run</em>' operation.
	 * @see org.eclipse.ease.ui.repository.IScript#run()
	 * @generated
	 */
	EOperation getScript__Run();

	/**
	 * Returns the meta object for the '{@link org.eclipse.ease.ui.repository.IScript#getName() <em>Get Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Name</em>' operation.
	 * @see org.eclipse.ease.ui.repository.IScript#getName()
	 * @generated
	 */
	EOperation getScript__GetName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.ease.ui.repository.IScript#getPath() <em>Get Path</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Path</em>' operation.
	 * @see org.eclipse.ease.ui.repository.IScript#getPath()
	 * @generated
	 */
	EOperation getScript__GetPath();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ease.ui.repository.ILocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Location</em>'.
	 * @see org.eclipse.ease.ui.repository.ILocation
	 * @generated
	 */
	EClass getLocation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ease.ui.repository.ILocation#getLocation <em>Location</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Location</em>'.
	 * @see org.eclipse.ease.ui.repository.ILocation#getLocation()
	 * @see #getLocation()
	 * @generated
	 */
	EAttribute getLocation_Location();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ease.ui.repository.ILocation#isUpdatePending <em>Update Pending</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Update Pending</em>'.
	 * @see org.eclipse.ease.ui.repository.ILocation#isUpdatePending()
	 * @see #getLocation()
	 * @generated
	 */
	EAttribute getLocation_UpdatePending();

	/**
	 * Returns the meta object for the '{@link org.eclipse.ease.ui.repository.ILocation#getContent() <em>Get Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content</em>' operation.
	 * @see org.eclipse.ease.ui.repository.ILocation#getContent()
	 * @generated
	 */
	EOperation getLocation__GetContent();

	/**
	 * Returns the meta object for the '{@link org.eclipse.ease.ui.repository.ILocation#getInputStream() <em>Get Input Stream</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Input Stream</em>' operation.
	 * @see org.eclipse.ease.ui.repository.ILocation#getInputStream()
	 * @generated
	 */
	EOperation getLocation__GetInputStream();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ease.ui.repository.IRepository <em>Repository</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Repository</em>'.
	 * @see org.eclipse.ease.ui.repository.IRepository
	 * @generated
	 */
	EClass getRepository();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ease.ui.repository.IRepository#getEntries <em>Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Entries</em>'.
	 * @see org.eclipse.ease.ui.repository.IRepository#getEntries()
	 * @see #getRepository()
	 * @generated
	 */
	EReference getRepository_Entries();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ease.ui.repository.IEntry <em>Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Entry</em>'.
	 * @see org.eclipse.ease.ui.repository.IEntry
	 * @generated
	 */
	EClass getEntry();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ease.ui.repository.IEntry#isRecursive <em>Recursive</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Recursive</em>'.
	 * @see org.eclipse.ease.ui.repository.IEntry#isRecursive()
	 * @see #getEntry()
	 * @generated
	 */
	EAttribute getEntry_Recursive();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ease.ui.repository.IEntry#isDefault <em>Default</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Default</em>'.
	 * @see org.eclipse.ease.ui.repository.IEntry#isDefault()
	 * @see #getEntry()
	 * @generated
	 */
	EAttribute getEntry_Default();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ease.ui.repository.IEntry#getScripts <em>Scripts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Scripts</em>'.
	 * @see org.eclipse.ease.ui.repository.IEntry#getScripts()
	 * @see #getEntry()
	 * @generated
	 */
	EReference getEntry_Scripts();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Parameter Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter Map</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString" keyRequired="true"
	 *        valueDataType="org.eclipse.emf.ecore.EString" valueRequired="true"
	 * @generated
	 */
	EClass getParameterMap();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getParameterMap()
	 * @generated
	 */
	EAttribute getParameterMap_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getParameterMap()
	 * @generated
	 */
	EAttribute getParameterMap_Value();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.core.runtime.IPath <em>Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Path</em>'.
	 * @see org.eclipse.core.runtime.IPath
	 * @model instanceClass="org.eclipse.core.runtime.IPath"
	 * @generated
	 */
	EDataType getPath();

	/**
	 * Returns the meta object for data type '{@link java.io.InputStream <em>Input Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Input Stream</em>'.
	 * @see java.io.InputStream
	 * @model instanceClass="java.io.InputStream"
	 * @generated
	 */
	EDataType getInputStream();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	IRepositoryFactory getRepositoryFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.ease.ui.repository.impl.ScriptImpl <em>Script</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ease.ui.repository.impl.ScriptImpl
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getScript()
		 * @generated
		 */
		EClass SCRIPT = eINSTANCE.getScript();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCRIPT__TIMESTAMP = eINSTANCE.getScript_Timestamp();

		/**
		 * The meta object literal for the '<em><b>Entry</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCRIPT__ENTRY = eINSTANCE.getScript_Entry();

		/**
		 * The meta object literal for the '<em><b>Script Parameters</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCRIPT__SCRIPT_PARAMETERS = eINSTANCE.getScript_ScriptParameters();

		/**
		 * The meta object literal for the '<em><b>User Parameters</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCRIPT__USER_PARAMETERS = eINSTANCE.getScript_UserParameters();

		/**
		 * The meta object literal for the '<em><b>Run</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SCRIPT___RUN = eINSTANCE.getScript__Run();

		/**
		 * The meta object literal for the '<em><b>Get Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SCRIPT___GET_NAME = eINSTANCE.getScript__GetName();

		/**
		 * The meta object literal for the '<em><b>Get Path</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SCRIPT___GET_PATH = eINSTANCE.getScript__GetPath();

		/**
		 * The meta object literal for the '{@link org.eclipse.ease.ui.repository.impl.LocationImpl <em>Location</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ease.ui.repository.impl.LocationImpl
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getLocation()
		 * @generated
		 */
		EClass LOCATION = eINSTANCE.getLocation();

		/**
		 * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LOCATION__LOCATION = eINSTANCE.getLocation_Location();

		/**
		 * The meta object literal for the '<em><b>Update Pending</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LOCATION__UPDATE_PENDING = eINSTANCE.getLocation_UpdatePending();

		/**
		 * The meta object literal for the '<em><b>Get Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation LOCATION___GET_CONTENT = eINSTANCE.getLocation__GetContent();

		/**
		 * The meta object literal for the '<em><b>Get Input Stream</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation LOCATION___GET_INPUT_STREAM = eINSTANCE.getLocation__GetInputStream();

		/**
		 * The meta object literal for the '{@link org.eclipse.ease.ui.repository.impl.RepositoryImpl <em>Repository</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryImpl
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getRepository()
		 * @generated
		 */
		EClass REPOSITORY = eINSTANCE.getRepository();

		/**
		 * The meta object literal for the '<em><b>Entries</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REPOSITORY__ENTRIES = eINSTANCE.getRepository_Entries();

		/**
		 * The meta object literal for the '{@link org.eclipse.ease.ui.repository.impl.EntryImpl <em>Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ease.ui.repository.impl.EntryImpl
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getEntry()
		 * @generated
		 */
		EClass ENTRY = eINSTANCE.getEntry();

		/**
		 * The meta object literal for the '<em><b>Recursive</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENTRY__RECURSIVE = eINSTANCE.getEntry_Recursive();

		/**
		 * The meta object literal for the '<em><b>Default</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ENTRY__DEFAULT = eINSTANCE.getEntry_Default();

		/**
		 * The meta object literal for the '<em><b>Scripts</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ENTRY__SCRIPTS = eINSTANCE.getEntry_Scripts();

		/**
		 * The meta object literal for the '{@link org.eclipse.ease.ui.repository.impl.ParameterMapImpl <em>Parameter Map</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ease.ui.repository.impl.ParameterMapImpl
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getParameterMap()
		 * @generated
		 */
		EClass PARAMETER_MAP = eINSTANCE.getParameterMap();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_MAP__KEY = eINSTANCE.getParameterMap_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER_MAP__VALUE = eINSTANCE.getParameterMap_Value();

		/**
		 * The meta object literal for the '<em>Path</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.core.runtime.IPath
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getPath()
		 * @generated
		 */
		EDataType PATH = eINSTANCE.getPath();

		/**
		 * The meta object literal for the '<em>Input Stream</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.InputStream
		 * @see org.eclipse.ease.ui.repository.impl.RepositoryPackageImpl#getInputStream()
		 * @generated
		 */
		EDataType INPUT_STREAM = eINSTANCE.getInputStream();

	}

} //IRepositoryPackage
