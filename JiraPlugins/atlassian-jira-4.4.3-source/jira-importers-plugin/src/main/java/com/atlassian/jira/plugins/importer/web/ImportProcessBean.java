/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;

public class ImportProcessBean
{
	private SiteConfiguration urlBean;

	private JdbcConnection jdbcConnection;

	private AbstractConfigBean configBean;

	public SiteConfiguration getUrlBean() {
		return urlBean;
	}

	public void setUrlBean(SiteConfiguration urlBean) {
		this.urlBean = urlBean;
	}

	public JdbcConnection getJdbcConnection() {
		return jdbcConnection;
	}

	public void setJdbcConnection(JdbcConnection jdbcConnection) {
		this.jdbcConnection = jdbcConnection;
	}

	public AbstractConfigBean getConfigBean() {
		return configBean;
	}

	public void setConfigBean(AbstractConfigBean configBean) {
		this.configBean = configBean;
	}

}
