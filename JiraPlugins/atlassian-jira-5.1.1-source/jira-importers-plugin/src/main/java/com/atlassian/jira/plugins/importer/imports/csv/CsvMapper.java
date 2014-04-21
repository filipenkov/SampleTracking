/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

public interface CsvMapper {
	public static final Logger log = Logger.getLogger(com.atlassian.jira.plugins.importer.imports.csv.CsvMapper.class);

	void init(String[] dataHeader) throws ImportException;

	String getKey(int number);

	@Nullable
	String getValue(int number, String[] dataRow);

	ListMultimap<String, String> mapDataRow(String[] dataRow);

	ImmutableList<String> getHeaderRow();
}
