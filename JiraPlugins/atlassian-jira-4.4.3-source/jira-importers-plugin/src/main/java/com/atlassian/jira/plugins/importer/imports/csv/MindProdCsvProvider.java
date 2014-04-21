/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import au.com.bytecode.opencsv.CSVReader;
import com.atlassian.jira.plugins.importer.external.beans.SetMultiHashMap;
import com.atlassian.jira.util.xml.JiraFileInputStream;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MindProdCsvProvider implements CsvProvider {
	private static final Logger log = Logger.getLogger(MindProdCsvProvider.class);

	private CSVReader csvReader;
	private CsvMapper csvMapper;
	private Character delimiter;
	private File file;

	private static final char DEFAULT_DELIMITER = ',';
	private String encoding;

	public MindProdCsvProvider(File file, String encoding, CsvMapper csvMapper, @Nullable Character delimiter) {
		this.encoding = encoding;
		this.csvMapper = csvMapper;
		this.delimiter = delimiter;
		this.file = file;
	}

	public void startSession() throws ImportException {
		try {
			this.csvReader = new CSVReader(new InputStreamReader(new JiraFileInputStream(file), encoding),
					getDelimiter(), '\"');

			csvMapper.init(StringUtils.stripAll(csvReader.readNext()));
		}
		catch (IOException e) {
			throw new ImportException(e);
		}
	}

	public char getDelimiter() {
		if (delimiter != null)
			return delimiter.charValue();
		return DEFAULT_DELIMITER;
	}

	public void stopSession() throws ImportException {
		try {
			csvReader.close();
		}
		catch (IOException e) {
			throw new ImportException(e);
		}
	}

	public Collection<String> getHeaderLine() throws ImportException {
		return csvMapper.getHeaderRow();
	}

	@Nullable
	public ListMultimap<String, String> getNextLine() throws ImportException {
		try {
			ListMultimap<String, String> nextLine = null;
			try {
				String[] dataRow = StringUtils.stripAll(csvReader.readNext());
				if (dataRow != null) {
					nextLine = csvMapper.mapDataRow(dataRow);
				}
			}
			catch (EOFException e) {
				// Just return null if eof;
			}

			return nextLine;
		}
		catch (IOException e) {
			throw new ImportException(e);
		}
	}

	public List<ListMultimap<String, String>> getRestOfFile() throws ImportException {
		List<ListMultimap<String, String>> restOfFile = Lists.newArrayList();

		ListMultimap<String, String> nextLine;
		while ((nextLine = getNextLine()) != null) {
			restOfFile.add(nextLine);
		}

		return restOfFile;
	}

	public SetMultiHashMap<String, String> readUniqueValues(Collection<String> headers) throws ImportException {
		SetMultiHashMap<String, String> uniqueValues = new SetMultiHashMap(HashMultimap.create());
		Multimap<String, String> issueMap;
		while ((issueMap = getNextLine()) != null) {
			for (Iterator iterator = headers.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				Object o = issueMap.get(key);
				if (o != null) {
					log.debug("o.getClass(): " + o.getClass());

					Collection values = (Collection) o;
					for (Iterator iterator1 = values.iterator(); iterator1.hasNext();) {
						String value = (String) iterator1.next();
						if (StringUtils.isNotBlank(value)) {
							uniqueValues.put(key, value);
						}
					}
				}
			}
		}
		return uniqueValues;
	}
}