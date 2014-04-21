/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;

import java.util.Collection;

public class RequiredUserTransformer extends UserTransformer {
	private final Collection<ExternalProject> projects;

	public RequiredUserTransformer(final BugzillaConfigBean configBean, Collection<ExternalProject> projects, ImportLogger importLogger) {
		super(configBean, importLogger);
		this.projects = projects;
	}

	@Override
	public String getSqlQuery() {
		if (projects.size() == 0) {
			return super.getSqlQuery();
		}

		final String projectIds = SqlUtils.comma(projects);
		final String fields = "SELECT prof.userid, prof.login_name, prof.realname, prof.disabledtext FROM profiles AS prof JOIN ";
		final String union = " UNION DISTINCT ";

		return
			fields + "bugs AS b ON (b.reporter = prof.userid OR b.assigned_to = prof.userid) WHERE b.product_id IN (" + projectIds + ")"
				+ union +
			fields + "longdescs AS l ON (l.who = prof.userid) JOIN bugs AS b ON (l.bug_id = b.bug_id) WHERE b.product_id IN (" + projectIds + ")"
				+ union +
			fields + "votes AS v ON (v.who = prof.userid) JOIN bugs AS b ON (v.bug_id = b.bug_id) WHERE b.product_id IN (" + projectIds + ")"
				+ union +
			fields + "cc AS c ON (c.who = prof.userid) JOIN bugs AS b ON (c.bug_id = b.bug_id) WHERE b.product_id IN (" + projectIds + ")"
				+ union +
			fields + "attachments AS a ON (a.submitter_id = prof.userid) JOIN bugs AS b ON (a.bug_id = b.bug_id) WHERE b.product_id IN (" + projectIds + ")"
				+ union +
			fields + "bugs_activity AS ba ON (ba.who = prof.userid) JOIN bugs AS b ON (ba.bug_id = b.bug_id)" +
                    " WHERE ba.fieldid = (SELECT " + configBean.getFielddefsIdColumn() + " FROM fielddefs WHERE name = 'work_time' LIMIT 1) AND b.product_id IN (" + projectIds + ")";
	}

}
