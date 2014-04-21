/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class SingleStringResultTransformer implements ResultSetTransformer<String> {
	private final String sqlQuery;

	public SingleStringResultTransformer(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public final String transform(ResultSet rs) throws SQLException {
		return rs.getString(1);
	}

	public final String getSqlQuery() {
		return sqlQuery;
	}
}
