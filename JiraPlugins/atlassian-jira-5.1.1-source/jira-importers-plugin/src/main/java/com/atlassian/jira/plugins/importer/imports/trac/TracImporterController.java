/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.plugins.importer.FileCopyUtil;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.web.AbstractImporterController;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ConfigFileHandler;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.plugins.importer.imports.trac.web.TracSetupPage;
import com.google.common.collect.Lists;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

public class TracImporterController extends AbstractImporterController {
	private static final Logger log = Logger.getLogger(TracImporterController.class);

	private final ExternalUtils utils;
	private final TracWikiConverter wikiConverter;
	private final ConfigFileHandler configFileHandler;

	public TracImporterController(JiraDataImporter importer, ExternalUtils utils, TracWikiConverter wikiConverter, ConfigFileHandler configFileHandler) {
		super(importer, "issue.importer.jira.trac.import.bean", "Trac");
		this.utils = utils;
		this.wikiConverter = wikiConverter;
		this.configFileHandler = configFileHandler;
	}

	@Override
	public boolean createImportProcessBean(AbstractSetupPage setupPage) {
		ImportProcessBean bean = new ImportProcessBean();

		try {
			final File environmentZip = setupPage.getMultipart().getFile(TracSetupPage.FILE_INPUT_NAME);
			final JdbcConnection jdbcConnection = createDatabaseConnectionBean(environmentZip);
			final AbstractConfigBean configBean = createConfigBean(environmentZip, jdbcConnection);

			if (!configFileHandler.populateFromConfigFile(setupPage, configBean)) {
				return false;
			}

			bean.setJdbcConnection(jdbcConnection);
			bean.setConfigBean(configBean);
		} catch(Exception e) {
			log.error("Unable to setup Trac importer", e);
			setupPage.addErrorMessage(e.getMessage());
			return false;
		}

        storeImportProcessBeanInSession(bean);
		return true;
	}

	@Override
	public ImportDataBean createDataBean() throws Exception {
		final ImportProcessBean importProcessBean = getImportProcessBeanFromSession();
			return new TracDataBean((TracConfigBean) importProcessBean.getConfigBean(), wikiConverter);
	}

	public static Configuration getEnvironmentConfiguration(ZipFile zip) throws IOException, ConfigurationException {
		final ZipArchiveEntry ze = zip.getEntry("conf/trac.ini");
		final InputStream is = zip.getInputStream(ze);
		if (is == null) {
			throw new FileNotFoundException("No conf/trac.ini found in ZIP file");
		}
		try {
			final PropertiesConfiguration config = new PropertiesConfiguration() {
				// treat comma as a normal character
				@Override
				protected List split(String token) {
					return Collections.singletonList(token);
				}
			};
			config.load(is);
			return config;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	protected JdbcConnection createConnectionBean(String driver, String url)
			throws URISyntaxException, URIException {
		final URI uri = new URI(url, true);

		final String dbUrl = uri.getScheme() + "://" + uri.getHost()
				+ (uri.getPort() != -1 ? (":" + uri.getPort()) : "") + uri.getPathQuery();

		final String userInfo = uri.getUserinfo();
		final String username = StringUtils.substringBefore(userInfo, ":");
		final String password = StringUtils.substringAfter(userInfo, ":");

		return new JdbcConnection(driver, "jdbc:" + dbUrl, username, password);
	}

	public JdbcConnection createDatabaseConnectionBean(File environmentZip)
			throws IOException, ConfigurationException, URISyntaxException {
		final ZipFile zip = new ZipFile(environmentZip);
		try {
			final Configuration config = getEnvironmentConfiguration(zip);
			final String database = StringUtils.defaultString(config.getString("database"), "");
			if (database.startsWith("sqlite:")) {
				final ZipArchiveEntry ze = zip.getEntry(StringUtils.removeStart(database, "sqlite:"));
				final File tempDb = File.createTempFile("trac-importer-controller-", ".sqlite");

				FileCopyUtil.copy(zip.getInputStream(ze), tempDb);

				return new JdbcConnection("org.sqlite.JDBC", "jdbc:sqlite:" + tempDb.getPath(),
						null, null);
			} else if (database.startsWith("postgres:")) {
				if (database.contains("@/")) {
					throw new UnsupportedOperationException("It's not possible to import Trac configured to use PostgreSQL via socket. Please change your trac.ini to use PostgreSQL TCP/IP connection.");
				}
				return createConnectionBean("org.postgresql.Driver", database.replace("postgres://", "postgresql://"));
			} else if (database.startsWith("mysql:")) {
				return createConnectionBean("com.mysql.jdbc.Driver", database);
			} else if (StringUtils.isEmpty(database)) {
				throw new UnsupportedOperationException("You database configuration stored in trac.ini is empty.");
			} else {
				throw new UnsupportedOperationException("You database configuration stored in trac.ini is not supported. Contact JIM developers for support.");
			}
		} finally {
			zip.close();
		}
	}

	public AbstractDatabaseConfigBean createConfigBean(final File environmentZip, JdbcConnection jdbcConnection)
			throws IOException, ConfigurationException {
		return new TracConfigBean(jdbcConnection, utils, environmentZip);
	}

	@Override
	public List<String> getSteps() {
		return Lists.newArrayList(
				"TracSetupPage",
				"ImporterProjectMappingsPage",
				"ImporterCustomFieldsPage",
				"ImporterFieldMappingsPage",
				"ImporterValueMappingsPage"
		);
	}
}