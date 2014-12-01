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
package org.eclipse.ease.ui.tools;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;

public class LocationImageDescriptor extends ImageDescriptor {

	private final String fLocation;

	public static ImageDescriptor createFromLocation(final String location) {
		if (location != null)
			return new LocationImageDescriptor(location);

		return null;
	}

	private LocationImageDescriptor(final String location) {
		fLocation = location;
	}

	@Override
	public ImageData getImageData() {
		ImageData result = null;
		InputStream in = ResourceTools.getInputStream(fLocation);

		// implementation copied from org.eclipse.jface.URLImageDescriptor
		if (in != null) {
			try {
				result = new ImageData(in);
			} catch (SWTException e) {
				if (e.code != SWT.ERROR_INVALID_IMAGE) {
					throw e;
					// fall through otherwise
				}
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, e.getLocalizedMessage(), e));
				}
			}
		}

		return (result != null) ? result : ImageDescriptor.getMissingImageDescriptor().getImageData();
	}
}
