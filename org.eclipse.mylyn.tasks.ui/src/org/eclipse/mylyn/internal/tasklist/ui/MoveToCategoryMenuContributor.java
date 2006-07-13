/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.tasklist.ui;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractTaskContainer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskListElement;

/**
 * @author Mik Kersten
 */
public class MoveToCategoryMenuContributor implements IDynamicSubMenuContributor {

	private static final String LABEL = "Move to Category";

	public MenuManager getSubMenuManager(final List<ITaskListElement> selectedElements) {
		final MenuManager subMenuManager = new MenuManager(LABEL);

		for (final AbstractTaskContainer category : MylarTaskListPlugin.getTaskListManager().getTaskList()
				.getCategories()) {
			if (!category.equals(MylarTaskListPlugin.getTaskListManager().getTaskList().getArchiveContainer())) {
				Action action = new Action() {
					@Override
					public void run() {
						for (ITaskListElement element : selectedElements) {
							if (element instanceof ITask) {
								MylarTaskListPlugin.getTaskListManager().getTaskList().moveToContainer(category,
										(ITask) element);
							} else if (element instanceof AbstractQueryHit) {
								ITask task = ((AbstractQueryHit) element).getCorrespondingTask();
								if (task != null) {
									MylarTaskListPlugin.getTaskListManager().getTaskList().moveToContainer(category,
											task);
								}
							}
						}
					}
				};
				action.setText(category.getDescription());
				action.setImageDescriptor(TaskListImages.CATEGORY);
				if (selectedElements.size() == 1 && selectedElements.get(0) instanceof AbstractQueryHit) {
					AbstractQueryHit hit = (AbstractQueryHit) selectedElements.get(0);
					if (hit.getCorrespondingTask() == null) {
						action.setEnabled(false);
					}
				}
				subMenuManager.add(action);
			}
		}
		return subMenuManager;
	}

}
