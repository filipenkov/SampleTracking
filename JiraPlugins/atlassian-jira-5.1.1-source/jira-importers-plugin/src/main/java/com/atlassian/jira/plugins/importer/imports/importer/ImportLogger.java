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
	void log(String format, Object...args);

	void warn(@Nullable Throwable e, String format, Object...args);

	void warn(String format, Object...args);

	void fail(@Nullable Throwable e, String format, Object...args);

	InputStream getImportLog();

	void beginImportSection(String s);

	void endImportSection(String s);

	void skip(String s);
}
