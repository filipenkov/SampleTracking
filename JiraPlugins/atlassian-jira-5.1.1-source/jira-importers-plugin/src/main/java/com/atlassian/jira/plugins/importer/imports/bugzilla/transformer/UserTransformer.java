/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserTransformer extends AbstractResultSetTransformer<ExternalUser> {
	protected final BugzillaConfigBean configBean;

	public UserTransformer(final BugzillaConfigBean configBean, ImportLogger log) {
		super(log);
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT userid, login_name, realname, disabledtext FROM profiles";
	}

	@Nullable
	public ExternalUser transform(final ResultSet rs) throws SQLException {
		final String fullName = rs.getString("realname");
		// login_name is e-mail too
		final String email = TextUtils.noNull(rs.getString("login_name"));

		// take it as e-mail then
		final String username = configBean.getUsernameForLoginName(email);
		final boolean active = StringUtils.isEmpty(rs.getString("disabledtext"));

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
