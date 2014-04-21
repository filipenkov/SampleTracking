/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;

public abstract class AbstractDatabaseConfigBean extends AbstractConfigBean2 {

	protected JdbcConnection jdbcConnection;

    protected AbstractDatabaseConfigBean(JdbcConnection jdbcConnection, ExternalUtils utils) {
		super(utils);

		this.jdbcConnection = jdbcConnection;
	}

	public JdbcConnection getJdbcConnection() {
		return jdbcConnection;
	}

}
