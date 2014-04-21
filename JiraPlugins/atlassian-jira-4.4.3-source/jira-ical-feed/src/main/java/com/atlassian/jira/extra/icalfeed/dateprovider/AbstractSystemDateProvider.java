package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import org.joda.time.DateTime;

import java.util.Date;

public abstract class AbstractSystemDateProvider implements DateProvider
{

    @Override
    public DateTime getStart(Issue issue, String fieldName)
    {
        Date startDate = getStartDateInternal(issue, fieldName);
        return null == startDate ? null : new DateTime(startDate.getTime());
    }

    protected abstract Date getStartDateInternal(Issue issue, String fieldName);

    @Override
    public DateTime getEnd(Issue issue, String fieldName)
    {
        DateTime startDate = getStart(issue, fieldName);
        return null == startDate ? null : startDate.plusDays(1);
    }

    @Override
    public boolean isAllDay(Issue issue, String fieldName)
    {
        return true;
    }
}
