/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.handler;

 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;

 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.ease.Logger;
 import org.eclipse.ease.service.ScriptType;
 import org.eclipse.ease.ui.ScriptEditorInput;
 import org.eclipse.ease.ui.repository.IScript;
 import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;

 public class EditScript extends AbstractHandler implements IHandler {

	 @Override
	 public Object execute(final ExecutionEvent event) throws ExecutionException {
		 ISelection selection = HandlerUtil.getCurrentSelection(event);
		 if (selection instanceof IStructuredSelection) {
			 for (final Object element : ((IStructuredSelection) selection).toList()) {
				 if (element instanceof IScript) {
					 final Object content = ((IScript) element).getResource();
					 if ((content instanceof IFile) && (((IFile) content).exists())) {
						 // open editor
						 IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						 try {
							 IDE.openEditor(page, (IFile) content);
						 } catch (PartInitException e) {
							 Logger.logError("Could not open editor for file " + content);
						 }

					 } else if ((content instanceof File) && (((File) content).exists())) {
						 ScriptType type = ((IScript) element).getType();
						 IEditorDescriptor descriptor = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("foo." + type.getDefaultExtension());
						 if (descriptor != null) {
							 IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							 try {
								 final ScriptEditorInput editorInput = new ScriptEditorInput((IScript) element);
								 final IEditorPart editor = page.openEditor(editorInput, descriptor.getId());

								 // editor will not save external script files, we need to do this on our own
								 editor.addPropertyListener(new IPropertyListener() {
									 @Override
									 public void propertyChanged(final Object source, final int propId) {

										 // check for changes of PROP_DIRTY from true to false,
										 // meaning the editor tried to save data
										 if (IEditorPart.PROP_DIRTY == propId) {
											 if ((editor instanceof AbstractDecoratedTextEditor) && (!editor.isDirty())) {
												 final IDocumentProvider documentProvider = ((AbstractTextEditor) editor).getDocumentProvider();
												 final String newSource = documentProvider.getDocument(editorInput).get();

												 FileOutputStream outputStream = null;
												 try {
													 outputStream = new FileOutputStream((File) content);
													 outputStream.write(newSource.getBytes());

												 } catch (Exception e) {
													 Logger.logError("Could not store recorded script.", e);
												 } finally {
													 if (outputStream != null) {
														 try {
															 outputStream.close();
														 } catch (IOException e) {
															 // giving up
														 }
													 }
												 }

												 // refresh script in repository
												 final IRepositoryService repositoryService = (IRepositoryService) PlatformUI.getWorkbench().getService(
														 IRepositoryService.class);
												 repositoryService.update(false);
											 }
										 }
									 }
								 });

							 } catch (PartInitException e) {
								 Logger.logError("Could not open editor for file " + content);
							 }
						 }
					 }
				 }
			 }
		 }

		 return null;
	 }
 }
