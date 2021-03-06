/*******************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.connector;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Benjamin Muskalla
 */
public class AssertionProgressMonitor extends NullProgressMonitor {
	private final StringBuilder progressLog = new StringBuilder();

	@Override
	public void beginTask(String name, int totalWork) {
		progressLog.append("beginTask|");
	}

	@Override
	public void done() {
		progressLog.append("done");
	}

	@Override
	public void subTask(String name) {
		progressLog.append("subTask|");
	}

	public String getProgressLog() {
		return progressLog.toString();
	}
}