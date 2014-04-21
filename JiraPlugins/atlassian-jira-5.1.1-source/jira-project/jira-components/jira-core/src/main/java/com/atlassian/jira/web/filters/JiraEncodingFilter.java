/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.encoding.AbstractEncodingFilter;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;

/**
 * This filter sets the request and response encoding.
 */
public class JiraEncodingFilter extends AbstractEncodingFilter
{
    protected String getEncoding()
    {
        try
        {
            return ComponentAccessor.getApplicationProperties().getEncoding();
        }
        catch (Exception e)
        {
            return "UTF-8";
        }
    }

    protected String getContentType()
    {
        try
        {
            return ComponentAccessor.getApplicationProperties().getContentType();
        }
        catch (Exception e)
        {
            return "text/html; charset=UTF-8";
        }
    }
}
