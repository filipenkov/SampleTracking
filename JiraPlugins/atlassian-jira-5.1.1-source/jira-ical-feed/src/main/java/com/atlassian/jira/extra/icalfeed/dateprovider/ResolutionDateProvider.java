package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.ResolutionDateSystemField;

import java.util.Date;

public class ResolutionDateProvider extends AbstractSystemDateProvider
{
    @Override
    protected Date getStartDateInternal(Issue issue, Field field)
    {
        return issue.getResolutionDate();
    }

    @Override
    public boolean handles(Field field)
    {
        return field instanceof ResolutionDateSystemField;
    }
}
