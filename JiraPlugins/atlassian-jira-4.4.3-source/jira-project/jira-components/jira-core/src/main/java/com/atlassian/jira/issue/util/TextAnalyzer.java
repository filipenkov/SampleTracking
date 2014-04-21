/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

/** Hook for passive analysis of all text entered into JIRA. */
public interface TextAnalyzer
{
    public void analyseContent(GenericValue issue, final String content);

    public void analyseContent(GenericValue issue, final String content, GenericValue action);

    public void analyseContent(Issue issue, final String content);

    public void analyseContent(Issue issue, final String content, GenericValue action);
}
