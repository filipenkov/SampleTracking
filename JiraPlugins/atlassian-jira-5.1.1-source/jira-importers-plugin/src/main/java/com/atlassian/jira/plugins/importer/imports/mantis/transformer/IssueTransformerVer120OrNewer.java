/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

public class IssueTransformerVer120OrNewer extends IssueTransformerVer118OrOlder {

	public IssueTransformerVer120OrNewer(String mantisUrl, MantisConfigBean configBean, ExternalProject externalProject,
			DateTimePickerConverter dateTimePickerConverter, ImportLogger importLogger) {
		super(mantisUrl, configBean, externalProject, dateTimePickerConverter, importLogger);
	}

	@Override
	public String getSqlQuery() {
		return "SELECT *, "
				+ "(SELECT username FROM mantis_user_table p WHERE p.id=b.reporter_id limit 1) AS reporter_name,"
				+ "(SELECT username FROM mantis_user_table p WHERE p.id=b.handler_id limit 1) AS assignee_name,"
				+ "(SELECT name FROM mantis_category_table c WHERE c.id=b.category_id) AS category"
				+ " FROM mantis_bug_table b LEFT OUTER JOIN mantis_bug_text_table t ON (b.bug_text_id=t.id) WHERE project_id=" + externalProject.getId();
	}
}
