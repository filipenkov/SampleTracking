/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleCommentMapper implements ExternalCommentMapper {

	private final CsvConfigBean configBean;

	private final static Pattern COMMENT_PATTERN = Pattern.compile("(?s)(?:(.+?);)?+(?:(.+?);)?+(.*)");

	public SimpleCommentMapper(CsvConfigBean configBean) {
		this.configBean = configBean;
	}

	@Nullable
	public List<ExternalComment> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		final Collection<String> commentBodies = bean.get(IssueFieldConstants.COMMENT);
		if (commentBodies != null && !commentBodies.isEmpty()) {
			final List<ExternalComment> externalComments = Lists.newArrayListWithCapacity(commentBodies.size());
			for (String commentBody : commentBodies) {
				if (StringUtils.isNotEmpty(commentBody)) {
					externalComments.add(parseComment(commentBody, log));
				}
			}
			return externalComments;
		} else {
			return null;
		}
	}

	protected ExternalComment parseComment(String commentBody, ImportLogger log) {
		String user = null;
		Date timePerformed = null;

		try {
			Matcher match = COMMENT_PATTERN.matcher(commentBody);
			if (match.matches()) {
				int groups = match.groupCount();

				if (groups > 1 && match.group(1) != null) {
					timePerformed = configBean.parseDate(StringUtils.strip(match.group(1)));
				}

				if (groups > 2 && match.group(2) != null) {
					user = StringUtils.strip(match.group(2)).toLowerCase(); // the user who added the comment
				}

				commentBody = StringUtils.strip(match.group(groups));
			}
		 } catch (Exception e) {
			// Parsing is not really important enough to stop processing
			log.warn(e, "Exception occurred parsing comment. Some values may not be set");
		}

		return new ExternalComment(commentBody, user, timePerformed);
	}
}
