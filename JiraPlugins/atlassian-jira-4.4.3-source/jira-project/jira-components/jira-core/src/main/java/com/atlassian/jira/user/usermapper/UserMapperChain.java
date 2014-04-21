/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.user.usermapper;

import com.opensymphony.user.User;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A caching chain of userMappers
 */
public class UserMapperChain implements UserMapper
{
    private final Collection userMappers;
    private final Map userMapCache = new WeakHashMap();

    public UserMapperChain(Collection userMappers)
    {
        this.userMappers = userMappers;
    }

    public User getUserFromEmailAddress(String emailAddress)
    {
        User user = (User) userMapCache.get(emailAddress);
        if (user != null)
            return user;

        for (Iterator iterator = userMappers.iterator(); iterator.hasNext();)
        {
            UserMapper userMapper = (UserMapper) iterator.next();
            user = userMapper.getUserFromEmailAddress(emailAddress);
            if (user != null)
            {
                userMapCache.put(emailAddress, user);
                return user;
            }
        }
        return null;
    }
}
