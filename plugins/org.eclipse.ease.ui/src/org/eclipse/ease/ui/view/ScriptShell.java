/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.ICompletionProvider;
import org.eclipse.ease.ui.completion.ModuleCompletionProvider;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.ease.ui.dnd.ShellDropTarget;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.ease.ui.scripts.IScriptSupport;
import org.eclipse.ease.ui.scripts.ui.ScriptComposite;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

/**
 * The JavaScript shell allows to interactively execute JavaScript code.
 */
public class ScriptShell extends ViewPart implements IScriptSupport, IPropertyChangeListener, IScriptEngineProvider, IExecutionListener {

	public static final String VIEW_ID = "org.eclipse.ease.views.scriptShell";

	private static final String XML_HISTORY_NODE = "history";

	private static final int TYPE_ERROR = 1;

	private static final int TYPE_OUTPUT = 2;

	private static final int TYPE_RESULT = 3;

	private static final int TYPE_COMMAND = 4;

	private SashForm fSashForm;

	private Combo fInputCombo;

	private StyledText fOutputText;

	private boolean fScrollLock = false;

	private boolean fPrintLock = false;

	private LocalResourceManager fResourceManager = null;

	private int[] fSashWeights = new int[] { 70, 30 };

	private IScriptEngine fScriptEngine;

	private IMemento fInitMemento;

	private ScriptComposite fScriptComposite;

	private int fHistoryLength;

	private boolean fAutoFocus;

	private boolean fKeepCommand;

	static {
		// add dynamic context menu for engine switching
		EngineContributionFactory.addContextMenu();

		// add dynamic context menu for module loading
		ModuleContributionFactory.addContextMenu();
	}

	private class AutoFocus implements KeyListener {

		@Override
		public void keyReleased(final KeyEvent e) {
			if ((e.keyCode == 'v') && ((e.stateMask & SWT.CONTROL) != 0)) {
				// CTRL-v pressed
				final Clipboard clipboard = new Clipboard(Display.getDefault());
				final Object content = clipboard.getContents(TextTransfer.getInstance());
				if (content != null)
					fInputCombo.setText(fInputCombo.getText() + content.toString());

				fInputCombo.setFocus();
				fInputCombo.setSelection(new Point(fInputCombo.getText().length(), fInputCombo.getText().length()));
			}
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (!((e.keyCode == 'c') && ((e.stateMask & SWT.CONTROL) != 0)) && (e.keyCode != SWT.CONTROL)) {
				fInputCombo.setText(fInputCombo.getText() + e.character);
				fInputCombo.setFocus();
				fInputCombo.setSelection(new Point(fInputCombo.getText().length(), fInputCombo.getText().length()));
			}
		}
	}

	private AutoFocus fAutoFocusListener = null;

	private ContentProposalAdapter fContentAssistAdapter = null;
	private TabItem fTabScripts;
	private TabItem ftabItem_1;
	private TabItem ftabItem_2;
	private Text ftext;
	private TabItem fTabVariables;
	private Composite fcomposite;
	private Tree ftree;
	private TreeViewer fVariablesTree;

	/**
	 * Default constructor.
	 */
	public ScriptShell() {
		super();

		// setup Script engine
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);

