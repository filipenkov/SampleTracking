/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VotesTransformer implements ResultSetTransformer<String> {
	protected String ixBug;
	protected final BugzillaConfigBean configBean;

	public VotesTransformer(String ixBug, final BugzillaConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
	}

	public String transform(ResultSet rs) throws SQLException {
		return configBean.getUsernameForLoginName(rs.getString("login_name"));
	}

	public String getSqlQuery() {
		return "SELECT login_name FROM votes AS v, profiles AS p WHERE v.who=p.userid AND v.bug_id = " + ixBug;
	}
}
