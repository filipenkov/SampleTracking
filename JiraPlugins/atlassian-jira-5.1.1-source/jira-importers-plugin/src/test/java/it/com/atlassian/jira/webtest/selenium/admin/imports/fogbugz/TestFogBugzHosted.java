/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedProjectsMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestFogBugzHosted extends BaseJiraWebTest {

	protected static final Function<BasicComponent, String> NAME_FUNCTION = new Function<BasicComponent, String>() {
		@Override
		public String apply(BasicComponent input) {
			return input.getName();
		}
	};

	private JiraRestClient restClient;
	private String username;
	private String password;
	private String url;

	@Before
	public void setUpTest() {
		url = ITUtils.getProperties().getString("fogbugz.ondemand.siteUrl");
		username = ITUtils.getProperties().getString("fogbugz.ondemand.siteUsername");
		password = ITUtils.getProperties().getString("fogbugz.ondemand.sitePassword");

		assertNotNull("Please configure FogBugz Hosted credentials (URL)", url);
		assertNotNull("Please configure FogBugz Hosted credentials (username)", username);
		assertNotNull("Please configure FogBugz Hosted credentials (password)", password);

		backdoor.restoreBlankInstance();
		restClient = ITUtils.createRestClient(jira.environmentData());
	}

	@Test
	public void testWizard() throws InterruptedException {
		FogBugzHostedImporterSetupPage setupPage = getSetupPage();

		final FogBugzHostedProjectsMappingsPage projectsMappingsPage = setupPage.next();
		projectsMappingsPage
				.createProject("Custom workflow", "Custom workflow", "CUS")
				.createProject("Inbox", "Inbox", "INB")
				.createProject("Sample Project", "Sample Project", "SAM");

		assertTrue(projectsMappingsPage.nextFields().next().next().next().waitUntilFinished().isSuccess());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(13, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-3", new NullProgressMonitor());
		assertEquals("rmk's bug", issue.getSummary());
		assertEquals("4-Fix If Time", issue.getPriority().getName());
		assertEquals(ImmutableList.of("User Interface"),
				Immutables.transformThenCopyToList(issue.getComponents(), NAME_FUNCTION));
	}

	@Test
	public void testAgainstStandalone() {
		url = ITUtils.getProperties().getString("fogbugz.standalone.siteUrl");
		username = ITUtils.getProperties().getString("fogbugz.standalone.siteUsername");
		password = ITUtils.getProperties().getString("fogbugz.standalone.sitePassword");

		FogBugzHostedImporterSetupPage setupPage = getSetupPage();

		final FogBugzHostedProjectsMappingsPage projectsMappingsPage = setupPage.next();
		projectsMappingsPage
				.createProject("Inbox", "Inbox", "INB")
				.createProject("Sample Project", "Sample Project", "SAM");

		assertTrue(projectsMappingsPage.nextFields().next().next().next().waitUntilFinished().isSuccess());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-3", new NullProgressMonitor());
		assertEquals("Attachments", issue.getSummary());
		assertEquals("3-Must Fix", issue.getPriority().getName());
		assertEquals(ImmutableList.of("Code"),
				Immutables.transformThenCopyToList(issue.getComponents(), NAME_FUNCTION));
		FogbugzITUtil.verifyComponentLeadImported(restClient);
	}

	private FogBugzHostedImporterSetupPage getSetupPage() {
		return jira.gotoLoginPage().loginAsSysAdmin(FogBugzHostedImporterSetupPage.class)
				.setSiteUrl(url)
				.setSiteUsername(username)
				.setSitePassword(password);
	}
}
