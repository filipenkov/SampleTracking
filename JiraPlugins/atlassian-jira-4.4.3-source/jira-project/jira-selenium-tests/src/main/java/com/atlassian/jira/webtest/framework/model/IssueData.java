package com.atlassian.jira.webtest.framework.model;

/**
 * Issue meta data, used by components associated with a particular issue.
 *
 * @since v4.3
 */
public interface IssueData
{
    /**
     * Database ID of the issue.
     *
     * @return ID of the issue
     */
    long id();

    /**
     * Issue key.
     *
     * @return key of the issue
     */
    String key();
}
