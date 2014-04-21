/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalLabelsMapper;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class StoryParser {

	private final SimpleExternalLabelsMapper.CreateLabelFromString labelFactory	=
			new SimpleExternalLabelsMapper.CreateLabelFromString();

	private static final String DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss z";
	static final String SUBTASK_STATUS_FINISHED = "subtask_finished";
	static final String SUBTASK_STATUS_OPEN = "subtask_open";

	public ExternalIssue parseStory(Element element) {
		final ExternalIssue externalIssue = new ExternalIssue();

		final List<ExternalCustomFieldValue> customFields = Lists.newArrayList();

		externalIssue.setExternalId(element.getChildTextTrim("id"));
		externalIssue.setReporter(element.getChildText("requested_by"));
		externalIssue.setStatus(element.getChildText("current_state"));
		externalIssue.setAssignee(element.getChildText("owned_by"));
		externalIssue.setIssueType(element.getChildText("story_type"));
		externalIssue.setSummary(element.getChildText("name"));
		externalIssue.setDescription(element.getChildText("description"));
		final DateTime creationDate = parseDateTime(element.getChildText("created_at"));
		externalIssue.setCreated(creationDate.toDate());
		final DateTime updateDate = parseDateTime(element.getChildText("updated_at"));
		externalIssue.setUpdated(updateDate.toDate());
		externalIssue.setExternalComments(parseNotes(element.getChild("notes")));
		externalIssue.setAttachments(parseAttachments(element.getChild("attachments")));
		externalIssue.setSubtasks(parseTasks(externalIssue.getAssignee(), externalIssue.getReporter(),
				element.getChild("tasks")));
		externalIssue.setLabels(parseLabels(element.getChild("labels")));
		final Element estimateElement = element.getChild("estimate");
		if (estimateElement != null && "integer".equals(estimateElement.getAttributeValue("type"))) {
			final Integer estimate = Integer.valueOf(estimateElement.getTextTrim());

			// PT usees -1 for unestimated, in JIRA it should be null
			if (estimate != -1) {
				customFields.add(new ExternalCustomFieldValue("Story Points", CustomFieldConstants.NUMBER_FIELD_TYPE,
					CustomFieldConstants.NUMBER_RANGE_FIELD_SEARCHER, estimate.toString()));
			}
		}

		customFields.add(new ExternalCustomFieldValue(DefaultJiraDataImporter.EXTERNAL_ISSUE_URL,
				CustomFieldConstants.URL_FIELD_TYPE, CustomFieldConstants.EXACT_TEXT_SEARCHER,
				element.getChildTextTrim("url")));

		externalIssue.setExternalCustomFieldValues(customFields);

		return externalIssue;
	}

	protected Set<Label> parseLabels(@Nullable Element labels) {
		final Set<Label> result = Sets.newHashSet();
		if (labels != null) {
			for(String label : StringUtils.split(labels.getText(), ',')) {
				final String cleanLabel = LabelParser.getCleanLabel(label);
				if (StringUtils.isNotBlank(cleanLabel)) {
					result.add(labelFactory.create(cleanLabel));
				}
			}
		}
		return result;
	}

	List<ExternalAttachment> parseAttachments(@Nullable Element element) {
		return Lists.newArrayList(Collections2.transform(XmlUtil.getChildren(element, "attachment"), new Function<Element, ExternalAttachment>() {
			@Override
			public ExternalAttachment apply(Element element) {
				final String filename = element.getChildText("filename");
				final String url = element.getChildText("url");
				final DateTime uploadedAt = parseDateTime(element.getChildText("uploaded_at"));

				final PivotalExternalAttachment attachment = new PivotalExternalAttachment(filename, url, uploadedAt.toDate());
				attachment.setAttacher(element.getChildText("uploaded_by"));
				attachment.setDescription(element.getChildText("description"));
				attachment.setId(element.getChildText("id"));

				return attachment;
			}
		}));

	}

	List<ExternalIssue> parseTasks(@Nullable final String assignee, @Nullable final String reporter, @Nullable Element element) {
		// use the order defined by position attribute, not the order in the XML
		final List<Element> taskElements = Ordering.natural().onResultOf(new Function<Element, Comparable>() {
			@Override
			public Comparable apply(Element input) {
				return Integer.valueOf(input.getChildTextTrim("position"));
			}
		}).sortedCopy(XmlUtil.getChildren(element, "task"));

		return Lists.newArrayList(Collections2.transform(taskElements, new Function<Element, ExternalIssue>() {
			@Override
			public ExternalIssue apply(Element input) {
				final ExternalIssue subtask = new ExternalIssue();
				subtask.setAssignee(assignee);
				subtask.setReporter(reporter);
				subtask.setSummary(input.getChildText("description"));
				subtask.setCreated(parseDateTime(input.getChildText("created_at")).toDate());
				subtask.setIssueType("subtask");
				final boolean finished = Boolean.valueOf(input.getChildTextTrim("complete"));
				subtask.setStatus(finished ? SUBTASK_STATUS_FINISHED : SUBTASK_STATUS_OPEN);
				return subtask;
			}
		}));
	}

	public List<ExternalComment> parseNotes(@Nullable Element element) {
		return Lists.newArrayList(Collections2.transform(XmlUtil.getChildren(element, "note"), new Function<Element, ExternalComment>() {
			@Override
			public ExternalComment apply(Element note) {
				return new ExternalComment(note.getChildText("text"), note.getChildText("author"),
						parseDateTime(note.getChildText("noted_at")));
			}
		}));
	}

	DateTime parseDateTime(String str) {
		// not using joda time as it cannot parse timezones strings like UTC, EST, etc.
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_PATTERN, Locale.ENGLISH);
		final ParsePosition parsePosition = new ParsePosition(0);
		final Date date = simpleDateFormat.parse(str, parsePosition);
		if (date == null || parsePosition.getIndex() != str.length()) {
			throw new IllegalArgumentException("Cannot parse [" + str + "] to date/time using pattern [" + DATE_TIME_PATTERN + "]");
		}
		return new DateTime(date);
	}


	public List<ExternalIssue> parseStories(@Nullable Element stories) {
		return Immutables.transformThenCopyToList(XmlUtil.getChildren(stories, "story"), new Function<Element, ExternalIssue>() {
					public ExternalIssue apply(Element from) {
						return parseStory(from);
					}
				});
	}

}
