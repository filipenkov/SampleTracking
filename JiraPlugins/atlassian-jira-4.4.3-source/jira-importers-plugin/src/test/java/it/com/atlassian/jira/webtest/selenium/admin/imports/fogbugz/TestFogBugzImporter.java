/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.junit.Test;

public class TestFogBugzImporter extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreBlankInstance();
		restClient = ITUtils.createRestClient(environmentData);
	}

	@Test
	public void testWizard() throws InterruptedException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		CommonImporterSetupPage setupPage = getSetupPage();

		ITUtils.setupConnection(setupPage, ITUtils.FOGBUGZ_7_3_6);

		ImporterProjectsMappingsPage projectMappingPage = setupPage
				.next()
				.setImportAllProjects(false)
				.setProjectImported("Inbox", true)
				.createProject("Inbox", "Inbox", "INB")
				.setProjectImported("Sample Project", true)
				.createProject("Sample Project", "Sample Project", "SAM");

		ImporterFieldMappingsPage fieldMappingsPage = projectMappingPage.next().next();

		fieldMappingsPage.setCheckbox("sPriority", true);
		fieldMappingsPage.setCheckbox("sFullName", true);
		fieldMappingsPage.setCheckbox("sCategory", true);

		ImporterValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		valueMappingsPage.setMappingSelect("sCategory", "Bug", "3");
		valueMappingsPage.setMappingSelect("sCategory", "Inquiry", "1");

		assertEquals("", valueMappingsPage.getMappingValue("sFullName", "Pawel Niewiadomski"));

		assertTrue(valueMappingsPage.next().next().waitUntilFinished().isSuccess());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(63, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
		assertEquals("Wojtek's case for testing import", issue.getSummary());
		assertEquals("1-Must Fix", issue.getPriority().getName());
		assertEquals("Fixed", issue.getResolution().getName());
		assertEquals(ImmutableList.of("Miscellaneous"), ImmutableList.copyOf(Iterables.transform(issue.getComponents(),
				new Function<BasicComponent, String>() {
					@Override
					public String apply(BasicComponent input) {
						return input.getName();
					}
				})));
	}

	/**
	 * Test if priority mapping works.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testCheckPriorityMappingWorks() throws InterruptedException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		CommonImporterSetupPage setupPage = getSetupPage();

		ITUtils.setupConnection(setupPage, ITUtils.FOGBUGZ_7_3_6);
		setupPage.setConfigFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/fogbugz/JIM-507.config");

		assertTrue(setupPage.next().next().next().next()
				.next().next().waitUntilFinished().isSuccess());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(63, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
		assertEquals("Wojtek's case for testing import", issue.getSummary());
		assertEquals("Critical", issue.getPriority().getName());
		assertEquals("Fixed", issue.getResolution().getName());
		assertEquals(ImmutableList.of("Miscellaneous"), ImmutableList.copyOf(Iterables.transform(issue.getComponents(),
				new Function<BasicComponent, String>() {
					@Override
					public String apply(BasicComponent input) {
						return input.getName();
					}
				})));
	}

	private CommonImporterSetupPage getSetupPage() {
		return product.gotoLoginPage().loginAsSysAdmin(FogBugzImporterSetupPage.class).webSudo();
	}
}
