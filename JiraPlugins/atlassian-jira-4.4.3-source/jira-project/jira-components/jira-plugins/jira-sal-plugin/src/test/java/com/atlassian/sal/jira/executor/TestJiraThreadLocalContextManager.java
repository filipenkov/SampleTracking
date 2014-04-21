package com.atlassian.sal.jira.executor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.SimpleVelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestJiraThreadLocalContextManager extends TestCase
{
    public void testGetThreadLocalContext() throws InterruptedException
    {
        Mock mockTenant = new Mock(Tenant.class);
        Tenant tenant = (Tenant) mockTenant.proxy();
        final SimpleVelocityRequestContext requestContext = new SimpleVelocityRequestContext("someurl");
        Mock mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        Mock mockTenantReference = new Mock(TenantReference.class);
        Mock mockVelocityRequestContextFactory = new Mock(VelocityRequestContextFactory.class);
        JiraThreadLocalContextManager manager = new JiraThreadLocalContextManager((JiraAuthenticationContext) mockAuthenticationContext.proxy(), (TenantReference) mockTenantReference.proxy(), (VelocityRequestContextFactory) mockVelocityRequestContextFactory.proxy());
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", null);
        mockTenantReference.expectAndReturn("get", tenant);
        mockVelocityRequestContextFactory.expectAndReturn("getJiraVelocityRequestContext", requestContext);
        Object ctx = manager.getThreadLocalContext();
        assertNotNull(ctx);
        JiraThreadLocalContextManager.JiraThreadLocalContext context = (JiraThreadLocalContextManager.JiraThreadLocalContext) ctx;
        assertNull(context.getUser());
        assertSame(tenant, context.getTenant());
        assertSame(requestContext, context.getVelocityRequestContext());
        mockAuthenticationContext.verify();
    }

    /* Commented out due to mockobjects bug when passing null
    public void testSetThreadLocalContext() throws Exception, ExecutionException, ImmutableException, DuplicateEntityException
    {
        Mock mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        JiraThreadLocalContextManager manager = new JiraThreadLocalContextManager((JiraAuthenticationContext) mockAuthenticationContext.proxy());
        mockAuthenticationContext.expectAndReturn("setUser", C.args(C.IS_NULL));
        manager.setThreadLocalContext(null);
        mockAuthenticationContext.verify();
    }

    public void testClearThreadLocalContext() throws InterruptedException, ExecutionException
    {
        Mock mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        JiraThreadLocalContextManager manager = new JiraThreadLocalContextManager((JiraAuthenticationContext) mockAuthenticationContext.proxy());
        mockAuthenticationContext.expectAndReturn("setUser", C.args(C.IS_NULL));
        manager.setThreadLocalContext(null);
        mockAuthenticationContext.verify();
    }
    */
}
