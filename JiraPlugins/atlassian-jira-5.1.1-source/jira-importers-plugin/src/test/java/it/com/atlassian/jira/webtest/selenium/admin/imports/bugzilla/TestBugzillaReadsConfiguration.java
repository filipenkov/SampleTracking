/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterCustomFieldsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.pageobjects.TestedProductFactory;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;

import java.io.File;

public class TestBugzillaReadsConfiguration extends FuncTestCase {
	private CommonImporterSetupPage setupPage;

	@Override
	public void setUpTest() {
		final JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreBlankInstance();
		setupPage = product.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class);
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);
		setupPage.setConfigFile(new File("src/test/resources/bugzilla/JIM-152.config").getAbsolutePath());
	}

	@Test
	public void testReadConfiguration() throws InterruptedException {
		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next();

		assertFalse(projectsMappingsPage.isProjectImported("A"));
		assertTrue(projectsMappingsPage.isProjectImported("TestProduct"));
		// we don't know mapped project name nor key at this stage

		final ImporterCustomFieldsPage customFieldsPage = projectsMappingsPage.next();
		assertEquals("OTHER_VALUE", customFieldsPage.getValue("cf_os_select"));
		assertEquals("OS", customFieldsPage.getValue("cf_os"));

		final ImporterFieldMappingsPage fieldMappingsPage = customFieldsPage.next();
		assertTrue(fieldMappingsPage.isMappingSelected("login_name"));

		final ImporterValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		assertEquals("USER4", valueMappingsPage.getMappingValue("login_name", "user4@example.com"));
		assertEquals("SELWOJ", valueMappingsPage.getMappingValue("login_name", "wseliga@atlassian.com"));

	}

}
