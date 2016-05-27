package org.eclipse.ease.lang.python.py4j.ui.dialogs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DefaultPythonInterpreterPage extends AbstractInterpreterProviderPage {

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		createWidgetsForPage(composite);
		setControl(composite);
		
	}

	private void createWidgetsForPage(Composite composite) {
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().applyTo(composite);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("The Python interpreter found on the System Path will be used.");
		GridDataFactory.swtDefaults().applyTo(label);
		
	}

	@Override
	protected boolean isValid() {
		return true;
	}
	

}
