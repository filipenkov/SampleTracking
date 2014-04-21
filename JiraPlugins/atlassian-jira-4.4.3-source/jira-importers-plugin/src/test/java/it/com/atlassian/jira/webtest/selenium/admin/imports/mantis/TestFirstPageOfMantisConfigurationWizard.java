/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.junit.Test;

public class TestFirstPageOfMantisConfigurationWizard extends ScreenshotFuncTestCase {

	@Override
	public void setUpTest() {
        product = TestedProductFactory.create(JiraTestedProduct.class);
	}

	@Test
	public void testConnectionFails() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		MantisImporterSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(MantisImporterSetupPage.class)
				.webSudo();

		assertEquals(ImmutableList.of("Error connecting to the database: Network error IOException: Connection refused"),
				setupPage.setDatabaseType("mysql")
				.setSiteUrl("http://localhost")
				.setJdbcHostname("localhost")
				.setJdbcDatabase("database")
				.setJdbcUsername("test")
				.setJdbcPassword("2342").nextWithError().getGlobalErrors());
	}
}
