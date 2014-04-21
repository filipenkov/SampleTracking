/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.LinkedHashMultimap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestDefaultExternalCustomFieldValueMapper {

	@Mock(answer = Answers.RETURNS_MOCKS)
	private CustomFieldManager customFieldManager;
	@Mock
	private ImportLogger log;
	@Mock
	private CsvConfigBean configBean;

	@Before
    public void createMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * If custom field value mapping is set to customfield_xxxxx there should be a call to CustomFieldManager
	 * to get details for custom field. Check if the call is actually executed.
	 */
	@Test
	public void testCustomFieldId() {
		ExternalCustomFieldValueMapper mapper = new DefaultExternalCustomFieldValueMapper(configBean, customFieldManager);
		LinkedHashMultimap<String, String> multimap = LinkedHashMultimap.create();

		multimap.put("customfield_10011", "Bug CSV");
		multimap.put("customfield_Reporter:com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "pniewiadomski");

		mapper.buildFromMultiMap(multimap, log);

		verify(customFieldManager).getCustomFieldObject("customfield_10011");
		verifyNoMoreInteractions(customFieldManager);
	}

	/**
	 * https://studio.atlassian.com/browse/JIM-443
	 *
	 * Check if mapper works for custom field type that has no searcher.
	 */
	@Test
	public void testCustomFieldHasNoSearcher() {
		CustomFieldType customFieldType = mock(CustomFieldType.class);
		CustomField customField = mock(CustomField.class);
		when(customField.getCustomFieldSearcher()).thenReturn(null);
		when(customField.getCustomFieldType()).thenReturn(customFieldType);
		CustomFieldTypeModuleDescriptor cfDescriptor = mock(CustomFieldTypeModuleDescriptor.class);
		when(cfDescriptor.getCompleteKey()).thenReturn("type");
		when(customFieldType.getDescriptor()).thenReturn(cfDescriptor);

		when(customFieldManager.getCustomFieldObject("customfield_10011")).thenReturn(customField);

		ExternalCustomFieldValueMapper mapper = new DefaultExternalCustomFieldValueMapper(configBean, customFieldManager);
		LinkedHashMultimap<String, String> multimap = LinkedHashMultimap.create();

		multimap.put("customfield_10011", "Bug CSV");

		List<ExternalCustomFieldValue> values = mapper.buildFromMultiMap(multimap, log);
		assertNotNull(values);
		assertEquals(1, values.size());
		assertEquals("type", values.get(0).getFieldType());
		assertNull(values.get(0).getSearcherType());

	}

}
