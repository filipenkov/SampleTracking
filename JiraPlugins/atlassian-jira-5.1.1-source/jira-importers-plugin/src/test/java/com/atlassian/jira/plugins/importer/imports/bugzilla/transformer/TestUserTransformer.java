/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import org.junit.Assert;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestUserTransformer {

	/**
	 * Test case for https://studio.plugins.atlassian.com/browse/JIM-3 / http://jira.atlassian.com/browse/JRA-14864
	 * <p/>
	 * Check if active flag is set to false if user has non-empty disabledtext field in her profile
	 */
	@Test
	public void testCreateInactiveUser() throws SQLException {
		BugzillaConfigBean configBean = mock(BugzillaConfigBean.class);
		UserTransformer transformer = new UserTransformer(configBean, ConsoleImportLogger.INSTANCE);

		when(configBean.getUsernameForLoginName("me@localhost")).thenReturn("me@localhost");

		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(true).thenReturn(false);
		when(rs.getString("disabledtext")).thenReturn("disabled");
		when(rs.getString("login_name")).thenReturn("me@localhost");

		ExternalUser user = transformer.transform(rs);
		Assert.assertNotNull(user);
		Assert.assertFalse(user.isActive());
		Assert.assertNotNull(user.getGroups());
		Assert.assertEquals(1, user.getGroups().size());

		verify(rs, atLeastOnce()).getString(anyString());
	}

	/**
	 * Test case for https://studio.plugins.atlassian.com/browse/JIM-3 / http://jira.atlassian.com/browse/JRA-14864
	 * <p/>
	 * Check if active flag is set to false if user has non-empty disabledtext field in her profile
	 */
	@Test
	public void testCreateActiveUser() throws SQLException {
		BugzillaConfigBean configBean = mock(BugzillaConfigBean.class);
		UserTransformer transformer = new UserTransformer(configBean, ConsoleImportLogger.INSTANCE);

		when(configBean.getUsernameForLoginName("me@localhost")).thenReturn("me@localhost");

		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(true).thenReturn(false);
		when(rs.getString("login_name")).thenReturn("me@localhost");

		ExternalUser user = transformer.transform(rs);
		Assert.assertNotNull(user);
		Assert.assertTrue(user.isActive());
		Assert.assertNotNull(user.getGroups());
		Assert.assertEquals(0, user.getGroups().size());

		verify(rs, atLeastOnce()).getString(anyString());
	}
}
