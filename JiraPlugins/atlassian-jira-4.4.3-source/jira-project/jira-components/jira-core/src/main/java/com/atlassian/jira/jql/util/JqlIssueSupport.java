package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import java.util.List;

/**
 * Some helper IssueLookup functions for JIRA.
 *
 * @since v4.0
 */
public interface JqlIssueSupport
{
    /**
     * Get the issue given its id if the passed user can see it. A null will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see the issue.
     *
     * @param id the id of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issue identified by the passed id if it can be seen by the passed user. A null value will be returned
     * if the issue does not exist or the user cannot see the issue.
     */
    Issue getIssue(long id, User user);

    /**
     * Get the issue given its id if the passed user can see it. A null will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see the issue.
     *
     * @param id the id of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issue identified by the passed id if it can be seen by the passed user. A null value will be returned
     * if the issue does not exist or the user cannot see the issue.
     */
    Issue getIssue(long id, com.opensymphony.user.User user);

    /**
     * Get the issue given its id. A null will be returned if the issue is not within JIRA.
     *
     * @param id the id of the issue to retrieve.
     * @return the issue identified by the passed id. A null value will be returned if the issue does not exist.
     */
    Issue getIssue(long id);

    /**
     * Get the issues with the passed key if the passed user can see it. This tries to do a case insensitive lookup
     * which is why it can return multiple values. An empty list will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see any of the issues.
     *
     * @param issueKey they key of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issues identified by the passed key if they can be seen by the passed user.
     * An empty list will be returned if the issue key is not within JIRA or if the user does not have
     * permission to see any of the issues.
     * @see #getIssues(String) for a version with no permission check
     */
    List<Issue> getIssues(String issueKey, User user);

    /**
     * Get the issues with the passed key if the passed user can see it. This tries to do a case insensitive lookup
     * which is why it can return multiple values. An empty list will be returned if the issue key is
     * not within JIRA or if the user does not have permission to see any of the issues.
     *
     * @param issueKey they key of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @param user the user who must have permission to see the issue.
     * @return the issues identified by the passed key if they can be seen by the passed user.
     * An empty list will be returned if the issue key is not within JIRA or if the user does not have
     * permission to see any of the issues.
     * @see #getIssues(String) for a version with no permission check
     */
    List<Issue> getIssues(String issueKey, com.opensymphony.user.User user);

    /**
     * Get the issues with the passed key. This tries to do a case insensitive lookup which is why it can return
     * multiple values. An empty list will be returned if the issue key is not within JIRA.
     *
     * @param issueKey they key of the issue to retreieve. A null key is assumed not to exist within JIRA.
     * @return the issues identified by the passed key. An empty list will be returned if the issue key is not within
     * JIRA.
     * @see #getIssues(String, User) for a version with permission checks
     */
    List<Issue> getIssues(String issueKey);
}
