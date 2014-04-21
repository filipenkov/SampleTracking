/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WatchersTransformer implements ResultSetTransformer<String> {
	private final String ixBug;
	private final MantisConfigBean configBean;

	public WatchersTransformer(String ixBug, MantisConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT username FROM mantis_bug_monitor_table m, mantis_user_table u WHERE m.user_id=u.id AND m.bug_id = " + ixBug;
	}

	public String transform(ResultSet rs) throws SQLException {
		return configBean.getUsernameForLoginName(rs.getString("username"));
	}
}
