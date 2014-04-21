package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

public class DisabledTextAnalyzer implements TextAnalyzer
{
    public void analyseContent(GenericValue issue, String content)
    {
        //do nothing - we are disabled!
    }

    public void analyseContent(GenericValue issue, String content, GenericValue action)
    {
        //do nothing - we are disabled!
    }

    public void analyseContent(Issue issue, final String content)
    {
        //do nothing - we are disabled!
    }

    public void analyseContent(Issue issue, final String content, GenericValue action)
    {
        //do nothing - we are disabled!
    }
}
