/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.portlet.bean;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;

public class AbstractPortlet
{
    public ApplicationProperties getApplicationProperties()
    {
        return ManagerFactory.getApplicationProperties();
    }
}
