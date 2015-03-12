package org.eclipse.ease.ui.scripts.ui;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.dnd.IShellDropHandler;
import org.eclipse.ease.ui.repository.IScript;

public class ShellDropHandler implements IShellDropHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ease.ui.dnd.IShellDropHandler#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(final Object element) {
		return element instanceof IScript;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ease.ui.dnd.IShellDropHandler#performDrop(org.eclipse.ease
	 * .ui.view.ScriptShell, java.lang.Object)
	 */
	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		if (element instanceof IScript)
			scriptEngine.executeAsync("include('script:/" + ((IScript) element).getPath() + "');");
	}
}
