/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.AbstractConfigBean2;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractDataBean<T extends AbstractConfigBean2> implements ImportDataBean {
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
	public Collection<ExternalCustomFieldValue> getGlobalCustomFields() {
		final Collection<ExternalCustomFieldValue> values = Lists.newArrayList();
		for (ExternalCustomField field : configBean.getCustomFields()) {
			if (field.getValueSet() == null) {
				// this procedure applies only to fields with explicit global value set defined
				continue;
			}

			if (configBean.isFieldMappedToIssueField(field.getId())) {
				// we don't create values for non-custom fields
				continue;
			}

			final String mapping = configBean.getFieldMapping(field.getId());
			// map to a specific custom field (if it exists)
			final String customFieldName = StringUtils.isNotEmpty(mapping) ? mapping  : field.getName();

			Collection<String> customFieldValues = field.getValueSet();
			if (customFieldValues != null) {
				for (String rawValue : customFieldValues) {
					final String value = configBean.getValueMappingHelper()
							.getValueMappingForImport(field.getId(), rawValue);
					final ExternalCustomFieldValue customFieldValue = new ExternalCustomFieldValue(customFieldName,
							field.getTypeKey(), field.getSearcherKey(), value);
					values.add(customFieldValue);
				}
			}
		}

		return values;
	}

	@Override
	public void afterProjectCreated(ExternalProject externalProject, Project project, ImportLogger importLogger) {
	}

	@Override
	@Nullable
	public String getExternalSystemUrl() {
		return null;
	}
}
