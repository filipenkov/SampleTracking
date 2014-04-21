package com.atlassian.streams.jira;

import com.atlassian.streams.api.StreamsEntry;

/**
 * Factory that creates a list of entries from a list of issues
 */
public interface JiraEntryFactory
{
    /**
     * Convert the given list of issues to {@code Entry}s
     *
     * @param activityItems The activity items to convert
     * @return The converted entries
     */
    Iterable<StreamsEntry> getEntries(Iterable<AggregatedJiraActivityItem> activityItems);
}
