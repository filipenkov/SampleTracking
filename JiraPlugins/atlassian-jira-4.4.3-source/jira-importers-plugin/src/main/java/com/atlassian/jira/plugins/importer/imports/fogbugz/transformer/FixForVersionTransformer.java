/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transforms entries from the FixFor table into ExternalVersion objects.
 * <p/>
 * This transformer works for FogBugz schema 7.0.x, where the schema now looks like this:
 * <p/>
 * <pre>
 *  +------------+--------------+
 *  | Field      | Type         |
 *  +------------+--------------+
 *  | ixFixFor   | int(11)      |
 *  | sFixFor    | varchar(255) |
 *  | dt         | datetime     |
 *  | fDeleted   | int(11)      |
 *  | ixProject  | int(11)      |
 *  | fInactive  | smallint(6)  |
 *  | dtStart    | datetime     |
 *  | sStartNote | varchar(255) |
 *  +------------+--------------+
 * </pre>
 * The main difference is that now we look for "archived" based on fInactive OR fDeleted.
 * <p/>
 * For more information see their Data Dictionary: http://www.fogcreek.com/fogbugz/KB/dbsetup/FogBugzSchema.html
 */
public class FixForVersionTransformer extends AbstractResultSetTransformer<ExternalVersion> {
	private final ExternalProject externalProject;

	public FixForVersionTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT ixFixFor, sFixFor, fDeleted, fInactive, dt FROM FixFor WHERE ixProject = -1 OR ixProject = "
				+ externalProject.getId() + " AND sFixFor IS NOT NULL AND sFixFor != '' ORDER BY dt DESC, sFixFor";
	}

	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		final ExternalVersion externalVersion = new ExternalVersion(rs.getString("sFixFor"));
		final boolean isArchived = rs.getBoolean("fInactive") || rs.getBoolean("fDeleted");
		externalVersion.setArchived(isArchived);
		externalVersion.setReleaseDate(rs.getTimestamp("dt"));
		return externalVersion;
	}
}
