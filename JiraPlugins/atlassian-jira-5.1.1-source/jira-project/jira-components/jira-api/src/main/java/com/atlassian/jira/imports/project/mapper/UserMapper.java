package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows you to map Users.
 * We keep the whole ExternalUser information so we can create Users that don't exist.
 *
 * @since v3.13
 */
public class UserMapper extends AbstractMapper
{
    private final Map users;
    private final Set usersInUse;
    private final UserUtil userUtil;
    private final Map userExistsCache;

    public UserMapper(final UserUtil userUtil)
    {
        this.userUtil = userUtil;
        users = new HashMap();
        usersInUse = new HashSet();
        // This collection does not need to be thread safe since it is never used in a multi-threaded invocation
        userExistsCache = new HashMap();
    }

    public void registerOldValue(final ExternalUser externalUser)
    {
        Assertions.notNull("externalUser", externalUser);
        // We never use the user ID to store links to users, we use the username.
        // hence, we store the userName as the "ID"
        super.registerOldValue(externalUser.getName(), externalUser.getName());
        // Remember the whole external user for later use.
        users.put(externalUser.getName(), externalUser);
    }

    public ExternalUser getExternalUser(final String userName)
    {
        return (ExternalUser) users.get(userName);
    }

    public String getMappedId(final String oldId)
    {
        if (userExists(oldId))
        {
            return oldId;
        }
        else
        {
            return null;
        }
    }

    public void flagUserAsMandatory(final String oldUserName)
    {
        super.flagValueAsRequired(oldUserName);
        // The mandatory users supersede the ones that are in use, but optional
        usersInUse.remove(oldUserName);
    }

    public void flagUserAsInUse(final String oldUserName)
    {
        // We only want to store this if the user has not already been flagged as mandatory
        if (!getRequiredOldIds().contains(oldUserName))
        {
            usersInUse.add(oldUserName);
        }
    }

    public Collection /*<ExternalUser>*/getUnmappedMandatoryUsers()
    {
        final Collection unmappedUsers = new ArrayList();
        for (final Iterator iterator = getRequiredOldIds().iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                ExternalUser user = (ExternalUser) users.get(userName);
                if (user == null)
                {
                    user = new ExternalUser(userName, "", "");
                }
                unmappedUsers.add(user);
            }
        }
        return unmappedUsers;
    }

    public List getUnmappedMandatoryUsersWithNoRegisteredOldValue()
    {
        final List unregisteredUsers = new ArrayList();
        for (final Iterator iterator = getRequiredOldIds().iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                final ExternalUser user = (ExternalUser) users.get(userName);
                if (user == null)
                {
                    unregisteredUsers.add(new ExternalUser(userName, "", ""));
                }
            }
        }
        return unregisteredUsers;
    }

    public List getUnmappedUsersInUseWithNoRegisteredOldValue()
    {
        final List unregisteredUsers = new ArrayList();
        for (final Iterator iterator = usersInUse.iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                final ExternalUser user = (ExternalUser) users.get(userName);
                if (user == null)
                {
                    unregisteredUsers.add(new ExternalUser(userName, "", ""));
                }
            }
        }
        return unregisteredUsers;
    }

    public Collection /*<ExternalUser>*/getUnmappedUsersInUse()
    {
        final Collection unmappedUsers = new ArrayList();
        for (final Iterator iterator = usersInUse.iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                ExternalUser user = (ExternalUser) users.get(userName);
                if (user == null)
                {
                    user = new ExternalUser(userName, "", "");
                }
                unmappedUsers.add(user);
            }
        }
        return unmappedUsers;
    }

    /**
     * Returns a List of users that can be automatically created by the import.
     * <p>This includes all optional and mandatory users that aren't in the current system, and the import file has the user details for.</p>
     * <p>Note that this method only makes sense if External User Management is off.</p>
     * @return a List of users that can be automatically created by the import.
     */
    public Collection /*<ExternalUser>*/getUsersToAutoCreate()
    {
        final List autoCreatable = new ArrayList();
        // Add in required users that aren't in the current system, and we have details for.
        for (final Iterator iterator = getRequiredOldIds().iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                // User is not in current system - check if we have details so we can auto-create
                final ExternalUser user = (ExternalUser) users.get(userName);
                if (user != null)
                {
                    autoCreatable.add(user);
                }
            }
        }
        // Add in optional users that aren't in the current system, and we have details for.
        for (final Iterator iterator = usersInUse.iterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            if (!userExists(userName))
            {
                // User is not in current system - check if we have details so we can auto-create
                final ExternalUser user = (ExternalUser) users.get(userName);
                if (user != null)
                {
                    autoCreatable.add(user);
                }
            }
        }
        return autoCreatable;
    }

    public Collection /*<String>*/getOptionalOldIds()
    {
        return usersInUse;
    }

    public boolean userExists(final String userName)
    {
        // Always check the cache first
        Boolean exists = (Boolean) userExistsCache.get(userName);
        if (exists == null)
        {
            exists = Boolean.valueOf(userUtil.userExists(userName));
            userExistsCache.put(userName, exists);
        }
        return exists.booleanValue();
    }

    public void clearMappedValues()
    {
        super.clearMappedValues();
        userExistsCache.clear();
    }
}
