/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis.config;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractDatabaseConfigBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MultipleSelectionValueMapper extends AbstractValueMappingDefinition {
    private final AbstractDatabaseConfigBean configBean;
    private final ExternalCustomField customField;
	private static final char VALUE_SEPARATOR = '|';

	protected MultipleSelectionValueMapper(final AbstractDatabaseConfigBean configBean,
			final JiraAuthenticationContext authenticationContext,
			final ExternalCustomField customField) {
		super(configBean.getJdbcConnection(), authenticationContext);
        this.configBean = configBean;
        this.customField = customField;
	}

	public String getExternalFieldId() {
		return customField.getName();
	}

	@Nullable
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mapped.to.custom.field", customField.getName(),
                configBean.getFieldMapping(customField.getId()));
	}

	public Set<String> getDistinctValues() {
		return Sets.newHashSet(jdbcConnection.queryDbAppendCollection(new ResultSetTransformer<Collection<String>>() {
			public String getSqlQuery() {
				return "SELECT DISTINCT value FROM mantis_custom_field_string_table WHERE field_id=" + customField
						.getId();
			}

			public Collection<String> transform(ResultSet rs) throws SQLException {
				String value = TextUtils.noNull(rs.getString("value"));
				return Sets.newHashSet(Iterables.filter(getValues(value), new Predicate<String>() {
					public boolean apply(@Nullable String input) {
						return StringUtils.isNotBlank(input);
					}
				}));
			}
		}));
	}

	public static List<String> getValues(String value) {
		return Arrays.asList(StringUtils.split(value, VALUE_SEPARATOR));
	}
}
