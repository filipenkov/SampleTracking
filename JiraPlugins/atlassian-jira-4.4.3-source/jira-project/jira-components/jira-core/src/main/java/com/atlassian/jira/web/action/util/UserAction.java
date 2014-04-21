/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import webwork.action.ActionSupport;

import java.util.Collection;

/**
 * Simple action to get the config object from WW, and to use it
 *
 * @deprecated this action should no longer be used as all references in JIRA code have been removed, and <webwork:action>
 * tags can be harmful.
 * TODO - remove this class for JIRA 4.2
 */
public class UserAction extends ActionSupport
{
    private static final Logger log = Logger.getLogger(UserAction.class);

    private final UserUtil userUtil;

    public UserAction(UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    /**
     * Returns a user found by the given username.
     *
     * @param username username to find the user by
     * @return user found or null if no such user exists
     * @deprecated
     */
    public User getUser(String username)
    {
        try
        {
            return UserUtils.getUser(username);
        }
        catch (EntityNotFoundException e)
        {
            log.error("User with username '" + username + "' does not exist!");
            return null; // ?? maybe we should have a NullUser ?
        }
    }

    /**
     * Return the list of all administrators.
     * It is a list of {@link User} objects. Can return null.
     *
     * @return the list of all administrators
     * @deprecated
     */
    public Collection /* <User> */ getAdministrators()
    {
        return userUtil.getAdministrators();
    }
}
