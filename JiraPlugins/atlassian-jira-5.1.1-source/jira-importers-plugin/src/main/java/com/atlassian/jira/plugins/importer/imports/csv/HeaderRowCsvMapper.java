/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses the first row as the Heading keys.
 * No Value translations are used.
 */
public class HeaderRowCsvMapper implements CsvMapper {
	protected LinkedHashMap<Integer, String> keyMappings;
	protected ImmutableList<String> originalDataHeader;

	public HeaderRowCsvMapper() {
		keyMappings = Maps.newLinkedHashMap();
	}

	public void init(String[] dataHeader) throws ImportException {
		if (dataHeader != null) {
			keyMappings = Maps.newLinkedHashMap();
			this.originalDataHeader = ImmutableList.copyOf(dataHeader);
			for (int i = 0; i < dataHeader.length; i++) {
				String header = dataHeader[i];
				keyMappings.put(keyMappings.size(), header);
			}

			log.debug("Headers read. Fields: " + dataHeader.length);
		}
	}

	/**
	 * To implement JIM-165 this method is allowed to return null.
	 *
	 * @param number column number
	 * @return key or null if there's no user defined mapping
	 */
	@Nullable
	public String getKey(int number) {
		return keyMappings.get(number);
	}

	public String getValue(int number, String[] dataRow) {
		if (number < dataRow.length) {
			return dataRow[number];
		} else {
			return null;
		}
	}

	public ListMultimap<String, String> mapDataRow(String[] dataRow) {
		ListMultimap<String, String> multiMap = ArrayListMultimap.create();

		for (Map.Entry<Integer, String> entry : keyMappings.entrySet()) {
			final Integer index = entry.getKey();
			final String value = getValue(index.intValue(), dataRow);

			if (StringUtils.isNotEmpty(value)) {
				// JIM-165 - ignore fields that were not mapped by the user
				String key = getKey(index.intValue());
				if (key != null) {
					multiMap.put(key, value);
				}
			}
		}

		// @HACK This is a hack to allow extra columns for comments
		int keys = originalDataHeader.size();
		if (dataRow.length > keys) {
			// Add all the non-keyed fields as comments
			for (int i = keys; i < dataRow.length; i++) {
				String comment = dataRow[i];
				if (StringUtils.isNotEmpty(comment)) {
					multiMap.put(IssueFieldConstants.COMMENT, comment);
				}
			}
		}
		return multiMap;
	}

	public ImmutableList<String> getHeaderRow() {
		return originalDataHeader;
	}
}
