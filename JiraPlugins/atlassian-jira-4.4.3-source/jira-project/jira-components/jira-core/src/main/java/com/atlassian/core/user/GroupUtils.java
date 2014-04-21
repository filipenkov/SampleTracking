package com.atlassian.core.user;

import com.opensymphony.user.*;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * A utility class for operating on groups.
 * This used to live in the atlassian-core library. This Class is deprecated and will be removed in JIRA v5.0
 * You should be using {@link com.atlassian.jira.security.groups.GroupManager} to do group operations.
 * If you cannot get this injected into your class and need static access, then use {@link com.atlassian.jira.component.ComponentAccessor#getGroupManager()}.
 *
 * @deprecated Use {@link com.atlassian.jira.security.groups.GroupManager} instead. Since v4.4.
 */
public class GroupUtils
{
    private static final Logger log = Logger.getLogger(GroupUtils.class);

    final static Comparator alphaGroupComparator = new Comparator()
    {
        public int compare(Object a, Object b)
        {
            if (a == null)
            {
                if (b == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (b == null)
                {
                    return 1;
                }
                else
                {
                    return alphaStringComparator.compare(((Group) a).getName(), ((Group) b).getName());
                }
            }
        }
    };

    final static Comparator alphaStringComparator = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            String a = (String) o1;
            String b = (String) o2;

            if (a == null)
            {
                if (b == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (b == null)
                {
                    return 1;
                }
                else
                {
                    return a.compareToIgnoreCase(b);
                }
            }
        }
    };

    /**
     * This method gets a group from OSUser - however if the group does not exist, it tries to create
     * it and then return it (mitigates the need for a setup file).
     */
    public static Group getGroupSafely(String name) throws ImmutableException
    {
        Group group = null;

        try
        {
            group = UserManager.getInstance().getGroup(name);
        }
        catch (EntityNotFoundException e)
        {
            try
            {
                group = UserManager.getInstance().createGroup(name);
            }
            catch (DuplicateEntityException e1) // should never happen - touch wood ;)
            {
                e.printStackTrace(System.err);
            }
        }

        return group;
    }

    /**
     * Get a group from the underlying user manager.
     * @return The Group from the underlying UserManager, or null if the group doesn't exist
     */
    public static Group getGroup(String name)
    {
        if (name == null)
        {
            return null;
        }
        else
        {
            if (GroupUtils.existsGroup(name))
            {
                try
                {
                    return UserManager.getInstance().getGroup(name);
                }
                catch (EntityNotFoundException e)
                {
                    log.error("Error getting group: " + name, e);
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * A simple method to tell if a group already exists or not
     */
    public static boolean existsGroup(String name)
    {
        try
        {
            UserManager.getInstance().getGroup(name);
        }
        catch (EntityNotFoundException e)
        {
            return false;
        }

        return true;
    }

    /**
     * @return A collection of all groups (sorted by name)
     */
    public static Collection getGroups()
    {
        List groups = UserManager.getInstance().getGroups();
        sortGroups(groups);
        return groups;
    }

    /**
     * Sort group names in alphabetical (*not* ascii) order. This means that
     * upper case and lower case characters are sorted together.
     * @param groups
     */
    public static void sortGroups(List groups)
    {
        Collections.sort(groups, alphaGroupComparator);
    }

    /**
     * Sort group names in alphabetical (*not* ascii) order. This means that
     * upper case and lower case characters are sorted together.
     * @param groups
     */
    public static void sortGroupNames(List groups)
    {
        Collections.sort(groups, alphaStringComparator);
    }

    /**
     * This will remove a group, and remove any users from that group
     */
    public static void removeGroup(Group group) throws Exception
    {
        Collection users = new HashSet(group.getUsers());

        for (Iterator iterator = users.iterator(); iterator.hasNext();)
        {
            try
            {
                User u = UserUtils.getUser((String) iterator.next());
                u.removeFromGroup(group);
            }
            catch (EntityNotFoundException e)
            {
                // don't care! :)
            }
        }

        group.remove();
    }

}
