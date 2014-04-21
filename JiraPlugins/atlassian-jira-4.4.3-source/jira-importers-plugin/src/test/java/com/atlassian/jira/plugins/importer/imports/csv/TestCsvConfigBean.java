/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.web.ImporterProcessSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileNotFoundException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestCsvConfigBean {

	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils utils;
	@Mock
	private JiraAuthenticationContext ac;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(utils.getAuthenticationContext()).thenReturn(ac);
	}

	@Test
	public void testIsIssueConstant() throws FileNotFoundException, ImportException {
		CsvConfigBean ccb = new CsvConfigBean(new File("src/test/resources/csv/JIM-80.csv"), "UTF-8", ',', utils);

		IssueConstant ic = mock(IssueConstant.class);
		assertFalse(ccb.isIssueConstantMappingSelected0(ic, "1", "3"));

		// IssueConstant is Bug, 1 but currentValue is bug - they should match
		when(ic.getId()).thenReturn("1");
	 	when(ic.getNameTranslation()).thenReturn("Bug");
		assertTrue(ccb.isIssueConstantMappingSelected0(ic, null, "bug"));

		// IssueConstant is Bug, 1 but currentValue is null, but mapping is 1 - they should match
		when(ic.getId()).thenReturn("1");
	 	when(ic.getNameTranslation()).thenReturn("Bug");
		assertTrue(ccb.isIssueConstantMappingSelected0(ic, "1", ""));
	}

	@Test
	public void testUtf8Config() throws Exception {
		CsvConfigBean ccb = new CsvConfigBean(new File("src/test/resources/csv/JIM-67.csv"), "UTF-8", ',', utils);
		ccb.copyFromProperties(new File("src/test/resources/csv/JIM-67.config"));

		assertEquals("field.r\u00e9f\u00e9rence", ccb.getFieldName("r\u00e9f\u00e9rence"));
		assertEquals("value.r\u00e9f\u00e9rence.ref", ccb.getValueMappingName("r\u00e9f\u00e9rence", "ref"));
		assertEquals("\u0142\u017c\u0138\u017a\u0107\u0142", ccb.getValue(ccb.getValueMappingName("r\u00e9f\u00e9rence", "ref")));
	}
}
