/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.label.Label;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Set;

public interface ExternalLabelsMapper extends ExternalObjectMapper {
	String LABELS = "labels";

	/**
	 * Takes multimap and returns a list of Strings containing the list
	 * of valid labels that can be found in the collection of Strings stored
	 * in the {@link #LABELS}
	 *
	 * @param bean
	 * @return list of validated strings for labels
	 */
	Set<Label> buildFromMultiMap(Multimap<String, String> bean);

	Set<Label> buildFrom(Collection<String> labels);
}