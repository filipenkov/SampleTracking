/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;


import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.plugins.importer.DateTimeMatcher;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class TestCsvImportWorklogs extends ScreenshotFuncTestCase {
	private JiraRestClient restClient;

	@Override
	protected void setUpTest() {
		super.setUpTest();
		URI jiraServerUri;
		try {
			jiraServerUri = environmentData.getBaseUrl().toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "admin", "admin");
	}

	public void testImportWorklogs() throws Exception {
		administration.timeTracking().enable(TimeTracking.Mode.MODERN);
		if (administration.project().projectExists("ABC Test")) {
			administration.project().deleteProject("ABC Test");
		}
		if (!administration.usersAndGroups().userExists("wseliga")) {
			administration.usersAndGroups().addUser("wseliga"); // this is necessary until JIM creates users seen in worklogs only
		}

		final ImporterFinishedPage finishedPage = doImport("misc/import-with-worklog.csv", "misc/import-with-worklog.cfg");
		assertTrue(finishedPage.isSuccessWithNoWarnings());

		final Issue issueA = restClient.getIssueClient().getIssue("ABC-1", new NullProgressMonitor());
		assertEquals("A", issueA.getSummary());

		final Iterable<Worklog> worklogsA = issueA.getWorklogs();
		assertEquals(1, Iterables.size(worklogsA));
		final Worklog worklogA = Iterables.getOnlyElement(worklogsA);
		assertEquals(1, worklogA.getMinutesSpent());

		final Issue issueB = restClient.getIssueClient().getIssue("ABC-2", new NullProgressMonitor());
		assertEquals("B", issueB.getSummary());

		final Iterable<Worklog> worklogsB = issueB.getWorklogs();
		assertEquals(1, Iterables.size(worklogsB));
		final Worklog worklogB = Iterables.getOnlyElement(worklogsB);
		assertEquals(100, worklogB.getMinutesSpent());
		assertNull(worklogB.getAuthor());

		final Issue issueD = restClient.getIssueClient().getIssue("ABC-4", new NullProgressMonitor());
		assertEquals("D", issueD.getSummary());

		assertEquals(2, Iterables.size(issueD.getWorklogs()));
		final Iterator<Worklog> worklogD = issueD.getWorklogs().iterator();

		verifyWorklog(worklogD.next(), 2, new DateTime(2012, 02, 10, 12, 30, 10, 00), "no comment");
		verifyWorklog(worklogD.next(), 60, null, "no comment");


		final Issue issueE = restClient.getIssueClient().getIssue("ABC-5", new NullProgressMonitor());
		assertEquals("E", issueE.getSummary());

		assertEquals(3, Iterables.size(issueE.getWorklogs()));
		final Iterator<Worklog> worklogE = issueE.getWorklogs().iterator();

		verifyWorklog(worklogE.next(), 3, new DateTime(2012, 02, 11, 12, 30, 10, 00), "With comment");
		verifyWorklog(worklogE.next(), 4, new DateTime(2012, 02, 12, 12, 30, 10, 00), "With; semicolon");
		verifyWorklog(worklogE.next(), 5, null, "With; semicolons; no; date;");


	}

	private void verifyWorklog(Worklog worklog, int minutes, DateTime startDate, String comment) {
		assertEquals(minutes, worklog.getMinutesSpent());
		if (startDate == null) {
			Assert.assertThat(worklog.getStartDate(), DateTimeMatcher.ago(Period.ZERO, 60));
		} else {
			assertEquals(startDate, worklog.getStartDate());
		}
		assertEquals(comment, worklog.getComment());
	}

	private ImporterFinishedPage doImport(String csv, String config) {
		CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		setupPage.setCsvFile(ITUtils.getCsvResource(csv));
		setupPage.setConfigurationFile(ITUtils.getCsvResource(config));
		return setupPage.next().next().next().next().waitUntilFinished();
	}

}
