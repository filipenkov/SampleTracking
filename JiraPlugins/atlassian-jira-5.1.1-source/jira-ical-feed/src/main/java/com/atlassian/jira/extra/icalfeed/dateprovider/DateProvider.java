package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import org.joda.time.DateTime;

public interface DateProvider
{
    /**
     * Implementations to return a start date based on the specified field.
     * @param issue
     * The issue containing the field.
     * @param field
     * The field to interpret as start date.
     * @return
     * A {@link DateTime} representing the start date.
     */
    DateTime getStart(Issue issue, Field field);

    /**
     * Implementations to return an end date based on the specified field.
     * @param issue
     * The issue containing the field.
     * @param field
     * The field to interpret as end date.
     * @param startDate
     * The startDate returned by {@link #getStart(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.fields.Field)}
     * @return
     * A {@link DateTime} representing the end date.
     */
    DateTime getEnd(Issue issue, Field field, DateTime startDate);

    /**
     * Implementations to return a flag indicating if a field should be interpreted as an all-day event.
     * @param issue
     * The issue containing the field.
     * @param field
     * The field to interpret.
     * @return
     * Returns {@code true} if the field should be interpreted as an all-day event; {@code false} otherwise.
     */
    boolean isAllDay(Issue issue, Field field);

    /**
     * Implementations to return a flag indicating if it handles the specified field,
     * @param field
     * The field to handle.
     * @return
     * Returns {@code true} if the field can be handled; {@code false} otherwise.
     */
    boolean handles(Field field);
}
