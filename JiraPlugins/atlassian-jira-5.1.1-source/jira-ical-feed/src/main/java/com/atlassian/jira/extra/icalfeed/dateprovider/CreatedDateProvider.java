package com.atlassian.jira.extra.icalfeed.dateprovider;


import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CreatedSystemField;
import com.atlassian.jira.issue.fields.Field;

import java.util.Date;

public class CreatedDateProvider extends AbstractSystemDateProvider
{
    @Override
    protected Date getStartDateInternal(Issue issue, Field field)
    {
        return issue.getCreated();
    }

    @Override
    public boolean handles(Field field)
    {
        return field instanceof CreatedSystemField;
    }
}
