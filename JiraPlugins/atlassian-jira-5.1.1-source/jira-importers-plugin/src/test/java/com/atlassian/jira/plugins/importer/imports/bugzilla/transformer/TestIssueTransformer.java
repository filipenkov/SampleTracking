/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.ExternalUtilsBuilder;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestIssueTransformer {

	private final ImportLogger importLogger = ConsoleImportLogger.INSTANCE;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private JdbcConnection jdbcConnection;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private JiraAuthenticationContext authenticationMock;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private WorkflowSchemeManager workflowSchemeManager;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private ConstantsManager constantsManager;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(jdbcConnection.queryDb(any(CustomFieldTransformer.class))).thenReturn(Collections.<ExternalCustomField>emptyList());
	}

	@Test
	public void testSqlQueryContainsProjectId() {
		ExternalProject project = new ExternalProject();
		project.setId("143234435345");

		DateTimePickerConverter dateTimeMock = mock(DateTimePickerConverter.class);

		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection, new ExternalUtilsBuilder().createExternalUtils()) {
			@Override
			public List<ExternalCustomField> getCustomFields() {
				return Lists.newArrayList();
			}

			@Override
			public List<String> getExternalProjectNames() {
				return Lists.newArrayList();
			}
		};
		IssueTransformer transformer = new IssueTransformer("http://localhost", configBean, project, dateTimeMock, importLogger, true);
		assertTrue("SQL query must include project id", transformer.getSqlQuery().contains(project.getId()));

		verifyZeroInteractions(dateTimeMock);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-19227
	 */
	@Test
	public void testNullComponent() throws SQLException {
		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection,
				new ExternalUtilsBuilder().setAuthenticationContext(authenticationMock)
						.setWorkflowSchemeManager(workflowSchemeManager)
						.setConstantsManager(constantsManager)
						.createExternalUtils()) {

			@Override
			public List<ExternalCustomField> getCustomFields() {
				return Lists.newArrayList();
			}

			@Override
			public List<String> getExternalProjectNames() {
				return Lists.newArrayList();
			}
		};

		DateTimePickerConverter dateTimeMock = mock(DateTimePickerConverter.class);

		IssueTransformer transformer = new IssueTransformer("http://localhost", configBean, new ExternalProject(),
				dateTimeMock, importLogger, true);

		ResultSet rsMock = mock(ResultSet.class);
		when(rsMock.getString("short_desc")).thenReturn("This is a test case");
		ExternalIssue issue = transformer.transform(rsMock);
		Assert.assertNotNull(issue);
		Assert.assertNull(issue.getComponents());

		verify(rsMock).getString("component");

		verifyZeroInteractions(dateTimeMock);
	}

	/**
	 * Test case for http://jira.atlassian.com/browse/JRA-19227
	 */
	@Test
	public void testComponentIsSet() throws SQLException {
		final String component = "Test component";

		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection,
				new ExternalUtilsBuilder().setAuthenticationContext(authenticationMock)
						.setWorkflowSchemeManager(workflowSchemeManager)
						.setConstantsManager(constantsManager).createExternalUtils()) {
			@Override
			public List<ExternalCustomField> getCustomFields() {
				return Lists.newArrayList();
			}

			@Override
			public List<String> getExternalProjectNames() {
				return Lists.newArrayList();
			}
		};

		DateTimePickerConverter dateTimeMock = mock(DateTimePickerConverter.class);

		IssueTransformer transformer = new IssueTransformer("http://localhost", configBean, new ExternalProject(),
				dateTimeMock, importLogger, true);

		ResultSet rsMock = mock(ResultSet.class);
		when(rsMock.getString("short_desc")).thenReturn("This is a test case");
		when(rsMock.getString("component")).thenReturn(component);

		ExternalIssue issue = transformer.transform(rsMock);
		Assert.assertNotNull(issue);
		Assert.assertEquals(Collections.singletonList(component), issue.getComponents());

		verifyZeroInteractions(dateTimeMock);
	}

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-300
     */
    @Test
    public void testEmptyDateCustomField() throws SQLException {
        BugzillaConfigBean configBean = mock(BugzillaConfigBean.class);
        JdbcConnection jdbcConnection = mock(JdbcConnection.class);

        List<String> customFieldValues = Lists.newArrayList("");
        PowerMockito.when(configBean.getJdbcConnection()).thenReturn(jdbcConnection);
        PowerMockito.when(jdbcConnection.queryDb(Matchers.<SingleStringResultTransformer>any())).thenReturn(customFieldValues);

        ExternalProject externalProject = new ExternalProject();
        DateTimePickerConverter dateTimePickerConverter = mock(DateTimePickerConverter.class);
        ImportLogger log = mock(ImportLogger.class);

        ExternalCustomField customField = ExternalCustomField.createDatetime("345", "Date");

        ResultSet rs = mock(ResultSet.class);

        IssueTransformer transformer = new IssueTransformer("http://localhost", configBean,
				externalProject, dateTimePickerConverter, log, true);
        Assert.assertNull(transformer.getCustomFieldValue(rs, "12", customField));
    }
}
