/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import com.atlassian.jira.plugins.importer.imports.trac.TracWikiConverter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentTransformer implements ResultSetTransformer<ExternalComment> {
	private final String ixBug;
	private final TracConfigBean configBean;
	private final TracWikiConverter wikiConverter;
	private final ImportLogger log;

	public CommentTransformer(final String ixBug, final TracConfigBean configBean,
			final TracWikiConverter wikiConverter, final ImportLogger log) {
		this.ixBug = ixBug;
		this.configBean = configBean;
		this.wikiConverter = wikiConverter;
		this.log = log;
	}

	public String getSqlQuery() {
		return "SELECT time,author,newvalue FROM ticket_change WHERE ticket = " + ixBug + " AND field='comment' ORDER BY time ASC";
	}

	@Nullable
	public ExternalComment transform(final ResultSet rs) throws SQLException {
		final String commenter = configBean.getUsernameForEmail(rs.getString("author"));
		final String comment = rs.getString("newvalue");

		if (StringUtils.isNotBlank(comment)) {
			return new ExternalComment(wikiConverter.convert(comment, log), commenter, configBean.getTimestamp(rs, "time"));
		}

		return null;
	}

}
