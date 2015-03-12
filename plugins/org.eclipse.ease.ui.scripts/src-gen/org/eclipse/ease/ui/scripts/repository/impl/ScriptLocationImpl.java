/**
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 */
package org.eclipse.ease.ui.scripts.repository.impl;

import java.util.Collection;

import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.scripts.repository.IRepositoryPackage;
import org.eclipse.ease.ui.scripts.repository.IScript;
import org.eclipse.ease.ui.scripts.repository.IScriptLocation;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Script Location</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ease.ui.scripts.repository.impl.ScriptLocationImpl#isRecursive <em>Recursive</em>}</li>
 *   <li>{@link org.eclipse.ease.ui.scripts.repository.impl.ScriptLocationImpl#isDefault <em>Default</em>}</li>
 *   <li>{@link org.eclipse.ease.ui.scripts.repository.impl.ScriptLocationImpl#getScripts <em>Scripts</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ScriptLocationImpl extends RawLocationImpl implements IScriptLocation {
	/**
	 * The default value of the '{@link #isRecursive() <em>Recursive</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isRecursive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean RECURSIVE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isRecursive() <em>Recursive</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isRecursive()
	 * @generated
	 * @ordered
	 */
	protected boolean recursive = RECURSIVE_EDEFAULT;

	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;

	/**
	 * The cached value of the '{@link #getScripts() <em>Scripts</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getScripts()
	 * @generated
	 * @ordered
	 */
	protected EList<IScript> scripts;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ScriptLocationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IRepositoryPackage.Literals.SCRIPT_LOCATION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRecursive(boolean newRecursive) {
		boolean oldRecursive = recursive;
		recursive = newRecursive;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IRepositoryPackage.SCRIPT_LOCATION__RECURSIVE, oldRecursive, recursive));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDefault() {
		return default_;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDefault(boolean newDefault) {
		boolean oldDefault = default_;
		default_ = newDefault;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IRepositoryPackage.SCRIPT_LOCATION__DEFAULT, oldDefault, default_));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<IScript> getScripts() {
		if (scripts == null) {
			scripts = new EObjectContainmentWithInverseEList<IScript>(IScript.class, this, IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS, IRepositoryPackage.SCRIPT__ENTRY);
		}
		return scripts;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getScripts()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				return ((InternalEList<?>)getScripts()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__RECURSIVE:
				return isRecursive();
			case IRepositoryPackage.SCRIPT_LOCATION__DEFAULT:
				return isDefault();
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				return getScripts();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__RECURSIVE:
				setRecursive((Boolean)newValue);
				return;
			case IRepositoryPackage.SCRIPT_LOCATION__DEFAULT:
				setDefault((Boolean)newValue);
				return;
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				getScripts().clear();
				getScripts().addAll((Collection<? extends IScript>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__RECURSIVE:
				setRecursive(RECURSIVE_EDEFAULT);
				return;
			case IRepositoryPackage.SCRIPT_LOCATION__DEFAULT:
				setDefault(DEFAULT_EDEFAULT);
				return;
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				getScripts().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case IRepositoryPackage.SCRIPT_LOCATION__RECURSIVE:
				return recursive != RECURSIVE_EDEFAULT;
			case IRepositoryPackage.SCRIPT_LOCATION__DEFAULT:
				return default_ != DEFAULT_EDEFAULT;
			case IRepositoryPackage.SCRIPT_LOCATION__SCRIPTS:
				return scripts != null && !scripts.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (recursive: ");
		result.append(recursive);
		result.append(", default: ");
		result.append(default_);
		result.append(')');
		return result.toString();
	}

	@Override
	public Object getResource() {
		return ResourceTools.resolveFolder(getLocation(), null, false);
	}
} // ScriptLocationImpl
