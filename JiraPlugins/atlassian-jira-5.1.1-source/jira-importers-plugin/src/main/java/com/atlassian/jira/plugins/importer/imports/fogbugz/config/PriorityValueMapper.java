/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugins.importer.SQLRuntimeException;
import com.atlassian.jira.plugins.importer.imports.AbstractPriorityValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

public class PriorityValueMapper extends AbstractPriorityValueMapper {
	public static final String PRIORITY_FIELD = "sPriority";

	public PriorityValueMapper(JdbcConnection jdbcConnection, JiraAuthenticationContext authenticationContext,
			FieldManager fieldManager) {
		super(jdbcConnection, authenticationContext, fieldManager);
	}

	public String getExternalFieldId() {
		return PRIORITY_FIELD;
	}

	@Nullable
	public String getDescription() {
		return null;
	}

	public static boolean isSqlServer(JdbcConnection jdbcConnection) throws SQLException {
		return jdbcConnection.getConnection().getMetaData().getDatabaseProductName().startsWith("Microsoft");
	}

	public Set<String> getDistinctValues() {
		try {
			if(isSqlServer(jdbcConnection)) {
				return new LinkedHashSet<String>(jdbcConnection.queryDb(
					new SingleStringResultTransformer("SELECT (CAST(ixPriority AS VARCHAR(20)) + '-' + sPriority) AS priority FROM Priority ORDER BY priority")));
			} else {
				return new LinkedHashSet<String>(jdbcConnection.queryDb(
					new SingleStringResultTransformer("SELECT CONCAT(ixPriority,CONCAT('-',sPriority)) AS priority FROM Priority ORDER BY priority")));
			}
		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		}
	}
}
