package com.atlassian.jira.plugins.importer.issue.customfields.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @since v3.13
 */
public class TestImportIdLinkCFType {
	@Test
	public void testGetProjectImporter() throws Exception {
		final CustomFieldValuePersister customFieldValuePersister = mock(CustomFieldValuePersister.class);
		final DoubleConverter doubleConverter = mock(DoubleConverter.class);
		final ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
		final GenericConfigManager genericConfigManager = mock(GenericConfigManager.class);

		ImportIdLinkCFType importIdLinkCFType = new ImportIdLinkCFType(customFieldValuePersister, doubleConverter,
				applicationProperties, genericConfigManager);
		assertTrue(importIdLinkCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);

		verifyZeroInteractions(customFieldValuePersister);
		verifyZeroInteractions(doubleConverter);
		verifyZeroInteractions(applicationProperties);
		verifyZeroInteractions(genericConfigManager);
	}

}
