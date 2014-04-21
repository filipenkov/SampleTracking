package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.issue.Issue;

import java.util.HashMap;
import java.util.Map;

/**
 * A basic issue operation that should serve most purposes, just uses a Velocity view.
 * <p/>
 * Also this is a very useful class to extend for people writing their own issue operation plugins.
 */
public class DefaultPluggableIssueOperation implements PluggableIssueOperation
{
    private IssueOperationModuleDescriptor descriptor;

    public void init(IssueOperationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    /**
     * Get the HTML for this operation.  The following variable are available in the
     * velocity context:
     * <dl>
     *  <dt>issueoperation</dt>
     *  <dd>This class - an instance of {@link DefaultPluggableIssueOperation}</dd>
     * </dl>
     * <dl>
     *  <dt>issue</dt>
     *  <dd>This issue - an instance of {@link Issue}</dd>
     * </dl>
     * <dl>
     *  <dt>descriptor</dt>
     *  <dd>This module descriptor - an instance of {@link IssueOperationModuleDescriptor}</dd>
     * </dl>
     */
    public String getHtml(Issue issue)
    {
        Map params = new HashMap();
        params.put("issueoperation", this);
        params.put("issue", issue);
        params.put("descriptor", descriptor);

        return descriptor.getHtml("view", params);
    }

    public boolean showOperation(Issue issue)
    {
        return true;
    }
}
