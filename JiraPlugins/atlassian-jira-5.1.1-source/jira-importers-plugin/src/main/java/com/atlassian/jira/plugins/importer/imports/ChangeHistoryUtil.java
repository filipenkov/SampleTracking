/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.history.ChangeItemBean;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ChangeHistoryUtil {
	/**
	 * Writes the given field changes to the db and optionally a changelog.
	 *
	 * @param before			  The issue before the update.
	 * @param incomingChangeItems Some {@link com.atlassian.jira.issue.history.ChangeItemBean}.
	 * @param changeAuthor		the User making the change.
	 * @return the ChangeGroup GenericValue
	 */
	@Nullable
	public static GenericValue createChangeGroup(User changeAuthor, GenericValue before, Collection<ChangeItemBean> incomingChangeItems, Timestamp changeTime)
			throws GenericEntityException {
		if (incomingChangeItems == null || incomingChangeItems.size() == 0)
			return null;

		final ArrayList<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>(incomingChangeItems);

		GenericValue changeGroup = EntityUtils.createValue("ChangeGroup",
				EasyMap.build("issue", before.getLong("id"), "author",
						(changeAuthor != null ? changeAuthor.getName() : null), "created", changeTime));

		for (ChangeItemBean cib : changeItems) {
			Map<String, String> fields = EasyMap.build("group", changeGroup.getLong("id"));
			fields.put("fieldtype", cib.getFieldType());
			fields.put("field", cib.getField());
			fields.put("oldvalue", cib.getFrom());
			fields.put("oldstring", cib.getFromString());
			fields.put("newvalue", cib.getTo());
			fields.put("newstring", cib.getToString());
			EntityUtils.createValue("ChangeItem", fields);
		}

		return changeGroup;
	}
}
