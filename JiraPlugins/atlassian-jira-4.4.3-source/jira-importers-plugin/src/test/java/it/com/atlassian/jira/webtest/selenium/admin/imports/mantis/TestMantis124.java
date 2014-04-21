/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterCustomFieldsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;

import java.util.List;

public class TestMantis124 extends ScreenshotFuncTestCase {
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
	 * Smoke test for Mantis 1.2.4
	 */
	public void testImport() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		MantisImporterSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(MantisImporterSetupPage.class)
				.webSudo();

		ITUtils.setupConnection(setupPage, ITUtils.MANTIS_1_2_4);

		ImporterCustomFieldsPage customFieldsPage = setupPage.next()
				.createProject("Another test project ", "Another test project", "ANO")
				.createProject("Mts", "Mts", "MTS")
				.createProject("No global categories", "No global categories", "NOG")
				.next();
		customFieldsPage.selectFieldMapping("priority", "issue-field:priority");

		ImporterFinishedPage logsPage = customFieldsPage.next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("3", logsPage.getProjectsImported());
		assertEquals("8", logsPage.getIssuesImported());
		assertEquals("User named null not found. attaching to issue with currently logged in user instead", Iterables.getOnlyElement(
				logsPage.getWarnings()));

		SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(8, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("ANO-1", new NullProgressMonitor());
		assertNotNull(issue);
		Field labels = issue.getField(IssueFieldConstants.LABELS);
		assertNotNull(labels);
		assertEquals("[\"and_another\",\"another_tag\",\"mulit_word_tag\",\"ąśćó\\\"';-_:\\\"'_000+=?\\/.\\\\\\\\\\\\\\\\||!@#%^&*()\\\"]\"]", labels.getValue());

		issue = restClient.getIssueClient().getIssue("ANO-2", new NullProgressMonitor());
		List<Attachment> attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(1, attachments.size());
		Attachment attachment = attachments.get(0);
		assertEquals("edit.jpg", attachment.getFilename());
		assertEquals(22814, attachment.getSize());
		assertEquals("General", Iterables.getOnlyElement(issue.getComponents()).getName());

		issue = restClient.getIssueClient().getIssue("NOG-1", new NullProgressMonitor());
		assertEquals("Default local category", Iterables.getOnlyElement(issue.getComponents()).getName());
	}

}
