package com.atlassian.jira.extra.icalfeed.dateprovider;


import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

public class CreatedDateProvider extends AbstractSystemDateProvider
{
    @Override
    protected Date getStartDateInternal(Issue issue, String fieldName)
    {
        return issue.getCreated();
    }

    @Override
    public boolean handles(String fieldName)
    {
        return StringUtils.equals(DocumentConstants.ISSUE_CREATED, fieldName);
    }
}
