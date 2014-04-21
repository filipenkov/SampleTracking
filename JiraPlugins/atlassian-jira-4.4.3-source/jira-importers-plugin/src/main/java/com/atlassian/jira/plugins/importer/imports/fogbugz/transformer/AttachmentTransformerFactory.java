/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.AbstractTransformerFactory;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class AttachmentTransformerFactory extends AbstractTransformerFactory {
	private static final Logger log = Logger.getLogger(AttachmentTransformerFactory.class);

	private static final String TABLE_NAME = "AttachmentReference";

	final boolean hasNewTable;

	public AttachmentTransformerFactory(final JdbcConnection jdbcConnection) {
		hasNewTable = checkForTableName(jdbcConnection);
	}

	/**
	 * Returns an instance of {@link com.atlassian.jira.plugins.importer.imports.fogbugz.transformer.FixForVersionTransformer} that works for the given version of FogBugz.
	 *
	 * @param importLogger
	 * @return an instance of VersionTransformer
	 */
	public ResultSetTransformer<ExternalAttachment> create(final String ixBug, final FogBugzConfigBean configBean,
			ImportLogger importLogger) {
		if (hasNewTable) {
			return new AttachmentTransformerVer8OrNewer(ixBug, configBean);
		} else {
			return new AttachmentTransformerVer7OrOlder(ixBug, configBean);
		}
	}

	private boolean checkForTableName(final JdbcConnection jdbcConnection) {
		Connection connection = null;
		try {
			return hasTable(connection = jdbcConnection.getConnection(), TABLE_NAME);
		} catch (final SQLException e) {
			log.info("Assuming FogBugz is version 7.x or lower");
			log.warn("Error checking table for " + TABLE_NAME, e);
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException e) {
					log.warn("Error closing connection", e);
				}
			}
		}
	}
}