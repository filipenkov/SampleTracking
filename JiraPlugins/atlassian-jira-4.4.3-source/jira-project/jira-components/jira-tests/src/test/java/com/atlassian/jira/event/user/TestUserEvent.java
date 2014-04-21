/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.user.MockUser;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;

public class TestUserEvent extends AbstractUsersTestCase
{
    public TestUserEvent(String s)
    {
        super(s);
    }

    public void testBlankConstructor() throws ImmutableException, DuplicateEntityException
    {
        User bob = new MockUser("bob");

        UserEvent event = new UserEvent(bob, UserEventType.USER_SIGNUP);
        assertEquals(0, event.getParams().size());
        assertEquals(bob, event.getUser());
    }

    public void testFullConstructor() throws ImmutableException, DuplicateEntityException
    {
        User bob = new MockUser("bob");

        UserEvent event = new UserEvent(EasyMap.build("foo", "bar"), bob, UserEventType.USER_SIGNUP);
        assertEquals("bar", event.getParams().get("foo"));
        assertEquals(bob, event.getUser());
    }
}
