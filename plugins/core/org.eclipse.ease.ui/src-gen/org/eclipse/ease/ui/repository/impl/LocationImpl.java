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
package org.eclipse.ease.ui.repository.impl;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.repository.ILocation;
import org.eclipse.ease.ui.repository.IRepositoryPackage;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Location</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ease.ui.repository.impl.LocationImpl#getLocation <em>Location</em>}</li>
 *   <li>{@link org.eclipse.ease.ui.repository.impl.LocationImpl#isUpdatePending <em>Update Pending</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class LocationImpl extends MinimalEObjectImpl.Container implements ILocation {
	/**
	 * The default value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLocation() <em>Location</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLocation()
	 * @generated
	 * @ordered
	 */
	protected String location = LOCATION_EDEFAULT;

	/**
	 * The default value of the '{@link #isUpdatePending() <em>Update Pending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUpdatePending()
	 * @generated
	 * @ordered
	 */
	protected static final boolean UPDATE_PENDING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isUpdatePending() <em>Update Pending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUpdatePending()
	 * @generated
	 * @ordered
	 */
	protected boolean updatePending = UPDATE_PENDING_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected LocationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IRepositoryPackage.Literals.LOCATION;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocation() {
		return location;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocation(String newLocation) {
		String oldLocation = location;
		location = newLocation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IRepositoryPackage.LOCATION__LOCATION, oldLocation, location));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isUpdatePending() {
		return updatePending;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUpdatePending(boolean newUpdatePending) {
		boolean oldUpdatePending = updatePending;
		updatePending = newUpdatePending;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IRepositoryPackage.LOCATION__UPDATE_PENDING, oldUpdatePending, updatePending));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public Object getContent() {
		return ResourceTools.getContent(getLocation());
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	@Override
	public InputStream getInputStream() {
		return ResourceTools.getInputStream(getLocation());
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case IRepositoryPackage.LOCATION__LOCATION:
				return getLocation();
			case IRepositoryPackage.LOCATION__UPDATE_PENDING:
				return isUpdatePending();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case IRepositoryPackage.LOCATION__LOCATION:
				setLocation((String)newValue);
				return;
			case IRepositoryPackage.LOCATION__UPDATE_PENDING:
				setUpdatePending((Boolean)newValue);
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
			case IRepositoryPackage.LOCATION__LOCATION:
				setLocation(LOCATION_EDEFAULT);
				return;
			case IRepositoryPackage.LOCATION__UPDATE_PENDING:
				setUpdatePending(UPDATE_PENDING_EDEFAULT);
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
			case IRepositoryPackage.LOCATION__LOCATION:
				return LOCATION_EDEFAULT == null ? location != null : !LOCATION_EDEFAULT.equals(location);
			case IRepositoryPackage.LOCATION__UPDATE_PENDING:
				return updatePending != UPDATE_PENDING_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case IRepositoryPackage.LOCATION___GET_CONTENT:
				return getContent();
			case IRepositoryPackage.LOCATION___GET_INPUT_STREAM:
				return getInputStream();
		}
		return super.eInvoke(operationID, arguments);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (location: ");
		result.append(location);
		result.append(", updatePending: ");
		result.append(updatePending);
		result.append(')');
		return result.toString();
	}

} // LocationImpl
