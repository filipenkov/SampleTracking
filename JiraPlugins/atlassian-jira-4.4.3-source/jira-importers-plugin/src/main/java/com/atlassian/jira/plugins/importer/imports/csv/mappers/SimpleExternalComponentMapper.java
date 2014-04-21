/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComponent;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleExternalComponentMapper implements ExternalComponentMapper {

	private static final String COMPONENT = IssueFieldConstants.COMPONENTS;

	@Nullable
	public List<ExternalComponent> buildFromMultiMap(Multimap<String, String> bean) {
		@SuppressWarnings("unchecked")
		Collection<String> componentNames = bean.get(COMPONENT);
		if (componentNames != null && !componentNames.isEmpty()) {
			List<ExternalComponent> externalComponents = new ArrayList<ExternalComponent>(componentNames.size());

			for (String componentName : componentNames) {

				if (StringUtils.isNotEmpty(componentName)) {
					ExternalComponent externalComponent = new ExternalComponent();
					externalComponent.setName(componentName);

					externalComponents.add(externalComponent);
				}
			}

			return externalComponents;
		} else {
			return null;
		}
	}
}
