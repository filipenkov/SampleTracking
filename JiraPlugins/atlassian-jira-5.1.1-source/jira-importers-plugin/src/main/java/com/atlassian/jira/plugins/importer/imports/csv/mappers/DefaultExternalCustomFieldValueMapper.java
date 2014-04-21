/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public class DefaultExternalCustomFieldValueMapper implements ExternalCustomFieldValueMapper {
	public static final String CUSTOMFIELD_PREFIX = "customfield_";

	private final CsvConfigBean configBean;
	private final CustomFieldManager customFieldManager;

	public DefaultExternalCustomFieldValueMapper(CsvConfigBean configBean, CustomFieldManager customFieldManager) {
		this.configBean = configBean;
		this.customFieldManager = customFieldManager;
	}

	public List<ExternalCustomFieldValue> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		final List<ExternalCustomFieldValue> result = Lists.newArrayList();
		for (final String key : bean.keySet()) {
			if (!key.startsWith(CUSTOMFIELD_PREFIX)) {
				continue;
			}

			// value from CSV file
			Collection<String> values = bean.get(key);
			String noPrefix = key.substring(CUSTOMFIELD_PREFIX.length());

			String customFieldType;
			String customFieldName;
			String customFieldSearcher = null;

			if (StringUtils.isNumeric(noPrefix)) {
				// numeric means that's an existing custom field, let's copy details about it
				CustomField field = customFieldManager.getCustomFieldObject(key);
				customFieldType = field.getCustomFieldType().getDescriptor().getCompleteKey();
				customFieldSearcher = field.getCustomFieldSearcher() != null
						? field.getCustomFieldSearcher().getDescriptor().getCompleteKey() : null;
				customFieldName = field.getName();
			} else {
				int idx = noPrefix.indexOf(":");
				customFieldName = noPrefix.substring(0, idx);
				customFieldType = noPrefix.substring(idx+1);
			}

			final Object value;
			if (values.contains(DefaultExternalIssueMapper.CLEAR_VALUE_MARKER)) {
				value = DefaultExternalIssueMapper.CLEAR_VALUE_MARKER;
			} else if (CustomFieldConstants.MULTISELECT_FIELD_TYPE.equals(customFieldType)
					|| CustomFieldConstants.MULTICHECKBOXES_FIELD_TYPE.equals(customFieldType)
					|| CustomFieldConstants.VERSION_PICKER_TYPE.equals(customFieldType)
					|| CustomFieldConstants.SINGLE_VERSION_PICKER_TYPE.equals(customFieldType)
                    || CustomFieldConstants.MULTIGROUP_PICKER_FIELD_TYPE.equals(customFieldType)
                    || CustomFieldConstants.MULTIUSER_PICKER_FIELD_TYPE.equals(customFieldType)
                    || CustomFieldConstants.GROUP_PICKER_FIELD_TYPE.equals(customFieldType)) {
				value = values;
			} else if (CustomFieldConstants.DATE_FIELD_TYPE.equals(customFieldType)
					|| CustomFieldConstants.DATE_PICKER_FIELD_TYPE.equals(customFieldType)
					|| CustomFieldConstants.DATETIME_FIELD_TYPE.equals(customFieldType)) {
				try {
					value = configBean.formatDate(configBean.parseDate(Iterables.getFirst(values, null)));
				} catch (ParseException e) {
					log.warn("Unable to parse date: %s", Iterables.getFirst(values, null));
					continue;
				}
			} else {
				value = Iterables.getFirst(values, null);
			}

			result.add(new ExternalCustomFieldValue(customFieldName, customFieldType, customFieldSearcher,
					value));
		}
		return result;
	}
}
