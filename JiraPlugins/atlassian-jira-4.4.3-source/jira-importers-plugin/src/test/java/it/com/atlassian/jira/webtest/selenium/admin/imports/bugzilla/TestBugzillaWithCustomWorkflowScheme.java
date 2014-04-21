/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

public class TestBugzillaWithCustomWorkflowScheme extends FuncTestCase {

	private JiraTestedProduct product;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreData("workflow-schemes.jira.xml");
	}

	@Test
	public void testWizard() throws InterruptedException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final CommonImporterSetupPage setupPage = product.gotoLoginPage()
				.loginAsSysAdmin(BugzillaImporterSetupPage.class)
				.webSudo();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		ImporterFieldMappingsPage fieldMappingsPage = setupPage
				.next()
				.createProject("A", "A", "AAA")
				.createProject("Invalid component id", "Invalid component id", "INV")
				.createProject("TestProduct", "TestProduct", "TES")
				.next()
				.selectFieldMapping("priority", "issue-field:priority")
				.next();

		fieldMappingsPage.setWorkflowScheme("My Test Workflow Scheme");

		ImporterValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		assertEquals("1", valueMappingsPage.getMappingValue("bug_status", "ASSIGNED"));

		List<WebElement> options = valueMappingsPage.getSelectOptions(valueMappingsPage.getMappingId("bug_status", "NEW"));
		assertEquals(2, options.size());
		assertEquals(ImmutableList.of("Open", "In Progress"), ImmutableList.copyOf(Collections2.transform(options, new Function<WebElement, String>() {
			@Override
			public String apply(WebElement input) {
				return input.getText();
			}
		})));

	}

}
