/*******************************************************************************
 * Copyright (c) 2016 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.bugzilla.rest.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.internal.bugzilla.rest.ui.messages"; //$NON-NLS-1$

	public static String BugzillaRestRepositorySettingsPage_RestRepositorySetting;

	public static String BugzillaRestRepositorySettingsPage_SupportsVersionMessage;

	public static String BugzillaRestSearchQueryPage_PropertiesForNewQuery;

	public static String BugzillaRestSearchQueryPage_PropertiesForQuery;

	public static String BugzillaRestTaskEditorPageFactory_Bugzilla;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
