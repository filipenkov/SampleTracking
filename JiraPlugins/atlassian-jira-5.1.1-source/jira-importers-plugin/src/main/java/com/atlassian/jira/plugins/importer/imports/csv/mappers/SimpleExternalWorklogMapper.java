/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalWorklog;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class SimpleExternalWorklogMapper implements ExternalWorklogMapper {

	public static final String DEFAULT_COMMENT = "no comment";

	private final CsvDateParser dateParser;

	public SimpleExternalWorklogMapper(CsvDateParser dateParser) {
		this.dateParser = dateParser;
	}

	@Override
    public List<ExternalWorklog> buildFromMultiMap(Multimap<String, String> bean, ExternalUserNameMapper userNameMapper, ImportLogger log) {
		final Collection<String> worklogs = bean.get(IssueFieldConstants.WORKLOG);
		if (worklogs != null && !worklogs.isEmpty()) {
			final List<ExternalWorklog> externalWorklogs = Lists.newArrayListWithCapacity(worklogs.size());
			for (String worklog : worklogs) {
				if (StringUtils.isNotEmpty(worklog)) {
					final ExternalWorklog externalWorklog = parseWorklog(worklog, userNameMapper, log);
					if (externalWorklog != null) {
						externalWorklogs.add(externalWorklog);
					}
				}
			}
			return externalWorklogs;
		} else {
			return null;
		}
    }
	
	
	@Nullable
	protected ExternalWorklog parseWorklog(String token, ExternalUserNameMapper userNameMapper, ImportLogger log) {
		try {
			final String[] tokens = StringUtils.splitPreserveAllTokens(token, ';');
			int i = tokens.length;

			final long timeSpent = Long.parseLong(StringUtils.strip(tokens[--i]));

			final String author = i > 0 ? userNameMapper.extractUserName(StringUtils.stripToNull(tokens[--i])) : null;

			final String dateString = i > 0 ? StringUtils.stripToNull(tokens[--i]) : null;
			final DateTime timePerformed = dateString != null ? new DateTime(dateParser.parseDate(dateString)) : null;

			final String comment;
			if (i == 0) {
				comment = DEFAULT_COMMENT;
			} else if (i == 1) {
				comment = tokens[0];
			} else {
				comment = StringUtils.join(tokens, ";", 0, i);
			}

			return new ExternalWorklog(author, comment, timePerformed, timeSpent);
		} catch (Exception e) {
			// Parsing is not really important enough to stop processing
			log.warn(e, "Exception occurred parsing worklog '%s'. It will be skipped", token);
			return null;
		}

	}

}
