/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer;

import java.sql.SQLException;

public class SQLRuntimeException extends RuntimeException {
	public SQLRuntimeException(SQLException e) {
		super(e);
	}

	@Override
	public String getMessage() {
		return getCause().getMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return getCause().getMessage();
	}
}
