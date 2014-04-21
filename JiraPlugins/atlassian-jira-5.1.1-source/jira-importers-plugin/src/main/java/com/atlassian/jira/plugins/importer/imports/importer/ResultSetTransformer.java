/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetTransformer<T> {
	String getSqlQuery();

	@Nullable
	T transform(ResultSet rs) throws SQLException;
}
