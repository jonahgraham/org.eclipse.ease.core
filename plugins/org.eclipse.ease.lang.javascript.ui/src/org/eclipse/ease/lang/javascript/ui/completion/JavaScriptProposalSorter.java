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

package org.eclipse.ease.lang.javascript.ui.completion;

import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.AbstractProposalSorter;

public class JavaScriptProposalSorter extends AbstractProposalSorter {

	@Override
	public int compare(final ICompletionProposal proposal1, final ICompletionProposal proposal2) {
		if ((proposal1 instanceof ScriptCompletionProposal) && (proposal1 instanceof ScriptCompletionProposal))
			return ((ScriptCompletionProposal) proposal1).compareTo((ScriptCompletionProposal) proposal2);

		return proposal1.getDisplayString().compareToIgnoreCase(proposal2.getDisplayString());
	}
}
