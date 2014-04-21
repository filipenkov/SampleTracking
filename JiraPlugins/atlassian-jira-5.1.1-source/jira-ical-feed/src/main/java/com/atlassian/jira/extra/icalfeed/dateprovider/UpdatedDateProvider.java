package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.UpdatedSystemField;

import java.util.Date;

public class UpdatedDateProvider extends AbstractSystemDateProvider
{
    @Override
    protected Date getStartDateInternal(Issue issue, Field field)
    {
        return issue.getUpdated();
    }

    @Override
    public boolean handles(Field field)
    {
        return field instanceof UpdatedSystemField;
    }
}
