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

package org.eclipse.ease.completion;

/**
 * Interface to describe a single item in a "call-chain".
 * 
 * This gives information about: 
 * 		o Type of source (see {@link SourceType}). 
 * 		o Class the item belongs to. 
 * 		o The name of the item in the call chain. 
 * 		o The actual item.
 * 
 * @author Martin Kloesch
 *
 */
public interface ICompletionSource {

	/**
	 * Enum to distinguish different types of ICompletion source.
	 * 
	 * @author Martin Kloesch
	 *
	 */
	public enum SourceType {
		/**
		 * Base for all constructors. Only used in static analysis.
		 */
		CONSTRUCTOR,

		/**
		 * Base for all functions and methods. Only used in static analysis.
		 */
		METHOD,

		/**
		 * Base for all items that are neither method, constructor nor string literal. Only used in static analysis.
		 */
		INSTANCE,

		STRING_LITERAL,

		/**
		 * Constructor for java class.
		 */
		JAVA_CONSTRUCTOR,

		/**
		 * Constructor for class defined in module.
		 */
		MODULE_CONSTRUCTOR,

		/**
		 * Constructor for local class.
		 */
		LOCAL_CONSTRUCTOR,

		/**
		 * Class method, might be local class, java class or class from module.
		 */
		CLASS_METHOD,

		/**
		 * Function defined in module.
		 */
		MODULE_METHOD,

		/**
		 * Local function.
		 */
		LOCAL_FUNCTION,

		/**
		 * Class field, might be local class, java class or class from module.
		 */
		CLASS_FIELD,

		/**
		 * Variable / constant defined in module.
		 */
		MODULE_FIELD,

		/**
		 * Local variable.
		 */
		LOCAL_VARIABLE,

		/**
		 * Java package.
		 */
		JAVA_PACKAGE,
		
		/**
		 * Keyword in language.
		 */
		KEYWORD
	}

	/**
	 * Getter method for the type of the item in the call chain.
	 * 
	 * @return source type of item in the call chain.
	 * @see SourceType
	 */
	SourceType getSourceType();

	/**
	 * Getter method for the name of the item in the call chain.
	 * 
	 * @return name of item in call chain.
	 */
	String getName();

	/**
	 * Getter method for class the item belongs to. Maybe <code>null</code> for first item in chain but every following method or field must belong to a class.
	 * 
	 * @return Class the item belongs to.
	 */
	Class<?> getClazz();

	/**
	 * Getter method for the actual object in call chain.
	 * 
	 * @return the actual object in chain.
	 */
	Object getObject();
	
	/**
	 * Getter method for a description of item in call chain.
	 * 
	 * @return description of completion source.
	 */
	String getDescription();
}