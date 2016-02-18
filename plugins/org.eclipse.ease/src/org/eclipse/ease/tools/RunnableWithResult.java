/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.tools;

public abstract class RunnableWithResult<T extends Object> implements Runnable {

	private T fResult = null;
	private Throwable fThrowable = null;

	/**
	 * Set the result of the runnable.
	 *
	 * @param result
	 *            runnable result
	 */
	protected void setResult(final T result) {
		fResult = result;
	}

	/**
	 * Get the result of the run execution. Does not consider eventually thrown exceptions.
	 *
	 * @return runnable result
	 */
	public T getResult() {
		return fResult;
	}

	@Override
	public void run() {
		try {
			runWithTry();
		} catch (Throwable e) {
			fThrowable = e;
		}
	}

	/**
	 * Get the result of the run execution. Does rethrow exceptions that occurred during the run.
	 *
	 * @return runnable result
	 * @throws Throwable
	 *             exceptions encountered during run
	 */
	public T getResultFromTry() throws Throwable {
		if (fThrowable != null)
			throw fThrowable;

		return fResult;
	}

	/**
	 * Run method to be implemented by the derived class. Exceptions thrown will automatically get caught an rethrown on a {@link #getResult()}.
	 */
	public void runWithTry() throws Throwable {
	}
}
