/*******************************************************************************
 * Copyright (c) 2013 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.bugzilla.rest.core;

import org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema;
import org.eclipse.mylyn.tasks.core.data.DefaultTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.common.collect.ImmutableMap;

public class BugzillaRestTaskSchema extends AbstractTaskSchema {

	private static final BugzillaRestTaskSchema instance = new BugzillaRestTaskSchema();

	private static ImmutableMap<String, String> field2AttributeFieldMapper = new ImmutableMap.Builder<String, String>()
			.put("summary", getDefault().SUMMARY.getKey()) //$NON-NLS-1$
			.put("description", getDefault().DESCRIPTION.getKey()) //$NON-NLS-1$
			.put("status", getDefault().STATUS.getKey()) //$NON-NLS-1$
			.put("product", getDefault().PRODUCT.getKey()) //$NON-NLS-1$
			.put("component", getDefault().COMPONENT.getKey()) //$NON-NLS-1$
			.put("CC", getDefault().CC.getKey()) //$NON-NLS-1$
			.put("severity", getDefault().SEVERITY.getKey()) //$NON-NLS-1$
			.put("priority", getDefault().PRIORITY.getKey()) //$NON-NLS-1$
			.put("assigned_to", getDefault().ASSIGNED_TO.getKey()) //$NON-NLS-1$
			.put("op_sys", getDefault().OS.getKey()) //$NON-NLS-1$
			.put("resolution", getDefault().RESOLUTION.getKey()) //$NON-NLS-1$
			.put("version", getDefault().VERSION.getKey()) //$NON-NLS-1$
			.put("dup_id", getDefault().DUPE_OF.getKey()) //$NON-NLS-1$
			.build();

	private static ImmutableMap<String, String> attribute2FieldMapper = new ImmutableMap.Builder<String, String>()
			.put(getDefault().SUMMARY.getKey(), "summary") //$NON-NLS-1$
			.put(getDefault().DESCRIPTION.getKey(), "description") //$NON-NLS-1$
			.put(getDefault().OPERATION.getKey(), "status") //$NON-NLS-1$
			.put(getDefault().PRODUCT.getKey(), "product") //$NON-NLS-1$
			.put(getDefault().COMPONENT.getKey(), "component") //$NON-NLS-1$
			.put(getDefault().CC.getKey(), "cc") //$NON-NLS-1$
			.put(getDefault().SEVERITY.getKey(), "severity") //$NON-NLS-1$
			.put(getDefault().PRIORITY.getKey(), "priority") //$NON-NLS-1$
			.put(getDefault().ASSIGNED_TO.getKey(), "assigned_to") //$NON-NLS-1$
			.put(getDefault().OS.getKey(), "op_sys") //$NON-NLS-1$
			.put(getDefault().VERSION.getKey(), "version") //$NON-NLS-1$
			.put(getDefault().RESOLUTION.getKey(), "resolution") //$NON-NLS-1$
			.put(getDefault().DUPE_OF.getKey(), "dup_id") //$NON-NLS-1$
			.put("resolutionInput", "resolution") //$NON-NLS-1$  //$NON-NLS-2$
			.build();

	public static String getAttributeNameFromFieldName(String fieldName) {
		String result = field2AttributeFieldMapper.get(fieldName);
		if (result == null) {
			result = fieldName;
		}
		return result;
	}

	public static String getFieldNameFromAttributeName(String attributeName) {
		String result = attribute2FieldMapper.get(attributeName);
		if (result == null) {
			result = attributeName;
		}
		return result;
	}

	public static BugzillaRestTaskSchema getDefault() {
		return instance;
	}

	private final DefaultTaskSchema parent = DefaultTaskSchema.getInstance();

	public final Field BUG_ID = createField("bug_id", "ID:", TaskAttribute.TYPE_SHORT_TEXT, Flag.REQUIRED);

	public final Field PRODUCT = inheritFrom(parent.PRODUCT).addFlags(Flag.REQUIRED).create();

	public final Field COMPONENT = inheritFrom(parent.COMPONENT).addFlags(Flag.REQUIRED)
			.dependsOn(PRODUCT.getKey())
			.create();

	public final Field SUMMARY = inheritFrom(parent.SUMMARY).addFlags(Flag.REQUIRED).create();

	public final Field VERSION = createField(TaskAttribute.VERSION, "Version", TaskAttribute.TYPE_SINGLE_SELECT, null,
			PRODUCT.getKey(), Flag.ATTRIBUTE, Flag.REQUIRED);

	public final Field DESCRIPTION = inheritFrom(parent.DESCRIPTION).addFlags(Flag.REQUIRED, Flag.READ_ONLY).create();

	public final Field OS = createField("os", "OS", TaskAttribute.TYPE_SINGLE_SELECT, Flag.ATTRIBUTE);

	public final Field PLATFORM = createField("platform", "Platform", TaskAttribute.TYPE_SINGLE_SELECT, Flag.ATTRIBUTE);

	public final Field PRIORITY = inheritFrom(parent.PRIORITY).create();

	public final Field SEVERITY = inheritFrom(parent.SEVERITY).create();

	public final Field STATUS = inheritFrom(parent.STATUS).create();

	public final Field ALIAS = createField("alias", "Alias", TaskAttribute.TYPE_SHORT_TEXT, Flag.ATTRIBUTE);

	public final Field ASSIGNED_TO = inheritFrom(parent.USER_ASSIGNED).label("Assigned to")
			.dependsOn(COMPONENT.getKey())
			.create();

	public final Field CC = createField(TaskAttribute.USER_CC, "CC", TaskAttribute.TYPE_PERSON, Flag.PEOPLE);

	public final Field ADD_SELF_CC = inheritFrom(parent.ADD_SELF_CC).addFlags(Flag.PEOPLE).create();

	public final Field COMMENT_ISPRIVATE = inheritFrom(parent.COMMENT_ISPRIVATE).addFlags(Flag.ATTRIBUTE).create();

	public final Field COMMENT_NUMBER = inheritFrom(parent.COMMENT_NUMBER).addFlags(Flag.ATTRIBUTE).create();

	public final Field QA_CONTACT = createField("qa_contact", "QA Contact", TaskAttribute.TYPE_PERSON, null,
			COMPONENT.getKey(), Flag.PEOPLE);

	public final Field TARGET_MILESTONE = createField("target_milestone", "Target milestone",
			TaskAttribute.TYPE_SINGLE_SELECT, null, PRODUCT.getKey(), Flag.ATTRIBUTE, Flag.REQUIRED);

	public final Field RESOLUTION = inheritFrom(parent.RESOLUTION).removeFlags(Flag.READ_ONLY).create();

	public final Field OPERATION = createField(TaskAttribute.OPERATION, "Operation", TaskAttribute.TYPE_OPERATION);

	public final Field NEW_COMMENT = inheritFrom(parent.NEW_COMMENT).create();

	public final Field DUPE_OF = createField("dupe_of", "Dup", TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID);

	@Override
	public void initialize(TaskData taskData) {
		for (Field field : getFields()) {
			if (field.equals(COMMENT_ISPRIVATE) || field.equals(COMMENT_NUMBER)) {
				continue;
			}
			TaskAttribute newField = field.createAttribute(taskData.getRoot());
			if (field.equals(DESCRIPTION)) {
				COMMENT_ISPRIVATE.createAttribute(newField);
				COMMENT_NUMBER.createAttribute(newField);
			}
		}
	}

}