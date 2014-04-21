package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import org.joda.time.DateTime;

import java.util.Date;

public abstract class AbstractSystemDateProvider implements DateProvider
{
    @Override
    public DateTime getStart(Issue issue, Field field)
    {
        Date startDate = getStartDateInternal(issue, field);
        return null == startDate ? null : new DateTime(startDate.getTime());
    }

    protected abstract Date getStartDateInternal(Issue issue, Field field);

    @Override
    public DateTime getEnd(Issue issue, Field field, DateTime startDate)
    {
        return null == startDate ? null : startDate.plusDays(1);
    }

    @Override
    public boolean isAllDay(Issue issue, Field field)
    {
        return true;
    }
}
