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
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestFogBugzImporter extends BaseJiraWebTest {

	@Before
	public void setUpTest() {
		backdoor.restoreBlankInstance();
	}

	@Test
	public void testWizard() throws InterruptedException {
		performImport(getSetupPage(), ITUtils.FOGBUGZ_7_3_6);

		final JiraRestClient restClient = ITUtils.createRestClient(jira.environmentData());
		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(62, result.getTotal());

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
		FogbugzITUtil.verifyComponentLeadImported(restClient);
	}

	@Test
	public void testWizard8() throws InterruptedException {
		performImport(getSetupPage(), ITUtils.FOGBUGZ_8);

		final JiraRestClient restClient = ITUtils.createRestClient(jira.environmentData());
		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, result.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
		assertEquals("This is a very long case summary with a lot of bla bla bla, see how long it goes lfsdljlllllllljjlljdlfjsldjflks jlksdjlkfjsdEND", issue.getSummary());
		assertEquals("3-Must Fix", issue.getPriority().getName());
		assertNull(issue.getResolution());
		assertEquals(ImmutableList.of("Code"), ImmutableList.copyOf(Iterables.transform(issue.getComponents(),
				new Function<BasicComponent, String>() {
					@Override
					public String apply(BasicComponent input) {
						return input.getName();
					}
				})));

		final Issue issue3 = restClient.getIssueClient().getIssue("SAM-2", new NullProgressMonitor());
		assertEquals("By Design", issue3.getResolution().getName());

		FogbugzITUtil.verifyComponentLeadImported(restClient);
	}

	public static void performImport(CommonImporterSetupPage setupPage, String systemName) {
		ITUtils.setupConnection(setupPage, systemName);

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

		valueMappingsPage.setMappingSelect("sCategory", "Bug", "Improvement");
		valueMappingsPage.setMappingSelect("sCategory", "Inquiry", "Task");

		assertEquals("", valueMappingsPage.getMappingValue("sFullName", "Wojtek Seliga"));

		assertTrue(valueMappingsPage.next().next().waitUntilFinished().isSuccess());
	}

	/**
	 * Test if priority mapping works.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testCheckPriorityMappingWorks() throws InterruptedException {
		CommonImporterSetupPage setupPage = getSetupPage();

		ITUtils.setupConnection(setupPage, ITUtils.FOGBUGZ_7_3_6);
		setupPage.setConfigFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/fogbugz/JIM-507.config");

		assertTrue(setupPage.next().next().next().next()
				.next().next().waitUntilFinished().isSuccess());

		final JiraRestClient restClient = ITUtils.createRestClient(jira.environmentData());
		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(62, result.getTotal());

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
		return jira.gotoLoginPage().loginAsSysAdmin(FogBugzImporterSetupPage.class);
	}
}
