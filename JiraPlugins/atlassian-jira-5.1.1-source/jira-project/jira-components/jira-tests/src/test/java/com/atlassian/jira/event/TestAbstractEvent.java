/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.event.MockEvent;

import java.util.Date;

public class TestAbstractEvent extends ListeningTestCase
{
    @Test
    public void testBlankConstructor()
    {
        Date before = new Date();
        MockEvent event = new MockEvent();
        Date after = new Date();

        assertEquals(0, event.getParams().size());
        assertTrue(before.before(event.getTime()) || before.equals(event.getTime()));
        assertTrue(after.after(event.getTime()) || after.equals(event.getTime()));
    }

    @Test
    public void testConstructor()
    {
        Date before = new Date();
        MockEvent event = new MockEvent(EasyMap.build("foo", "bar"));
        Date after = new Date();

        assertEquals(1, event.getParams().size());
        assertEquals("bar", event.getParams().get("foo"));
        assertTrue(before.before(event.getTime()) || before.equals(event.getTime()));
        assertTrue(after.after(event.getTime()) || after.equals(event.getTime()));
    }
}
