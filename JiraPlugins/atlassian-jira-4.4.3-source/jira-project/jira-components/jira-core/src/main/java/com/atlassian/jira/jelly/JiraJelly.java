/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.jira.config.properties.SystemPropertyKeys;

public class JiraJelly
{
    public static final String JELLY_NOT_ON_MESSAGE = "Can not run script because jelly is not turned on.";

    public static boolean allowedToRun()
    {
        return Boolean.valueOf(System.getProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY)).booleanValue();
    }
}
