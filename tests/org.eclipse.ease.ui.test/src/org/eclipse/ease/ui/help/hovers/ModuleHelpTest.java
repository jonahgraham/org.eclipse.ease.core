/*******************************************************************************
 * Copyright (c) 2016 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ease.modules.ModuleDefinition;
import org.junit.Before;
import org.junit.Test;

public class ModuleHelpTest {

	private Object fSampleModule;
	private ModuleDefinition fSampleModuleDefinition;
	private Method fSampleMethod;
	private Field fSampleField;

	@Before
	public void setUp() throws Exception {
		fSampleModule = new org.eclipse.ease.ui.help.hovers.SampleModule();
		fSampleModuleDefinition = ModuleDefinition.getDefinition(fSampleModule);
		fSampleMethod = fSampleModuleDefinition.getMethods().get(0);
		fSampleField = fSampleModuleDefinition.getFields().get(0);
	}

	@Test
	public void getModuleHelpTip() {
		assertFalse(ModuleHelp.getModuleHelpTip(fSampleModuleDefinition).equals(""));
	}

	@Test
	public void getMethodHelpTip() {
		assertFalse(ModuleHelp.getMethodHelpTip(fSampleMethod).equals(""));
	}

	@Test
	public void getConstantHelpTip() {
		assertFalse(ModuleHelp.getConstantHelpTip(fSampleField).equals(""));
	}
}
