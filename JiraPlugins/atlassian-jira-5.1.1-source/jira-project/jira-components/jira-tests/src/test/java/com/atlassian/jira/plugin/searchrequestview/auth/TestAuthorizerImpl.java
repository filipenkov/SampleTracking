package com.atlassian.jira.plugin.searchrequestview.auth;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import com.atlassian.jira.security.groups.DefaultGroupManager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This is an Integration Test
 */
public class TestAuthorizerImpl extends LegacyJiraMockTestCase
{
    private final SearchProvider searchProvider = (SearchProvider) DuckTypeProxy.getProxy(SearchProvider.class, new Object());
    private final AtomicReference<String> groupName = new AtomicReference<String>();
    private final AtomicReference<String> count = new AtomicReference<String>();
    private final Object props = new Object()
    {
        @SuppressWarnings("unused")
        public String getDefaultBackedString(final String string)
        {
            if (!"jira.search.views.max.limit".equals(string))
            {
                return groupName.get();
            }
            return count.get();
        }
    };
    ApplicationProperties properties = (ApplicationProperties) DuckTypeProxy.getProxy(ApplicationProperties.class, props);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        groupName.set(null);
        count.set("10");
    }

    public void testNullSearchProviderCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(null, properties, new MockGroupManager(false, null));
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    public void testNullPropertiesCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(searchProvider, null, new MockGroupManager(false, null));
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    public void testNulluserManagerCtor() throws Exception
    {
        try
        {
            new AuthorizerImpl(searchProvider, properties, null);
            fail("Should have thrown ex");
        }
        catch (final RuntimeException yay)
        {}
    }

    public void testUnknownGroup() throws Exception
    {
        final String name = "blah-de-blah-de-blah";
        groupName.set(name);
        try
        {
            new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        }
        catch (final RuntimeException e)
        {
            assertTrue(e.getMessage().indexOf(name) > -1);
        }
    }

    public void testKnownGroup() throws Exception
    {
        final String name = "i-am-a-teapot";
        groupName.set(name);
        final Group theGroup = createMockGroup(name);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(true, theGroup));
        final Authorizer delegate = authorizer.getDelegate();
        assertTrue(delegate instanceof UserAuthorizer);
        final UserAuthorizer userAuthorizer = (UserAuthorizer) delegate;
        assertEquals(theGroup, userAuthorizer.getGroup());
    }

    public void testNullGroupDoesntCreateUserAuthorizer() throws Exception
    {
        groupName.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertFalse(delegate instanceof UserAuthorizer);
    }

    public void testCrapGroupDoesntCreateUserAuthorizer() throws Exception
    {
        groupName.set("i-am-not-a-teapot-i-am-a-mug");

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertFalse(delegate instanceof UserAuthorizer);
    }

    public void testSizeAuthorizerCreated() throws Exception
    {
        groupName.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertTrue(delegate instanceof SearchResultSizeAuthorizer);
        final SearchResultSizeAuthorizer searchResultSizeAuthorizer = (SearchResultSizeAuthorizer) delegate;
        assertEquals(10, searchResultSizeAuthorizer.getMaxAllowed());
    }

    public void testNoLimit() throws Exception
    {
        count.set(null);

        final AuthorizerImpl authorizer = new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
        final Authorizer delegate = authorizer.getDelegate();
        assertSame(Authorizer.ALWAYS, delegate);
    }

    public void testCtor() throws Exception
    {
        new AuthorizerImpl(searchProvider, properties, new MockGroupManager(false, null));
    }

    private class MockGroupManager extends DefaultGroupManager
    {
        private boolean userIsInGroup;
        private Group group;

        public MockGroupManager(boolean userIsInGroup, final Group group)
        {
            super(null);
            this.userIsInGroup = userIsInGroup;
            this.group = group;
        }

        @Override
        public com.atlassian.crowd.embedded.api.Group getGroupObject(String groupName)
        {
            return group;
        }

        @Override
        public boolean isUserInGroup(final User user, final com.atlassian.crowd.embedded.api.Group group)
        {
            return userIsInGroup;
        }

        @Override
        public Group getGroup(final String groupname)
        {
            return group;
        }
    }

}