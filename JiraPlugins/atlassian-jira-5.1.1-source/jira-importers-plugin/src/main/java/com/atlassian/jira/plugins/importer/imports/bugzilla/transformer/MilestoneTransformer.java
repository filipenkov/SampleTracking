/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.atlassian.jira.plugins.importer.imports.AbstractResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MilestoneTransformer extends AbstractResultSetTransformer<ExternalVersion> {
	private final ExternalProject externalProject;

	public MilestoneTransformer(final ExternalProject externalProject, ImportLogger importLogger) {
		super(importLogger);
		this.externalProject = externalProject;
	}

	public String getSqlQuery() {
		return "SELECT DISTINCT target_milestone FROM bugs WHERE product_id = "
				+ externalProject.getId()
				+ " AND target_milestone!='---' AND target_milestone IS NOT NULL AND target_milestone != '' ORDER BY target_milestone";
	}

	@Nullable
	public ExternalVersion transform(final ResultSet rs) throws SQLException {
		return new ExternalVersion(rs.getString("target_milestone"));
	}
}
