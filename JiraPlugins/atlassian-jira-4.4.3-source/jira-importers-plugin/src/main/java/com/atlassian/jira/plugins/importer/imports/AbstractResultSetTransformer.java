/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

public abstract class AbstractResultSetTransformer<T> implements ResultSetTransformer<T> {
	protected final ImportLogger log;

	public AbstractResultSetTransformer(ImportLogger log) {
		this.log = log;
	}
}
