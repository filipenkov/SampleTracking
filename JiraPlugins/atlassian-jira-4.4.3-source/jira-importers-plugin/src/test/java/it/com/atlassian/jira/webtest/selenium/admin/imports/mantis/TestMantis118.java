/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;

public class TestMantis118 extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		administration.restoreBlankInstance();
		administration.attachments().enable();
		ITUtils.doWebSudoCrap(navigation, tester);

        product = TestedProductFactory.create(JiraTestedProduct.class);

		restClient = ITUtils.createRestClient(environmentData);
	}

	/**
	 * Smoke test for Mantis 1.1.8
	 */
	public void testImport() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		MantisImporterSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(MantisImporterSetupPage.class)
				.webSudo();

		ITUtils.setupConnection(setupPage, ITUtils.MANTIS_1_1_8);

		ImporterFinishedPage logsPage = setupPage.next()
				.createProject("Mantis project", "Mantis project", "MAN")
				.createProject("Test", "Test", "TES")
				.next().next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("2", logsPage.getProjectsImported());
		assertEquals("5", logsPage.getIssuesImported());
		assertEquals("User named null not found. attaching to issue with currently logged in user instead", Iterables
				.getOnlyElement(logsPage.getWarnings()));

		SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(5, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MAN-1",
				new NullProgressMonitor());
		assertEquals("External", issue.getComponents().iterator().next().getName());
		assertEquals("Mantis project bug", issue.getSummary());
		assertEquals("root", issue.getReporter().getDisplayName());
		assertNull(issue.getAssignee());
		assertEquals("Open", issue.getStatus().getName());
		assertEquals("This is a test", issue.getField(IssueFieldConstants.DESCRIPTION).getValue());
	}

}
