/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

import java.util.Collection;
import java.util.Map;

import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.ScriptType;

/**
 * Interface for completion context. This context helps ICompletionProvider to simplify completion proposal calculation. Stores information about given input,
 * filter for part of interest, and Source stack for part of interest.
 *
 * @author Martin Kloesch
 */
public interface ICompletionContext {

	public enum Type {
		UNKNOWN, NONE, STATIC_CLASS, CLASS_INSTANCE, PACKAGE, STRING_LITERAL
	};

	String getOriginalCode();

	String getProcessedCode();

	String getFilter();

	Class<? extends Object> getReferredClazz();

	Object getResource();

	IScriptEngine getScriptEngine();

	ScriptType getScriptType();

	Collection<ModuleDefinition> getLoadedModules();

	Map<Object, String> getIncludedResources();

	public Type getType();

	int getOffset();

	int getSelectionRange();
}