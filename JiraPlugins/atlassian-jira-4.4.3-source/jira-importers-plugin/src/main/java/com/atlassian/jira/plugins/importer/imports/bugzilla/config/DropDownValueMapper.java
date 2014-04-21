/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.config;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.AbstractSelectFieldValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Sets;

import java.util.Set;

public class DropDownValueMapper extends AbstractSelectFieldValueMapper {

	protected DropDownValueMapper(final AbstractDatabaseConfigBean configBean,
			final JiraAuthenticationContext authenticationContext, ExternalCustomField customField) {
		super(configBean, authenticationContext, customField);
	}

	public Set<String> getDistinctValues() {
		return Sets.newHashSet(jdbcConnection.queryDb(new SingleStringResultTransformer(
				"SELECT DISTINCT " + customField.getId() + " FROM bugs WHERE " + customField.getId() + "!='---'")));
	}
}
