/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentTransformer implements ResultSetTransformer<ExternalComment> {
	private final String ixBug;
	private final BugzillaConfigBean configBean;

	public CommentTransformer(final String ixBug, final BugzillaConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT thetext, who, bug_when, "
				+ "(select login_name from profiles where userid=who) AS login_name FROM longdescs WHERE bug_id = " + ixBug
				+ " ORDER BY bug_when ASC";
	}

	public ExternalComment transform(final ResultSet rs) throws SQLException {
		if (rs.isFirst()) {
			return null; // first comment in Bugzilla is actually the description which we want to ignore here
		}

		ExternalComment externalComment = null;

		final String commenter = configBean.getUsernameForLoginName(rs.getString("login_name"));
		final String comment = rs.getString("thetext");

		if (StringUtils.isNotBlank(comment)) {
			externalComment = new ExternalComment(comment, commenter, rs.getTimestamp("bug_when"));
		}

		return externalComment;
	}
}
