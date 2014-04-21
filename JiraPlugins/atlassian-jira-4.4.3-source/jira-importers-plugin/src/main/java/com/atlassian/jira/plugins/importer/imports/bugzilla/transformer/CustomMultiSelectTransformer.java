/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomMultiSelectTransformer implements ResultSetTransformer<String> {
	private final String oldId;
	private final BugzillaConfigBean configBean;
	private final String customFieldId;

	public CustomMultiSelectTransformer(String oldId, BugzillaConfigBean configBean, String customFieldId) {
		this.oldId = oldId;
		this.configBean = configBean;
		this.customFieldId = customFieldId;
	}

	public String getSqlQuery() {
		return "SELECT value FROM bug_" + customFieldId + " WHERE bug_id=" + oldId;
	}

	public String transform(ResultSet rs) throws SQLException {
		return configBean.getValueMappingHelper().getValueMappingForImport(customFieldId, rs.getString(1));
	}
}
