/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.searchrequestview.auth;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.groups.DefaultGroupManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.OSUserConverter;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestUserAuthorizer extends ListeningTestCase
{
    private User privilegedUser;
    private User unprivilegedUser;
    private Group group;
    private final AtomicBoolean delegateCalled = new AtomicBoolean();
    private final Authorizer delegate = new Authorizer()
    {
        @Override
        public Result isSearchRequestAuthorized(com.atlassian.crowd.embedded.api.User user, SearchRequest searchRequest, SearchRequestParams params)
        {
            delegateCalled.set(true);
            return Result.OK;
        }

        public Result isSearchRequestAuthorized(final User user, final SearchRequest searchRequest, final SearchRequestParams params)
        {
            delegateCalled.set(true);
            return Result.OK;
        }
    };
    private final SearchRequest request = null;
    private final SearchRequestParams params = null;

    @Test
    public void testPrivilegedUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(true, group));

        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(privilegedUser, request, params));
        assertFalse(delegateCalled.get());
    }

    @Test
    public void testUnprivilegedUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(false, group));
        // will be ok, but delegate called
        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(unprivilegedUser, request, params));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testNullUser()
    {
        Authorizer authorizer = new UserAuthorizer(group, delegate, new MockGroupManager(false, null));
        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(null, request, params));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testNullGroup()
    {
        try
        {
            new UserAuthorizer(null, delegate, new MockGroupManager(false, null));
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Test
    public void testNullDelegate()
    {
        try
        {
            new UserAuthorizer(group, null, new MockGroupManager(false, null));
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Before
    public void setUp()
    {
        privilegedUser = OSUserConverter.convertToOSUser(new MockUser("test"));
        unprivilegedUser = OSUserConverter.convertToOSUser(new MockUser("nong"));
        group = OSUserConverter.convertToOSGroup(new MockGroup("test"));

        delegateCalled.set(false);
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
        public boolean isUserInGroup(final com.atlassian.crowd.embedded.api.User user, final com.atlassian.crowd.embedded.api.Group group)
        {
            return userIsInGroup;
        }

        @Override
        public com.opensymphony.user.Group getGroup(final String groupname)
        {
            return OSUserConverter.convertToOSGroup(group);
        }
    }

}
