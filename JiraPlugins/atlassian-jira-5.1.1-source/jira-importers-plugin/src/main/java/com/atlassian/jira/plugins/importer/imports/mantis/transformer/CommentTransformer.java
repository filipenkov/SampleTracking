/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CommentTransformer implements ResultSetTransformer<ExternalComment> {
	private final String ixBug;
	private final MantisConfigBean configBean;
    private MantisConfigBean.TimestampHelper timestampHelper;

    public CommentTransformer(final String ixBug, final MantisConfigBean configBean) {
		this.ixBug = ixBug;
		this.configBean = configBean;
        this.timestampHelper = new MantisConfigBean.TimestampHelper();
	}

	public String getSqlQuery() {
		return "SELECT note, (SELECT username FROM mantis_user_table WHERE id=b.reporter_id) AS username, date_submitted FROM mantis_bugnote_table AS b"
				+ " JOIN mantis_bugnote_text_table AS bt ON (b.bugnote_text_id=bt.id)"
				+ " WHERE bug_id = " + ixBug + " ORDER BY date_submitted ASC";
	}

	public ExternalComment transform(final ResultSet rs) throws SQLException {
		final String commenter = configBean.getUsernameForLoginName(rs.getString("username"));
		final String comment = IssueTransformerVer118OrOlder.escapeMantisString(rs.getString("note"));

		if (StringUtils.isNotBlank(comment)) {
			return new ExternalComment(comment, commenter, timestampHelper.getTimestamp(rs, "date_submitted"));
		}

		return null;
	}
}
