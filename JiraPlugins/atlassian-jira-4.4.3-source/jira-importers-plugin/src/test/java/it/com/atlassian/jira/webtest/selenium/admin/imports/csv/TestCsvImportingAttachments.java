/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

public class TestCsvImportingAttachments extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;
	private final NullProgressMonitor pm = new NullProgressMonitor();

	@Before
	public void setUpTest() {
		administration.restoreBlankInstance();
		administration.attachments().enable();

		ITUtils.doWebSudoCrap(navigation, tester);
		restClient = ITUtils.createRestClient(environmentData);

		product = TestedProductFactory.create(JiraTestedProduct.class);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-237
	 */
	@Test
	public void testDownloadAttachments() {
		ITUtils.doCsvImport(product,
				"src/test/resources/csv/JIM-237.csv", "src/test/resources/csv/JIM-237.config");

		Issue issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"HTTPS\"",
						pm).getIssues().iterator().next().getKey(), pm);
		List<Attachment> attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(1, attachments.size());

		issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"Multiple\"",
						pm).getIssues().iterator().next().getKey(), pm);
		attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(ImmutableList.of(
				"1716530418_ceb0d8c2a9_z.jpg", "2446204823_f8d412dda5_z.jpg",
				"3687761465_edc670eac0_z.jpg"), ImmutableList.copyOf(
				Collections2.transform(attachments, new Function<Attachment, String>() {
					@Override
					public String apply(@Nullable Attachment input) {
						return input.getFilename();
					}
				})));

		issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"Local\"",
						pm).getIssues().iterator().next().getKey(), pm);
		attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(0, attachments.size());

		issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"issue\"",
						pm).getIssues().iterator().next().getKey(), pm);
		attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(0, attachments.size());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-237
	 */
	@Test
	public void testErrorMappingFieldToAttachmentIfJiraHasAttachmentsDisabled() {
		administration.attachments().disable();

		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();
		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-237.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-237.config");

		assertEquals(ImmutableList.of("You can't import attachments because they are disabled in JIRA.\nAttachments can be enabled here."),
				setupPage.next().next().nextWithError().getGlobalErrors());
	}

}
