package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * A helper to provide activity related to collectors.
 */
public interface CollectorActivityHelper
{
    static final String COLLECTOR_LABEL_PREFIX = "collector-";

    /**
     * Given a collector this method returns the JQL string used to return all issues created by this collector.
     *
     * @param loggedInUser The user performing the search
     * @param collector The collector for which to return activity
     * @return The JQL string used to return activity for this collector
     */
    String getJql(final User loggedInUser, final Collector collector);

    /**
     * Returns the issue navigator URL needed to display activity for the collector provided.
     *
     * @param loggedInUser The user performing the search
     * @param collector The collector for which to return activity
     * @return the issue navigator URL needed to display activity for the collector provided
     */
    String getIssueNavigatorUrl(final User loggedInUser, final Collector collector);

    /**
     * Returns a list of issue counts created per day for the collector provided.  This can be used to display a daily
     * activity chart for a collector.
     *
     * @param loggedInUser The user performing the search
     * @param collector The collector for which to return activity
     * @param daysPast The number of days to look for activity in the past
     * @return A list of issue counts per day for the number of days in the past.
     */
    List<Integer> getIssuesCreatedPerDay(final User loggedInUser, final Collector collector, int daysPast);

	/**
	 * Returns the list of recent issues ordered by creation date (desc).
	 *
	 * @param loggedInUser The user performing the search
	 * @param collector The collector for which to return activity
	 * @return List of recent issues
	 */
	public List<Issue> getCollectorIssues(User loggedInUser, Collector collector, int limit);

	/**
	 * Returns the sum of all issues created with the collector
	 *
	 * @param loggedInUser The user performing the search
	 * @param collector The collector for which to return count
	 * @return number of issues
	 */
	public int getAllCollectorIssuesCount(final User loggedInUser, final Collector collector);
}
