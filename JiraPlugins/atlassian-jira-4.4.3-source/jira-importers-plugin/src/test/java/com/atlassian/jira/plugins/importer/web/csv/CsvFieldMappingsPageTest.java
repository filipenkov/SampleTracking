/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web.csv;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class CsvFieldMappingsPageTest {

	private CsvFieldMappingsPage page;
	private Object usageTrackingService;

	public CsvFieldMappingsPageTest() {
		page = mock(CsvFieldMappingsPage.class);
		when(page.getModel()).thenCallRealMethod();
	}

	@Test
	public void testGetModelAsString() throws Exception {
		Collection<CsvFieldMappingsPage.FieldMapping> testModel	= Lists.newArrayList();
		testModel.add(new CsvFieldMappingsPage.FieldMapping("csvsummary", true, "Summary", true));
		testModel.add(new CsvFieldMappingsPage.FieldMapping("Version", false, "fixversion", false));
		testModel.add(new CsvFieldMappingsPage.FieldMapping("['5432_+FSDa()", false, "&&\\/><'\"", false));
		when(page.getModelImpl()).thenReturn(testModel);
		Assert.assertNotNull(page.getModel());
		System.out.println(page.getModel());
		final Collection<CsvFieldMappingsPage.FieldMapping> map = new ObjectMapper().readValue(page.getModel(), new TypeReference<Collection<CsvFieldMappingsPage.FieldMapping>>() {
		});
		System.out.println(map);

	}
}
