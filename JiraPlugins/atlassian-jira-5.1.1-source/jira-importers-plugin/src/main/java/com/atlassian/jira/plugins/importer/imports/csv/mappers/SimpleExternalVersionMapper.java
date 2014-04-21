/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.external.beans.ExternalVersion;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SimpleExternalVersionMapper implements ExternalVersionMapper {
	private final String versionPrefix;

	public SimpleExternalVersionMapper(String versionPrefix) {
		this.versionPrefix = versionPrefix;
	}

	@Nullable
	public List<ExternalVersion> buildFromMultiMap(Multimap<String, String> bean) {
		@SuppressWarnings("unchecked")
		final Collection<String> versionNames = bean.get(versionPrefix);
		if (versionNames == null || versionNames.isEmpty()) {
			return null;
		}

		if (versionNames.contains(DefaultExternalIssueMapper.CLEAR_VALUE_MARKER)) {
			return Collections.emptyList();
		}

		final List<ExternalVersion> externalVersions = new ArrayList<ExternalVersion>(versionNames.size());

		for (String versionName : versionNames) {
			if (StringUtils.isNotEmpty(versionName)) {
				final ExternalVersion externalVersion = new ExternalVersion();
				externalVersion.setName(versionName);
				externalVersion.setDescription(versionName);

				externalVersions.add(externalVersion);
			}
		}

		return externalVersions;
	}
}
