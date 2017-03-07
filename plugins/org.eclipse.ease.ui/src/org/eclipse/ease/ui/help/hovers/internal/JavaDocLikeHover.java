/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This class is taken from org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Genady Beryozkin <eclipse@genady.org> - [hovering] tooltip for constant string does not show constant value - https://bugs.eclipse.org/bugs/show_bug.cgi?id=85382
 *     Stephan Herrmann - Contribution for Bug 403917 - [1.8] Render TYPE_USE annotations in Javadoc hover/view
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers.internal;

import org.eclipse.ease.ui.help.hovers.IHoverContentProvider;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IInputChangedListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Provides a javadoc-like hover info.
 */
public class JavaDocLikeHover {

	public static final String CONSTANT_VALUE_SEPARATOR = " : "; //$NON-NLS-1$

	public static class FallbackInformationPresenter extends HTMLTextPresenter {
		public FallbackInformationPresenter() {
			super(false);
		}

		@Override
		public String updatePresentation(Drawable drawable, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {
			// FIXME
			// final String warningInfo = JavaHoverMessages.JavadocHover_fallback_warning;
			final String warningInfo = "JavaHoverMessages.JavadocHover_fallback_warning";
			final String warning = super.updatePresentation(drawable, warningInfo, presentation, maxWidth, maxHeight);
			presentation.clear();

			final String content = super.updatePresentation(drawable, hoverInfo, presentation, maxWidth, maxHeight);
			return content + "\n\n" + warning; //$NON-NLS-1$
		}
	}

	/**
	 * Action to go back to the previous input in the hover control.
	 *
	 * @since 3.4
	 */
	private static final class BackAction extends Action {
		private final BrowserInformationControl fInfoControl;

		public BackAction(BrowserInformationControl infoControl) {
			fInfoControl = infoControl;
			// FIXME
			// setText(JavaHoverMessages.JavadocHover_back);
			setText("JavaHoverMessages.JavadocHover_back");
			final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
			setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));

			update();
		}

		@Override
		public void run() {
			final BrowserInformationControlInput previous = (BrowserInformationControlInput) fInfoControl.getInput().getPrevious();
			if (previous != null) {
				fInfoControl.setInput(previous);
			}
		}

