package com.atlassian.jira.plugin.issueoperation;

import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

/**
 * A very simple helper class that abstracts away the handling of the descriptor, and also provides
 * a standard way of producing the cream bullet that most operations use.
 */
public abstract class AbstractPluggableIssueOperation implements PluggableIssueOperation
{
    protected IssueOperationModuleDescriptor descriptor;
    private final VelocityRequestContextFactory requestContextFactory = new DefaultVelocityRequestContextFactory();

    public void init(IssueOperationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    protected String getBullet()
    {
        return "<img src=\"" + requestContextFactory.getJiraVelocityRequestContext().getBaseUrl() + "/images/icons/bullet_creme.gif\" height=\"8\" width=\"8\" border=\"0\" align=\"absmiddle\" > ";
    }
}
