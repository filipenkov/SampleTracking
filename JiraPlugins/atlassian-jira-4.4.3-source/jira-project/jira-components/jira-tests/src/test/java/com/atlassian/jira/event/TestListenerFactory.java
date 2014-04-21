/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.event.MockListener;

public class TestListenerFactory extends LegacyJiraMockTestCase
{
    public TestListenerFactory(String s)
    {
        super(s);
    }

    public void testListenerFactory() throws ListenerException
    {
        MockListener listener = (MockListener) ListenerFactory.getListener("com.atlassian.jira.mock.event.MockListener", EasyMap.build("foo", "bar"));
        assertEquals("bar", listener.getParam("foo"));
    }

    public void testInvalidListener()
    {
        boolean exceptionThrown = false;
        try
        {
            ListenerFactory.getListener("foobar", null);
        }
        catch (ListenerException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
