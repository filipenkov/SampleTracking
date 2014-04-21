/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import java.text.ParseException;
import java.util.Date;

public interface CsvDateParser {
	Date parseDate(String translatedValue) throws ParseException;
}