		// try to load preferred engine
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);
		final String engineID = prefs.get(IPreferenceConstants.SHELL_DEFAULT_ENGINE, IPreferenceConstants.DEFAULT_SHELL_DEFAULT_ENGINE);
		EngineDescription engineDescription = scriptService.getEngineByID(engineID);

		if (engineDescription == null) {
			// not found, try to load any JavaScript engine
			engineDescription = scriptService.getEngine("JavaScript");

			if (engineDescription == null) {
				// no luck either, get next engine of any type
				final Collection<EngineDescription> engines = scriptService.getEngines();
				if (!engines.isEmpty())
					engineDescription = engines.iterator().next();
			}
		}

		if (engineDescription != null)
			setEngine(engineDescription.getID());

		// add dynamic context menu for scripts
		// ScriptContributionFactory.addContextMenu("org.eclipse.ease.commands.script.toggleScriptPane.popup");

		// FIXME add preferences lookup
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public final void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);

		// cannot restore command history right now, do this in createPartControl()
		fInitMemento = memento;
	}

	@Override
	public final void saveState(final IMemento memento) {
		// save command history
		for (final String item : fInputCombo.getItems())
			memento.createChild(XML_HISTORY_NODE).putTextData(item);

		super.saveState(memento);
	}

	@Override
	public final void createPartControl(final Composite parent) {

		// setup resource manager
		fResourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

		// setup layout
		parent.setLayout(new GridLayout());

		fSashForm = new SashForm(parent, SWT.NONE);
		fSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fOutputText = new StyledText(fSashForm, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);

		// set monospaced font
		final Object os = Platform.getOS();
		if ("win32".equals(os))
			fOutputText.setFont(fResourceManager.createFont(FontDescriptor.createFrom("Courier New", 10, SWT.NONE)));

		else if ("linux".equals(os))
			fOutputText.setFont(fResourceManager.createFont(FontDescriptor.createFrom("Monospace", 10, SWT.NONE)));

		fOutputText.setEditable(false);
		fOutputText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// copy line under cursor in input box
				final String selected = fOutputText.getLine(fOutputText.getLineIndex(e.y));
				if (!selected.isEmpty()) {
					fInputCombo.setText(selected);
					fInputCombo.setFocus();
					fInputCombo.setSelection(new Point(0, selected.length()));
				}
			}
		});

		final TabFolder tabFolder = new TabFolder(fSashForm, SWT.BOTTOM);

		fTabScripts = new TabItem(tabFolder, SWT.NONE);
		fTabScripts.setText("Scripts");
		fScriptComposite = new ScriptComposite(this, getSite(), tabFolder, SWT.NONE);
		fScriptComposite.setEngine(fScriptEngine.getDescription().getID());
		fTabScripts.setControl(fScriptComposite);

		fTabVariables = new TabItem(tabFolder, SWT.NONE);
		fTabVariables.setText("Variables");

		fcomposite = new Composite(tabFolder, SWT.NONE);
		fTabVariables.setControl(fcomposite);
		final TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		fcomposite.setLayout(treeColumnLayout);

		fVariablesTree = new TreeViewer(fcomposite, SWT.BORDER);
		ftree = fVariablesTree.getTree();
		ftree.setHeaderVisible(true);
		ftree.setLinesVisible(true);

		fVariablesTree.setFilters(new ViewerFilter[] {

				// filter modules
				new ViewerFilter() {

					@Override
					public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
						return !((Entry<?, ?>) element).getKey().toString().startsWith(EnvironmentModule.MODULE_PREFIX);
					}
				},

				// filter default methods of Object class
				new ViewerFilter() {

					@Override
					public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
						final Object name = ((Entry<?, ?>) element).getKey();
						if (("wait()".equals(name)) || ("notify()".equals(name)) || ("notifyAll()".equals(name)) || ("equals()".equals(name))
								|| ("getClass()".equals(name)) || ("hashCode()".equals(name)) || ("toString()".equals(name)))
							return false;

						return true;
					}
				} });

		fVariablesTree.setComparator(new ViewerComparator() {
			@Override
			public int category(final Object element) {
				return (((Entry<?, ?>) element).getKey().toString().endsWith("()")) ? 2 : 1;
			}
		});

		fVariablesTree.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(final Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object getParent(final Object element) {
				return null;
			}

			@Override
			public Object[] getElements(final Object inputElement) {
				return getScriptEngine().getVariables().entrySet().toArray();
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				final Object parent = ((Entry<?, ?>) parentElement).getValue();

				// use reflection to resolve elements
				final Map<String, Object> children = new HashMap<String, Object>();

				if (!((Entry<?, ?>) parentElement).getKey().toString().endsWith("()")) {
					// fields
					for (final Field field : parent.getClass().getFields()) {
						try {
							children.put(field.getName(), field.get(parent));
						} catch (final Exception e) {
							// ignore, try next
						}
					}

					// methods
					for (final Method method : parent.getClass().getMethods()) {
						try {
							children.put(method.getName() + "()", method.getReturnType().getName());
						} catch (final Exception e) {
							// ignore, try next
						}
					}
				}

				return children.entrySet().toArray();
			}
		});

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column = treeViewerColumn.getColumn();
		treeColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText("Variable");
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Entry<?, ?>) element).getKey().toString();
			}

			@Override
			public Image getImage(final Object element) {
				if (((Entry<?, ?>) element).getKey().toString().endsWith("()"))
					return Activator.getImage("org.eclipse.wst.jsdt.ui", "/icons/full/obj16/methpub_obj.gif", true);

				return Activator.getImage("org.eclipse.wst.jsdt.ui", "/icons/full/obj16/field_public_obj.gif", true);
			}
		});

		final TreeViewerColumn treeViewerColumn2 = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column2 = treeViewerColumn2.getColumn();
		treeColumnLayout.setColumnData(column2, new ColumnWeightData(1));
		column2.setText("Content");
		treeViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((Entry<?, ?>) element).getValue().toString();
			}
		});

		fVariablesTree.setInput(this);

		fSashForm.setWeights(fSashWeights);
		fInputCombo = new Combo(parent, SWT.NONE);
		fInputCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				final String input = fInputCombo.getText();
				fInputCombo.setText("");

				addToHistory(input);
				fScriptEngine.executeAsync(input);
			}
		});

		fInputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// restore command history
		if (fInitMemento != null) {
			for (final IMemento node : fInitMemento.getChildren(XML_HISTORY_NODE)) {
				if (node.getTextData() != null)
					fInputCombo.add(node.getTextData());
			}
		}

		addAutoCompletion();

		// clear reference as we are done with initialization
		fInitMemento = null;

		// add DND support
		ShellDropTarget.addDropSupport(fOutputText, this);

		// set view title
		setPartName(fScriptEngine.getName() + " " + super.getTitle());

		// read default preferences
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		fHistoryLength = prefs.getInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, IPreferenceConstants.DEFAULT_SHELL_HISTORY_LENGTH);
		fAutoFocus = prefs.getBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, IPreferenceConstants.DEFAULT_SHELL_AUTOFOCUS);
		fKeepCommand = prefs.getBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, IPreferenceConstants.DEFAULT_SHELL_KEEP_COMMAND);

		if (fAutoFocus) {
			if (fAutoFocusListener == null)
				fAutoFocusListener = new AutoFocus();

			fOutputText.addKeyListener(fAutoFocusListener);
		}

		// run startup commands, do this for all supported script types
		runStartupCommands();
	}

	private void addAutoCompletion() {
		// we cannot detach an existing provider, so disable them
		if (fContentAssistAdapter != null)
			fContentAssistAdapter.setEnabled(false);

		// get auto completion provider for current engine
		final ICompletionProvider provider = ModuleCompletionProvider.getCompletionProvider(fScriptEngine.getDescription());

		if (provider != null) {
			try {
				final KeyStroke activationKey = KeyStroke.getInstance("Ctrl+Space");
				final ContentProposalAdapter adapter = new ContentProposalAdapter(fInputCombo, new ComboContentAdapter(), provider, activationKey,
						provider.getActivationChars());
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
				fContentAssistAdapter = adapter;
			} catch (final ParseException e) {
				Logger.logError("Cannot create content assist", e);
			}
		}
	}

	public void runStartupCommands() {
		final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		for (final ScriptType scriptType : fScriptEngine.getDescription().getSupportedScriptTypes()) {
			final String initCommands = prefs.get(IPreferenceConstants.SHELL_STARTUP + scriptType.getName(), "").trim();
			if (!initCommands.isEmpty())
				fScriptEngine.executeAsync(initCommands);
		}
	}

	/**
	 * Add a command to the command history. History is stored in a ring buffer, so old entries will drop out once new entries are added. History will be
	 * preserved over program sessions.
	 *
	 * @param input
	 *            command to be stored to history.
	 */
	private void addToHistory(final String input) {
		if (fInputCombo.getSelectionIndex() != -1)
			fInputCombo.remove(fInputCombo.getSelectionIndex());

		else {
			// new element; check if we already have such an element in our history
			for (int index = 0; index < fInputCombo.getItemCount(); index++) {
				if (fInputCombo.getItem(index).equals(input)) {
					fInputCombo.remove(index);
					break;
				}
			}
		}

		// avoid history overflows
		while (fInputCombo.getItemCount() >= fHistoryLength)
			fInputCombo.remove(fInputCombo.getItemCount() - 1);

		fInputCombo.add(input, 0);
	}

	@Override
	public final void dispose() {
		if (fScriptEngine != null) {
			fScriptEngine.removeExecutionListener(this);
			fScriptEngine.terminate();
		}

		fResourceManager.dispose();

		super.dispose();
	}

	@Override
	public final void setFocus() {
		fInputCombo.setFocus();
	}

	/**
	 * Clear the output text.
	 */
	public final void clearOutput() {
		fOutputText.setText("");
		fOutputText.setStyleRanges(new StyleRange[0]);
	}

	/**
	 * Set/unset the scroll lock feature.
	 *
	 * @param lock
	 *            true when auto scrolling shall be locked
	 */
	public final void setScrollLock(final boolean lock) {
		fScrollLock = lock;
	}

	/**
	 * Set/unset the print lock feature. When
	 *
	 * @param lock
	 *            true when printing shall be disabled
	 */
	public final void setPrintLock(final boolean lock) {
		fPrintLock = lock;
	}

	/**
	 * Print to the output pane or to console. Text in the output pane may be formatted in different styles depending on the style flag. Printing is executed if
	 * printLock is turned off or in case of error output.
	 *
	 * @param text
	 *            text to print
	 * @param style
	 *            style to use (see JavaScriptShell.STYLE_* constants)
	 */

	private void localPrint(final String message, final int style) {
		if (message != null) {
			if ((!fPrintLock) || (style == TYPE_ERROR)) {
				// // print to output pane
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						String out = message;
						if (style != TYPE_COMMAND)
							// indent message
							out = "\t" + message.replaceAll("\\r?\\n", "\n\t");

						if (!fOutputText.isDisposed()) {
							fOutputText.append("\n");

							// create new style range
							final StyleRange styleRange = getStyle(style, fOutputText.getText().length(), out.length());

							fOutputText.append(out);
							fOutputText.setStyleRange(styleRange);

							// scroll to end of window
							if (!fScrollLock) {
								fOutputText.setHorizontalPixel(0);
								fOutputText.setTopPixel(fOutputText.getLineHeight() * fOutputText.getLineCount());
							}
						}
					}
				});
			}
		}
	}

	/**
	 *
	 * @param style
	 *            style to use (see JavaScriptShell.STYLE_* constants)
	 * @param start
	 *            start of text to be styled
	 * @param length
	 *            length of text to be styled
	 * @return StyleRange for text
	 */
	private StyleRange getStyle(final int style, final int start, final int length) {

		final StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;

		switch (style) {
		case TYPE_RESULT:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_DARK_GRAY)));
			break;

		case TYPE_COMMAND:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_BLUE)));
			styleRange.fontStyle = SWT.BOLD;
			break;

		case TYPE_ERROR:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor
					.createFrom(getViewSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED)));
			styleRange.fontStyle = SWT.ITALIC;
			break;

		case TYPE_OUTPUT:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_BLACK)));
			break;

		default:
			break;
		}

		return styleRange;
	}

	/**
	 * Get the text selected in the output pane. if no text is selected, the whole content will be returned.
	 *
	 * @return selected text of output pane
	 */
	public final String getSelectedText() {
		final String text = fOutputText.getSelectionText();
		if (text.isEmpty())
			return fOutputText.getText();

		return text;
	}

	@Override
	public final void toggleMacroManager() {
		if (fSashForm.getWeights()[1] == 0)
			fSashForm.setWeights(fSashWeights);

		else {
			fSashWeights = fSashForm.getWeights();
			fSashForm.setWeights(new int[] { 100, 0 });
		}
	}

	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		// a preference property changed

		if (IPreferenceConstants.SHELL_AUTOFOCUS.equals(event.getProperty())) {
			if (Boolean.parseBoolean(event.getNewValue().toString())) {
				if (fAutoFocusListener == null)
					fAutoFocusListener = new AutoFocus();

				fOutputText.addKeyListener(fAutoFocusListener);

			} else
				fOutputText.removeKeyListener(fAutoFocusListener);

		} else if (IPreferenceConstants.SHELL_KEEP_COMMAND.equals(event.getProperty())) {
			fKeepCommand = Boolean.parseBoolean(event.getNewValue().toString());

		} else if (IPreferenceConstants.SHELL_HISTORY_LENGTH.equals(event.getProperty())) {
			fHistoryLength = Integer.parseInt(event.getNewValue().toString());
		}
	}

	public StyledText getOutput() {
		return fOutputText;
	}

	public void stopScriptEngine() {
		fScriptEngine.terminateCurrent();
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fScriptEngine;
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {

		try {
			switch (status) {
			case SCRIPT_START:
				localPrint(script.getCode(), TYPE_COMMAND);
				break;

			case SCRIPT_END:
				if (script.getResult().hasException())
					localPrint(script.getResult().getException().getLocalizedMessage(), TYPE_ERROR);

				else {
					final Object result = script.getResult().getResult();
					if (result != null)
						localPrint(script.getResult().getResult().toString(), TYPE_RESULT);
					else
						localPrint("[null]", TYPE_RESULT);

					// add to content assist
					if (fContentAssistAdapter != null)
						((ICompletionProvider) fContentAssistAdapter.getContentProposalProvider()).addCode(script.getCode());

					// update variables
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							fVariablesTree.refresh();
						}
					});
				}

				if (fKeepCommand) {
					final String code = script.getCode();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {

							if (!fInputCombo.isDisposed()) {
								fInputCombo.setText(code);
								fInputCombo.setSelection(new Point(0, code.length()));
							}
						}
					});
				}

				break;

			default:
				// do nothing
				break;
			}

		} catch (final Exception e) {
		}
	}

	public final void setEngine(final String id) {
		if (fScriptEngine != null) {
			fScriptEngine.removeExecutionListener(this);
			fScriptEngine.terminate();
		}

		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		fScriptEngine = scriptService.getEngineByID(id).createEngine();

		if (fScriptEngine != null) {
			fScriptEngine.setTerminateOnIdle(false);

			// set view title
			setPartName(fScriptEngine.getName() + " Script Shell");

			// prepare console
			final ScriptConsole console = ScriptConsole.create(fScriptEngine.getName() + "Script Shell", fScriptEngine);
			fScriptEngine.setOutputStream(console.getOutputStream());
			fScriptEngine.setErrorStream(console.getErrorStream());

			// register at script engine
			fScriptEngine.addExecutionListener(this);

			// set script type filter
			if (fScriptComposite != null)
				fScriptComposite.setEngine(fScriptEngine.getDescription().getID());

			// start script engine
			fScriptEngine.schedule();

			// execute startup scripts
			// TODO currently we cannot run this on the first launch as the UI is not ready yet
			if (fInputCombo != null)
				runStartupCommands();
		}
	}
}
