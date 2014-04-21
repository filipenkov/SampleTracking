/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedProjectMappingsPage;
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

public class TestFogBugzHosted extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;
	private String username;
	private String password;
	private String url;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		url = ITUtils.getProperties().getString("fogbugz.ondemand.siteUrl");
		username = ITUtils.getProperties().getString("fogbugz.ondemand.siteUsername");
		password = ITUtils.getProperties().getString("fogbugz.ondemand.sitePassword");

		assertNotNull("Please configure FogBugz Hosted credentials", url);
		assertNotNull("Please configure FogBugz Hosted credentials", username);
		assertNotNull("Please configure FogBugz Hosted credentials", password);

		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreBlankInstance();
		restClient = ITUtils.createRestClient(environmentData);
	}

	@Test
	public void testWizard() throws InterruptedException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		FogBugzHostedImporterSetupPage setupPage = getSetupPage();

		final FogBugzHostedProjectMappingsPage projectMappingsPage = setupPage.next();
		projectMappingsPage
				.createProject("Custom workflow", "Custom workflow", "CUS")
				.createProject("Inbox", "Inbox", "INB")
				.createProject("Sample Project", "Sample Project", "SAM");

		assertTrue(projectMappingsPage.nextFields().next().next().next().waitUntilFinished().isSuccess());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(13, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-3", new NullProgressMonitor());
		assertEquals("rmk's bug", issue.getSummary());
		assertEquals("4-Fix If Time", issue.getPriority().getName());
		assertEquals(ImmutableList.of("User Interface"), ImmutableList.copyOf(Iterables.transform(issue.getComponents(),
				new Function<BasicComponent, String>() {
					@Override
					public String apply(BasicComponent input) {
						return input.getName();
					}
				})));
	}

	private FogBugzHostedImporterSetupPage getSetupPage() {
		return product.gotoLoginPage().loginAsSysAdmin(FogBugzHostedImporterSetupPage.class).webSudo()
				.setSiteUrl(url)
				.setSiteUsername(username)
				.setSitePassword(password);
	}
}
