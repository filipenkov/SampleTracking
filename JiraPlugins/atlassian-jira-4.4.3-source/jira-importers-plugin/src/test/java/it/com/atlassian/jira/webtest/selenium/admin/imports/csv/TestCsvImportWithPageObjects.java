/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvValueMappingsPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@Restore("xml/blankprojects.xml")
public class TestCsvImportWithPageObjects extends BaseJiraWebTest {

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-480
	 */
	@Test
	public void testAddConstants() {
		CsvSetupPage setupPage = jira.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/medium.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/existingCustomDateTime.properties");
		CsvValueMappingsPage valueMappingsPage = setupPage.next().next().next();

		assertEquals(5, valueMappingsPage.getAddConstantLinks().size());

		valueMappingsPage.addConstant("Priority", "normal");

		assertEquals(4, valueMappingsPage.getAddConstantLinks().size());
		assertEquals("6", valueMappingsPage.getValue("value.2"));
	}

}
