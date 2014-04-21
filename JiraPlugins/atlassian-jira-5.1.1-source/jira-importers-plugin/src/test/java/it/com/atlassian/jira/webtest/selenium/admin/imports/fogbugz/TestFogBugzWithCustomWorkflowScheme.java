/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.pageobjects.TestedProductFactory;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;

public class TestFogBugzWithCustomWorkflowScheme extends FuncTestCase {

	private JiraTestedProduct product;

	@Override
	public void setUpTest() {
		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreData("workflow-schemes.jira.xml");
	}

	@Test
	public void testWizard() throws InterruptedException {
		final CommonImporterSetupPage setupPage = product.gotoLoginPage().loginAsSysAdmin(FogBugzImporterSetupPage.class);
		ITUtils.setupConnection(setupPage, ITUtils.FOGBUGZ_7_3_6);
		setupPage.setConfigFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/fogbugz/JIM-408.config");

		final ImporterFieldMappingsPage fieldMappingsPage = setupPage
				.next()
				.next()
				.next();

		assertEquals("My Test Workflow Scheme", fieldMappingsPage.getWorkflowScheme());

		assertTrue(fieldMappingsPage.next().next().next().waitUntilFinished().isSuccess());
	}

}
