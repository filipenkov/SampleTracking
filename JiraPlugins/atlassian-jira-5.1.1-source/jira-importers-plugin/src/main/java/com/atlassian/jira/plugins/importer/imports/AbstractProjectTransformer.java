/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.config.ValueMappingHelper;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractProjectTransformer implements ResultSetTransformer<ExternalProject> {

	private final AbstractDatabaseConfigBean configBean;

	public AbstractProjectTransformer(AbstractDatabaseConfigBean configBean) {
		this.configBean = configBean;
	}

	protected abstract ExternalProject create(ResultSet rs) throws SQLException;

	@Override
	@Nullable
	public ExternalProject transform(final ResultSet rs) throws SQLException {
		return map(create(rs));
	}

	@Nullable
	protected ExternalProject map(ExternalProject externalProject) {
        return map(configBean, externalProject);
    }

    @Nonnull
    public static ExternalProject map(@Nonnull AbstractConfigBean2 configBean, @Nonnull ExternalProject externalProject) {
		ExternalProject project = new ExternalProject();
		project.setExternalName(externalProject.getName());
		project.setKey(configBean.getProjectKey(externalProject.getName()));
		project.setName(configBean.getProjectName(externalProject.getName()));

		project.setId(externalProject.getId());
		project.setDescription(externalProject.getDescription());
		project.setLead(configBean.getProjectLead(externalProject.getName()));

		final ValueMappingHelper helper = configBean.getValueMappingHelper();
		if (helper.isWorkflowSchemeDefined()) {
			project.setWorkflowSchemeName(helper.getWorkflowSchemeName());
		}
		return project;
	}
}
