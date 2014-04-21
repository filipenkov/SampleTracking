/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.web.SiteConfiguration;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachmentTransformerFactory {
	private static final Logger log = Logger.getLogger(AttachmentTransformerFactory.class);

	private static final String META_COLUMN_NAME = "COLUMN_NAME";

	private static final String TABLE_NAME = "attachments";
	private static final String OLD_COLUMN_NAME = "thedata";

	final boolean hasOldColumnName;

	public AttachmentTransformerFactory(final JdbcConnection jdbcConnection) {
		hasOldColumnName = checkForOldColumnName(jdbcConnection);
	}

	public ResultSetTransformer<ExternalAttachment> create(final BugzillaConfigBean configBean,
			final String externalIssueOldId, final SiteConfiguration bugzillaUrl, ImportLogger log) {
		if (hasOldColumnName) {
			return new AttachmentTransformerForDataInAttachments(externalIssueOldId, configBean, log);
		} else {
			return new AttachmentTransformerForAttachDataAndBigFiles(externalIssueOldId, configBean, bugzillaUrl, log);
		}
	}

	private boolean checkForOldColumnName(final JdbcConnection jdbcConnection) {
		Connection connection = null;
		try {
			return hasOldColumnName(connection = jdbcConnection.getConnection());
		}
		catch (final SQLException e) {
			log.info("Assuming Bugzilla has attachments table with thedata column and doesn't support Big Files");
			log.warn("Error getting the column name data from database", e);
			return false;
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (final SQLException e) {
					log.warn("Error closing connection", e);
				}
			}
		}
	}

	private boolean hasOldColumnName(final Connection connection) throws SQLException {
		log.debug("Getting the column names of the '" + TABLE_NAME + "' table");
		final ResultSet columns = connection.getMetaData().getColumns(null, null, TABLE_NAME, null);
		try {
			while (columns.next()) {
				final String columnName = columns.getString(META_COLUMN_NAME);
				if (OLD_COLUMN_NAME.equals(columnName)) {
					if (log.isDebugEnabled()) {
						log.debug("'" + TABLE_NAME + "' table contains old column '" + OLD_COLUMN_NAME + "'");
						log.debug("Assuming Bugzilla doesn't support Big Files");
					}
					return true;
				}
			}
			return false;
		}
		finally {
			SqlUtils.close(columns);
		}
	}
}
