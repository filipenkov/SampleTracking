/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorklogTransformer implements ResultSetTransformer<ExternalWorklog> {
	private final String ixBug;
	private final BugzillaConfigBean configBean;

	public WorklogTransformer(final String ixBug, final BugzillaConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT who, added, bug_when, login_name FROM bugs_activity AS ba, profiles AS p"
				+ " WHERE ba.who = p.userid AND bug_id = " + ixBug
				+ " AND fieldid = (SELECT " + configBean.getFielddefsIdColumn()
				+ " FROM fielddefs WHERE name = 'work_time' LIMIT 1) ORDER BY bug_when ASC";
	}

	public ExternalWorklog transform(ResultSet rs) throws SQLException {
		final Float added = rs.getFloat("added");
		ExternalWorklog worklog = new ExternalWorklog();
		worklog.setAuthor(configBean.getUsernameForLoginName(rs.getString("login_name")));
		worklog.setStartDate(rs.getTimestamp("bug_when"));
		worklog.setComment(configBean.getI18n().getText("jira-importer-plugin.external.worklog.comment",
									worklog.getStartDate()));
		worklog.setTimeSpent(Long.valueOf((long) (3600.0 * added)));
		return worklog;
	}
}
