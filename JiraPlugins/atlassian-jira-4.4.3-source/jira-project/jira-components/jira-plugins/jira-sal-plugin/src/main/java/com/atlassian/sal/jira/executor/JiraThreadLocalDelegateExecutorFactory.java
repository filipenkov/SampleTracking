package com.atlassian.sal.jira.executor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.multitenant.TenantReference;
import com.atlassian.sal.core.executor.DefaultThreadLocalDelegateExecutorFactory;

/**
 * Instance of the delegate executor factory tailored to JIRA
 */
public class JiraThreadLocalDelegateExecutorFactory extends DefaultThreadLocalDelegateExecutorFactory
{
    public JiraThreadLocalDelegateExecutorFactory(JiraAuthenticationContext authenticationContext,
            TenantReference tenantReference, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        super(new JiraThreadLocalContextManager(authenticationContext, tenantReference, velocityRequestContextFactory));
    }
}
