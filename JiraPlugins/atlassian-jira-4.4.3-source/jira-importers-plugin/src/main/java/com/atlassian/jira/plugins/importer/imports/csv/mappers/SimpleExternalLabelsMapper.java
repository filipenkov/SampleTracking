/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SimpleExternalLabelsMapper implements ExternalLabelsMapper {
	private final CreateLabelFromString labelFactory = new CreateLabelFromString();

	@Nullable
	public Set<Label> buildFromMultiMap(Multimap<String, String> bean) {
		if (bean == null || bean.isEmpty()) {
			return Sets.newHashSet();
		}
		Collection<String> labels = bean.get(LABELS);
		return buildFrom(labels);
	}

	@Nullable
	public Set<Label> buildFrom(Collection<String> labels) {
		if (labels == null || labels.isEmpty()) {
			return Collections.emptySet();
		}
		Set<Label> externalLabels = new LinkedHashSet<Label>(labels.size());
		for (final String label : labels) {
			String labelString = StringUtils.trimToNull(label);
			externalLabels.addAll(LabelParser.buildFromString(labelFactory, labelString));
		}
		return externalLabels;
	}

	public static class CreateLabelFromString implements LabelParser.CreateFromString<Label> {
		public Label create(final String stringIn) {
			return new Label(null, null, null, stringIn);
		}
	}
}