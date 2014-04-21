/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.IterableMatcher;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalClient;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalExternalAttachment;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.configuration.Configuration;
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
		final PivotalClient pivotalClient = new PivotalClient();
		pivotalClient.login(username, password);
		final Collection<String> allProjectNames = pivotalClient.getAllProjectNames();
		final String[] allProjects = configuration.getStringArray("pivotal.allProjects");
		Assert.assertThat(allProjectNames, IterableMatcher.hasOnlyElements(allProjects));
	}

	@Test
	public void testAuthenticationToAttachments() throws Exception {
		final PivotalClient pivotalClient = new PivotalClient() {
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

		final Collection<ExternalAttachment> result = pivotalClient.getAttachmentsForIssue(issue, new ConsoleImportLogger());
		final File file = Iterables.getOnlyElement(result).getAttachedFile();
		Assert.assertTrue(file.exists());
		Assert.assertEquals(configuration.getInt("pivotal.attachmentSize"), file.length());
	}

	@Test
	public void testDownloadWorklog() throws Exception {
		final PivotalClient pivotalClient = new PivotalClient();
		pivotalClient.login(username, password);
		final Collection<ExternalWorklog> worklog = pivotalClient.getWorklog(280023, new ConsoleImportLogger());
		Assert.assertEquals(2, worklog.size());
		verifyWorklog(Iterables.get(worklog, 0), "2011-04-21", "Test Member", 8, "");
		verifyWorklog(Iterables.get(worklog, 1), "2011-04-22", "Test Member", 6, "six hours");

		final Collection<ExternalWorklog> emptyWorklog = pivotalClient.getWorklog(255199, new ConsoleImportLogger());
		Assert.assertTrue(emptyWorklog.size() == 0);
	}

	private void verifyWorklog(ExternalWorklog externalWorklog, String date, String assignee, double time, String descr)
			throws Exception {
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse(date), externalWorklog.getStartDate());
		Assert.assertEquals(assignee, externalWorklog.getAuthor());
		Assert.assertEquals(time * 3600, externalWorklog.getTimeSpent().doubleValue(), 0.0001);
		Assert.assertEquals(descr, externalWorklog.getComment());

	}
}
