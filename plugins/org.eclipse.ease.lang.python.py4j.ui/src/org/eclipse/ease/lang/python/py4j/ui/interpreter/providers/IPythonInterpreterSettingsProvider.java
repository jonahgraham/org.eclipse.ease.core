package org.eclipse.ease.lang.python.py4j.ui.interpreter.providers;

import java.util.List;

/**
 * Interface for describing a class that provides
 * settings for specifying a Python interpreter instance. 
 * 
 * @author Tracy
 *
 */
public interface IPythonInterpreterSettingsProvider {
	
	/**
	 * Id of the interpreter settings provider 
	 * (As specified in plugin.xml)
	 * 
	 * @return id of the provider
	 */
	public String getId();

	/**
	 * User facing name of the provider
	 * 
	 * @return name of the provider
	 */
	public String getName();
	
	/**
	 * Return the list of settings for the given provider. 
	 * 
	 * @return the list of settings or {@code null} if there are no settings defined
	 */
	public List<String> getSettingsEntries();
}
