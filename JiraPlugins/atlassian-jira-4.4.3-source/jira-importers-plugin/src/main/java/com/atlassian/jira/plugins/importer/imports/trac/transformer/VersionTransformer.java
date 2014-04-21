/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionTransformer extends AbstractResultSetTransformer<ExternalVersion> {

	public VersionTransformer(ImportLogger importLogger) {
		super(importLogger);
	}

	public String getSqlQuery() {
		return "SELECT name,time,description FROM version WHERE name IS NOT NULL AND name != ''";
	}

	@Nullable
	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		ExternalVersion version = new ExternalVersion(rs.getString("name"));
		version.setDescription(rs.getString("description"));
		version.setReleaseDate(TracConfigBean.getTimestamp(rs, "time"));
		return version;
	}
}
