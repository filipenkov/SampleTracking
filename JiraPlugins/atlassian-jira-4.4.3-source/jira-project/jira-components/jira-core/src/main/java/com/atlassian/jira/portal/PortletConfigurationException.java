/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import org.apache.commons.lang.exception.NestableException;

public class PortletConfigurationException extends NestableException
{
    public PortletConfigurationException()
    {
    }

    public PortletConfigurationException(String s)
    {
        super(s);
    }

    public PortletConfigurationException(Throwable throwable)
    {
        super(throwable);
    }

    public PortletConfigurationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
