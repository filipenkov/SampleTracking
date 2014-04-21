package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.DueDateSystemField;
import com.atlassian.jira.issue.fields.Field;

import java.util.Date;

public class DueDateProvider extends AbstractSystemDateProvider
{
    @Override
    protected Date getStartDateInternal(Issue issue, Field field)
    {
        return issue.getDueDate();
    }

    @Override
    public boolean handles(Field field)
    {
        return field instanceof DueDateSystemField;
    }
}
