package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import org.joda.time.DateTime;

public interface DateProvider
{
    DateTime getStart(Issue issue, String fieldName);

    DateTime getEnd(Issue issue, String fieldName);

    boolean isAllDay(Issue issue, String fieldName);

    boolean handles(String fieldName);
}
