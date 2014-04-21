/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Find;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestCsvImportingAttachments extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;
	private final NullProgressMonitor pm = new NullProgressMonitor();

	@Before
	public void setUpTest() {
		super.setUpTest();

		administration.restoreBlankInstance();
		administration.attachments().enable();

		ITUtils.doWebSudoCrap(navigation, tester);
		restClient = ITUtils.createRestClient(environmentData);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-237
	 */
	@Test
	public void testDownloadAttachments() {
        CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		final ImporterFinishedPage finishedPage = setupPage.setCsvFile(ITUtils.getCsvResource("JIM-237.csv"))
				.setConfigurationFile(ITUtils.getCsvResource("JIM-237.config")).next().next()
				.next().next().waitUntilFinished();
		assertTrue(finishedPage.isSuccess());
		Assert.assertThat(Iterables.getOnlyElement(finishedPage.getWarnings()),
				new Find("Attachment file not found or not readable, skipping: .*/etc/fstab"));

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

		CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);
		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-237.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-237.config");

		assertEquals(ImmutableList.of("You can't import attachments because they are disabled in JIRA.\nAttachments can be enabled here."),
				setupPage.next().next().nextWithError().getGlobalErrors());
	}


	@Test
	public void testDownloadAttachmentsWithFullInformation() {
		final CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		final ImporterFinishedPage finishedPage = setupPage.setCsvFile(ITUtils.getCsvResource("attachments-with-more-than-just-uri.csv"))
				.setConfigurationFile(ITUtils.getCsvResource("attachments-with-more-than-just-uri.config")).next().next()
				.next().next().waitUntilFinished();
		assertTrue(finishedPage.isSuccessWithNoWarnings());

		final Issue issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"HTTPS attachment with full info\"",
						pm).getIssues().iterator().next().getKey(), pm);
		final List<Attachment> attachments = Lists.newArrayList(issue.getAttachments());
		final Attachment attachment = Iterables.getOnlyElement(attachments);
		assertEquals("user", attachment.getAuthor().getName());
		assertEquals("anotherfilefromStAC.gif", attachment.getFilename());
		assertEquals(new org.joda.time.DateTime(2010, 11, 8, 12, 13, 14), attachment.getCreationDate());
	}

	@Test
	public void testRetrieveLocalAttachments() throws IOException {

		final File imports = new File(administration.getJiraHomeDirectory(), "import/attachments");
		final File subdir = new File(imports, "subdir");
		FileUtils.forceMkdir(subdir);
		final File defaultFile = new File(imports, "default-file.txt");
		final File subdirFile = new File(subdir, "subdir-file.txt");
		final String defaultFileContents = "default-file";
		final String subdirFileContents = "subdir-file-that-is-longer";
		FileUtils.writeStringToFile(defaultFile, defaultFileContents);
		FileUtils.writeStringToFile(subdirFile, subdirFileContents);

		final CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		final ImporterFinishedPage finishedPage = setupPage.setCsvFile(ITUtils.getCsvResource("attachments-with-local-paths.csv"))
				.setConfigurationFile(ITUtils.getCsvResource("attachments-with-local-paths.config")).next().next()
				.next().next().waitUntilFinished();
		assertTrue(finishedPage.isSuccess());
		assertEquals(ImmutableList.of("Imported attachment file is outside of permitted base directory, skipping: subdir/../../../dbconfig.xml"),
				finishedPage.getWarnings());

		assertTrue(defaultFile.exists()); // JIRA attachment creation moves files
		assertTrue(subdirFile.exists());

		final Issue issue = restClient.getIssueClient().getIssue(
				restClient.getSearchClient().searchJql("project=CSVI and summary ~ \"Local attachments\"",
						pm).getIssues().iterator().next().getKey(), pm);
		assertEquals("Trivial", issue.getPriority().getName());
		final List<Attachment> attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(4, attachments.size());
		final ImmutableMap<String, Attachment> byFileName = Maps.uniqueIndex(attachments, new Function<Attachment, String>() {
			@Override
			public String apply(Attachment input) {
				return input.getFilename();
			}
		});

		assertEquals(ImmutableSet.of("explicit-name.txt", "default-file.txt", "subdir-file.txt", "explicit-subdir.txt"),
				byFileName.keySet());

		assertEquals(defaultFileContents.length(), byFileName.get("explicit-name.txt").getSize());
		assertEquals(defaultFileContents.length(), byFileName.get("default-file.txt").getSize());
		assertEquals(subdirFileContents.length(), byFileName.get("subdir-file.txt").getSize());
		assertEquals(subdirFileContents.length(), byFileName.get("explicit-subdir.txt").getSize());
	}

}
