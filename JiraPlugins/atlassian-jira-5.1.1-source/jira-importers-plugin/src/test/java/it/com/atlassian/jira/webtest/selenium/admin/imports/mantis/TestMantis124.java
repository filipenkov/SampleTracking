/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.po.common.ImporterCustomFieldsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TestMantis124 extends BaseJiraWebTest {
	private JiraRestClient restClient;

    protected String instance;

	@Before
	public void setUpTest() {
		backdoor.restoreBlankInstance();
		backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        instance = ITUtils.MANTIS_1_2_4;

        restClient = ITUtils.createRestClient(jira.environmentData());
	}

	/**
	 * Smoke test for Mantis 1.2.4
	 */
	@Test
	public void testImport() {
		MantisImporterSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(MantisImporterSetupPage.class);

		ITUtils.setupConnection(setupPage, instance);

		ImporterCustomFieldsPage customFieldsPage = setupPage.next()
				.createProject("Another test project ", "Another test project", "ANO")
				.createProject("Mts", "Mts", "MTS")
				.createProject("No global categories", "No global categories", "NOG")
				.next();
		customFieldsPage.selectFieldMapping("priority", "issue-field:priority");

		ImporterFinishedPage logsPage = customFieldsPage.next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccessWithNoWarnings());
		assertEquals("3", logsPage.getProjectsImported());
		assertEquals("8", logsPage.getIssuesImported());

		SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(8, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("ANO-1", new NullProgressMonitor());
		assertNotNull(issue);
		Set<String> labels = issue.getLabels();
		assertNotNull(labels);
		assertEquals(ImmutableSet.of("another_tag", "and_another", "ąśćó\"';-_:\"'_000+=?\\/.\\\\\\\\||!@#%^&*()\"]", "mulit_word_tag"), labels);

		issue = restClient.getIssueClient().getIssue("ANO-2", new NullProgressMonitor());
		List<Attachment> attachments = Lists.newArrayList(issue.getAttachments());
		assertEquals(1, attachments.size());
		Attachment attachment = attachments.get(0);
		assertEquals("dialog.png", attachment.getFilename());
		assertEquals(15827, attachment.getSize());
		assertEquals("General", Iterables.getOnlyElement(issue.getComponents()).getName());

		issue = restClient.getIssueClient().getIssue("NOG-1", new NullProgressMonitor());
		assertEquals("Default local category", Iterables.getOnlyElement(issue.getComponents()).getName());

		final Project project = restClient.getProjectClient().getProject("NOG", new NullProgressMonitor());
		assertEquals(ImmutableList.of("Default local category|administrator"),
				Immutables.transformThenCopyToList(project.getComponents(), new Function<BasicComponent, String>() {
					@Override
					public String apply(@Nullable BasicComponent input) {
						final BasicUser lead = restClient.getComponentClient().getComponent(
								input.getSelf(), new NullProgressMonitor()).getLead();
						return input.getName() + "|" + (lead != null ? lead.getName() : "");
					}
				}));

		final Project mtsProject = restClient.getProjectClient().getProject("MTS", new NullProgressMonitor());
		assertEquals(ImmutableList.of("General|", "Global category|"),
				Immutables.transformThenCopyToList(mtsProject.getComponents(), new Function<BasicComponent, String>() {
					@Override
					public String apply(@Nullable BasicComponent input) {
						final BasicUser lead = restClient.getComponentClient().getComponent(
								input.getSelf(), new NullProgressMonitor()).getLead();
						return input.getName() + "|" + (lead != null ? lead.getName() : "");
					}
				}));
	}



}
