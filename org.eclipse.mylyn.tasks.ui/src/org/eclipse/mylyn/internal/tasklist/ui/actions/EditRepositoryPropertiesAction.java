/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.tasklist.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.ui.views.TaskRepositoriesView;
import org.eclipse.mylar.internal.tasklist.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class EditRepositoryPropertiesAction extends Action {

	private static final String ID = "org.eclipse.mylar.tasklist.repositories.properties";

	private TaskRepositoriesView repositoriesView;

	public EditRepositoryPropertiesAction(TaskRepositoriesView repositoriesView) {
		this.repositoriesView = repositoriesView;
		setText("Properties");
		setId(ID);
	}

	public void init(IViewPart view) {
		// ignore
	}

	public void run() {
		try {
			IStructuredSelection selection = (IStructuredSelection) repositoriesView.getViewer().getSelection();
			if (selection.getFirstElement() instanceof TaskRepository) {
				EditRepositoryWizard wizard = new EditRepositoryWizard((TaskRepository) selection.getFirstElement());
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				if (wizard != null && shell != null && !shell.isDisposed()) {
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.create();
					// dialog.getShell().setText("Mylar Tasks");
					dialog.setBlockOnOpen(true);
					if (dialog.open() == Dialog.CANCEL) {
						dialog.close();
						return;
					}
				}
			}
			// TODO: move
			if (TaskRepositoriesView.getFromActivePerspective() != null) {
				TaskRepositoriesView.getFromActivePerspective().getViewer().refresh();
			}
		} catch (Exception e) {
			MylarStatusHandler.fail(e, e.getMessage(), true);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}
}
