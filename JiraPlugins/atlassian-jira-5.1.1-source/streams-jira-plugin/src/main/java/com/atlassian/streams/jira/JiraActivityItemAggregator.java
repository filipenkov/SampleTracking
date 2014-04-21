package com.atlassian.streams.jira;


/**
 * Combines related activity item entries under certain conditions
 */
public interface JiraActivityItemAggregator
{
    /**
     * Aggregate related activity item entries under certain conditions into a single entry
     * @param activityItems the activity item list to aggregate
     * @return the modified activity item list with combined entries
     */
    public Iterable<AggregatedJiraActivityItem> aggregate(Iterable<JiraActivityItem> activityItems);
}
