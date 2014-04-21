/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentTransformer implements ResultSetTransformer<ExternalComment> {
	private final String ixBug;
	private final FogBugzConfigBean configBean;
	private final ImportLogger log;

	public CommentTransformer(final String ixBug, final FogBugzConfigBean configBean, ImportLogger log) {
		this.ixBug = ixBug;
		this.configBean = configBean;
		this.log = log;
	}

	public String getSqlQuery() {
		return "SELECT b.s AS comment, b.dt AS date, p.sFullName, b.fEmail "
				+ "FROM BugEvent b, Person p WHERE b.ixPerson = p.ixPerson AND b.ixBug = " + ixBug + " ORDER BY b.dt ";
	}

	public ExternalComment transform(final ResultSet rs) throws SQLException {
		final String commenter = configBean.getUsernameForFullName(rs.getString("sFullName"));
		final String comment = rs.getString("comment");

		if (StringUtils.isNotBlank(comment)) {
			final String body;
			if (!rs.getBoolean("fEmail")) {
				body = StringUtils.trimToEmpty(comment);
			} else {
				try {
					body = ExternalUtils.getTextDataFromMimeMessage(comment);
				} catch (Exception e) {
					log.warn("Error decoding comment from string [" + comment + "]. Comment will be skipped", e);
					return null;
				}
			}

			return new ExternalComment(body, commenter, rs.getTimestamp("date"));
		}

		return null;
	}
}
