/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AbstractDatabaseImporterControllerTest {
	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private AbstractDatabaseImporterController controller;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	ImporterSetupPage setupPage;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		setupPage.setJdbcDatabase("dbname");
		setupPage.setJdbcHostname("hostname");
		setupPage.setJdbcUsername("user");
		setupPage.setJdbcPassword("pass");
		setupPage.setJdbcPort("1234");

	}

	@Test
	public void testGluingTogetherMSSQLAdvancedJDBCSettings() throws Exception {
		setupPage.setDatabaseType("mssql");
		String url = controller.createDatabaseConnectionBean(setupPage).getUrl();
		Assert.assertEquals("jdbc:jtds:sqlserver://hostname:1234/dbname", url);

		setupPage.setJdbcAdvanced("advanced=yes;another=true");
		url = controller.createDatabaseConnectionBean(setupPage).getUrl();
		Assert.assertEquals("jdbc:jtds:sqlserver://hostname:1234/dbname;advanced=yes;another=true", url);
	}

	@Test
	public void testGluingTogetherPostgreSQLAdvancedJDBCSettings() throws Exception {
		setupPage.setDatabaseType("postgres72");
		String url = controller.createDatabaseConnectionBean(setupPage).getUrl();
		Assert.assertEquals("jdbc:postgresql://hostname:1234/dbname", url);

		setupPage.setJdbcAdvanced("advanced=yes&another=true");
		url = controller.createDatabaseConnectionBean(setupPage).getUrl();
		Assert.assertEquals("jdbc:postgresql://hostname:1234/dbname?advanced=yes&another=true", url);
	}

	@Test
	public void testGluingTogetherMySQLAdvancedJDBCSettings() throws Exception {
		// MySQL requires some additional params we don't want to care in this test
		setupPage.setDatabaseType("mysql");
		setupPage.setJdbcAdvanced("advanced=yes&another=true");
		final String url = controller.createDatabaseConnectionBean(setupPage).getUrl();
		Assert.assertTrue(url.startsWith("jdbc:mysql://hostname:1234/dbname?"));
		Assert.assertTrue(url.endsWith("&advanced=yes&another=true"));
	}
}
