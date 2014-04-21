/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.external.beans.ExternalComment;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PvcsComment implements ExternalCommentMapper {
	private static final String STAR_TEAM_FORMAT = "MM/dd/yy hh:mm:ss a";

	@Override
	@Nullable
	public List<ExternalComment> buildFromMultiMap(Multimap<String, String> bean, ImportLogger log) {
		Collection<String> commentBodies = bean.get(IssueFieldConstants.COMMENT);
		if (commentBodies != null && !commentBodies.isEmpty()) {
			List externalComments = new ArrayList(commentBodies.size());

			final SimpleDateFormat startTeamDateFormat = new SimpleDateFormat(STAR_TEAM_FORMAT);
			for (Iterator iterator = commentBodies.iterator(); iterator.hasNext();) {
				String commentBody = (String) iterator.next();

				if (StringUtils.isNotEmpty(commentBody)) {
					String user = null;
					Date timePerformed = null;

					try {
						// Parse body
						StringTokenizer st = new StringTokenizer(commentBody, ":");
						st.nextToken(); // the type of comment

						user = StringUtils.strip(st.nextToken().toLowerCase()); // the user who added the comment

						String timeString = st.nextToken() + ":" + st.nextToken() + ":" + st.nextToken();
						timePerformed = startTeamDateFormat.parse(timeString);
					} catch (Exception e) {
						// Parsing is not really important enough to stop processing
						log.warn(e, "Exception occurred parsing comment. Some values may not be set");
					}
					externalComments.add(new ExternalComment(commentBody, user, timePerformed));
				}
			}
			return externalComments;
		} else {
			return null;
		}
	}
}
