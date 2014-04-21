/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SimpleCustomFieldVersionMapper implements ExternalVersionMapper {
	private CustomFieldManager customFieldManager;

	public SimpleCustomFieldVersionMapper(CustomFieldManager customFieldManager) {
		this.customFieldManager = customFieldManager;
	}

	public List<ExternalVersion> buildFromMultiMap(Multimap<String, String> bean) {
		final List<ExternalVersion> externalVersions = Lists.newArrayList();

		for (final String key : bean.keySet()) {
			if (!key.startsWith(DefaultExternalCustomFieldValueMapper.CUSTOMFIELD_PREFIX)) {
				continue;
			}

			final String customFieldType;

			if (StringUtils.isNumeric(
					StringUtils.removeStart(key, DefaultExternalCustomFieldValueMapper.CUSTOMFIELD_PREFIX))) {
				// numeric means that's an existing custom field, let's copy details about it
				CustomField field = customFieldManager.getCustomFieldObject(key);
				customFieldType = field.getCustomFieldType().getDescriptor().getCompleteKey();
			} else {
				customFieldType = key; // we don't actually care about the exact type
			}

			if (customFieldType.endsWith(CustomFieldConstants.SINGLE_VERSION_PICKER_TYPE)
					|| customFieldType.endsWith(CustomFieldConstants.VERSION_PICKER_TYPE)) {

				final Collection<String> versionNames = bean.get(key);

				if (versionNames.contains(DefaultExternalIssueMapper.CLEAR_VALUE_MARKER)) {
					return Collections.emptyList();
				}

				for (String versionName : versionNames) {
					if (StringUtils.isNotEmpty(versionName)) {
						ExternalVersion externalVersion = new ExternalVersion();
						externalVersion.setName(versionName);
						externalVersion.setDescription(versionName);

						externalVersions.add(externalVersion);
					}
				}
			}
		}
		return externalVersions;
	}
}
