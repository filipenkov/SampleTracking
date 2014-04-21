/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FilterUtils
{
    /**
     * Returned string is non-null IFF there is a true value (ie some text)
     */
    public static String verifyString(String s)
    {
        if (TextUtils.stringSet(TextUtils.noNull(s).trim()))
        {
            return s;
        }
        else
        {
            return null;
        }
    }

    /**
     * Retirned string array is non-null IFF there is a true value (ie some text)
     */
    public static String[] verifyStringArray(String[] sa)
    {
        List result = new ArrayList();

        for (int i = 0; i < sa.length; i++)
        {
            String s = verifyString(sa[i]);
            if (s != null)
                result.add(s);
        }

        if (result.size() == 0)
        {
            return null;
        }
        else
        {
            String[] resultSa = new String[result.size()];
            int count = 0;
            for (Iterator iterator = result.iterator(); iterator.hasNext();)
            {
                resultSa[count++] = (String) iterator.next();
            }

            return resultSa;
        }
    }

    public static Long verifyLong(Long id)
    {
        if (id != null && id.longValue() > 0)
        {
            return id;
        }

        return null;
    }

    /**
     * Get the groups that this filter can be shared with.  If this is a regular user, this will be the user's groups.
     * If the user is an admin, it will return all groups.
     *
     * @return A collection of strings representing the groups.
     */
    public static Collection getGroups(User user)
    {
        Collection groups = new ArrayList();

        if (user == null)
            return null;

        if (ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, user))
        {
            //have to convert groups to group names
            for (Iterator iterator = GroupUtils.getGroups().iterator(); iterator.hasNext();)
            {
                Group group = (Group) iterator.next();
                groups.add(group.getName());
            }
        }
        else
        {
            groups = user.getGroups();
        }
        return groups;
    }
}
