/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.google.common.collect.Lists;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class TestRequiredUserTransformer {

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-177
	 */
	@Test
	public void testSqlIn() {

		MantisConfigBean configBean = mock(MantisConfigBean.class);
		ImportLogger log = mock(ImportLogger.class);

		ExternalProject p1 = new ExternalProject();
		p1.setId("1");

		ExternalProject p2 = new ExternalProject();
		p2.setId("2");

		RequiredUserTransformer rut = new RequiredUserTransformer(configBean, Lists.newArrayList(p1, p2), log);

		assertTrue(rut.getSqlQuery().contains("project_id IN (1,2)"));

		verifyZeroInteractions(configBean);
		verifyZeroInteractions(log);
	}
}
