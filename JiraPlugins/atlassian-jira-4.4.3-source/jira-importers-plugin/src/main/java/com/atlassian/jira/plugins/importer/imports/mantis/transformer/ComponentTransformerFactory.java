/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComponentTransformerFactory {
	private static final Logger log = Logger.getLogger(ComponentTransformerFactory.class);

	private static final String TABLE_NAME = "mantis_project_category_table";

	final boolean hasOldTable;

	public ComponentTransformerFactory(final JdbcConnection jdbcConnection) {
		hasOldTable = checkForOldColumnName(jdbcConnection);
	}

	/**
	 * Returns an instance of {@link com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.FixForVersionTransformer} that works for the given version of FogBugz.
	 *
	 * @param externalProject external project
	 * @param importLogger
	 * @return an instance of VersionTransformer
	 */
	public ResultSetTransformer<ExternalComponent> create(final MantisConfigBean configBean,
			final ExternalProject externalProject, ImportLogger importLogger) {
		if (hasOldTable) {
			return new ComponentTransformerVer118OrOlder(configBean, externalProject, importLogger);
		} else {
			return new ComponentTransformerVer120OrNewer(configBean, externalProject, importLogger);
		}
	}

	private boolean checkForOldColumnName(final JdbcConnection jdbcConnection) {
		Connection connection = null;
		try {
			return hasOldTable(connection = jdbcConnection.getConnection());
		}
		catch (final SQLException e) {
			log.info("Assuming Mantis is version 1.2.0 or higher");
			log.warn("Error checking table for " + TABLE_NAME, e);
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

	private boolean hasOldTable(final Connection connection) throws SQLException {
		final ResultSet columns = connection.getMetaData().getTables(null, null, TABLE_NAME, null);
		try {
			while (columns.next()) {
				if (log.isDebugEnabled()) {
					log.debug("'" + TABLE_NAME + "' exists. Assuming Mantis older than 1.2.0");
				}
				return true;
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