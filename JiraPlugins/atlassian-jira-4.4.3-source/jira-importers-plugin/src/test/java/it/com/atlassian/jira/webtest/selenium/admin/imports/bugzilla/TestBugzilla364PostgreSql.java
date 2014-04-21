/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestBugzilla364PostgreSql extends FuncTestCase {

	private JiraTestedProduct product;

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}
		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreBlankInstance();
		administration.attachments().enable();
	}

	@Test
	public void testWizard() throws InterruptedException, IOException {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final CommonImporterSetupPage setupPage = product.gotoLoginPage()
				.loginAsSysAdmin(BugzillaImporterSetupPage.class)
				.webSudo();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4_POSTGRESQL);
		final ImporterFinishedPage logsPage = setupPage
				.next()
				.createProject("TestProduct", "TestProduct", "TES")
				.next()
				.selectFieldMapping("priority", "issue-field:priority")
				.next()
				.next()
				.next()
				.next()
				.waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		assertEquals("6", logsPage.getIssuesImported());

		final JiraRestClient restClient = ITUtils.createRestClient(environmentData);
		final Issue issue = restClient.getIssueClient().getIssue("TES-1", new NullProgressMonitor());
		final List<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(2, attachments.size());
		assertEquals("JIM-188.csv", attachments.get(0).getFilename());
		assertEquals(182, attachments.get(0).getSize());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(6, search.getTotal());
	}
}
