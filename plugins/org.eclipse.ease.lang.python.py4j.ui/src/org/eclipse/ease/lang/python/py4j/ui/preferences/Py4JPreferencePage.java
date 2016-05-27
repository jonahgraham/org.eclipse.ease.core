package org.eclipse.ease.lang.python.py4j.ui.preferences;

import org.eclipse.ease.lang.python.py4j.ui.Activator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class Py4JPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private ComboViewer viewer;
	public Py4JPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Python Interpreter Provider Settings");
	}


	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = GridLayoutFactory.swtDefaults().create();
		composite.setLayout(gridLayout);
		composite.setLayoutData(GridDataFactory.swtDefaults().create());
		
//		Label label = new Label(composite, SWT.NONE);
//		label.setText("Python Interpreter Provider:");
		
		viewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(viewer.getControl());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider(){
			@Override
			public String getText(Object element) {
				// TODO Auto-generated method stub
				return super.getText(element);
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				displaySelectedProviderPage();
				updateButtons();
			}
		});
		
		initializeProviders();
		return composite;
	}


	private void initializeProviders() {
		// TODO: create the providers
	
	}


	protected void updateButtons() {
		// TODO Auto-generated method stub
		
	}


	protected void displaySelectedProviderPage() {
		// TODO Auto-generated method stub
		
	}

}