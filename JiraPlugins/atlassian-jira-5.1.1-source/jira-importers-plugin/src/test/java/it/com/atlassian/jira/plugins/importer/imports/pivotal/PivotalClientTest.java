/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.IterableMatcher;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.imports.pivotal.MockUserNameMapper;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalClient;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalRemoteException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PivotalClientTest {
	private final Configuration configuration = ITUtils.getProperties();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	private String username;
	private String password;

	@Before
	public void setUp() throws Exception {
		username = configuration.getString("pivotal.username");
		password = configuration.getString("pivotal.password");
	}

	@Test
	public void testGetAllProjectNamesFromPivotal() throws Exception {
		final PivotalClient pivotalClient = new PivotalClient(UserNameMapper.NO_MAPPING);
		pivotalClient.login(username, password);
		final Collection<String> allProjectNames = pivotalClient.getAllProjectNames();
		final String[] allProjects = configuration.getStringArray("pivotal.allProjects");
		Assert.assertThat(allProjectNames, IterableMatcher.hasOnlyElements(allProjects));
	}

	@Test
	public void testAuthenticationToAttachments() throws Exception {
		final PivotalClient pivotalClient = new PivotalClient(UserNameMapper.NO_MAPPING) {
			@Override
			protected File getTempDir() {
				return temporaryFolder.getRoot();
			}
		};
		pivotalClient.login(configuration.getString("pivotal.username"), configuration.getString("pivotal.password"));

		final ExternalAttachment attachment = new PivotalExternalAttachment("1.png", configuration.getString(
				"pivotal.attachmentUrl"), new Date());
		final ExternalIssue issue = new ExternalIssue();
		issue.setAttachments(Collections.singletonList(attachment));

		verifyAttachmentResult(pivotalClient.getAttachmentsForIssue(issue, new ConsoleImportLogger()));

		// retrying to see if the auth is re-entrant
		verifyAttachmentResult(pivotalClient.getAttachmentsForIssue(issue, new ConsoleImportLogger()));
	}

	private void verifyAttachmentResult(Collection<ExternalAttachment> result) {
		final File file = Iterables.getOnlyElement(result).getAttachment();
		Assert.assertTrue(file.exists());
		Assert.assertEquals(configuration.getInt("pivotal.attachmentSize"), file.length());
	}

	@Test
	public void testDownloadWorklog() throws Exception {
		final PivotalClient pivotalClient = new PivotalClient(new MockUserNameMapper("Test Member", "Translated Test Member"));
		pivotalClient.login(username, password);
		verifyWorklogResult(pivotalClient.getWorklog(280023, new ConsoleImportLogger()));

		final Collection<ExternalWorklog> emptyWorklog = pivotalClient.getWorklog(255199, new ConsoleImportLogger());
		Assert.assertTrue(emptyWorklog.size() == 0);

		// load again to verify authentication works as expected
		verifyWorklogResult(pivotalClient.getWorklog(280023, new ConsoleImportLogger()));

	}

	private void verifyWorklogResult(Collection<ExternalWorklog> worklog) throws Exception {
		Assert.assertEquals(2, worklog.size());
		verifyWorklog(Iterables.get(worklog, 0), "2011-04-21", "Translated Test Member", 8, "");
		verifyWorklog(Iterables.get(worklog, 1), "2011-04-22", "Translated Test Member", 6, "six hours");
	}

	private void verifyWorklog(ExternalWorklog externalWorklog, String date, String assignee, double time, String descr)
			throws Exception {
		Assert.assertEquals(new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse(date)).toInstant(), externalWorklog.getStartDate().toInstant());
		Assert.assertEquals(assignee, externalWorklog.getAuthor());
		Assert.assertEquals(time * 3600, externalWorklog.getTimeSpent().doubleValue(), 0.0001);
		Assert.assertEquals(descr, externalWorklog.getComment());

	}

	@Test
	public void testGetStories() throws Exception {
		implTestPagination(2);
		implTestPagination(1);
		implTestPagination(100);
	}

	private void implTestPagination(final int storyPaginationLimit) throws PivotalRemoteException {
		final PivotalClient pivotalClient = new PivotalClient(new MockUserNameMapper("Test Member", "Translated Test Member"));
		pivotalClient.login(username, password);
		pivotalClient.setStoryPaginationLimit(storyPaginationLimit);
		final Collection<ExternalProject> allProjects = pivotalClient.getAllProjects(ConsoleImportLogger.INSTANCE);
		final ExternalProject sampleProject = Iterables.find(allProjects, new Predicate<ExternalProject>() {
			@Override
			public boolean apply(ExternalProject input) {
				return "My Test Project".equals(input.getExternalName());
			}
		});
		final List<ExternalIssue> stories = pivotalClient.getStories(sampleProject.getId(), ConsoleImportLogger.INSTANCE);
		Assert.assertEquals(17, stories.size());
		pivotalClient.logout();
	}

}
