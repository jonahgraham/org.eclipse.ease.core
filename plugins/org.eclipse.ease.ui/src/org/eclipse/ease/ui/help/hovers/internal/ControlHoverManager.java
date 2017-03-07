/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This class is taken from org.eclipse.jface.text.TextViewerHoverManager
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.help.hovers.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.ui.help.hovers.IHoverContentProvider;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension5;
import org.eclipse.jface.text.ITextViewerExtension8.EnrichMode;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

//FIXME: try to port back to org.eclipse.jface.text

/**
 * This manager controls the layout, content, and visibility of an information control in reaction to mouse hover events issued by a control. It overrides
 * <code>computeInformation</code>, so that the computation is performed in a dedicated background thread. This implies that the used
 * <code>IHoverContentProvider</code> objects must be capable of operating in a non-UI thread.
 */
public class ControlHoverManager extends AbstractHoverInformationControlManager implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {

	/**
	 * Priority of the hovers managed by this manager. Default value: <code>0</code>;
	 *
	 * @since 3.0
	 */
	public final static int WIDGET_PRIORITY = 0;

	/** The hover information computation thread */
	private Thread fThread;
	/** Internal monitor */
	private final Object fMutex = new Object();
	/**
	 * Tells whether the next mouse hover event should be processed.
	 *
	 * @since 3.0
	 */
	private boolean fProcessMouseHoverEvent = true;
	/**
	 * Internal mouse move listener.
	 *
	 * @since 3.0
	 */
	private MouseMoveListener fMouseMoveListener;

	private final Control fControl;

	private Control fHoverArea;

	private IWidgetTokenOwner fWidgetTokenOwner;

	private final IHoverContentProvider fHoverContent;

	public ControlHoverManager(Control hoverOrigin, Control hoverArea, IWidgetTokenOwner owner, IHoverContentProvider hoverContent) {
		super(new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});

		fControl = hoverOrigin;
		fHoverArea = hoverArea;
		fHoverContent = hoverContent;

		fMouseMoveListener = new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent event) {
				fProcessMouseHoverEvent = true;
			}
		};
		fControl.addMouseMoveListener(fMouseMoveListener);

		install(fControl, fHoverArea);

		final JavaDocLikeHover javaDocLikeHover = new JavaDocLikeHover(fHoverContent);

		setHoverEnrichMode(EnrichMode.IMMEDIATELY);
		setCustomInformationControlCreator(javaDocLikeHover.getHoverControlCreator());

		final StickyHoverManager myStickyHoverManager = new StickyHoverManager(fControl, fHoverArea, owner);
		setInformationControlReplacer(myStickyHoverManager);

		fWidgetTokenOwner = owner;
	}

	/**
	 * Determines all necessary details and delegates the computation into a background thread.
	 */
	@Override
	protected void computeInformation() {

		if (!fProcessMouseHoverEvent) {
			setInformation(null, null);
			return;
		}

		if (fThread != null) {
			setInformation(null, null);
			return;
		}

		final Object origin = getHoverOrigin();
		if (origin == null) {
			setInformation(null, null);
			return;
		}

		final Object details = getHoverDetails();
		final Rectangle area = getHoverInterestArea();

		fThread = new Thread("Text Viewer Hover Presenter") { //$NON-NLS-1$
			@Override
			public void run() {
				// http://bugs.eclipse.org/bugs/show_bug.cgi?id=17693
				boolean hasFinished = false;
				try {
					if (fThread != null) {
						final Object content = fHoverContent.getContent(origin, details);
						if (content instanceof BrowserInformationControlInput)
							setInformation(content, area);

						else if (content != null)
							setInformation(JavaDocLikeHover.getHoverInfo(content.toString(), null), area);

						else
							setInformation(null, null);

					} else {
						setInformation(null, null);
					}
					hasFinished = true;
				} catch (final RuntimeException ex) {
					final String PLUGIN_ID = "org.eclipse.jface.text"; //$NON-NLS-1$
					final ILog log = Platform.getLog(Platform.getBundle(PLUGIN_ID));
					log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, "Unexpected runtime error while computing a text hover", ex)); //$NON-NLS-1$
				} finally {
					synchronized (fMutex) {
						fThread = null;
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=44756
						if (!hasFinished)
							setInformation(null, null);
					}
				}
			}
		};

		fThread.setDaemon(true);
		fThread.setPriority(Thread.MIN_PRIORITY);
		synchronized (fMutex) {
			fThread.start();
		}
	}

	/**
	 * Get the details for this hover event.
	 *
	 * @return hover details or <code>null</code>
	 */
	protected Object getHoverDetails() {
		return null;
	}

	/**
	 * Get the origin of this hover event. In case <code>null</code> is returned the hover event is aborted.
	 *
	 * @return element that this hover was registered upon or <code>null</code>
	 */
	protected Object getHoverOrigin() {
		return fControl;
	}

	/**
	 * Calculate the area that is relevant for the hover event. While the mouse cursor remains in this area, the hover will not be closed. Further this area
	 * defines the bounds which should not get covered by the hover. The hover will be placed below, above or next to this area.
	 *
	 * @return area of interest
	 */
	protected Rectangle getHoverInterestArea() {
		return new Rectangle(0, 0, fControl.getBounds().width, fControl.getBounds().height);
	}

	/**
	 * As computation is done in the background, this method is also called in the background thread. Delegates the control flow back into the UI thread, in
	 * order to allow displaying the information in the information control.
	 */
	@Override
	protected void presentInformation() {
		if ((fControl != null) && !fControl.isDisposed()) {
			final Display display = fControl.getDisplay();
			if (display == null)
				return;

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					ControlHoverManager.super.presentInformation();
				}
			});
		}
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		super.hideInformationControl();
		return true;
	}

	@Override
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		if (priority > WIDGET_PRIORITY) {
			super.hideInformationControl();
			return true;
		}
		return false;
	}

	@Override
	public boolean setFocus(IWidgetTokenOwner owner) {
		if (!hasInformationControlReplacer())
			return false;

		final IInformationControl iControl = getCurrentInformationControl();
		if (canReplace(iControl)) {
			if (cancelReplacingDelay())
				replaceInformationControl(true);

			return true;
		}
		if (iControl instanceof IInformationControlExtension5) {
			return true; // The iControl didn't return an information presenter control creator, so let's stop here.
		}

		return false;
	}

	@Override
	public void dispose() {
		if ((fControl != null) && !fControl.isDisposed())
			fControl.removeMouseMoveListener(fMouseMoveListener);

		fMouseMoveListener = null;

		super.dispose();
	}

	@Override
	protected void showInformationControl(Rectangle subjectArea) {
		if ((fWidgetTokenOwner != null) && fWidgetTokenOwner.requestWidgetToken(this))
			super.showInformationControl(subjectArea);
		else if (DEBUG)
			System.out.println("TextViewerHoverManager#showInformationControl(..) did not get widget token"); //$NON-NLS-1$
	}

	@Override
	protected void hideInformationControl() {
		try {
			super.hideInformationControl();
		} finally {
			if (fWidgetTokenOwner != null)
				fWidgetTokenOwner.releaseWidgetToken(this);
		}
	}

	@Override
	void replaceInformationControl(boolean takeFocus) {
		if (fWidgetTokenOwner != null)
			fWidgetTokenOwner.releaseWidgetToken(this);

		super.replaceInformationControl(takeFocus);
	}

	@Override
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		} finally {
			if (fWidgetTokenOwner != null)
				fWidgetTokenOwner.releaseWidgetToken(this);
		}
	}
}
