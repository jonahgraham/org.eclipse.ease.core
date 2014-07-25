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

import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
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
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

	private SashForm sashForm;

	private static final String XML_HISTORY_NODE = "history";

	protected static final int TYPE_ERROR = 1;

	protected static final int TYPE_OUTPUT = 2;

	protected static final int TYPE_RESULT = 3;

	private static final int TYPE_COMMAND = 4;

	private Combo mInputCombo;

	private StyledText mOutputText;

	private boolean mScrollLock = false;

	private boolean mPrintLock = false;

	private LocalResourceManager mResourceManager = null;

	private int[] mSashWeights = new int[] { 70, 30 };

	private IScriptEngine fScriptEngine;

	private IMemento mInitMemento;

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
					mInputCombo.setText(mInputCombo.getText() + content.toString());

				mInputCombo.setFocus();
				mInputCombo.setSelection(new Point(mInputCombo.getText().length(), mInputCombo.getText().length()));
			}
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (!((e.keyCode == 'c') && ((e.stateMask & SWT.CONTROL) != 0)) && (e.keyCode != SWT.CONTROL)) {
				mInputCombo.setText(mInputCombo.getText() + e.character);
				mInputCombo.setFocus();
				mInputCombo.setSelection(new Point(mInputCombo.getText().length(), mInputCombo.getText().length()));
			}
		}
	}

	private AutoFocus fAutoFocusListener = null;

	private ContentProposalAdapter fContentAssistAdapter = null;

	/**
	 * Default constructor.
	 */
	public ScriptShell() {
		super();

		// setup Script engine
		final IScriptService scriptService = (IScriptService) PlatformUI.getWorkbench().getService(IScriptService.class);
		ScriptType scriptType = scriptService.getAvailableScriptTypes().get("JavaScript");
		Collection<EngineDescription> engines = scriptType.getEngines();
		if (!engines.isEmpty())
			setEngine(engines.iterator().next().getID());

		// add dynamic context menu for scripts
		// ScriptContributionFactory.addContextMenu("org.eclipse.ease.commands.script.toggleScriptPane.popup");

		// FIXME add preferences lookup
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public final void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);

		// cannot restore command history right now, do this in
		// createPartControl()
		mInitMemento = memento;
	}

	@Override
	public final void saveState(final IMemento memento) {
		// save command history
		for (final String item : mInputCombo.getItems())
			memento.createChild(XML_HISTORY_NODE).putTextData(item);

		super.saveState(memento);
	}

	@Override
	public final void createPartControl(final Composite parent) {

		// setup resource manager
		mResourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

		// setup layout
		parent.setLayout(new GridLayout());

		sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		mOutputText = new StyledText(sashForm, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);

		// set monospaced font
		final Object os = Platform.getOS();
		if ("win32".equals(os))
			mOutputText.setFont(mResourceManager.createFont(FontDescriptor.createFrom("Courier New", 10, SWT.NONE)));

		else if ("linux".equals(os))
			mOutputText.setFont(mResourceManager.createFont(FontDescriptor.createFrom("Monospace", 10, SWT.NONE)));

		mOutputText.setEditable(false);
		mOutputText.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(final MouseEvent e) {
			}

			@Override
			public void mouseDown(final MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				// copy line under cursor in input box
				String selected = mOutputText.getLine(mOutputText.getLineIndex(e.y));
				if (!selected.isEmpty()) {
					mInputCombo.setText(selected);
					mInputCombo.setFocus();
					mInputCombo.setSelection(new Point(0, selected.length()));
				}
			}
		});

		fScriptComposite = new ScriptComposite(this, getSite(), sashForm, SWT.NONE);
		fScriptComposite.setEngine(fScriptEngine.getDescription().getID());

		sashForm.setWeights(mSashWeights);
		mInputCombo = new Combo(parent, SWT.NONE);
		mInputCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				final String input = mInputCombo.getText();
				mInputCombo.setText("");

				addToHistory(input);
				fScriptEngine.executeAsync(input);
			}
		});

		mInputCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		// restore command history
		if (mInitMemento != null) {
			for (final IMemento node : mInitMemento.getChildren(XML_HISTORY_NODE)) {
				if (node.getTextData() != null)
					mInputCombo.add(node.getTextData());
			}
		}

		addAutoCompletion();

		// clear reference as we are done with initialization
		mInitMemento = null;

		// add DND support
		ShellDropTarget.addDropSupport(mOutputText, this);

		// set view title
		setPartName(fScriptEngine.getName() + " " + super.getTitle());

		// read default preferences
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		fHistoryLength = prefs.getInt(IPreferenceConstants.SHELL_HISTORY_LENGTH, IPreferenceConstants.DEFAULT_SHELL_HISTORY_LENGTH);
		fAutoFocus = prefs.getBoolean(IPreferenceConstants.SHELL_AUTOFOCUS, IPreferenceConstants.DEFAULT_SHELL_AUTOFOCUS);
		fKeepCommand = prefs.getBoolean(IPreferenceConstants.SHELL_KEEP_COMMAND, IPreferenceConstants.DEFAULT_SHELL_KEEP_COMMAND);

		if (fAutoFocus) {
			if (fAutoFocusListener == null)
				fAutoFocusListener = new AutoFocus();

			mOutputText.addKeyListener(fAutoFocusListener);
		}

		// run startup commands, do this for all supported script types
		runStartupCommands();
	}

	private void addAutoCompletion() {
		// we cannot detach an existing provider, so disable them
		if (fContentAssistAdapter != null)
			fContentAssistAdapter.setEnabled(false);

		// get auto completion provider for current engine
		ICompletionProvider provider = ModuleCompletionProvider.getCompletionProvider(fScriptEngine.getDescription());

		if (provider != null) {
			try {
				KeyStroke activationKey = KeyStroke.getInstance("Ctrl+Space");
				ContentProposalAdapter adapter = new ContentProposalAdapter(mInputCombo, new ComboContentAdapter(), provider, activationKey,
						provider.getActivationChars());
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
				fContentAssistAdapter = adapter;
			} catch (ParseException e) {
				Logger.logError("Cannot create content assist", e);
			}
		}
	}

	public void runStartupCommands() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(IPreferenceConstants.NODE_SHELL);

		for (ScriptType scriptType : fScriptEngine.getDescription().getSupportedScriptTypes()) {
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
		if (mInputCombo.getSelectionIndex() != -1)
			mInputCombo.remove(mInputCombo.getSelectionIndex());

		else {
			// new element; check if we already have such an element in our history
			for (int index = 0; index < mInputCombo.getItemCount(); index++) {
				if (mInputCombo.getItem(index).equals(input)) {
					mInputCombo.remove(index);
					break;
				}
			}
		}

		// avoid history overflows
		while (mInputCombo.getItemCount() >= fHistoryLength)
			mInputCombo.remove(mInputCombo.getItemCount() - 1);

		mInputCombo.add(input, 0);
	}

	@Override
	public final void dispose() {
		if (fScriptEngine != null) {
			fScriptEngine.removeExecutionListener(this);
			fScriptEngine.terminate();
		}

		mResourceManager.dispose();

		super.dispose();
	}

	@Override
	public final void setFocus() {
		mInputCombo.setFocus();
	}

	/**
	 * Clear the output text.
	 */
	public final void clearOutput() {
		mOutputText.setText("");
		mOutputText.setStyleRanges(new StyleRange[0]);
	}

	/**
	 * Set/unset the scroll lock feature.
	 * 
	 * @param lock
	 *            true when auto scrolling shall be locked
	 */
	public final void setScrollLock(final boolean lock) {
		mScrollLock = lock;
	}

	/**
	 * Set/unset the print lock feature. When
	 * 
	 * @param lock
	 *            true when printing shall be disabled
	 */
	public final void setPrintLock(final boolean lock) {
		mPrintLock = lock;
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
			if ((!mPrintLock) || (style == TYPE_ERROR)) {
				// // print to output pane
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						String out = message;
						if (style != TYPE_COMMAND)
							// indent message
							out = "\t" + message.replaceAll("\\r?\\n", "\n\t");

						if (!mOutputText.isDisposed()) {
							mOutputText.append("\n");

							// create new style range
							final StyleRange styleRange = getStyle(style, mOutputText.getText().length(), out.length());

							mOutputText.append(out);
							mOutputText.setStyleRange(styleRange);

							// scroll to end of window
							if (!mScrollLock) {
								mOutputText.setHorizontalPixel(0);
								mOutputText.setTopPixel(mOutputText.getLineHeight() * mOutputText.getLineCount());
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
			styleRange.foreground = mResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_DARK_GRAY)));
			break;

		case TYPE_COMMAND:
			styleRange.foreground = mResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_BLUE)));
			styleRange.fontStyle = SWT.BOLD;
			break;

		case TYPE_ERROR:
			styleRange.foreground = mResourceManager.createColor(ColorDescriptor
					.createFrom(getViewSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED)));
			styleRange.fontStyle = SWT.ITALIC;
			break;

		case TYPE_OUTPUT:
			styleRange.foreground = mResourceManager.createColor(ColorDescriptor.createFrom(getViewSite().getShell().getDisplay()
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
		final String text = mOutputText.getSelectionText();
		if (text.isEmpty())
			return mOutputText.getText();

		return text;
	}

	@Override
	public final void toggleMacroManager() {
		if (sashForm.getWeights()[1] == 0)
			sashForm.setWeights(mSashWeights);

		else {
			mSashWeights = sashForm.getWeights();
			sashForm.setWeights(new int[] { 100, 0 });
		}
	}

	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		// a preference property changed

		if (IPreferenceConstants.SHELL_AUTOFOCUS.equals(event.getProperty())) {
			if (Boolean.parseBoolean(event.getNewValue().toString())) {
				if (fAutoFocusListener == null)
					fAutoFocusListener = new AutoFocus();

				mOutputText.addKeyListener(fAutoFocusListener);

			} else
				mOutputText.removeKeyListener(fAutoFocusListener);

		} else if (IPreferenceConstants.SHELL_KEEP_COMMAND.equals(event.getProperty())) {
			fKeepCommand = Boolean.parseBoolean(event.getNewValue().toString());

		} else if (IPreferenceConstants.SHELL_HISTORY_LENGTH.equals(event.getProperty())) {
			fHistoryLength = Integer.parseInt(event.getNewValue().toString());
		}
	}

	public StyledText getOutput() {
		return mOutputText;
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

				}

				if (fKeepCommand) {
					final String code = script.getCode();
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!mInputCombo.isDisposed()) {
								mInputCombo.setText(code);
								mInputCombo.setSelection(new Point(0, code.length()));
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

	public void setEngine(final String id) {
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
			ScriptConsole console = ScriptConsole.create(fScriptEngine.getName() + "Script Shell", fScriptEngine);
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
			if (mInputCombo != null)
				runStartupCommands();
		}
	}
}
