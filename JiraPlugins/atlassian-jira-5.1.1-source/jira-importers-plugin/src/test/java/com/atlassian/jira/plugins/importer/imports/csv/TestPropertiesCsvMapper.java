/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TestPropertiesCsvMapper {
	PropertiesCsvMapper testObject;
	static final String[] DATA_ROW = new String[]{"", "Sev I", "Open", "Data Model CR", "12/13/2004 12:12"};

	@Mock(answer = Answers.RETURNS_MOCKS)
	private JiraAuthenticationContext authenticationContext;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private ExternalUtils utils;
	@Mock(answer = Answers.RETURNS_MOCKS)
	private OutlookDate outlookDate;
	@Mock
	private CustomFieldManager customFieldManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(utils.getAuthenticationContext()).thenReturn(authenticationContext);
		when(authenticationContext.getOutlookDate()).thenReturn(outlookDate);
		when(outlookDate.formatDateTimePicker((Date) any())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				return invocationOnMock.getArguments()[0].toString();
			}
		});
		when(customFieldManager.getCustomFieldObject(Matchers.<Long>any())).thenReturn(null);

		CsvConfigBean configBean = new CsvConfigBean(
				new File("src/test/resources/csv/testPropertyCsvMapper.csv"), "UTF-8", ',', utils) {
			@Override
			protected CustomFieldManager getCustomFieldManager() {
				return customFieldManager;
			}
		};
		configBean.copyFromProperties(new File("src/test/resources/csv/testmapper.properties"));

		testObject = new PropertiesCsvMapper(configBean);
		testObject.init(new String[]{"CR Number", "Severity", "Status", "Type", "Closed On"});
	}

	@Test
	public void testGetKey() throws Exception {
		// Execute
		String key = testObject.getKey(1);
		assertEquals("customfield_Severity:select", key);
	}

	@Test
	public void testgetValue() throws Exception {
		// Execute
		assertEquals("Open", testObject.getValue(2, DATA_ROW));
		assertEquals("2", testObject.getValue(3, DATA_ROW));
	}

	@Test
	public void testgetNoValue() throws Exception {
		// Execute
		assertTrue(StringUtils.isEmpty(testObject.getValue(0, DATA_ROW)));
	}

	@Test
	public void testMapDateRow() throws Exception {
		// Execute
		Multimap multiMap = testObject.mapDataRow(DATA_ROW);
		assertEquals(4, multiMap.size());
		assertEquals("2", multiMap.get("type").iterator().next());
	}

	@Test
	public void testMapNoValueRow() throws Exception {
		// Execute
		Multimap multiMap = testObject.mapDataRow(DATA_ROW);
		assertEquals(4, multiMap.size());
		assertTrue(multiMap.get("customfield_CR Number").isEmpty());
	}

}
