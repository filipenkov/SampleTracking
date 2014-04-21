/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.tracking.UsageTrackingService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ImporterSetupPage extends AbstractSetupPage implements RemoteSiteImporterSetupPage {

	private static final Map<String, DatabaseType> databaseTypeMap = MapBuilder.<String, DatabaseType>newBuilder()
            .add("postgres72", DatabaseType.POSTGRES)
            .add("mysql", DatabaseType.MY_SQL)
            .add("mssql", DatabaseType.SQL_SERVER)
            .toMap();

	private final ConfigFileHandler configFileHandler;

	private String siteUrl;
	private String siteUsername;
	private String sitePassword;
	private boolean siteCredentials;

	private String databaseType;
	private String jdbcHostname;
	private String jdbcPort;
	private String jdbcUsername;
	private String jdbcPassword;
	private String jdbcDatabase;
	private String jdbcAdvanced;

	public ImporterSetupPage(ExternalUtils utils, UsageTrackingService usageTrackingService,
			ConfigFileHandler configFileHandler, WebInterfaceManager webInterfaceManager,
			PluginAccessor pluginAccessor) {
		super(utils, usageTrackingService, webInterfaceManager, pluginAccessor);
		this.configFileHandler = configFileHandler;
	}

	public Map<String, String> getDatabaseTypes() {
		final Map<String, String> selectList = Maps.newHashMap();

		selectList.put("", getText("setupdb.database.selectType"));
		selectList.put("postgres72", "PostgreSQL");
		selectList.put("mysql", "MySQL");
		selectList.put("mssql", "Microsoft SQL Server");

		return selectList;
	}

	public DatabaseType getDatabaseTypeEnum()
	{
		final DatabaseType type = databaseTypeMap.get(databaseType);
		if (type == null) {
			throw new IllegalStateException("Unknown database type '" + databaseType + "'");
		}
		return type;
	}

	@Override
	protected void doValidation() {
		if (isPreviousClicked()) {
			return;
		}

        super.doValidation();

		if (!configFileHandler.verifyConfigFileParam(this)) return;

		// Make sure a database type is selected!
		if (StringUtils.isEmpty(databaseType)) {
			addError("databaseType", getText("setupdb.error.selectDatabaseType"));
		}

		if (StringUtils.isEmpty(jdbcHostname)) {
			addError("jdbcHostname", getText("setupdb.error.requireJdbcHostname"));
		}

		if (StringUtils.isEmpty(jdbcPort)) {
			addError("jdbcPort", getText("setupdb.error.requireJdbcPort"));
		}

		if (StringUtils.isEmpty(jdbcDatabase)) {
			addError("jdbcDatabase", getText("setupdb.error.requireDatabase"));
		}

		if (StringUtils.isEmpty(jdbcUsername)) {
			addError("jdbcUsername", getText("setupdb.error.requireJdbcUsername"));
		}
	}

	@Override
	@RequiresXsrfCheck
	protected String doExecute() throws Exception {
		final ImporterController controller = getController();
		if (controller == null) {
			return RESTART_NEEDED;
		}

        if (!isPreviousClicked() && !controller.createImportProcessBean(this)) {
            return INPUT;
        }

		if (isNextClicked()) {
			SessionConnectionConfiguration.setCurrentSession(getExternalSystem(),
					new SessionConnectionConfiguration(
							new JdbcConfiguration(getDatabaseType(), getJdbcHostname(), getJdbcPort(),
									getJdbcDatabase(), getJdbcUsername(), getJdbcPassword(),
									getJdbcAdvanced()),
							new SiteConfiguration(getSiteUrl(), getSiteCredentials(),
									getSiteUsername(), getSitePassword())));
		}

		return super.doExecute();
	}

	@Override
	public String doDefault() throws Exception {
		if (!isAdministrator()) {
			return "denied";
		}

		final ImporterController controller = getController();
		if (controller == null) {
			return RESTART_NEEDED;
		}

		final SessionConnectionConfiguration scc = SessionConnectionConfiguration
				.getCurrentSession(getExternalSystem());
		if (scc != null) {
			final JdbcConfiguration jdbcConfiguration = scc.getJdbcConfiguration();
			final SiteConfiguration urlBean = scc.getSiteConfiguration();
			if (jdbcConfiguration != null) {
				setDatabaseType(jdbcConfiguration.getDatabaseType());
				setJdbcHostname(jdbcConfiguration.getJdbcHostname());
				setJdbcPort(jdbcConfiguration.getJdbcPort());
				setJdbcDatabase(jdbcConfiguration.getJdbcDatabase());
				setJdbcUsername(jdbcConfiguration.getJdbcUsername());
				setJdbcPassword(jdbcConfiguration.getJdbcPassword());
				setJdbcAdvanced(jdbcConfiguration.getJdbcAdvanced());
			}

			if (urlBean != null) {
				setSiteUrl(urlBean.getUrl());
				setSiteUsername(urlBean.getUsername());
				setSitePassword(urlBean.getPassword());
				setSiteCredentials(urlBean.isUseCredentials());
			}
		}

		return INPUT;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = StringUtils.trim(siteUrl);
	}

	public String getSiteUsername() {
		return siteUsername;
	}

	public void setSiteUsername(String siteUsername) {
		this.siteUsername = StringUtils.trim(siteUsername);
	}

	public String getSitePassword() {
		return sitePassword;
	}

	public void setSitePassword(String sitePassword) {
		this.sitePassword = StringUtils.trim(sitePassword);
	}

	public boolean getSiteCredentials() {
		return siteCredentials;
	}

	public void setSiteCredentials(boolean siteCredentials) {
		this.siteCredentials = siteCredentials;
	}

	public String getJdbcHostname() {
		return jdbcHostname;
	}

	public void setJdbcHostname(String jdbcHostname) {
		this.jdbcHostname = StringUtils.trim(jdbcHostname);
	}

	public String getJdbcUsername() {
		return jdbcUsername;
	}

	public void setJdbcUsername(String jdbcUsername) {
		this.jdbcUsername = StringUtils.trim(jdbcUsername);
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = StringUtils.trim(jdbcPassword);
	}

	public String getJdbcDatabase() {
		return jdbcDatabase;
	}

	public void setJdbcDatabase(String databaseName) {
		this.jdbcDatabase = StringUtils.trim(databaseName);
	}

	@Override
	public boolean isSiteUrlRequired() {
		return false;
	}

	@Override
	public String getLoginLabel() {
		return getText("jira-importer-plugin.site.login.name", getTitle());
	}

	@Override
	public String getPasswordLabel() {
		return getText("jira-importer-plugin.site.login.password", getTitle());
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getJdbcPort() {
		return jdbcPort;
	}

	public void setJdbcPort(String databasePort) {
		this.jdbcPort = StringUtils.trim(databasePort);
	}

	public String getJdbcAdvanced() {
		return jdbcAdvanced;
	}

	public void setJdbcAdvanced(String jdbcAdvanced) {
		this.jdbcAdvanced = StringUtils.trim(jdbcAdvanced);
	}
}
