/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.jdom.Element;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

public class IterationParser {

	private final StoryParser storyParser;

	public IterationParser() {
		this.storyParser = new StoryParser();
	}

	public List<PivotalIteration> parseIterations(@Nullable Element iterations) {
		return Lists.newArrayList(Collections2.transform(XmlUtil.getChildren(iterations, "iteration"),
				new Function<Element, PivotalIteration>() {
					public PivotalIteration apply(Element from) {
						return parseIteration(from);
					}
				}));
	}

	protected PivotalIteration parseIteration(Element from) {
		final String number = from.getChildTextTrim("number");
		final DateTime start = storyParser.parseDateTime(from.getChildTextTrim("start"));
		final DateTime finish = storyParser.parseDateTime(from.getChildTextTrim("finish"));
		final List<ExternalIssue> issues = storyParser.parseStories(from.getChild("stories"));

		return new PivotalIteration(number, start, finish, issues);
	}

}
