/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.view;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * @author ponteseg
 *
 */
public class ScriptHistoryText extends StyledText implements IExecutionListener {

	public static final int STYLE_ERROR = 1;

	public static final int STYLE_RESULT = 3;

	public static final int STYLE_COMMAND = 4;

	private final LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources(), getParent());

	public ScriptHistoryText(final Composite parent, final int style) {
		super(parent, style);

		initialize();
	}

	public void addScriptEngine(final IScriptEngine engine) {
		if (engine != null)
			engine.addExecutionListener(this);
	}

	public void removeScriptEngine(final IScriptEngine engine) {
		if (engine != null)
			engine.removeExecutionListener(this);
	}

	private void initialize() {
		// set monospaced font
		final Object os = Platform.getOS();
		if ("win32".equals(os))
			setFont(fResourceManager.createFont(FontDescriptor.createFrom("Courier New", 10, SWT.NONE)));

		else if ("linux".equals(os))
			setFont(fResourceManager.createFont(FontDescriptor.createFrom("Monospace", 10, SWT.NONE)));

		setEditable(false);
	}

	@Override
	public void dispose() {
		fResourceManager.dispose();

		super.dispose();
	}

	public void clear() {
		setText("");
		setStyleRanges(new StyleRange[0]);
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {

		try {
			switch (status) {
			case SCRIPT_START:
				localPrint(script.getCode(), STYLE_COMMAND);
				break;

			case SCRIPT_END:
				if (script.getResult().hasException())
					localPrint(script.getResult().getException().getLocalizedMessage(), STYLE_ERROR);

				else {
					final Object result = script.getResult().getResult();
					if (result != null)
						localPrint(script.getResult().getResult().toString(), STYLE_RESULT);
					else
						localPrint("[null]", STYLE_RESULT);
				}

				break;

			default:
				// do nothing
				break;
			}

		} catch (final Exception e) {
			// script.getCode() failed, ignore
		}
	}

	/**
	 * Print a given message. Text in the output pane may be formatted in different styles depending on the style flag.
	 *
	 * @param text
	 *            text to print
	 * @param style
	 *            style to use (see JavaScriptShell.STYLE_* constants)
	 */
	public void localPrint(final String message, final int style) {
		if (message != null) {
			// // print to output pane
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					String out = message;
					if (style != STYLE_COMMAND)
						// indent message
						out = "\t" + message.replaceAll("\\r?\\n", "\n\t");

					if (!isDisposed()) {
						append("\n");

						// create new style range
						final StyleRange styleRange = getStyle(style, getText().length(), out.length());

						append(out);
						setStyleRange(styleRange);

						// scroll to end of window
						setHorizontalPixel(0);
						setTopPixel(getLineHeight() * getLineCount());
					}
				}
			});
		}
	}

	/**
	 * Create a new style range for a given offset/length.
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
		case STYLE_RESULT:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY)));
			break;

		case STYLE_COMMAND:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE)));
			styleRange.fontStyle = SWT.BOLD;
			break;

		case STYLE_ERROR:
			styleRange.foreground = fResourceManager.createColor(ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_RED)));
			styleRange.fontStyle = SWT.ITALIC;
			break;

		default:
			break;
		}

		return styleRange;
	}
}
