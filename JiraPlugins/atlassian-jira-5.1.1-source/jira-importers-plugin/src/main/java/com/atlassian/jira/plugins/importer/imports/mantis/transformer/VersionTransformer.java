/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VersionTransformer extends AbstractResultSetTransformer<ExternalVersion> {
    private final ExternalProject externalProject;
    private MantisConfigBean.TimestampHelper timestampHelper;

    public VersionTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
        this.timestampHelper = new MantisConfigBean.TimestampHelper();
        this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT * FROM mantis_project_version_table WHERE project_id = "
				+ externalProject.getId() + " AND version IS NOT NULL AND version != ''";
	}

	public ExternalVersion transform(final ResultSet rs) throws SQLException {
        final ExternalVersion version = new ExternalVersion(rs.getString("version"));
        version.setReleaseDate(timestampHelper.getTimestamp(rs, "date_order"));
        version.setDescription(rs.getString("description"));
        version.setReleased(rs.getBoolean("released"));
        if (SqlUtils.getColumnNames(rs.getMetaData()).contains("obsolete")) {
            if (rs.getBoolean("obsolete")) {
                return null;
            }
        }
		return version;
	}
}
