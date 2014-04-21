package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.issue.Issue;

/**
 * An operation that defines an action URL and Simple description in order to provide a
 * list of "Actions" that can be done on an issue.
 *
 * @since v4.0
 */
public interface ActionBackedPluggableIssueOperation extends PluggableIssueOperation
{
    /**
     * The url to an action (including params). E.g. /secure/AssignIssue!default.jspa?id=12345
     *
     * @param issue The issue that the action will be performed on
     * @return The URL of the action.
     */
    String getActionURL(Issue issue);

    /**
     * A simple descrition of the action to perform.  This should contain only text, no HTML.  E.g. "Assign this issue"
     *
     * @param issue The issue that the action will be performed on
     * @return A simple description of the action
     */
    String getSimpleDescription(Issue issue);

}
