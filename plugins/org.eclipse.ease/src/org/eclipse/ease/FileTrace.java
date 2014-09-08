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
package org.eclipse.ease;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * File trace containing called files and current line numbers. A file trace is quite similar to a stack trace. It just indicates currently executed
 * files down
 * to the current include level.
 */
public class FileTrace implements Iterable<FileTrace.Trace> {

	/**
	 * Trace object. Contains trace data for one file.
	 */
	public final class Trace {

		/** Line number currently executed. */
		private int fLineNumber = 0;

		/** File URL. */
		private String fUrl = null;

		/** Optionally execution content (for dynamically generated code). */
		private String fContent = "";

		/**
		 * Constructor.
		 * 
		 * @param url
		 *        file URL or <code>null</code>
		 * @param lineNumber
		 *        line number
		 * @param content
		 *        script content in case that URL is <code>null</code>
		 */
		private Trace(final String url, final int lineNumber, final String content) {
			fUrl = url;
			fLineNumber = lineNumber;
			fContent = content;
		}

		public Trace(final Object reference) {
			if(reference instanceof IFile)
				fUrl = ((IFile)reference).getFullPath().toPortableString();

			else if(reference instanceof File)
				fUrl = ((File)reference).getAbsolutePath();

			else if(reference != null)
				fContent = reference.toString();
		}

		/**
		 * Get the line number currently executed.
		 * 
		 * @return line number
		 */
		public int getLineNumber() {
			return fLineNumber;
		}

		/**
		 * Get the file currently executed.
		 * 
		 * @return executed file
		 */
		public Object getFile() {
			if(fUrl != null) {
				// first try to resolve the file in workspace
				try {
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fUrl));
					if(file.exists())
						return file;

				} catch (final Exception e) {
				}

				// didn't work, resolve from system
				File file = new File(fUrl);
				if(file.exists())
					return file;
			}

			return null;
		}

		public String getFileName() {
			// if (mUrl != null)
			return fUrl;

			// return "";
		}

		public void setLineNumber(final int lineNumber) {
			fLineNumber = lineNumber;
		}

		public String getContent() {
			return fContent;
		}
	}

	/** Trace stack. */
	private final List<FileTrace.Trace> fTrace = new ArrayList<FileTrace.Trace>();

	public final void push(final String url, final int lineNumber, final String content) {
		fTrace.add(0, new Trace(url, lineNumber, content));
	}

	@Override
	public final Iterator<FileTrace.Trace> iterator() {
		return fTrace.iterator();
	}

	/**
	 * Get the whole file trace.
	 * 
	 * @return trace stack
	 */
	public final List<Trace> getTrace() {
		return fTrace;
	}

	public void push(final Object reference) {
		if(reference instanceof Trace)
			fTrace.add(0, (Trace)reference);

		else
			fTrace.add(0, new Trace(reference));
	}

	public Trace pop() {
		if(!fTrace.isEmpty())
			return fTrace.remove(0);

		return null;
	}

	public Trace peek() {
		if(!fTrace.isEmpty())
			return fTrace.get(0);

		return null;
	}

	public List<Object> getFileStack() {
		List<Object> files = new ArrayList<Object>();

		for(Trace trace : getTrace()) {
			Object file = trace.getFile();
			if((file instanceof IFile) && (((IFile)file).exists()))
				files.add(file);

			else if((file instanceof File) && (((File)file).exists()))
				files.add(file);
		}

		return files;
	}

	public Object getTopMostFile() {
		List<Object> list = getFileStack();
		return (list.isEmpty()) ? null : list.get(0);
	}
}
