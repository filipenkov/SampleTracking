/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ConsoleImportLogger implements ImportLogger {

    public static final ImportLogger INSTANCE = new ConsoleImportLogger();

	public static final Logger logger = Logger.getLogger(ConsoleImportLogger.class);

	public void log(String fmt, Object... args) {
		logger.info(String.format(fmt, args));
	}

	public void warn(@Nullable Throwable e, String s, Object...args) {
		logger.warn(String.format(s, args), e);
	}

	public void warn(String s, Object...args) {
		warn(null, s, args);
	}

	public void fail(@Nullable Throwable e, String fmt, Object... args) {
		logger.error(String.format(fmt, args), e);
	}

	public InputStream getImportLog() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public void beginImportSection(String s) {
	}

	public void endImportSection(String s) {
	}

	public void skip(String s) {
	}
}