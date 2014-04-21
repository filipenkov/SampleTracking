/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.TextAnalyzer;
import org.ofbiz.core.entity.GenericValue;

public class MockTextAnalyzer implements TextAnalyzer
{
    public void analyseContent(GenericValue issue, String content)
    {
        //do nothing.
    }

    public void analyseContent(GenericValue issue, String content, GenericValue action)
    {
        //do nothing.
    }

    public void analyseContent(Issue issue, final String content)
    {
        //do nothing.
    }

    public void analyseContent(Issue issue, final String content, GenericValue action)
    {
        //do nothing.
    }
}
