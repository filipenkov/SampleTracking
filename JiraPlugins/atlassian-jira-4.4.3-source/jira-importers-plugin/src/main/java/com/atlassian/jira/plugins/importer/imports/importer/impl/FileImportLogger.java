/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileImportLogger implements ImportLogger {
    private final Logger logger;
    private final File output;

    public FileImportLogger(File output) {
        this.output = output;

        logger = Logger.getLogger(getClass() + Thread.currentThread().toString());
        logger.setLevel(Level.ALL);

        try {
            Appender ap = new FileAppender(new PatternLayout("%d{ISO8601} %p - %m%n"), output.getAbsolutePath());
            logger.removeAllAppenders();
            logger.addAppender(ap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public void log(String fmt, Object...args) {
		logger.info(String.format(fmt, args));
	}

	@Override
	public void fail(@Nullable Throwable e, String fmt, Object...args) {
		logger.error(String.format(fmt, args), e);
	}

	@Override
	public void warn(@Nullable Throwable e, String s, Object...args) {
        logger.warn(String.format(s, args), e);
    }

	@Override
	public void warn(String s, Object...args) {
        warn(null, s, args);
    }

	@Override
	public InputStream getImportLog() {
        try {
            return new FileInputStream(output);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public void beginImportSection(String s) {
		banner("Importing: %s", s);
		log("Only new items will be imported");
	}

	private void banner(String fmt, Object...args) {
        logger.info(StringUtils.repeat("-", 30));
        logger.info(String.format(fmt, args));
        logger.info(StringUtils.repeat("-", 30));
	}

	@Override
	public void endImportSection(String s) {
		banner("Finished Importing : %s", s);
	}

	@Override
	public void skip(String s) {
		log("Skipped importing of " + s);
	}
}
