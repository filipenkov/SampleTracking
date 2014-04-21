/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestBugzillaConfigBean {
	private JdbcConnection jdbcConnection;
	private ExternalUtils utils;
	private WorkflowSchemeManager workflowSchemeManager;

	@Before
	public void setup() throws SQLException {
		jdbcConnection = mock(JdbcConnection.class);
		setupHasCustomFields(jdbcConnection);

		utils = mock(ExternalUtils.class);
		JiraAuthenticationContext authenticationMock = mock(JiraAuthenticationContext.class, RETURNS_MOCKS);
		ConstantsManager constantsManager = mock(ConstantsManager.class);
		workflowSchemeManager = mock(WorkflowSchemeManager.class, RETURNS_MOCKS);

		when(utils.getAuthenticationContext()).thenReturn(authenticationMock);
		when(utils.getWorkflowSchemeManager()).thenReturn(workflowSchemeManager);
		when(utils.getConstantsManager()).thenReturn(constantsManager);
		when(authenticationMock.getLocale()).thenReturn(Locale.ENGLISH);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-140
	 */
	@Test
	public void testLinkNames() throws SQLException {
		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection, utils);

		List<String> links = configBean.getLinkNamesFromDb();
		assertNotNull(links);
		assertEquals(2, links.size());
		assertTrue(links.contains(BugzillaConfigBean.DEPENDS_LINK_NAME));
		assertTrue(links.contains(BugzillaConfigBean.DUPLICATES_LINK_NAME));
	}

	/**
	 * Test for https://studio.atlassian.com/browse/JIM-152
	 *
	 * There's no mapping for the user, username should be returned as lowercase
	 */
	@Test
	public void testUsernameNoMappingUpperCase() throws SQLException, GenericEntityException {
		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection, utils);

		assertEquals("user", configBean.getUsernameForLoginName("USER"));
	}

	/**
	 * Test for https://studio.atlassian.com/browse/JIM-152
	 *
	 * There's a mapping for the user which is actually uper case, username should be returned as lowercase
	 */
	@Test
	public void testUsernameMappingUpperCase() throws Exception {
		BugzillaConfigBean configBean = new BugzillaConfigBean(jdbcConnection, utils);

		configBean.copyFromProperties(new File("src/test/resources/bugzilla/JIM-152.config"));

		assertEquals("user4", configBean.getUsernameForLoginName("user4@example.com"));
	}

	protected void setupHasCustomFields(JdbcConnection jdbcConnection) throws SQLException {
		Connection connectionMock = mock(Connection.class);
		DatabaseMetaData metaMock = mock(DatabaseMetaData.class);
		ResultSet rsMock = mock(ResultSet.class);
		Mockito.when(jdbcConnection.getConnection()).thenReturn(connectionMock);
		Mockito.when(connectionMock.getMetaData()).thenReturn(metaMock);
		Mockito.when(metaMock.getColumns((String) isNull(), (String) isNull(), anyString(), (String) isNull()))
				.thenReturn(rsMock);
	}
}
