/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.comparator.UserNameComparator;
import com.atlassian.jira.user.util.UserUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserPickerFilter extends PagerFilter
{
    private String nameFilter;
    private String emailFilter;
    private String group;
    private final Locale userLocale;

    public UserPickerFilter(Locale userLocale)
    {
        this.userLocale = userLocale;
    }

    public String getNameFilter()
    {
        return nameFilter;
    }

    public void setNameFilter(final String nameFilter)
    {
        this.nameFilter = FilterUtils.verifyString(nameFilter);
    }

    public String getEmailFilter()
    {
        return emailFilter;
    }

    public void setEmailFilter(final String emailFilter)
    {
        this.emailFilter = FilterUtils.verifyString(emailFilter);
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(final String group)
    {
        this.group = FilterUtils.verifyString(group);
    }

    /**
     * Get a list of users based on the parameters of the filter
     **/
    public List<User> getFilteredUsers() throws Exception
    {
        final List<User> filteredUsers = new ArrayList<User>();

        // get list of filtered users
        @SuppressWarnings("unchecked")
        final Collection<User> users = getUsersFilteredByGroup();

        for (final User user : users)
        {
            if ((nameFilter != null) && (emailFilter != null))
            {
                if (((user.getEmailAddress() != null) && (user.getEmailAddress().toLowerCase().indexOf(emailFilter.toLowerCase()) >= 0)) && ((user.getDisplayName() != null) && (user.getDisplayName().toLowerCase().indexOf(
                    nameFilter.toLowerCase()) >= 0)))
                {
                    filteredUsers.add(user);
                }
            }
            else if (((emailFilter == null) && (nameFilter == null)))
            {
                filteredUsers.add(user);
            }
            else if (((emailFilter == null) && (nameFilter != null)) && ((user.getDisplayName() != null) && (user.getDisplayName().toLowerCase().indexOf(
                nameFilter.toLowerCase()) >= 0)))
            {
                filteredUsers.add(user);
            }
            else if (((nameFilter == null) && (emailFilter != null)) && ((user.getEmailAddress() != null) && (user.getEmailAddress().toLowerCase().indexOf(
                emailFilter.toLowerCase()) >= 0)))
            {
                filteredUsers.add(user);
            }
        }

        Collections.sort(filteredUsers, new UserNameComparator(userLocale));

        return filteredUsers;
    }
    
     private Collection getUsersFilteredByGroup()
    {
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        if (group != null)
        {
            return userUtil.getUsersInGroupNames(EasyList.build(group));
        }
        else
        {
            return userUtil.getUsers();
        }
    }
}
