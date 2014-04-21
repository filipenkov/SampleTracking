/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;

public class PropertiesCsvMapper extends HeaderRowCsvMapper {
	private final CsvConfigBean configBean;

	private final Map<Integer, Integer> duplicateIdMap = Maps.newHashMap();

	public PropertiesCsvMapper(CsvConfigBean configBean)
			throws ConfigurationException {
		this.configBean = configBean;
	}

	public void init(String[] dataHeader) throws ImportException {
		super.init(dataHeader);
	}

	@Nullable
	public String getKey(int number) {
		final String key = super.getKey(number);
		final String existingCf = configBean.getExistingCfNameValue(key);
		if (StringUtils.isNotEmpty(existingCf)) {
			return existingCf;
		}
		final String newCf = configBean.translateNewCfMapping(key);
		if (StringUtils.isNotEmpty(newCf)) {
			return newCf;
		}
		return configBean.getStringValue(configBean.getFieldName(key));
	}

	@Nullable
	public String getValue(int number, String[] dataRow) {
		final String key = super.getKey(number);
		Integer indexInt = number;
		if (duplicateIdMap.containsKey(indexInt)) {
			number = duplicateIdMap.get(indexInt).intValue();
		}

		String value = super.getValue(number, dataRow);
		String translatedValue = null;

		if (StringUtils.isNotEmpty(value)) {
			translatedValue = StringUtils.defaultIfEmpty(
					configBean.getStringValue(configBean.getValueMappingName(key, value)), value);
			if (ValueMappingHelper.NULL_VALUE.equals(translatedValue)) {
				translatedValue = "";
			}
		}

		return translatedValue;
	}

}
