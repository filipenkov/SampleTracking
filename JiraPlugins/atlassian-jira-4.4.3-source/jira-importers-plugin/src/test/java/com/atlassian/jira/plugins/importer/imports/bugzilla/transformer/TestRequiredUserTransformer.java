/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Lists;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestRequiredUserTransformer {

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-177
	 */
	@Test
	public void testSqlIn() {

		BugzillaConfigBean configBean = mock(BugzillaConfigBean.class);
		ImportLogger log = mock(ImportLogger.class);

		ExternalProject p1 = new ExternalProject();
		p1.setId("1");

		ExternalProject p2 = new ExternalProject();
		p2.setId("2");

		when(configBean.getFielddefsIdColumn()).thenReturn("columnIdIsHere");

		RequiredUserTransformer rut = new RequiredUserTransformer(configBean, Lists.newArrayList(p1, p2), log);

		String sqlQuery = rut.getSqlQuery();
		assertTrue(sqlQuery.contains("b.product_id IN (1,2)"));
		assertTrue(sqlQuery.contains("columnIdIsHere"));

		verifyZeroInteractions(log);
	}
}
