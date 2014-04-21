/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.JiraException;

///CLOVER:OFF
public class ImportException extends JiraException {
	public ImportException() {
	}

	public ImportException(String s) {
		super(s);
	}

	public ImportException(Throwable throwable) {
		super(throwable);
	}

	public ImportException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
