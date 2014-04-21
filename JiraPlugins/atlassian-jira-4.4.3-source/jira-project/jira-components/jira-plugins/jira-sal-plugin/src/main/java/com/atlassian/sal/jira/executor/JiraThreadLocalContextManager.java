package com.atlassian.sal.jira.executor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;
import com.atlassian.sal.core.executor.ThreadLocalContextManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * Manages the thread local state for JIRA
 */
public class JiraThreadLocalContextManager implements ThreadLocalContextManager
{
    private final JiraAuthenticationContext authenticationContext;
    private final TenantReference tenantReference;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public JiraThreadLocalContextManager(JiraAuthenticationContext authenticationContext, TenantReference tenantReference, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.authenticationContext = authenticationContext;
        this.tenantReference = tenantReference;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    /**
     * Get the thread local context of the current thread
     *
     * @return The thread local context
     */
    public Object getThreadLocalContext()
    {
        return new JiraThreadLocalContext(authenticationContext.getLoggedInUser(), tenantReference.get(), velocityRequestContextFactory.getJiraVelocityRequestContext());
    }

    /**
     * Set the thread local context on the current thread
     *
     * @param context The context to set
     */
    public void setThreadLocalContext(Object context)
    {
        JiraThreadLocalContext ctx = (JiraThreadLocalContext) context;
        tenantReference.set(ctx.getTenant(), false);
        authenticationContext.setLoggedInUser(ctx.getUser());
        velocityRequestContextFactory.cacheVelocityRequestContext(ctx.getVelocityRequestContext());
    }

    /**
     * Clear the thread local context on the current thread
     */
    public void clearThreadLocalContext()
    {
        velocityRequestContextFactory.clearVelocityRequestContext();
        authenticationContext.setLoggedInUser(null);
        tenantReference.remove();
    }

    static class JiraThreadLocalContext
    {
        private final User user;
        private final Tenant tenant;
        private final VelocityRequestContext velocityRequestContext;

        private JiraThreadLocalContext(User user, Tenant tenant, VelocityRequestContext velocityRequestContext)
        {
            this.user = user;
            this.tenant = tenant;
            this.velocityRequestContext = velocityRequestContext;
        }

        public User getUser()
        {
            return user;
        }

        public Tenant getTenant()
        {
            return tenant;
        }

        public VelocityRequestContext getVelocityRequestContext()
        {
            return velocityRequestContext;
        }
    }

}
