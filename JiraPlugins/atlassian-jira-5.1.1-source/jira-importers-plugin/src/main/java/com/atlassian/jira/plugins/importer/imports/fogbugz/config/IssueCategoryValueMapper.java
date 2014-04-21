/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.config;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.plugins.importer.imports.AbstractIssueTypeValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class IssueCategoryValueMapper extends AbstractIssueTypeValueMapper {
	private static final String CATEGORY_FIELD = "sCategory";

	public IssueCategoryValueMapper(JdbcConnection jdbcConnection,
			JiraAuthenticationContext authenticationContext, final ConstantsManager constantsManager) {
		super(jdbcConnection, authenticationContext, constantsManager);
	}

	@Override
	public String getExternalFieldId() {
		return CATEGORY_FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.external.fogbugz.mappings.value.category");
	}

	@Override
	public Set<String> getDistinctValues() {
		return new LinkedHashSet<String>(jdbcConnection.queryDb(
				new SingleStringResultTransformer("SELECT sCategory FROM Category ORDER BY sCategory")));
	}
}
