package com.atlassian.jira.issue.search.util;

/**
 * 
 * @since v4.0
 */
public interface QueryCreator
{
    String QUERY_PREFIX = "IssueNavigator.jspa?reset=true&mode=show&summary=true&description=true&body=true";
    String NULL_QUERY = "IssueNavigator.jspa?mode=show";

    String createQuery(String searchString);
}
