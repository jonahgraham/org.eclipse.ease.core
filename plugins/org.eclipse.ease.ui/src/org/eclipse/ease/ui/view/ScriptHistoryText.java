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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

public class ScriptHistoryText extends StyledText implements IExecutionListener {

	public static final int STYLE_ERROR = 1;

	public static final int STYLE_RESULT = 3;

	public static final int STYLE_COMMAND = 4;

	private class BlendBackgroundJob extends UIJob {

		private boolean fRun;
		private volatile boolean fStarted;

		public BlendBackgroundJob() {
			super("Darken Script Shell background");
			setSystem(true);
		}

		public void arm() {
			fRun = true;
			fStarted = false;
			schedule(300);
		}

		public void disarm() {
			fRun = false;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fStarted = true;
			if (fRun)
				setBackground(fResourceManager.createColor(fColorDarkenedBackground));

			return Status.OK_STATUS;
		}
	}

	private BlendBackgroundJob fBlendBackgroundJob = null;

	private final LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources(), getParent());

	private final ColorDescriptor fColorDescriptorResult = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	private final ColorDescriptor fColorDescriptorCommand = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE));
	private final ColorDescriptor fColorDescriptorError = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
	private ColorDescriptor fColorDefaultBackground = null;
	private ColorDescriptor fColorDarkenedBackground = null;

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

		fColorDefaultBackground = ColorDescriptor.createFrom(getBackground());

		final RGB defaultBackground = getBackground().getRGB();
		final RGB darkenedBackground = new RGB(defaultBackground.red - 0x10, defaultBackground.green - 0x10, defaultBackground.blue - 0x10);
		fColorDarkenedBackground = ColorDescriptor.createFrom(darkenedBackground);

		setEditable(false);

		fBlendBackgroundJob = new BlendBackgroundJob();
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
				fBlendBackgroundJob.arm();
				localPrint(script.getCode(), STYLE_COMMAND);
				break;

			case SCRIPT_END:
				fBlendBackgroundJob.disarm();
				if (fBlendBackgroundJob.fStarted) {
					// we need to reset the background color
					Display.getDefault().asyncExec(() -> setBackground(fResourceManager.createColor(fColorDefaultBackground)));
				}

				if (script.getResult().hasException()) {
					localPrint(script.getResult().getException().getLocalizedMessage(), STYLE_ERROR);
				} else {
					final Object result = script.getResult().getResult();
					if (result != null) {
						localPrint(result.toString(), STYLE_RESULT);
					}
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
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorResult);
			break;

		case STYLE_COMMAND:
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorCommand);
			styleRange.fontStyle = SWT.BOLD;
			break;

		case STYLE_ERROR:
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorError);
			styleRange.fontStyle = SWT.ITALIC;
			break;

		default:
			break;
		}

		return styleRange;
	}
}
