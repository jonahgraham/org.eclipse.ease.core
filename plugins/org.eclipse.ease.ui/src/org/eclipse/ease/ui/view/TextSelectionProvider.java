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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

public class TextSelectionProvider implements ISelectionProvider, SelectionListener {

	private class TextSelection implements ITextSelection {

		private final String fText;

		public TextSelection(String text) {
			fText = text;
		}

		@Override
		public boolean isEmpty() {
			return fText.isEmpty();
		}

		@Override
		public int getOffset() {
			return -1;
		}

		@Override
		public int getLength() {
			return fText.length();
		}

		@Override
		public int getStartLine() {
			return -1;
		}

		@Override
		public int getEndLine() {
			return -1;
		}

		@Override
		public String getText() {
			return fText;
		}
	}

	private final ListenerList fListeners = new ListenerList();
	private ITextSelection fSelection = null;

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return fSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof ITextSelection) {
			fSelection = (ITextSelection) selection;

			if (!fListeners.isEmpty()) {
				// send event to listeners
				final SelectionChangedEvent event = new SelectionChangedEvent(this, fSelection);
				for (final Object listener : fListeners.getListeners())
					((ISelectionChangedListener) listener).selectionChanged(event);
			}
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		final Object source = e.getSource();
		if (source instanceof Text)
			setSelection(new TextSelection(((Text) source).getSelectionText()));

		else if (source instanceof StyledText)
			setSelection(new TextSelection(((StyledText) source).getSelectionText()));
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}
