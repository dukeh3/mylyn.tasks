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

package org.eclipse.mylar.tasks.core;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public interface IOfflineTaskHandler {
	
	/**
	 * @return null if date cannot be parsed
	 */
	public Date getDateForAttributeType(String attributeKey, String dateString);
	
	public AbstractAttributeFactory getAttributeFactory();
	
	// TODO: remove proxy parameter
	public RepositoryTaskData downloadTaskData(TaskRepository repository, String taskId) throws CoreException;
		
	/**
	 * returns all tasks if date is null or an error occurs
	 */
	public abstract Set<AbstractRepositoryTask> getChangedSinceLastSync(TaskRepository repository,
			Set<AbstractRepositoryTask> tasks) throws CoreException, UnsupportedEncodingException;
 }
