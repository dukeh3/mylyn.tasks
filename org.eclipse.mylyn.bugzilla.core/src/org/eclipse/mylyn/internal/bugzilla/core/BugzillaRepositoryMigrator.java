/*******************************************************************************
 * Copyright (c) 2004, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.bugzilla.core;

import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Robert Elves
 */
public class BugzillaRepositoryMigrator extends AbstractRepositoryMigrator {

	public static final String REPOSITORY_PROPERTY_AVATAR_SUPPORT = "avatarSupport"; //$NON-NLS-1$

	@Override
	public String getConnectorKind() {
		return BugzillaCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public boolean migrateRepository(TaskRepository repository) {
		boolean migrated = false;
		if (repository.getCategory() == null) {
			repository.setCategory(TaskRepository.CATEGORY_BUGS);
			migrated = true;
		}
		// FIXME the Eclipse.org Bugzilla URL should not be hard coded here
		if (repository.getProperty(REPOSITORY_PROPERTY_AVATAR_SUPPORT) == null
				&& "https://bugs.eclipse.org/bugs".equals(repository.getRepositoryUrl())) { //$NON-NLS-1$
			repository.setProperty(REPOSITORY_PROPERTY_AVATAR_SUPPORT, Boolean.TRUE.toString());
			migrated = true;
		}
		return migrated;
	}

}
