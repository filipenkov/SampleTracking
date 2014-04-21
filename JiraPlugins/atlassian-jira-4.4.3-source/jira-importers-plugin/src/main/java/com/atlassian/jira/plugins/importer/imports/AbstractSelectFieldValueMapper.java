/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Sets;

import java.util.Set;

public class AbstractSelectFieldValueMapper extends AbstractValueMappingDefinition {
    private final AbstractDatabaseConfigBean configBean;
    protected final ExternalCustomField customField;

	protected AbstractSelectFieldValueMapper(final AbstractDatabaseConfigBean configBean,
			final JiraAuthenticationContext authenticationContext, ExternalCustomField customField) {
		super(configBean.getJdbcConnection(), authenticationContext);
        this.configBean = configBean;
        this.customField = customField;
	}

	public String getExternalFieldId() {
		return customField.getId();
	}

	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mapped.to.custom.field", customField.getName(),
                configBean.getFieldMapping(customField.getId()));
	}

	public Set<String> getDistinctValues() {
		return Sets.newHashSet(jdbcConnection.queryDb(new SingleStringResultTransformer(
				"SELECT DISTINCT " + customField.getId() + " FROM bugs WHERE " + customField.getId() + "!='---'")));
	}
}
