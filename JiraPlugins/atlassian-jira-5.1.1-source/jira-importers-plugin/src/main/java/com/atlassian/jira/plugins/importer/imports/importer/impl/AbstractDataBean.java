/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractDataBean<T extends AbstractConfigBean2> extends ImportDataBean {
	protected final T configBean;

	protected AbstractDataBean(T configBean) {
		this.configBean = configBean;
	}

	public Set<ExternalProject> getSelectedProjects(final ImportLogger log) {
		return Sets.newHashSet(Iterables.filter(
				getAllProjects(log), new Predicate<ExternalProject>() {
					public boolean apply(@Nullable ExternalProject input) {
						return input != null && configBean.isProjectSelected(input.getExternalName())
								&& StringUtils.isNotBlank(input.getKey()) && StringUtils.isNotBlank(input.getName());
					}
				}
		));
	}

	@Override
	public Collection<ExternalCustomField> getCustomFields() {
		final Collection<ExternalCustomField> mappedCustomFields = Lists.newArrayList();

		for (ExternalCustomField field : configBean.getCustomFields()) {
			if (configBean.isFieldMappedToIssueField(field.getId())) {
				// we don't create mappedCustomFields for non-custom fields
				continue;
			}

			final String mapping = configBean.getFieldMapping(field.getId());
			// map to a specific custom field (if it exists)
			final String customFieldName = StringUtils.isNotEmpty(mapping) ? mapping  : field.getName();
			final ExternalCustomField mappedCustomField = new ExternalCustomField(field.getId(), customFieldName, field.getTypeKey(), field.getSearcherKey());

			mappedCustomFields.add(mappedCustomField);

			if (field.getValueSet() == null) {
				continue;
			}

			final Collection<String> rawValues = field.getValueSet();
			final List<String> mappedValues = Lists.newArrayList();

			if (rawValues != null) {
				for (String rawValue : rawValues) {
					mappedValues.add(configBean.getValueMappingHelper().getValueMappingForImport(field.getId(), rawValue));
				}
				mappedCustomField.setValueSet(mappedValues);
			}
		}

		return mappedCustomFields;
	}

    @Override
    public String getReturnLinks() {
        return String.format("<div id=\"importAgain\"><a href=\"ExternalImport1.jspa\">%s</a></div>",
                configBean.getI18n().getText("jira-importer-plugin.ImporterLogsPage.import.another"));
    }
}
