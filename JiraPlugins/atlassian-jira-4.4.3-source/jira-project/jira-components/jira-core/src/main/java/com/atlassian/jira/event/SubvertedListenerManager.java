/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */


/**
 * A Noop listener manager.  Register this in ManagerFactory to disable email notifications, eg. during bulk imports.
 */
package com.atlassian.jira.event;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SubvertedListenerManager implements ListenerManager
{
    private static final Logger log = Logger.getLogger(SubvertedListenerManager.class);

    public Map getListeners()
    {
        return new HashMap(0);
    }

    public void refresh()
    {
    }
}
