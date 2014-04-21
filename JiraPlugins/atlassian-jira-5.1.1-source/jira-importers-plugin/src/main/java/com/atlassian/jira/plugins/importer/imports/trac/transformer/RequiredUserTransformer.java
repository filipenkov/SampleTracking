/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequiredUserTransformer extends AbstractResultSetTransformer<ExternalUser> {
	private final TracConfigBean configBean;

	public RequiredUserTransformer(final TracConfigBean configBean, ImportLogger log) {
		super(log);
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT owner FROM ticket"
				+ " UNION SELECT reporter FROM ticket"
				+ " UNION SELECT author FROM attachment WHERE type='ticket'"
				+ " UNION SELECT author FROM ticket_change WHERE field='comment'"
				+ " UNION SELECT owner FROM component";
	}

	@Nullable
	public ExternalUser transform(final ResultSet rs) throws SQLException {
		final String fullName = configBean.extractFullName(rs.getString(1));
		final String username = configBean.getUsernameForEmail(rs.getString(1));

		if (StringUtils.isBlank(username)) {
			log.warn("Username is blank and is not imported");
			return null;
		}

		ExternalUser user = new ExternalUser(username,
				StringUtils.isEmpty(fullName) ? getFullNameFromEmail(username) : fullName, username);

		return user;
	}

	@Nullable
	public static ExternalUser transform(TracConfigBean configBean, String email, ImportLogger log) {
		final String fullName = configBean.extractFullName(email);
		final String username = configBean.getUsernameForEmail(email);

		if (StringUtils.isBlank(username)) {
			log.warn("Username is blank and is not imported");
			return null;
		}

		ExternalUser user = new ExternalUser(username,
				StringUtils.isEmpty(fullName) ? getFullNameFromEmail(username) : fullName, username);

		return user;
	}

	protected static String getFullNameFromEmail(final String email) {
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
