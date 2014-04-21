/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 1:25:07 PM
 */
package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.issue.Issue;

/**
 * A simple interface to add your own operations to JIRA via the plugin system.
 *
 * @deprecated Use WebItems instead. See <a href=http://confluence.atlassian.com/display/JIRADEV/Issue+Operations+Plugin+Module>Issue Operations Plugin Module docs</a>. Since v4.1.
 */
public interface PluggableIssueOperation
{
    void init(IssueOperationModuleDescriptor descriptor);

    /**
     * Get the HTML to present on screen
     */
    String getHtml(Issue issue);

    /**
     * Whether or not to show this operation for the given issue.
     */
    boolean showOperation(Issue issue);
}