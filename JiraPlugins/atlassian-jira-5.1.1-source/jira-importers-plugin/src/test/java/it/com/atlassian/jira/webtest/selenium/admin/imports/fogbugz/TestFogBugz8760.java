/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFogBugz8760 extends BaseJiraWebTest {

	@Before
	public void setUpTest() {
		backdoor.restoreBlankInstance();
	}

	@Test
	public void testWizard() throws InterruptedException {
		TestFogBugzImporter.performImport(getSetupPage(), ITUtils.FOGBUGZ_8_7_60);

		final JiraRestClient restClient = ITUtils.createRestClient(jira.environmentData());
		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
		assertEquals("Testing importer", issue.getSummary());
		assertEquals("3-Must Fix", issue.getPriority().getName());
	}

	private CommonImporterSetupPage getSetupPage() {
		return jira.gotoLoginPage().loginAsSysAdmin(FogBugzImporterSetupPage.class);
	}
}
