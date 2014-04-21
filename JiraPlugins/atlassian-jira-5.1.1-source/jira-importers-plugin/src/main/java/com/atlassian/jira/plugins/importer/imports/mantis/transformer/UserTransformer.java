/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserTransformer extends AbstractResultSetTransformer<ExternalUser> {
	private final MantisConfigBean configBean;

	public UserTransformer(final MantisConfigBean configBean, ImportLogger log) {
		super(log);
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT id, username, realname, email, enabled FROM mantis_user_table";
	}

	public ExternalUser transform(final ResultSet rs) throws SQLException {
		final String fullName = TextUtils.noNull(rs.getString("realname"));
		final String email = TextUtils.noNull(rs.getString("email"));
		final String username = configBean.getUsernameForLoginName(rs.getString("username"));
		final boolean active = rs.getBoolean("enabled");

		if (StringUtils.isBlank(username)) {
			log.warn("Username is blank and is not imported");
			return null;
		}
		ExternalUser user = new ExternalUser(username,
				StringUtils.isEmpty(fullName) ? getFullNameFromEmail(email) : fullName, email);
		if (!active) {
			user.setActive(false);
			user.getGroups().add(configBean.getInactiveUsersGroup());
		}
		return user;
	}

	public String getFullNameFromEmail(final String email) {
		if (email == null) {
			return "";
		}

		final int index = email.indexOf("@");
		if (index != -1) {
			return email.substring(0, index);
		} else {
			return "";
		}
	}

}
