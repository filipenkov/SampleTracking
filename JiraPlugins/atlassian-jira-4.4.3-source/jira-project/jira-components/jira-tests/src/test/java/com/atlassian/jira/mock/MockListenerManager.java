/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.event.ListenerManager;

import java.util.HashMap;
import java.util.Map;

public class MockListenerManager implements ListenerManager
{
    Map listeners;

    public MockListenerManager()
    {
        this.listeners = new HashMap();
    }

    public Map getListeners()
    {
        return listeners;
    }

    public void addListener(String name, JiraListener listener)
    {
        listeners.put(name, listener);
    }

    public void refresh()
    {
        listeners = new HashMap();
    }
}
