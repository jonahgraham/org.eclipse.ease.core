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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Scriptable object. Consists of scriptable data and a result container.
 */
public class Script {

	/** command to be executed. */
	private final Object fCommand;

	/** script result returned from command. */
	private final ScriptResult fResult;

	/** Internal buffer when delivering code data from streams. */
	private String fCodeBuffer = null;

	/** Script title (optional). */
	private final String fTitle;

	/**
	 * Constructor.
	 *
	 * @param title
	 *            name of script object
	 * @param command
	 *            command (sequence) to be executed
	 */
	public Script(final String title, final Object command) {
		fTitle = title;
		fCommand = command;
		fResult = new ScriptResult();
	}

	/**
	 * Constructor. Using no title for this script
	 *
	 * @param command
	 *            command (sequence) to be executed
	 */
	public Script(final Object command) {
		this(null, command);
	}

	/**
	 * Get the scriptable data as {@link InputStream}. The caller needs to close the stream when it is not used anymore. Calling this method multiple times will
	 * return different streams with the same text content.
	 *
	 * @return scriptable data
	 * @throws Exception
	 *             when stream cannot be established
	 */
	public InputStream getCodeStream() throws Exception {
		return new ByteArrayInputStream(getCode().getBytes());
	}

	/**
	 * Get the scriptable data as {@link String}.
	 *
	 * @return scriptable data
	 * @throws Exception
	 *             when code cannot be read from source
	 */
	public String getCode() throws Exception {
		if (fCodeBuffer != null)
			return fCodeBuffer;

		if (fCommand instanceof String)
			return (String) fCommand;

		if (fCommand instanceof InputStream)
			// streams can only be read once, therefore buffer
			return bufferStream((InputStream) fCommand);

		if (fCommand instanceof Reader)
			// readers can only be read once, therefore buffer
			return bufferReader((Reader) fCommand);

		// if we already have a scriptable
		if (fCommand instanceof IScriptable)
			return bufferStream(((IScriptable) fCommand).getSourceCode());

		// try to adapt to scriptable
		final Object scriptable = Platform.getAdapterManager().getAdapter(fCommand, IScriptable.class);
		if (scriptable != null)
			return bufferStream(((IScriptable) scriptable).getSourceCode());

		// last resort, convert to String
		if (fCommand != null) {
			// better buffer stuff, we do not know if toString() remains constant
			fCodeBuffer = fCommand.toString();
			return fCodeBuffer;
		}

		return null;
	}

	private String bufferReader(final Reader command) throws IOException {
		fCodeBuffer = ResourceTools.toString(command);
		return fCodeBuffer;
	}

	private String bufferStream(final InputStream command) throws IOException {
		fCodeBuffer = ResourceTools.toString(command);
		return fCodeBuffer;
	}

	/**
	 * Returns the command object of this script.
	 *
	 * @return command object
	 */
	public final Object getCommand() {
		return fCommand;
	}

	/**
	 * Get execution result.
	 *
	 * @return execution result.
	 */
	public final ScriptResult getResult() {
		return fResult;
	}

	/**
	 * Set the execution result.
	 *
	 * @param result
	 *            execution result
	 */
	public final void setResult(final Object result) {
		fResult.setResult(result);

		// gracefully close input streams & readers
		closeInput();
	}

	/**
	 * Set an execution exception.
	 *
	 * @param e
	 *            exception
	 */
	public final void setException(final Throwable e) {
		fResult.setException(e);

		// gracefully close input streams & readers
		closeInput();
	}

	private void closeInput() {
		if (fCommand instanceof InputStream) {
			try {
				((InputStream) fCommand).close();
			} catch (final IOException ex) {
			}

		} else if (fCommand instanceof Reader) {
			try {
				((Reader) fCommand).close();
			} catch (final IOException ex) {
			}
		}
	}

	/**
	 * Returns the file instance, if the current command is backed by a file.
	 *
	 * @return {@link IFile}, {@link File} or <code>null</code>
	 */
	public Object getFile() {
		if ((fCommand instanceof IFile) || (fCommand instanceof File))
			return fCommand;

		return null;
	}

	/**
	 * Check if this script is defined by dynamically generated code. Generated code might be hidden while debugging.
	 *
	 * @return <code>true</code> when not a file and not an {@link URL}
	 */
	public boolean isDynamic() {
		return !((fCommand instanceof URL) || (getFile() != null));
	}

	@Override
	public String toString() {
		return (getTitle() != null) ? getTitle() : "(unknown script source)";
	}

	/**
	 * Get the title of this script. Title has to be set by the caller via the constructor. Typically this is used for dynamic code to indicate its purpose. If
	 * no title is set we try to extract the name of the executed resource.
	 *
	 * @return script title or <code>null</code>
	 */
	public String getTitle() {
		if (fTitle != null)
			return fTitle;

		if (fCommand instanceof IFile)
			return ((IFile) fCommand).getName();

		if (fCommand instanceof File)
			return ((File) fCommand).getName();

		if (fCommand instanceof URI)
			return fCommand.toString();

		if (fCommand instanceof URL)
			return fCommand.toString();

		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((fCommand == null) ? 0 : fCommand.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Script other = (Script) obj;
		if (fCommand == null) {
			if (other.fCommand != null)
				return false;
		} else if (!fCommand.equals(other.fCommand))
			return false;
		return true;
	}
}
