/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.core.sync;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskHistory;
import org.eclipse.mylyn.tasks.core.sync.TaskJob;

/**
 * @author Steffen Pingel
 */
public class GetTaskHistoryJob extends TaskJob {

	private final TaskRepository repository;

	private final ITask task;

	private final AbstractRepositoryConnector connector;

	private IStatus errorStatus;

	private TaskHistory history;

	public GetTaskHistoryJob(AbstractRepositoryConnector connector, TaskRepository repository, ITask task) {
		super("Retrieving History");
		this.connector = connector;
		this.repository = repository;
		this.task = task;
	}

	@Override
	protected IStatus run(IProgressMonitor jobMonitor) {
		try {
			monitor.setCanceled(false);
			monitor.attach(jobMonitor);
			try {
				history = connector.getTaskHistory(repository, task, monitor);
			} catch (CoreException e) {
				errorStatus = e.getStatus();
			} catch (OperationCanceledException e) {
				errorStatus = Status.CANCEL_STATUS;
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, ITasksCoreConstants.ID_PLUGIN,
						"Unexpected error while retrieving task history", e)); //$NON-NLS-1$
				errorStatus = new Status(IStatus.ERROR, ITasksCoreConstants.ID_PLUGIN, "Unexpected error: " //$NON-NLS-1$
						+ e.getMessage(), e);
			} finally {
				monitor.done();
			}
			return (errorStatus == Status.CANCEL_STATUS) ? Status.CANCEL_STATUS : Status.OK_STATUS;
		} finally {
			monitor.detach(jobMonitor);
		}
	}

	public TaskHistory getHistory() {
		return history;
	}

	@Override
	public IStatus getStatus() {
		return errorStatus;
	}

	public ITask getTask() {
		return task;
	}

}