/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.label.LabelParser;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SimpleExternalLabelsMapper implements ExternalLabelsMapper {
	@Nullable
	public Set<String> buildFromMultiMap(Multimap<String, String> bean) {
		if (bean == null || bean.isEmpty()) {
			return Sets.newHashSet();
		}
		Collection<String> labels = bean.get(LABELS);
		return buildFrom(labels);
	}

	@Nullable
	public Set<String> buildFrom(Collection<String> labels) {
		if (labels == null || labels.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> externalLabels = Sets.newLinkedHashSet();
		for (final String label : labels) {
			String labelString = StringUtils.trimToNull(label);
			externalLabels.addAll(LabelParser.buildFromString(new LabelParser.CreateFromString<String>() {
                @Override
                public String create(String s) {
                    return s;
                }
            }, labelString));
		}
		return externalLabels;
	}
}