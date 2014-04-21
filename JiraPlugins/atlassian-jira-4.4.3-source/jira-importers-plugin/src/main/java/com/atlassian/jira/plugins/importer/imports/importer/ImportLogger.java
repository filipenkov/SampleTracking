/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * Logger used by {@link com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter}
 */
public interface ImportLogger {
	void log(String fmt, Object...args);

	void warn(@Nullable Throwable e, String fmt, Object...args);

	void warn(String fmt, Object...args);

	void fail(@Nullable Throwable e, String fmt, Object...args);

	InputStream getImportLog();

	void beginImportSection(String s);

	void endImportSection(String s);

	void skip(String s);
}
