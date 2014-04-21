/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ComponentTransformerFactory {
	private static final Logger log = Logger.getLogger(ComponentTransformerFactory.class);

	private static final String TABLE_NAME = "mantis_category_table";

	final boolean hasNewTable;

	public ComponentTransformerFactory(final JdbcConnection jdbcConnection) {
		hasNewTable = checkForNewTable(jdbcConnection);
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
		if (!hasNewTable) {
			return new ComponentTransformerVer118OrOlder(configBean, externalProject, importLogger);
		} else {
			return new ComponentTransformerVer120OrNewer(configBean, externalProject, importLogger);
		}
	}

	private boolean checkForNewTable(final JdbcConnection jdbcConnection) {
		Connection connection = null;
		try {
			return SqlUtils.hasTable(connection = jdbcConnection.getConnection(), TABLE_NAME);
		}
		catch (final SQLException e) {
			log.info("Assuming Mantis is version lower than 1.2.0");
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

}