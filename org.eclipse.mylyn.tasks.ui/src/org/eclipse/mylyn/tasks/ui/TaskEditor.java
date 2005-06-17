/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on 19-Jan-2005
 */
package org.eclipse.mylar.tasks.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylar.tasks.ITask;
import org.eclipse.mylar.tasks.MylarTasksPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;


/**
 * @author ebooth
 */
public class TaskEditor extends MultiPageEditorPart {

	protected ITask task;
	private TaskSummaryEditor taskSummaryEditor;
	private TaskEditorInput taskEditorInput;
	
	public TaskEditor() {
		super();

		// get the workbench page and add a listener so we can detect when it closes
		IWorkbench wb = MylarTasksPlugin.getDefault().getWorkbench();
		IWorkbenchWindow aw = wb.getActiveWorkbenchWindow();
		IWorkbenchPage ap = aw.getActivePage();
		TaskEditorListener listener = new TaskEditorListener();
		ap.addPartListener(listener);
		
		taskSummaryEditor = new TaskSummaryEditor();
	}

	/**
	 * Creates page 1 of the multi-page editor,
	 * which displays the task for viewing.
	 */
	private void createTaskSummaryPage() {
		taskSummaryEditor.createPartControl(getContainer());
		Composite composite = taskSummaryEditor.getEditorComposite();
		int index = addPage(composite);
		setPageText(index, "Summary");
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void createPages() {
		createTaskSummaryPage();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		if (!(input instanceof TaskEditorInput))
			throw new PartInitException("Invalid Input: Must be TaskEditorInput");
		taskEditorInput = (TaskEditorInput)input;
		super.init(site, input);

		/*
		 * The task data is saved only once, at the initialization of the editor.  This is
		 * then passed to each of the child editors.  This way, only one instance of 
		 * the task data is stored for each editor opened.
		*/
		task = taskEditorInput.getTask();		
		try {
			taskSummaryEditor.init(this.getEditorSite(), this.getEditorInput());
			taskSummaryEditor.setTask(task);
			// Set the title on the editor's tab
			this.setPartName(taskEditorInput.getLabel());
		} catch (Exception e) {
			throw new PartInitException(e.getMessage());
		}
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Class to listen for editor events
	 */
	private class TaskEditorListener implements IPartListener
	{

		/**
		 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			// don't care about this event
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart part) {
			// don't care about this event
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
			// don't care about this event
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
			// don't care about this event
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part) {
			// don't care about this event
		}
	}
}