		public void update() {
			final BrowserInformationControlInput current = fInfoControl.getInput();

			if ((current != null) && (current.getPrevious() != null)) {
				setToolTipText("back to previous");
				setEnabled(true);
			} else {
				setToolTipText("JavaHoverMessages.JavadocHover_back");
				setEnabled(false);
			}
		}
	}

	/**
	 * Action to go forward to the next input in the hover control.
	 *
	 * @since 3.4
	 */
	private static final class ForwardAction extends Action {
		private final BrowserInformationControl fInfoControl;

		public ForwardAction(BrowserInformationControl infoControl) {
			fInfoControl = infoControl;
			// FIXME
			// setText(JavaHoverMessages.JavadocHover_forward);
			setText("JavaHoverMessages.JavadocHover_forward");
			final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
			setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));

			update();
		}

		@Override
		public void run() {
			final BrowserInformationControlInput next = (BrowserInformationControlInput) fInfoControl.getInput().getNext();
			if (next != null) {
				fInfoControl.setInput(next);
			}
		}

		public void update() {
			final BrowserInformationControlInput current = fInfoControl.getInput();

			if ((current != null) && (current.getNext() != null)) {
				setToolTipText("forward");
				setEnabled(true);
			} else {
				setToolTipText("JavaHoverMessages.JavadocHover_forward_toolTip");
				setEnabled(false);
			}
		}
	}

	/**
	 * Presenter control creator.
	 *
	 * @since 3.3
	 */
	public final class PresenterControlCreator extends AbstractReusableInformationControlCreator {

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			if (BrowserInformationControl.isAvailable(parent)) {
				final ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
				final String font = PreferenceConstants.APPEARANCE_JAVADOC_FONT;
				final BrowserInformationControl iControl = new BrowserInformationControl(parent, font, tbm);

				fHoverContent.populateToolbar(iControl, tbm);
				final BackAction backAction = new BackAction(iControl);
				backAction.setEnabled(false);
				tbm.add(backAction);
				final ForwardAction forwardAction = new ForwardAction(iControl);
				tbm.add(forwardAction);
				forwardAction.setEnabled(false);

				final IInputChangedListener inputChangeListener = new IInputChangedListener() {
					@Override
					public void inputChanged(Object newInput) {
						backAction.update();
						forwardAction.update();
					}
				};
				iControl.addInputChangeListener(inputChangeListener);

				tbm.update(true);

				return iControl;

			} else {
				return new DefaultInformationControl(parent, true);
			}
		}
	}

	/**
	 * Hover control creator.
	 *
	 * @since 3.3
	 */
	public static final class HoverControlCreator extends AbstractReusableInformationControlCreator {
		/**
		 * The information presenter control creator.
		 *
		 * @since 3.4
		 */
		private final IInformationControlCreator fInformationPresenterControlCreator;

		/**
		 * @param informationPresenterControlCreator
		 *            control creator for enriched hover
		 * @since 3.4
		 */
		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator) {
			fInformationPresenterControlCreator = informationPresenterControlCreator;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			final String tooltipAffordanceString = "Press 'F2' for focus";
			if (BrowserInformationControl.isAvailable(parent)) {
				final String font = PreferenceConstants.APPEARANCE_JAVADOC_FONT;
				return new BrowserInformationControl(parent, font, tooltipAffordanceString) {
					/*
					 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
					 */
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return fInformationPresenterControlCreator;
					}
				};
			} else {
				return new DefaultInformationControl(parent, tooltipAffordanceString) {
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return new IInformationControlCreator() {
							@Override
							public IInformationControl createInformationControl(Shell parentShell) {
								return new DefaultInformationControl(parentShell, (ToolBarManager) null, new FallbackInformationPresenter());
							}
						};
					}
				};
			}
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.IInformationControl)
		 */
		@Override
		public boolean canReuse(IInformationControl control) {
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4) {
				final String tooltipAffordanceString = "Press 'F2' for focus";
				((IInformationControlExtension4) control).setStatusText(tooltipAffordanceString);
			}

			return true;
		}
	}

	/**
	 * The style sheet (css).
	 *
	 * @since 3.4
	 */
	private static String fgStyleSheet;

	/**
	 * The hover control creator.
	 *
	 * @since 3.2
	 */
	private IInformationControlCreator fHoverControlCreator;
	/**
	 * The presentation control creator.
	 *
	 * @since 3.2
	 */
	private IInformationControlCreator fPresenterControlCreator;

	private final IHoverContentProvider fHoverContent;

	/**
	 * @param hoverContent
	 */
	public JavaDocLikeHover(IHoverContentProvider hoverContent) {
		fHoverContent = hoverContent;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractJavaEditorTextHover#getInformationPresenterControlCreator()
	 *
	 * @since 3.1
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fPresenterControlCreator == null)
			fPresenterControlCreator = new PresenterControlCreator();
		return fPresenterControlCreator;
	}

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 *
	 * @since 3.2
	 */
	public IInformationControlCreator getHoverControlCreator() {
		if (fHoverControlCreator == null)
			fHoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
		return fHoverControlCreator;
	}

	/**
	 * Computes the hover info.
	 *
	 * @param elements
	 *            the resolved elements
	 * @param editorInputElement
	 *            the editor input, or <code>null</code>
	 * @param hoverRegion
	 *            the text range of the hovered word, or <code>null</code>
	 * @param previousInput
	 *            the previous input, or <code>null</code>
	 * @return the HTML hover info for the given element(s) or <code>null</code> if no information is available
	 * @since 3.4
	 */
	public static BrowserInformationControlInput getHoverInfo(String content, BrowserInformationControlInput previousInput) {
		final StringBuffer buffer = new StringBuffer(content);

		// HTMLPrinter.addParagraph(buffer, content);

		if (buffer.length() > 0) {
			HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
			HTMLPrinter.addPageEpilog(buffer);

			return new BrowserInformationControlInput(previousInput) {

				@Override
				public String getHtml() {
					return buffer.toString();
				}

				@Override
				public Object getInputElement() {
					return getHtml();
				}

				@Override
				public String getInputName() {
					return "";
				}
			};
		}

		return null;
	}

	/**
	 * Returns the Javadoc hover style sheet with the current Javadoc font from the preferences.
	 *
	 * @return the updated style sheet
	 * @since 3.4
	 */
	private static String getStyleSheet() {
		if (fgStyleSheet == null) {
			// FIXME
			// fgStyleSheet = loadStyleSheet("/JavadocHoverStyleSheet.css"); //$NON-NLS-1$

			fgStyleSheet = "/* Font definitions */\r\n"
					+ "html         { font-family: sans-serif; font-size: 9pt; font-style: normal; font-weight: normal; }\r\n"
					+ "body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }\r\n"
					+ "pre          { font-family: monospace; }\r\n" + "\r\n" + "/* Margins */\r\n"
					+ "body	     { overflow: auto; margin-top: 0px; margin-bottom: 0.5em; margin-left: 0.3em; margin-right: 0px; }\r\n"
					+ "h1           { margin-top: 0.3em; margin-bottom: 0.04em; }	\r\n" + "h2           { margin-top: 2em; margin-bottom: 0.25em; }\r\n"
					+ "h3           { margin-top: 1.7em; margin-bottom: 0.25em; }\r\n" + "h4           { margin-top: 2em; margin-bottom: 0.3em; }\r\n"
					+ "h5           { margin-top: 0px; margin-bottom: 0px; }\r\n" + "p            { margin-top: 1em; margin-bottom: 1em; }\r\n"
					+ "pre          { margin-left: 0.6em; }\r\n"
					+ "ul	         { margin-top: 0px; margin-bottom: 1em; margin-left: 1em; padding-left: 1em;}\r\n"
					+ "li	         { margin-top: 0px; margin-bottom: 0px; } \r\n" + "li p	     { margin-top: 0px; margin-bottom: 0px; } \r\n"
					+ "ol	         { margin-top: 0px; margin-bottom: 1em; margin-left: 1em; padding-left: 1em; }\r\n"
					+ "dl	         { margin-top: 0px; margin-bottom: 1em; }\r\n"
					+ "dt	         { margin-top: 0px; margin-bottom: 0px; font-weight: bold; }\r\n"
					+ "dd	         { margin-top: 0px; margin-bottom: 0px; }\r\n" + "\r\n" + "/* Styles and colors */\r\n"
					+ "a:link	     { color: #0000FF; }\r\n" + "a:hover	     { color: #000080; }\r\n" + "a:visited    { text-decoration: underline; }\r\n"
					+ "a.header:link    { text-decoration: none; color: InfoText }\r\n" + "a.header:visited { text-decoration: none; color: InfoText }\r\n"
					+ "a.header:hover   { text-decoration: underline; color: #000080; }\r\n" + "h4           { font-style: italic; }\r\n"
					+ "strong	     { font-weight: bold; }\r\n" + "em	         { font-style: italic; }\r\n" + "var	         { font-style: italic; }\r\n"
					+ "th	         { font-weight: bold; }\r\n" + "\r\n" + "/* Workarounds for new Javadoc stylesheet (1.7) */ \r\n"
					+ "ul.blockList li.blockList, ul.blockListLast li.blockList {\r\n" + "    list-style:none;\r\n" + "}\r\n"
					+ "ul.blockList ul.blockList ul.blockList ul.blockList li.blockListLast {\r\n" + "    list-style:none;\r\n" + "}\r\n" + "";

			// additional content we need for EASE
			fgStyleSheet += ".optional { display:block; margin-left: 2em; }\r\n";
			fgStyleSheet += ".optional b { font-weight: normal; }\r\n";
			fgStyleSheet += ".description { margin-left: 2em; }\r\n";
			fgStyleSheet += ".description::before { content: \"... \" }\r\n";
		}
		String css = fgStyleSheet;
		if (css != null) {
			final FontData fontData = JFaceResources.getFontRegistry().getFontData(PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}

		return css;
	}
}
