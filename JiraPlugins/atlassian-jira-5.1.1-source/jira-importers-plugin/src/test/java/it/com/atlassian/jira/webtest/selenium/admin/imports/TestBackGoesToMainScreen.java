/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.plugins.importer.po.common.FrontPage;
import com.atlassian.pageobjects.TestedProductFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class TestBackGoesToMainScreen extends FuncTestCase {
	private JiraTestedProduct product;

	@Override
	public void setUpTest() {
		product = TestedProductFactory.create(JiraTestedProduct.class);
	}

	@Test
	public void testWizard() throws InterruptedException {
		FrontPage frontPage = product.gotoLoginPage().loginAsSysAdmin(FrontPage.class);

		for (String importer : ImmutableList.of("com.atlassian.jira.plugins.jira-importers-plugin:bugzillaImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:csvImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:fogbugzImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:fogbugzOnDemandImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:mantisImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:pivotalTrackerImporter",
				"com.atlassian.jira.plugins.jira-importers-plugin:tracImporter",
                "com.atlassian.jira.plugins.jira-importers-plugin:jsonImporter")) {
			frontPage = frontPage.clickImporter(importer).previous();
		}
	}

}
