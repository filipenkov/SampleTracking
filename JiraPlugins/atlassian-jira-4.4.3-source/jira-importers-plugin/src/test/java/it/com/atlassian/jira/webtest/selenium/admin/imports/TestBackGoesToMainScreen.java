/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.plugins.importer.po.common.FrontPage;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class TestBackGoesToMainScreen extends FuncTestCase {
	private JiraTestedProduct product;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		product = TestedProductFactory.create(JiraTestedProduct.class);
	}

	@Test
	public void testWizard() throws InterruptedException {
		FrontPage frontPage = product.gotoLoginPage()
				.loginAsSysAdmin(FrontPage.class)
				.webSudo();

		for (String importer : ImmutableList.of("Bugzilla", "CSV", "FogBugz", "FogBugzHosted", "Mantis", "Pivotal", "Trac")) {
			frontPage = frontPage.clickImporter(importer).previous();
		}
	}

}
