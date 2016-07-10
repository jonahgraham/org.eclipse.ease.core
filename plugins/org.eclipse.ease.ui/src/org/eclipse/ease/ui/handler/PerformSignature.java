/*******************************************************************************
 * Copyright (c) 2016 Varun Raval and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Varun Raval - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.handler;

import java.io.ByteArrayInputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.sign.ScriptSignatureException;
import org.eclipse.ease.sign.SignatureHelper;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.sign.GetInfo;
import org.eclipse.ease.ui.sign.PerformSignWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class PerformSignature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IFile iFile = null;
		final ISelection iSelection = HandlerUtil.getCurrentSelection(event);

		// to check selection from explorers
		if (iSelection instanceof IStructuredSelection) {
			final IStructuredSelection iStructuredSelection = (IStructuredSelection) iSelection;
			final Object firstElement = iStructuredSelection.getFirstElement();
			if (firstElement instanceof IFile) {
				iFile = (IFile) firstElement;
			}
		} else {
			Logger.info(Activator.PLUGIN_ID, "Editor Selection");
			final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
			if (activeEditor != null)
				iFile = activeEditor.getEditorInput().getAdapter(IFile.class);
		}

		if (iFile != null) {
			final ScriptType scriptType = PlatformUI.getWorkbench().getService(IScriptService.class).getScriptType(iFile.getFullPath().toString());
			if (scriptType == null) {
				new MessageDialog(HandlerUtil.getActiveShell(event), "Error", null, "Improper file chosen.", MessageDialog.ERROR, new String[] { "DONE" }, 0)
						.open();
				return null;
			}

			// getInfo is used to get signature related information from wizard
			// wizard will be storing signature information in this getInfo instance
			final GetInfo getInfo = new GetInfo();
			final WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), new PerformSignWizard(iFile, scriptType, getInfo));
			if (wizardDialog.open() == Window.OK) {
				// perform file operations here
				// signature info is obtained in GetInfo instance: getInfo
				try {
					final String signatureBlock = SignatureHelper.getSignatureInFormat(scriptType, getInfo.getSignature(), getInfo.getCertificates(),
							getInfo.getSignMessageDigestAlgo(), getInfo.getSignProvider());

					// remove signature if already present
					if (getInfo.getSignaturePresence())
						iFile.setContents(new ByteArrayInputStream(getInfo.getContentOnly().getBytes()), true, true, null);

					// appending signature to file
					final StringBuffer signToAppend = new StringBuffer();
					signToAppend.append(AbstractCodeFactory.LINE_DELIMITER);
					signToAppend.append(scriptType.getCodeFactory().createCommentedString(signatureBlock, true));
					signToAppend.append(AbstractCodeFactory.LINE_DELIMITER);
					iFile.appendContents(new ByteArrayInputStream(signToAppend.toString().getBytes()), false, true, null);

				} catch (final ScriptSignatureException e) {
					showError(e.getMessage(), event);
					return null;

				} catch (final CoreException e) {
					Logger.error(Activator.PLUGIN_ID, e.getMessage(), e);
					showError(e.getMessage(), event);
					return null;

				}

				new MessageDialog(HandlerUtil.getActiveShell(event), "Signature Performed", null,
						"Sigature performed successfully. \n Do \"Perform Signature...\" again to update it.", MessageDialog.CONFIRM, new String[] { "DONE" },
						0).open();
			}

		} else {
			Logger.error(Activator.PLUGIN_ID, "IFile object is null");
			showError("Cannot get corresponding file.", event);

		}
		return null;
	}

	void showError(String msg, ExecutionEvent event) {
		new MessageDialog(HandlerUtil.getActiveShell(event), "Error", null, msg, MessageDialog.ERROR, new String[] { "DONE" }, 0).open();
	}
}
