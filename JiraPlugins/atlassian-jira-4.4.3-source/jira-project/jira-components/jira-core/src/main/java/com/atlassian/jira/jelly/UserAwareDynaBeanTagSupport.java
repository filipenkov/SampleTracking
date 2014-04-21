/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.opensymphony.user.User;
import org.apache.commons.jelly.DynaBeanTagSupport;

import java.util.Map;

public abstract class UserAwareDynaBeanTagSupport extends DynaBeanTagSupport implements UserAware
{
    protected static final String KEY_VARIABLE_NAME = "var";

    public UserAwareDynaBeanTagSupport()
    {
        super(new ActionTagSupportDynaBean(new ActionTagSupportDynaClass()));
    }

    public Map getProperties()
    {
        return ((ActionTagSupportDynaBean) getDynaBean()).getProperties();
    }

    private final String[] requiredContextVariables = new String[] { JellyTagConstants.USERNAME };

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public String getUsername()
    {
        return (String) getContext().getVariable(JellyTagConstants.USERNAME);
    }

    public User getUser()
    {
        return ManagerFactory.getUserManager().getUser(getUsername());
    }
}
