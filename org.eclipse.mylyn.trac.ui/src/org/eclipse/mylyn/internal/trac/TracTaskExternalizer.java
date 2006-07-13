/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.trac;

import org.eclipse.mylar.context.core.MylarStatusHandler;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
import org.eclipse.mylar.tasks.core.AbstractTaskContainer;
import org.eclipse.mylar.tasks.core.DelegatingTaskExternalizer;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.ITaskListExternalizer;
import org.eclipse.mylar.tasks.core.TaskExternalizationException;
import org.eclipse.mylar.tasks.core.TaskList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Steffen Pingel
 */
public class TracTaskExternalizer extends DelegatingTaskExternalizer {

	private static final String KEY_TRAC = "Trac";

	private static final String KEY_TRAC_CATEGORY = KEY_TRAC + KEY_CATEGORY;

	private static final String KEY_TRAC_TASK = KEY_TRAC + KEY_TASK;

	private static final String KEY_TRAC_QUERY_HIT = KEY_TRAC + KEY_QUERY_HIT;

	private static final String KEY_TRAC_QUERY = KEY_TRAC + KEY_QUERY;

	// category related methods
	
	@Override
	public boolean canReadCategory(Node node) {
		return node.getNodeName().equals(KEY_TRAC_CATEGORY);
	}

	@Override
	public String getCategoryTagName() {
		return KEY_TRAC_CATEGORY;
	}

	// task related methods
	
	@Override
	public boolean canCreateElementFor(ITask task) {
		return task instanceof TracTask;
	}

	@Override
	public String getTaskTagName() {
		return KEY_TRAC_TASK;
	}

	@Override
	public ITask readTask(Node node, TaskList taskList, AbstractTaskContainer category, ITask parent)
			throws TaskExternalizationException {

		Element element = (Element) node;
		String handle;
		String label;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);
		} else {
			throw new TaskExternalizationException("Handle not stored for task");
		}
		if (element.hasAttribute(KEY_LABEL)) {
			label = element.getAttribute(KEY_LABEL);
		} else {
			throw new TaskExternalizationException("Description not stored for task");
		}

		TracTask task = new TracTask(handle, label, false);
		readTaskInfo(task, taskList, element, parent, category);
		return task;
	}

	// query related methods
	
	@Override
	public boolean canCreateElementFor(AbstractQueryHit queryHit) {
		return queryHit instanceof TracQueryHit;
	}

	@Override
	public boolean canCreateElementFor(AbstractRepositoryQuery category) {
		return category instanceof TracRepositoryQuery;
	}

	@Override
	public boolean canReadQuery(Node node) {
		return node.getNodeName().equals(KEY_TRAC_QUERY);
	}

	@Override
	public boolean canReadQueryHit(Node node) {
		return node.getNodeName().equals(KEY_TRAC_QUERY_HIT);
	}

	@Override
	public Element createQueryElement(AbstractRepositoryQuery query, Document doc, Element parent) {
		String queryTagName = getQueryTagNameForElement(query);
		Element node = doc.createElement(queryTagName);

		node.setAttribute(KEY_LABEL, query.getDescription());
		node.setAttribute(KEY_REPOSITORY_URL, query.getRepositoryUrl());
		node.setAttribute(KEY_QUERY, query.getQueryUrl());

		for (AbstractQueryHit hit : query.getHits()) {
			try {
				Element element = null;
				for (ITaskListExternalizer externalizer : super.getDelegateExternalizers()) {
					if (externalizer.canCreateElementFor(hit))
						element = externalizer.createQueryHitElement(hit, doc, node);
				}
				if (element == null)
					createQueryHitElement(hit, doc, node);
			} catch (Exception e) {
				MylarStatusHandler.log(e, e.getMessage());
			}
		}
		parent.appendChild(node);
		return node;
	}

	@Override
	public Element createQueryHitElement(AbstractQueryHit queryHit, Document doc, Element parent) {
		Element node = doc.createElement(getQueryHitTagName());

		node.setAttribute(KEY_HANDLE, queryHit.getHandleIdentifier());
		node.setAttribute(KEY_PRIORITY, queryHit.getPriority());

		parent.appendChild(node);
		return null;
	}


	@Override
	public String getQueryHitTagName() {
		return KEY_TRAC_QUERY_HIT;
	}

	@Override
	public String getQueryTagNameForElement(AbstractRepositoryQuery query) {
		if (query instanceof TracRepositoryQuery) {
			return KEY_TRAC_QUERY;
		}
		return "";
	}

	@Override
	public AbstractRepositoryQuery readQuery(Node node, TaskList taskList) throws TaskExternalizationException {
		Element element = (Element) node;
		String repositoryUrl;
		String queryUrl;
		String label;
		if (element.hasAttribute(KEY_REPOSITORY_URL)) {
			repositoryUrl = element.getAttribute(KEY_REPOSITORY_URL);
		} else {
			throw new TaskExternalizationException("Repository URL not stored for task");
		}
		if (element.hasAttribute(KEY_QUERY)) {
			queryUrl = element.getAttribute(KEY_QUERY);
		} else {
			throw new TaskExternalizationException("Query URL not stored for task");
		}
		if (element.hasAttribute(KEY_LABEL)) {
			label = element.getAttribute(KEY_LABEL);
		} else {
			throw new TaskExternalizationException("Description not stored for task");
		}

		TracRepositoryQuery query = new TracRepositoryQuery(repositoryUrl, queryUrl, label, MylarTaskListPlugin
				.getTaskListManager().getTaskList());

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node child = list.item(i);
			try {
				readQueryHit(child, taskList, query);
			} catch (TaskExternalizationException e) {
				MylarStatusHandler.log(e, e.getMessage());
			}
		}
		return query;
	}

	@Override
	public void readQueryHit(Node node, TaskList taskList, AbstractRepositoryQuery query)
			throws TaskExternalizationException {
		Element element = (Element) node;

		String handle;
		if (element.hasAttribute(KEY_HANDLE)) {
			handle = element.getAttribute(KEY_HANDLE);
		} else {
			throw new TaskExternalizationException("Handle not stored for bug report");
		}

		ITask correspondingTask = taskList.getTask(handle);
		if (correspondingTask instanceof TracTask) {
			String id = AbstractRepositoryTask.getTaskId(handle);
			TracQueryHit hit = new TracQueryHit((TracTask) correspondingTask, query.getRepositoryUrl(), id);
			hit.setHandleIdentifier(handle);
			query.addHit(hit, taskList);
		}
	}

}
