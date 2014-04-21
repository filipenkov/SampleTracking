/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IssueTransformerFactory {
	private static final Logger log = Logger.getLogger(IssueTransformerFactory.class);

	private static final String META_COLUMN_NAME = "COLUMN_NAME";

	private static final String TABLE_NAME = "mantis_bug_table";
	private static final String OLD_COLUMN_NAME = "category";

	final boolean hasOldColumnName;

	public IssueTransformerFactory(final JdbcConnection jdbcConnection) {
		hasOldColumnName = checkForOldColumnName(jdbcConnection);
	}

	public ResultSetTransformer<ExternalIssue> create(final String mantisUrl,
			final MantisConfigBean configBean, final ExternalProject project,
			DateTimePickerConverter dateTimePickerConverter, ImportLogger log) {
		if (hasOldColumnName) {
			return new IssueTransformerVer118OrOlder(mantisUrl, configBean, project, dateTimePickerConverter, log);
		} else {
			return new IssueTransformerVer120OrNewer(mantisUrl, configBean, project, dateTimePickerConverter, log);
		}
	}

	private boolean checkForOldColumnName(final JdbcConnection jdbcConnection) {
		Connection connection = null;
		try {
			return hasOldColumnName(connection = jdbcConnection.getConnection());
		}
		catch (final SQLException e) {
			log.info("Assuming Mantis is version 5.0.17 or higher");
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
						log.debug("Assuming Mantis older than 5.0.17");
					}
					return true;
				}
			}
			return false;
		}
		finally {
			if (columns != null) {
				columns.close();
			}
		}
	}
}
