/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TestCsvDataBean {

	@Mock
	private CustomFieldManager customFieldManager;
	@Mock
	private ProjectManager projectManager;

	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils utils;
	@Mock
	private JiraAuthenticationContext authenticationContext;
	@Mock
	private ImportLogger log;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private OutlookDate outlookDate;

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
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils);
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-69.config"));

		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager);
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
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils);
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-360.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		ExternalIssue issue = cdb.getIssues(project, log).iterator().next();

		assertNotNull(issue);
		List<ExternalComment> comments = issue.getExternalComments();
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
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils);
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-464.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		ExternalIssue issue = cdb.getIssues(project, log).iterator().next();

		assertNotNull(issue);
		List<ExternalComment> comments = issue.getExternalComments();
		assertNotNull(comments);
		assertEquals(7, comments.size());

		// make sure order is the same as in CSV
		assertEquals("Comment; Adam; 05/05/2010 10:20:30 AM; This is comment", comments.get(0).getBody());
		assertNull(comments.get(0).getCreated());
		assertNull(comments.get(0).getUsername());

		assertEquals("Comment2", comments.get(1).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(1).getCreated());
		assertEquals("adam", comments.get(1).getUsername());

		assertEquals("Comment3", comments.get(2).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(2).getCreated());
		assertEquals("zosia", comments.get(2).getUsername());

		assertEquals("Comment34", comments.get(3).getBody());
		assertNull(comments.get(3).getCreated());
		assertNull(comments.get(3).getUsername());

		assertEquals("Comment3", comments.get(4).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(4).getCreated());
		assertNull(comments.get(4).getUsername());

		assertEquals("asjdh;skjdhskdfujh;ksud", comments.get(5).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(5).getCreated());
		assertEquals("adam", comments.get(5).getUsername());

		assertEquals("34", comments.get(6).getBody());
		assertEquals(new DateTime(2010, 05, 05, 11, 20, 30, 0), comments.get(6).getCreated());
		assertEquals("adam", comments.get(6).getUsername());
	}

	@Test
	public void testMultilineComments() throws Exception {
		final File csv = new File("src/test/resources/csv/multiline-comments.csv");
		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils);
		configBean.copyFromProperties(new File("src/test/resources/csv/multiline-comments.config"));
		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager);

		ExternalProject project = new ExternalProject("Test", "TST");
		cdb.setProjectMapper(new StaticProjectMapper(project));

		final ImmutableList<ExternalIssue> issues = ImmutableList.copyOf(cdb.getIssues(project, log));
		assertEquals("This comment works", issues.get(0).getExternalComments().get(0).getBody());
		assertEquals("This comment\nfails", issues.get(1).getExternalComments().get(0).getBody());
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-369
	 *
	 * Test if issues read from CSV file are ordered the same way they are in CSV.
	 */
	@Test
	public void testIssuesOrder() throws Exception {
		final File csv = new File("src/test/resources/csv/JIM-369.csv");

		CsvConfigBean configBean = new CsvConfigBean(csv, "UTF-8", ',', utils);
		configBean.copyFromProperties(new File("src/test/resources/csv/JIM-369.config"));

		CsvDataBean cdb = new CsvDataBean(configBean, customFieldManager);

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

}
