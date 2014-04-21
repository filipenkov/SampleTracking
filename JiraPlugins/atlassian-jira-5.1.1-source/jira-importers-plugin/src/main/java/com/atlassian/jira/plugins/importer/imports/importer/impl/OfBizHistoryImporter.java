/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugins.importer.external.beans.ExternalHistoryGroup;
import com.atlassian.jira.plugins.importer.external.beans.ExternalHistoryItem;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class OfBizHistoryImporter {
	private final OfBizDelegator delegator;

	@Autowired
	public OfBizHistoryImporter(OfBizDelegator delegator) {
		this.delegator = delegator;
	}

	/**
	 * Inspired by {@link com.atlassian.jira.issue.history.ChangeLogUtils#createChangeGroup(com.atlassian.crowd.embedded.api.User, org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, java.util.Collection, boolean)} )}
	 *
	 * @param issueId Issue ID for which the history is generated
	 * @param history the change history
	 */
	public void importHistory(Long issueId, List<ExternalHistoryGroup> history) {
		for (ExternalHistoryGroup group : history) {
			final GenericValue changeGroup = delegator.createValue("ChangeGroup", ImmutableMap.<String, Object>of(
					"issue", issueId,
					"author", group.getAuthor(), // hmm
					"created", (group.getCreated() != null ? new Timestamp(group.getCreated().getMillis()) : null)));
			for (ExternalHistoryItem changeItem : group.getItems()) {
				final ImmutableMap<String, Object> fields = ImmutableMap.<String, Object>builder()
						.put("group", changeGroup.getLong("id"))
						.put("fieldtype", changeItem.getFieldType())
						.put("field", changeItem.getField())
						.put("oldvalue", changeItem.getOldValue())
						.put("oldstring", changeItem.getOldDisplayValue())
						.put("newvalue", changeItem.getNewValue())
						.put("newstring", changeItem.getNewDisplayValue())
						.build();
				delegator.createValue("ChangeItem", fields);
			}
		}
	}
}
