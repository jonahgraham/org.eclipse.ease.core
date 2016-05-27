package org.eclipse.ease.lang.python.py4j.ui.preferences;

import org.eclipse.ease.lang.python.py4j.ui.Activator;
import org.eclipse.ease.lang.python.py4j.ui.dialogs.DefaultPythonInterpreterPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
		GridLayout gridLayout = GridLayoutFactory.fillDefaults().create();
		composite.setLayout(gridLayout);
		composite.setLayoutData(GridDataFactory.fillDefaults().create());
		
		viewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(viewer.getControl());
		
		Group group = new Group(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(group);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		
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
		createOptionsPage(group);
		return composite;
	}


	private void createOptionsPage(Composite composite) {
		IDialogPage page = new DefaultPythonInterpreterPage();
		page.createControl(composite);
		page.setVisible(true);
		composite.layout(true); 
		
		
	}


	private void initializeProviders() {
		// TODO: create the providers
		String[] strings = new String[] {"Use Python Inpterpreter on PATH", "Specify Path to Python Interpreter", "Use PyDev Python Interpreter"};
		
		viewer.setInput(strings);
		viewer.setSelection(new StructuredSelection(strings[0]));
		
	}


	protected void updateButtons() {
		// TODO Auto-generated method stub
		
	}


	protected void displaySelectedProviderPage() {
		// TODO Auto-generated method stub
		
	}

}