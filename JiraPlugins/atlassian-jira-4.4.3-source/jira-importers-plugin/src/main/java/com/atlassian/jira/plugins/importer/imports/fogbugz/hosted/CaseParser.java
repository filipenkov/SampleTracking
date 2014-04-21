/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.hosted;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomFieldValue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.SimpleExternalLabelsMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.ResolutionValueMapper;
import com.atlassian.jira.plugins.importer.imports.fogbugz.config.StatusValueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.pivotal.PivotalExternalAttachment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class CaseParser {

	private static final int SECONDS_IN_HOUR = 3600;
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssz";

	private final SimpleExternalLabelsMapper.CreateLabelFromString labelFactory	=
			new SimpleExternalLabelsMapper.CreateLabelFromString();

	public ExternalIssue parseCase(Element element, ImportLogger log) {
		final String summary = element.getChildText("sTitle");

		if (StringUtils.isBlank(summary)) {
			log.warn("Issuename is blank and is not imported");
			return null;
		}

		final String oldId = element.getChildText("ixBug");
		final ExternalIssue externalIssue = new ExternalIssue();
		externalIssue.setExternalId(oldId);
		externalIssue.setSummary(summary);
		//externalIssue.setDescription(summary);
		externalIssue.setIssueType(element.getChildText("sCategory"));

		final String ixPriority = element.getChildText("ixPriority");
		final String sPriority = element.getChildText("sPriority");
		if (StringUtils.isNotEmpty(ixPriority)) {
			externalIssue.setPriority(ixPriority + "-" + sPriority);
		}

		externalIssue.setExternalComponents(Lists.<String>newArrayList(element.getChildText("sArea")));

		final String version = element.getChildText("sVersion");
		if (StringUtils.isNotBlank(version)) {
			externalIssue.setAffectedVersions(Lists.<String>newArrayList(version));
		}

		final String fixFor = element.getChildText("sFixFor");
		if (StringUtils.isNotBlank(fixFor)) {
			externalIssue.setFixedVersions(Lists.<String>newArrayList(fixFor));
		}

		externalIssue.setReporter(element.getChildText("ixPersonOpenedBy"));
		externalIssue.setAssignee(element.getChildText("ixPersonAssignedTo"));

		String dtOpened = element.getChildText("dtOpened"), dtDue = element.getChildText("dtDue");
		if (StringUtils.isNotEmpty(dtOpened)) {
			externalIssue.setCreated(parseDateTime(dtOpened));
		}
		if (StringUtils.isNotEmpty(dtDue)) {
			externalIssue.setDuedate(parseDateTime(dtDue));
		}

		externalIssue.setLabels(parseTags(element.getChild("tags")));

		final DateTime closed = StringUtils.isNotEmpty(element.getChildText("dtClosed"))
				? parseDateTime(element.getChildText("dtClosed")) : null;
		final DateTime resolved = StringUtils.isNotEmpty(element.getChildText("dtResolved"))
				? parseDateTime(element.getChildText("dtResolved")) : null;
		//don't need to do a null check here for the resolved date, since the ExternalUtils.convertExternalIssueToIssue()
		//method used by the DefaultJiraDataImporter will fall back to the last updated date anyway, if this is null
		//and the issue is marked as resolved.
		externalIssue.setResolutionDate(resolved);
		if ((closed != null) && (resolved == null)) {
			externalIssue.setUpdated(closed);
		} else if ((closed == null) && (resolved != null)) {
			externalIssue.setUpdated(resolved);
		} else if (closed != null) {
			if (closed.isAfter(resolved)) {
				externalIssue.setUpdated(closed);
			} else {
				externalIssue.setUpdated(resolved);
			}
		}

//		externalIssue.setExternalCustomFieldValues(getCustomFieldValues(rs, oldId));

		final String fogBugzStatus = element.getChildText("sStatus");
		final String cleanedResolution = ResolutionValueMapper.getCleanedResolution(fogBugzStatus);
		final String cleanedStatus = StatusValueMapper.getCleanedStatus(fogBugzStatus);

		externalIssue.setStatus(Boolean.valueOf(element.getChildText("fOpen")) ? cleanedStatus : IssueFieldConstants.CLOSED_STATUS);
		if (cleanedResolution != null) {
			externalIssue.setResolution(cleanedResolution);
		}

		// Deal with work estimates
		final long originalEstimate = element.getChildText("hrsOrigEst") != null
				? (long)(Double.valueOf(element.getChildText("hrsOrigEst")) * SECONDS_IN_HOUR) : 0;
		final long timeSpent = element.getChildText("hrsElapsed") != null
				? (long)(Double.valueOf(element.getChildText("hrsElapsed")) * SECONDS_IN_HOUR) : 0;

		final double hrsCurrEst = element.getChildText("hrsCurrEst") != null
				? Double.valueOf(element.getChildText("hrsCurrEst")) : 0;
		final long currentEstimate = (long)(hrsCurrEst * SECONDS_IN_HOUR) - timeSpent;

		externalIssue.setOriginalEstimate(originalEstimate > 0 ? originalEstimate : null);
		externalIssue.setEstimate(currentEstimate > 0 ? currentEstimate : null);
		externalIssue.setTimeSpent(timeSpent > 0 ? timeSpent : null);

		// Deal with comments
		externalIssue.setExternalComments(parseComments(element.getChild("events")));
		externalIssue.setAttachments(parseAttachments(element.getChild("events")));

		final List<ExternalCustomFieldValue> customFields = Lists.newArrayList();
		customFields.add(new ExternalCustomFieldValue("Customer Email",
				CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.TEXT_FIELD_SEARCHER,
				element.getChildText("sCustomerEmail")));

		customFields.add(new ExternalCustomFieldValue("Computer",
				CustomFieldConstants.TEXT_FIELD_TYPE, CustomFieldConstants.TEXT_FIELD_SEARCHER,
				element.getChildText("sComputer")));

		externalIssue.setExternalCustomFieldValues(customFields);

		return externalIssue;
	}

	protected Set<Label> parseTags(@Nullable Element tags) {
		final Set<Label> result = Sets.newHashSet();
		if (tags != null) {
			for(Object tag : tags.getChildren("tag")) {
				final String cleanLabel = LabelParser.getCleanLabel(((Element) tag).getText());
				if (StringUtils.isNotBlank(cleanLabel)) {
					result.add(labelFactory.create(cleanLabel));
				}
			}
		}
		return result;
	}

	List<ExternalComment> parseComments(@Nullable Element events) {
		final Collection<Element> comments = Collections2.filter(XmlUtil.getChildren(events, "event"),
				new Predicate<Element>() {
					@Override
					public boolean apply(@Nullable Element input) {
						return input != null && StringUtils.isNotBlank(input.getChildText("s"));
					}
				});

		return Lists.newArrayList(Collections2.transform(comments, new Function<Element, ExternalComment>() {
			@Override
			public ExternalComment apply(Element element) {
				final String commenter = element.getChildText("ixPerson");
				final String comment = element.getChildText("s");

				if (StringUtils.isNotBlank(comment)) {
					final String body;
					if (!Boolean.valueOf(element.getChildText("fEmail"))) {
						body = StringUtils.trimToEmpty(comment);
					} else {
						body = ExternalUtils.getTextDataFromMimeMessage(comment);
					}

					return new ExternalComment(body, commenter, parseDateTime(element.getChildText("dt")));
				}

				return null;
			}
		}));
	}

	List<ExternalAttachment> parseAttachments(@Nullable Element events) {
		List<ExternalAttachment> attachments = Lists.newArrayList();

		for(Element event : XmlUtil.getChildren(events, "event")) {
			for(Element rgAttachments : XmlUtil.getChildren(event, "rgAttachments")) {
				for(Element attachment : XmlUtil.getChildren(rgAttachments, "attachment")) {
					final String filename = attachment.getChildText("sFileName");
					final String url = attachment.getChildTextNormalize("sURL").replace("&amp;", "&");
					final DateTime uploadedAt = parseDateTime(event.getChildText("dt"));

					final PivotalExternalAttachment pae = new PivotalExternalAttachment(filename, url, uploadedAt.toDate());
					pae.setAttacher(event.getChildText("ixPerson"));
//					pae.setDescription(event.getChildText("description"));

					attachments.add(pae);
				}
			}
		};

		return attachments;
	}

	static DateTime parseDateTime(String str) {
		if (str.endsWith("Z")) {
			str = StringUtils.removeEnd(str, "Z") + "-0000";
		}
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_PATTERN, Locale.ENGLISH);
		final ParsePosition parsePosition = new ParsePosition(0);
		final Date date = simpleDateFormat.parse(str, parsePosition);
		if (date == null || parsePosition.getIndex() != str.length()) {
			throw new IllegalArgumentException("Cannot parse [" + str + "] to date/time using pattern [" + DATE_TIME_PATTERN + "]");
		}
		return new DateTime(date);
	}


	public List<ExternalIssue> parseCases(@Nullable Element stories, final ImportLogger log) {
		return Immutables.transformThenCopyToList(XmlUtil.getChildren(stories, "case"), new Function<Element, ExternalIssue>() {
					public ExternalIssue apply(Element from) {
						return parseCase(from, log);
					}
				});
	}

	public List<ExternalLink> parseSubcases(@Nullable Element stories, final ImportLogger log) {
		return Immutables.transformThenCopyToList(Collections2.filter(XmlUtil.getChildren(stories, "case"), new Predicate<Element>() {
			@Override
			public boolean apply(@Nullable Element input) {
				return input != null && StringUtils.isNotEmpty(input.getChildText("ixBugParent"))
						&& !"0".equals(input.getChildText("ixBugParent"))
						&& StringUtils.isNotEmpty(input.getChildText("ixBug"));
			}
		}), new Function<Element, ExternalLink>() {
			@Override
			public ExternalLink apply(Element from) {
				return new ExternalLink(FogBugzConfigBean.SUBCASE_LINK_NAME,
						from.getChildText("ixBugParent"), from.getChildText("ixBug"));
			}
		});
	}

}
