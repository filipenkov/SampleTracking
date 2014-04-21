/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.plugins.importer.transformer.AbstractProjectTransformer;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectTransformer extends AbstractProjectTransformer implements Function<String, ExternalProject> {
	public ProjectTransformer(AbstractDatabaseConfigBean configBean) {
		super(configBean);
	}

	@Override
	protected ExternalProject create(ResultSet rs) throws SQLException {
		throw new UnsupportedOperationException("Trac project transformer is not SQL based");
	}

	@Override
	public String getSqlQuery() {
		throw new UnsupportedOperationException("Trac project transformer is not SQL based");
	}

	@Override
	public ExternalProject apply(@Nullable String input) {
		return map(new ExternalProject(input, null));
	}
}
