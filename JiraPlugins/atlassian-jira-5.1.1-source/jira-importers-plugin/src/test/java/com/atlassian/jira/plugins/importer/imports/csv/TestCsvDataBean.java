/*
 * Copyright (C) 2002-2012 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.config.util.AbstractJiraHome;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.StaticProjectMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TestCsvDataBean {

	@Mock
	private CustomFieldManager customFieldManager;
	@Mock
	private ProjectManager projectManager;

	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils utils;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private AttachmentManager attachmentManager;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private IssueManager issueManager;

	@Mock
	private JiraAuthenticationContext authenticationContext;
	@Mock
	private ImportLogger log;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private OutlookDate outlookDate;

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private JiraHome jiraHome = new AbstractJiraHome() {
		@Override
		public File getHome() {
			return temporaryFolder.getRoot();
		}
	};


	@Before
    public void createMocks() {
		MockitoAnnotations.initMocks(this);
		when(utils.getAuthenticationContext()).thenReturn(authenticationContext);
		when(authenticationContext.getOutlookDate()).thenReturn(outlookDate);
		when(outlookDate.formatDateTimePicker((Date) any())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				return invocationOnMock.getArguments()[0].toString();
			}
		});
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-69
	 *
	 * If CSV include project data including project lead, the lead should be returned by getRequiredUsers.
	 */
	@Test
	public void testProjectLeadIsInUsers() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-69.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-69.config"));

		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);
		CsvConfiguration csvConfig = new CsvConfiguration(configBean, projectManager);

		cdb.setUserMappers(csvConfig.getCustomUserMappers());

		Collection<String> users = Collections2.transform(cdb.getRequiredUsers(
				Collections.<ExternalProject>emptyList(), new ConsoleImportLogger()), new Function<ExternalUser, String>() {

			@Override
			public String apply(ExternalUser externalUser) {
				return externalUser.getFullname();
			}
		});

		assertEquals(3, users.size());
		assertTrue(users.contains("testuser"));
		assertTrue(users.contains("testuser2"));
		assertTrue(users.contains("halo"));
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-360
	 *
	 * Test if comments read from CSV file are ordered the same way they are in CSV.
	 */
	@Test
	public void testCommentsOrder() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-360.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-360.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		ExternalIssue issue = cdb.getIssues(project, log).iterator().next();

		assertNotNull(issue);
		List<ExternalComment> comments = issue.getComments();
		assertNotNull(comments);
		assertEquals(7, comments.size());

		// make sure order is the same as in CSV
		assertTrue(comments.get(0).getBody().contains("This is comment"));
		assertTrue(comments.get(1).getBody().contains("Comment2"));
		assertTrue(comments.get(2).getBody().endsWith("Comment3"));
		assertTrue(comments.get(3).getBody().endsWith("Comment34"));
		assertTrue(comments.get(4).getBody().endsWith("Comment3"));
		assertTrue(comments.get(5).getBody().endsWith("asjdhskjdhskdfujhksud"));
		assertTrue(comments.get(6).getBody().endsWith("34"));
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-468
	 */
	@Test
	public void testCommentsHaveDateAndAuthor() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-464.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-464.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		ExternalIssue issue = cdb.getIssues(project, log).iterator().next();

		assertNotNull(issue);
		List<ExternalComment> comments = issue.getComments();
		assertNotNull(comments);
		assertEquals(7, comments.size());

		// make sure order is the same as in CSV
		assertEquals("Comment; Adam; 05/05/2010 10:20:30 AM; This is comment", comments.get(0).getBody());
		assertNull(comments.get(0).getCreated());
		assertNull(comments.get(0).getAuthor());

		assertEquals("Comment2", comments.get(1).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(1).getCreated());
		assertEquals("adam", comments.get(1).getAuthor());

		assertEquals("Comment3", comments.get(2).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(2).getCreated());
		assertEquals("zosia", comments.get(2).getAuthor());

		assertEquals("Comment34", comments.get(3).getBody());
		assertNull(comments.get(3).getCreated());
		assertNull(comments.get(3).getAuthor());

		assertEquals("Comment3", comments.get(4).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(4).getCreated());
		assertNull(comments.get(4).getAuthor());

		assertEquals("asjdh;skjdhskdfujh;ksud", comments.get(5).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(5).getCreated());
		assertEquals("adam", comments.get(5).getAuthor());

		assertEquals("34", comments.get(6).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(6).getCreated());
		assertEquals("adam", comments.get(6).getAuthor());
	}

	@Test
	public void testMultilineComments() throws Exception {
		final File csv = new File("src/test/resources/csv/multiline-comments.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/multiline-comments.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		final ImmutableList<ExternalIssue> issues = ImmutableList.copyOf(cdb.getIssues(project, log));
		assertEquals("This comment works", issues.get(0).getComments().get(0).getBody());
		assertEquals("This comment\nfails", issues.get(1).getComments().get(0).getBody());
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-369
	 *
	 * Test if issues read from CSV file are ordered the same way they are in CSV.
	 */
	@Test
	public void testIssuesOrder() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-369.csv");

		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-369.config"));

		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		ImmutableList<String> issues = ImmutableList.copyOf(Collections2.transform(cdb.getIssues(project, log),
				new Function<ExternalIssue, String>() {
					@Override
					public String apply(ExternalIssue input) {
						return input.getSummary();
					}
				}));
		assertEquals(13, issues.size());
		assertEquals(ImmutableList.of("abc 1", "abc 1", "abc 2", "abc 2", "abc 1", "xcvxcv", "xcb", "wer3",
				"xcvb", "345", "23", "12212", "sdf"), issues);
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-654
	 *
	 * Test if paren/sub-task links read from CSV file are ordered the same way they are in CSV.
	 */
	@Test
	public void testSubtasksOrder() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-654.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-654.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager, jiraHome, issueManager, attachmentManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		final Map<String, String> subtasks = Maps.newLinkedHashMap();
		for(ExternalIssue issue : cdb.getIssues(project, log)) {
			subtasks.put(issue.getExternalId(), issue.getSummary());
		}

		Collection<ExternalLink> links = cdb.getLinks(log);
		List<String> expectedOrder = Lists.newArrayList("Sub-task 01", "Sub-task 02", "Sub-task 03", "Sub-task 04", "Sub-task 05", "Sub-task 06", "Sub-task 07", "Sub-task 08");

		assertNotNull(links);
		assertEquals(expectedOrder.size(), links.size());
		for(int i = 0, s = links.size(); i < s; ++i) {
			assertEquals(expectedOrder.get(i), subtasks.get(Iterables.get(links, i).getSourceId()));
		}
	}

	@Test
	public void testRetrievingAttachmentsFromLocalFolder() throws Exception {
		final File home = temporaryFolder.getRoot();
		final File importDir = new File(new File(home, "import"), "attachments");
		final File subdir = new File(importDir, "subdir");
		FileUtils.forceMkdir(subdir);

		final File f1 = new File(importDir, "f1");
		final File f2 = new File(subdir, "f2");
		FileUtils.writeStringToFile(f1, "contents1");
		FileUtils.writeStringToFile(f2, "contents2");

		final CsvDataBean csvDataBean = new CsvDataBean(Mockito.mock(CsvConfigBean.class), customFieldManager, jiraHome, issueManager, attachmentManager);
		final List<String> validUrls = ImmutableList.of(
				"file:f1",
				"f1.txt;file:/f1",
				"user;;file://f1",
				"user;f1.txt;file:///f1",
				"file:subdir/f2",
				"file:/subdir/f2",
				"file://subdir/f2",
				"file:///subdir/f2");

		final Collection<ExternalAttachment> attachments = csvDataBean.getAttachmentsForIssue(Mockito
				.mock(ExternalIssue.class), validUrls, log);
		Mockito.verifyZeroInteractions(log);
		assertEquals(validUrls.size(), attachments.size());
		final Iterator<ExternalAttachment> iterator = attachments.iterator();
		verifyAttachment(null, "f1", "contents1", iterator.next());
		verifyAttachment(null, "f1.txt", "contents1", iterator.next());
		verifyAttachment("user", "f1", "contents1", iterator.next());
		verifyAttachment("user", "f1.txt", "contents1", iterator.next());
		verifyAttachment(null, "f2", "contents2", iterator.next());
		verifyAttachment(null, "f2", "contents2", iterator.next());
		verifyAttachment(null, "f2", "contents2", iterator.next());
		verifyAttachment(null, "f2", "contents2", iterator.next());
	}

	@Test
	public void testRetrievingAttachmentsFromLocalFolderWithInvalidPaths() throws Exception {
		final File home = temporaryFolder.getRoot();
		final File importDir = new File(new File(home, "import"), "attachments");
		final File subdir = new File(importDir, "subdir");
		FileUtils.forceMkdir(subdir);

		final File f1 = new File(importDir, "f1");
		final File f2 = new File(subdir, "f2");
		FileUtils.writeStringToFile(f1, "contents");
		FileUtils.writeStringToFile(f2, "contents");

		final CsvDataBean csvDataBean = new CsvDataBean(Mockito.mock(CsvConfigBean.class), customFieldManager, jiraHome, issueManager, attachmentManager);
		final List<String> invalidUrls = ImmutableList.of(
				"file:../f1",
				"f1.txt;file:../f1",
				"user;;file://../../f1",
				"user;f1.txt;file:///file/../../f1",
				"file:subdir/../../f2",
				"file:valid-but-absent.txt");

		final Collection<ExternalAttachment> attachments = csvDataBean.getAttachmentsForIssue(Mockito
				.mock(ExternalIssue.class), invalidUrls, log);
		assertEquals(Collections.emptyList(), attachments);
	}

	private void verifyAttachment(String user, String fileName, String contents, ExternalAttachment attachment)
			throws IOException {
		assertEquals(user, attachment.getAttacher());
		assertEquals(fileName, attachment.getName());
		assertEquals(contents, FileUtils.readFileToString(attachment.getAttachment()));
	}

}
