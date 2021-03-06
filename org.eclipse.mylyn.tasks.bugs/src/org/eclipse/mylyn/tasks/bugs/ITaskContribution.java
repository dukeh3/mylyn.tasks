/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.bugs;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Steffen Pingel
 * @since 3.4
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITaskContribution {

	/**
	 * Appends <code>text</code> to the description of the task.
	 */
	public abstract void appendToDescription(String text);

	public abstract String getAttribute(String name);

	public abstract IProduct getProduct();

	public abstract IStatus getStatus();

	public abstract boolean isHandled();

	public abstract void setAttribute(String name, String value);

	public abstract void setHandled(boolean handled);

}
