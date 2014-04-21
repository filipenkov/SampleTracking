/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Test for {@link com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalLabelsMapper}.
 *
 * @since 4.2
 */
public class TestSimpleExternalLabelsMapper extends TestCase {
	private ExternalLabelsMapper labelsMapper;
	private CreateLabelFromString labelFactory = new CreateLabelFromString();

	public void setUp() {
		labelsMapper = new SimpleExternalLabelsMapper();
	}

	@Override
	protected void tearDown() throws Exception {
		labelsMapper = null;
	}

	public void testBuildFromMultiMapWithNullParameters() {
		assertEquals(0, labelsMapper.buildFromMultiMap(HashMultimap.<String, String>create()).size());
	}

	public void testBuildFromStringWithNullParameters() {
		assertEquals(Collections.<Label>emptySet(), LabelParser.buildFromString(labelFactory, null));
		assertEquals(Collections.<Label>emptySet(), LabelParser.buildFromString(labelFactory, ""));
	}

	public void testBuildFromMultiMapWithNullLabelsInMap() {
		Multimap<String, String> map = HashMultimap.create();
		map.put("a", "b");
		assertEquals(0, labelsMapper.buildFromMultiMap(map).size());
	}

	public void testBuildFromMultiMapWithEmptyAndNullLabel() throws Exception {
		final String label = "a";

		Multimap<String, String> map = HashMultimap.create();
		map.put(ExternalLabelsMapper.LABELS, null);
		map.put(ExternalLabelsMapper.LABELS, "");
		map.put(ExternalLabelsMapper.LABELS, label);

		assertEquals(createLabels(label), labelsMapper.buildFromMultiMap(map));
	}

	public void testBuildFromMultiMapWithValidLabelWithSplit() throws Exception {
		final String goodLabel = "a b";

		Multimap<String, String> map = HashMultimap.create();
		map.put(ExternalLabelsMapper.LABELS, null);
		map.put(ExternalLabelsMapper.LABELS, "");
		map.put(ExternalLabelsMapper.LABELS, goodLabel);

		assertEquals(createLabels("a", "b"), labelsMapper.buildFromMultiMap(map));
	}

	public void testBuildFromStringWithValidLabelWithSplit() throws Exception {
		final String goodLabel = "a b";

		assertEquals(createLabels("a", "b"), LabelParser.buildFromString(labelFactory, goodLabel));
	}

	public void testBuildFromMultiMapWithLabelIsTrimmed() throws Exception {
		Multimap<String, String> map = HashMultimap.create();
		map.put(ExternalLabelsMapper.LABELS, " b     ");
		assertEquals(createLabels("b"), labelsMapper.buildFromMultiMap(map));
	}

	public void testBuildFromStringWithLabelIsTrimmed() throws Exception {
		assertEquals(createLabels("b"), LabelParser.buildFromString(labelFactory, " b     "));
	}

	private static Set<Label> createLabels(String... labels) {
		Set<Label> labelObjects = new LinkedHashSet<Label>();
		for (String label : labels) {
			labelObjects.add(new Label(null, null, label));
		}

		return labelObjects;
	}

	static class CreateLabelFromString implements LabelParser.CreateFromString<Label> {

		public Label create(final String stringIn) {
			return new Label(null, null, null, stringIn);
		}
	}
}