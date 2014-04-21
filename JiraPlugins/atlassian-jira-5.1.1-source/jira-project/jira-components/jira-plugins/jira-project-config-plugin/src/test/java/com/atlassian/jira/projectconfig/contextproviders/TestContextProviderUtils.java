package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.order.MockComparatorFactory;
import com.atlassian.jira.projectconfig.util.MockProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.ProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.UrlEncoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link ContextProviderUtils}
 *
 * @since v4.4
 */
public class TestContextProviderUtils
{
    private IMocksControl mockControl;
    private VelocityRequestContextFactory requestContextFactory;
    private Project project;
    private VelocityRequestContext requestContext;
    private JiraAuthenticationContext authenticationContext;
    private User user;
    private PermissionManager permissionManager;
    private I18nHelper i18nHelper;
    private UrlEncoder encoder;
    private ProjectConfigRequestCache cache;
    private MockComparatorFactory comparatorFactory;

    @Before
    public void setUp()
    {
        mockControl = EasyMock.createControl();

        authenticationContext = mockControl.createMock(JiraAuthenticationContext.class);
        requestContextFactory = mockControl.createMock(VelocityRequestContextFactory.class);
        permissionManager = mockControl.createMock(PermissionManager.class);

        requestContext = mockControl.createMock(VelocityRequestContext.class);
        user = mockControl.createMock(User.class);

        project = mockControl.createMock(Project.class);
        i18nHelper = mockControl.createMock(I18nHelper.class);
        encoder = mockControl.createMock(UrlEncoder.class);
        cache = new MockProjectConfigRequestCache();
        comparatorFactory = new MockComparatorFactory();
    }

    @After
    public void tearDown()
    {
        mockControl = null;
        authenticationContext = null;
        requestContextFactory = null;
        permissionManager = null;
        requestContext = null;
        user = null;
        project = null;
        i18nHelper = null;
        encoder = null;
        cache = null;
        comparatorFactory = null;
    }

    @Test
    public void testDefaultContext() throws Exception
    {
        try
        {
            HttpServletRequest mockRequest = createMockRequest();
            ExecutingHttpRequest.set(mockRequest, null);
            cache.setProject(project);

            expect(authenticationContext.getI18nHelper()).andReturn(i18nHelper);
            expect(authenticationContext.getLoggedInUser()).andReturn(user).anyTimes();

            expect(project.getKey()).andReturn("KEY").anyTimes();

            expect(permissionManager.hasPermission(eq(Permissions.ADMINISTER), same(user))).andReturn(false);
            expect(permissionManager.hasPermission(eq(Permissions.PROJECT_ADMIN), eq(project), same(user))).andReturn(true);

            mockControl.replay();

            final ContextProviderUtils contextProviderUtils = new ContextProviderUtils(requestContextFactory, authenticationContext, permissionManager, encoder, cache, comparatorFactory)
            {
                @Override
                String encode(String value)
                {
                    return "{encoded}" + value;
                }
            };

            final Map<String, Object> expectedMap = MapBuilder.<String, Object>newBuilder()
                    .add(ContextProviderUtils.CONTEXT_PROJECT_KEY, project)
                    .add(ContextProviderUtils.CONTEXT_IS_ADMIN_KEY, false)
                    .add(ContextProviderUtils.CONTEXT_IS_PROJECT_ADMIN_KEY, true)
                    .add(ContextProviderUtils.CONTEXT_I18N_KEY, i18nHelper)
                    .add(ContextProviderUtils.CONTEXT_PROJECT_KEY_ENCODED, "{encoded}KEY")
                    .toMap();

            assertEquals(expectedMap, contextProviderUtils.getDefaultContext());
        }
        finally
        {
            ExecutingHttpRequest.clear();
        }

        mockControl.verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFlattenErrorsNoArgument()
    {
        final ContextProviderUtils contextProviderUtils = new ContextProviderUtils(requestContextFactory, authenticationContext, permissionManager, encoder, cache, comparatorFactory);

        mockControl.replay();
        contextProviderUtils.flattenErrors(null);
        mockControl.verify();
    }

    @Test
    public void testFlattenErrors()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage("one");
        collection.addErrorMessage("two");
        collection.addErrorMessage("three");
        collection.addError("random", "three");
        collection.addError("random", "four");

        mockControl.replay();
        final ContextProviderUtils contextProviderUtils = new ContextProviderUtils(requestContextFactory, authenticationContext, permissionManager, encoder, cache, comparatorFactory);
        Set<String> actualErrors = contextProviderUtils.flattenErrors(collection);
        Set<String> expectedErrors = new HashSet<String>();
        expectedErrors.addAll(collection.getErrorMessages());
        expectedErrors.addAll(collection.getErrors().values());
        assertEquals(actualErrors.size(), expectedErrors.size());
        assertTrue(expectedErrors.containsAll(actualErrors));
        mockControl.verify();
    }

    @Test
    public void testGetBaseUrl() throws Exception
    {
        String baseurl = "baseurl";
        
        expect(requestContextFactory.getJiraVelocityRequestContext()).andReturn(requestContext);
        expect(requestContext.getBaseUrl()).andReturn(baseurl);

        mockControl.replay();

        final ContextProviderUtils providerUtils = new ContextProviderUtils(requestContextFactory, authenticationContext, permissionManager, encoder, cache, comparatorFactory);
        assertEquals(baseurl, providerUtils.getBaseUrl());

        mockControl.verify();
    }

    @Test
    public void testCreateUrlBuilder() throws Exception
    {
        String baseurl = "baseurl";

        expect(requestContextFactory.getJiraVelocityRequestContext()).andReturn(requestContext).anyTimes();
        expect(requestContext.getBaseUrl()).andReturn(baseurl).anyTimes();

        mockControl.replay();

        final ContextProviderUtils providerUtils = new ContextProviderUtils(requestContextFactory, authenticationContext, permissionManager, encoder, cache, comparatorFactory);
        assertEquals(baseurl, providerUtils.createUrlBuilder(null).asUrlString());
        assertEquals(baseurl, providerUtils.createUrlBuilder("        ").asUrlString());
        assertEquals(baseurl + "jack", providerUtils.createUrlBuilder(" jack  ").asUrlString());

        mockControl.verify();
    }

    private static HttpServletRequest createMockRequest()
    {
        final Map<String, Object> attributes = new HashMap<String, Object>();
        return (HttpServletRequest) DuckTypeProxy.getProxy(HttpServletRequest.class, new Object()
        {
            public void setAttribute(String name, Object o)
            {
                attributes.put(name, o);
            }

            public Object getAttribute(String name)
            {
                return attributes.get(name);
            }
        });
    }
}
