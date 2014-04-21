/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.NamedExternalObject;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class PivotalIteration implements NamedExternalObject {

	private final String id;

	private final List<ExternalIssue> issues;

	private final DateTime start;

	private final DateTime finish;

	private static final DateTimeFormatter NAME_DATE_FORMAT = DateTimeFormat.forPattern("d MMM");

	public PivotalIteration(String id, DateTime start, DateTime finish, List<ExternalIssue> issues) {
		this.finish = finish;
		this.start = start;
		this.id = id;
		this.issues = ImmutableList.copyOf(issues);
	}

	public PivotalIteration(PivotalIteration iteration) {
		id = iteration.id;
		start = iteration.start;
		finish = iteration.finish;
		issues = ImmutableList.copyOf(Collections2.transform(iteration.issues, new Function<ExternalIssue, ExternalIssue>() {
					@Override
					public ExternalIssue apply(ExternalIssue input) {
						return new ExternalIssue(input);
					}
				}));
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return getId() + " : " + getStart().toString(NAME_DATE_FORMAT);
	}

	public DateTime getStart() {
		return start;
	}

	public DateTime getFinish() {
		return finish;
	}

	public List<ExternalIssue> getStories() {
		return issues;
	}
}
