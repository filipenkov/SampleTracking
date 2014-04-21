package com.atlassian.jira.webtest.framework.model;

import com.atlassian.jira.webtest.framework.core.PageObject;

/**
 * A page object that is associated with a particular issue and is aware of its details.
 *
 * @since v4.3
 */
public interface IssueAware extends PageObject
{

    /**
     * Issue data of the associated issue.
     *
     * @return issue data
     */
    IssueData issueData();
}
