package org.eclipse.ease.lang.python.py4j.ui.dialogs;

import org.eclipse.jface.dialogs.DialogPage;

/**
 * Base class for Interpreter Provider Pages
 * 
 * @author Tracy
 *
 */
public abstract class AbstractInterpreterProviderPage extends DialogPage {

    protected abstract boolean isValid();
    
    //TODO: constructors, perform apply, perform defaults
}
