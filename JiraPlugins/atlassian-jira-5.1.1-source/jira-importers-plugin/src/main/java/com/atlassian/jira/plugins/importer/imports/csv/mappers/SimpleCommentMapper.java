/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
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

	private final CsvDateParser dateParser;

	/* for future generations:
		(?s) means DOTALL mode (multiline)
		?+ in (?:(.+?);)?+ means _possessive_ (greedy without backing out) {0-1} - not an error despite what IDEA says
		+? is reluctant
		(?: in (?:(.+?);)?+ means only inner set of () is used as matching groups, outer is just grouping

		NEVER EVER touch this before adding test to SimpleCommentMapperTest first!
	 */
	private static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)(?:(.+?);)?+(?:(.+?);)?+(.*)");

	public SimpleCommentMapper(CsvDateParser dateParser) {
		this.dateParser = dateParser;
	}

	@Nullable
	public List<ExternalComment> buildFromMultiMap(Multimap<String, String> bean, ExternalUserNameMapper userNameMapper, ImportLogger log) {
		final Collection<String> commentBodies = bean.get(IssueFieldConstants.COMMENT);
		if (commentBodies != null && !commentBodies.isEmpty()) {
			final List<ExternalComment> externalComments = Lists.newArrayListWithCapacity(commentBodies.size());
			for (String commentBody : commentBodies) {
				if (StringUtils.isNotEmpty(commentBody)) {
					externalComments.add(parseComment(commentBody, userNameMapper, log));
				}
			}
			return externalComments;
		} else {
			return null;
		}
	}

	protected ExternalComment parseComment(String commentBody, ExternalUserNameMapper userNameMapper, ImportLogger log) {
		String user = null;
		Date timePerformed = null;

		try {
			Matcher match = COMMENT_PATTERN.matcher(commentBody);
			if (match.matches()) {
				int groups = match.groupCount();

				if (groups > 1 && match.group(1) != null) {
					timePerformed = dateParser.parseDate(StringUtils.strip(match.group(1)));
				}

				if (groups > 2 && match.group(2) != null) {
					user = userNameMapper.extractUserName(StringUtils.strip(match.group(2))); // the user who added the comment
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
