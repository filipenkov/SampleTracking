/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import webwork.action.ActionContext;

import javax.annotation.Nullable;

public class SessionConnectionConfiguration {
	final JdbcConfiguration jdbcConfiguration;
	final SiteConfiguration siteConfiguration;

	public static final String IMPORT_SESSION_CONNECTION_CONFIGURATION = "issue.importer.jira.session.connection.configuration";

	public SessionConnectionConfiguration(@Nullable JdbcConfiguration jdbcConfiguration,
			@Nullable SiteConfiguration siteConfiguration) {
		this.jdbcConfiguration = jdbcConfiguration;
		this.siteConfiguration = siteConfiguration;
	}

	@Nullable
	public JdbcConfiguration getJdbcConfiguration() {
		return jdbcConfiguration;
	}

	@Nullable
	public SiteConfiguration getSiteConfiguration() {
		return siteConfiguration;
	}

	@Nullable
	public static SessionConnectionConfiguration getCurrentSession(String externalSystem) {
		try {
			return (SessionConnectionConfiguration)
					ActionContext.getSession().get(IMPORT_SESSION_CONNECTION_CONFIGURATION + externalSystem);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void setCurrentSession(String externalSystem,
			SessionConnectionConfiguration sessionConnectionConfiguration) {
		ActionContext.getSession().put(IMPORT_SESSION_CONNECTION_CONFIGURATION + externalSystem, sessionConnectionConfiguration);
	}
}
