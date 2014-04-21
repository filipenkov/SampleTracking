/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMantis118 extends BaseJiraWebTest {

	private JiraRestClient restClient;

	@Before
	public void setUpTest() {
		backdoor.restoreBlankInstance();
		backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

		restClient = ITUtils.createRestClient(jira.environmentData());
	}

	/**
	 * Smoke test for Mantis 1.1.8
	 */
	@Test
	public void testImport() {
        final int expectedIssues = 6;

		MantisImporterSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(MantisImporterSetupPage.class);

		ITUtils.setupConnection(setupPage, ITUtils.MANTIS_1_1_8);

		ImporterFinishedPage logsPage = setupPage.next()
				.createProject("Mantis project", "Mantis project", "MAN")
				.createProject("Test", "Test", "TES")
				.next().next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccessWithNoWarnings());
		assertEquals("2", logsPage.getProjectsImported());
		assertEquals(Integer.toString(expectedIssues), logsPage.getIssuesImported());

		SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(expectedIssues, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MAN-1",
				new NullProgressMonitor());
		assertEquals("External", issue.getComponents().iterator().next().getName());
		assertEquals("Mantis project bug", issue.getSummary());
		assertEquals("root", issue.getReporter().getDisplayName());
		assertNull(issue.getAssignee());
		assertEquals("Open", issue.getStatus().getName());
		assertEquals("This is a test", issue.getDescription());

		issue = restClient.getIssueClient().getIssue("MAN-4", new NullProgressMonitor());
		assertTrue(Iterables.isEmpty(issue.getComponents()));

        Project project = restClient.getProjectClient().getProject("MAN", new NullProgressMonitor());
        Iterable<Version> versions = project.getVersions();
        assertNotNull(versions);
        assertEquals(2, Iterables.size(versions));
        assertEquals("0.1.0", Iterables.get(versions, 0).getName());
        assertTrue(Iterables.get(versions, 0).isReleased());
        assertEquals("0.2.0", Iterables.get(versions, 1).getName());
        assertTrue(Iterables.get(versions, 1).isReleased());
	}

}
