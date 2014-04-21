/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

public class JdbcConfiguration {

	private final String databaseType;
	private final String jdbcHostname;
	private final String jdbcPort;
	private final String jdbcDatabase;
	private final String jdbcUsername;
	private final String jdbcPassword;
	private final String jdbcAdvanced;

	public JdbcConfiguration(String databaseType, String jdbcHostname, String jdbcPort,
			String jdbcDatabase, String jdbcUsername, String jdbcPassword, String jdbcAdvanced) {
		this.databaseType = databaseType;
		this.jdbcHostname = jdbcHostname;
		this.jdbcPort = jdbcPort;
		this.jdbcDatabase = jdbcDatabase;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
		this.jdbcAdvanced = jdbcAdvanced;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public String getJdbcHostname() {
		return jdbcHostname;
	}

	public String getJdbcPort() {
		return jdbcPort;
	}

	public String getJdbcDatabase() {
		return jdbcDatabase;
	}

	public String getJdbcUsername() {
		return jdbcUsername;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public String getJdbcAdvanced() {
		return jdbcAdvanced;
	}
}
