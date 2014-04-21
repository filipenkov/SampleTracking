/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.junit.Test;

public class TestFirstPageOfMantisConfigurationWizard extends ScreenshotFuncTestCase {

	@Test
	public void testConnectionFails() {
		MantisImporterSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(MantisImporterSetupPage.class);

		assertEquals(ImmutableList.of("Error connecting to the database: Could not create connection to database server. Attempted reconnect 3 times. Giving up."),
				setupPage.setDatabaseType("mysql")
				.setSiteUrl("http://localhost")
				.setJdbcHostname("localhost")
				.setJdbcDatabase("database")
				.setJdbcUsername("test")
				.setJdbcPassword("2342").nextWithError().getGlobalErrors());
	}
}
