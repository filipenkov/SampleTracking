/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import junitx.framework.StringAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBugzilla220 extends BaseJiraWebTest {

	@Before
	public void setUpTest() {
        backdoor.restoreBlankInstance();
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
	}

	@Test
	public void testWizard() throws InterruptedException {
		final CommonImporterSetupPage setupPage = jira.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class);
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_2_20);

		final ImporterFinishedPage logsPage = setupPage
				.next()
				.createProject("ABC Testing", "B", "ABC")
				.createProject("Another Testing Product", "C", "CCC")
				.createProject("Test Product from 2.20", "D", "DDD")
				.next()
				.selectFieldMapping("priority", "issue-field:priority")
				.next()
				.next()
				.next()
				.next()
				.waitUntilFinished();

		assertTrue(logsPage.isSuccess());

		final JiraRestClient restClient = ITUtils.createRestClient(jira.environmentData());
		final Issue issue = restClient.getIssueClient().getIssue("ABC-2", new NullProgressMonitor());
		final List<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(2, attachments.size());
		assertEquals("MANIFEST.MF", attachments.get(1).getFilename());
		assertEquals(389, attachments.get(1).getSize());
		assertEquals("build.properties", attachments.get(0).getFilename());
		assertEquals(686, attachments.get(0).getSize());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(4, search.getTotal());
	}

	@Test
	public void testReimport() {
		CommonImporterSetupPage setupPage = jira.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class);
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_2_20);

		ImporterFinishedPage logsPage = setupPage
				.next()
				.createProject("ABC Testing", "B", "ABC")
				.createProject("Another Testing Product", "Another", "CCC")
				.createProject("Test Product from 2.20", "Test Product", "TES")
				.next()
				.next()
				.next()
				.next()
				.next()
				.waitUntilFinished();

		assertEquals("4", logsPage.getIssuesImported());

		setupPage = jira.visit(BugzillaImporterSetupPage.class);

		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_2_20);

		final ImporterProjectsMappingsPage mappingsPage = setupPage.next();
		Assert.assertTrue(mappingsPage.setProject("ABC Testing", "B"));
		Assert.assertTrue(mappingsPage.setProject("Another Testing Product", "Another"));
		Assert.assertTrue(mappingsPage.setProject("Test Product from 2.20", "Test Product"));

		logsPage = mappingsPage
				.next()
				.next()
				.next()
				.next()
				.next()
				.waitUntilFinished();
		assertEquals("4 of 4 issues have been skipped because they already exist in destination projects.",
				Iterables.getFirst(logsPage.getWarnings(), null));

		String log = logsPage.getLog();
		StringAssert.assertContains("External issue 1 already exists as ABC-1, not importing.", log);
		StringAssert.assertContains("External issue 2 already exists as ABC-2, not importing.", log);
		StringAssert.assertContains("External issue 3 already exists as TES-1, not importing.", log);

		setupPage = jira.visit(BugzillaImporterSetupPage.class);

		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_2_20);

		logsPage = setupPage
				.next()
				.createProject("ABC Testing", "Different ABC", "DIFFABC")
				.createProject("Another Testing Product", "Different Another", "DIFFCCC")
				.createProject("Test Product from 2.20", "Different Test Product", "DIFFDDD")
				.next()
				.next()
				.next()
				.next()
				.next()
				.waitUntilFinished();

		assertEquals("4", logsPage.getIssuesImported());
	}

}
