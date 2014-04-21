/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.SqlUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

import java.util.Collection;

public class RequiredUserTransformer extends UserTransformer {
	private final Collection<ExternalProject> projects;

	public RequiredUserTransformer(final MantisConfigBean configBean, Collection<ExternalProject> projects, ImportLogger log) {
		super(configBean, log);
		this.projects = projects;
	}

	@Override
	public String getSqlQuery() {
		if (projects.size() == 0) {
			return super.getSqlQuery();
		}
		
		final String projectIds = SqlUtils.comma(projects);
		final String fields = "SELECT u.id,username,realname,email,enabled FROM mantis_user_table AS u JOIN ";
		final String union = " UNION DISTINCT ";

		return
			fields + "mantis_bug_table AS b ON (b.reporter_id = u.id OR b.handler_id = u.id) WHERE project_id IN (" + projectIds + ")"
				+ union +
			fields + "mantis_bugnote_table AS n ON (n.reporter_id = u.id) JOIN mantis_bug_table AS b ON (n.bug_id = b.id) WHERE b.project_id IN (" + projectIds + ")"
                + union +
            fields + "mantis_bug_monitor_table AS m ON (m.user_id = u.id) JOIN mantis_bug_table AS b ON (m.bug_id = b.id) WHERE b.project_id IN (" + projectIds + ")";
	}

}
