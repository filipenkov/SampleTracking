/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractTransformerFactory {
	protected boolean hasTable(final Connection connection, final String tableName) throws SQLException {
		final ResultSet columns = connection.getMetaData().getTables(null, null, tableName, null);
		try {
			while (columns.next()) {
				return true;
			}
			return false;
		} finally {
			if (columns != null) {
				columns.close();
			}
		}
	}
}
