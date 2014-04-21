package com.atlassian.sal.jira.executor;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.velocity.SimpleVelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.easymock.EasyMockAnnotations.initMocks;
import static com.atlassian.jira.easymock.EasyMockAnnotations.replayMocks;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;

public class TestJiraThreadLocalContextManager
{
    @Mock
    Tenant mockTenant;

    @Mock
    JiraAuthenticationContextImpl mockAuthenticationContext;

    @Mock
    TenantReference mockTenantReference;

    @Mock
    VelocityRequestContextFactory mockVelocityRequestContextFactory;

    @Test
    public void testGetThreadLocalContext() throws InterruptedException
    {
        final SimpleVelocityRequestContext requestContext = new SimpleVelocityRequestContext("someurl");

        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(null);
        expect(mockTenantReference.get()).andReturn(mockTenant);
        expect(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(requestContext);
        replayMocks(this);

        JiraThreadLocalContextManager manager = new JiraThreadLocalContextManager(mockAuthenticationContext, mockTenantReference, mockVelocityRequestContextFactory);
        Object ctx = manager.getThreadLocalContext();
        Assert.assertNotNull(ctx);
        JiraThreadLocalContextManager.JiraThreadLocalContext context = (JiraThreadLocalContextManager.JiraThreadLocalContext) ctx;
        Assert.assertNull(context.getUser());
        Assert.assertSame(mockTenant, context.getTenant());
        Assert.assertSame(requestContext, context.getVelocityRequestContext());

        verify(mockTenant, mockAuthenticationContext, mockTenantReference, mockVelocityRequestContextFactory);
    }

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
    }
}
