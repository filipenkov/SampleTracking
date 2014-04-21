/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.plugins.importer.external.beans.SetMultiHashMap;
import com.google.common.collect.ListMultimap;

import java.util.Collection;
import java.util.List;

public interface CsvProvider {

	void startSession() throws ImportException;

	void stopSession() throws ImportException;

	Collection<String> getHeaderLine() throws ImportException;

	/**
	 * Gets the next line as a Map. The Keys are either the Headers read from readHeaderLine or simply the values if
	 * the mathod readHeaderLine() has not been invoked.
	 * <p/>
	 * Returns null if there are no more lines
	 *
	 * @return Map of String - String value pairs
	 */
	ListMultimap<String, String> getNextLine() throws ImportException;


	/**
	 * Parses the rest of the File that has not been read.
	 *
	 * @return List of Maps of String - String value pairs
	 */
	List<ListMultimap<String, String>> getRestOfFile() throws ImportException;

	SetMultiHashMap<String, String> readUniqueValues(Collection<String> headers) throws ImportException;
}