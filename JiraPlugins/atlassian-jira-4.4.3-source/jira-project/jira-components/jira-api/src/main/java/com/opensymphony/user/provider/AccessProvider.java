/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.user.provider;

import java.util.List;

/**
 * The AccessProvider is a UserProvider specifically used for storing details about
 * Groups and memberships. All Entities referred to are of type
 * <b>Group</b>, and all Entity.Accessor objects can be safely cast to Group.Accessor
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision: 1.2 $
 *
 * @see com.opensymphony.user.provider.UserProvider
 * @see com.opensymphony.user.Group
 */
@Deprecated
public interface AccessProvider extends UserProvider
{
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * Add user to group.
     *
     * @return Whether user was successfully added to group.
     */
    boolean addToGroup(String username, String groupname);

    /**
     * Find out whether given user is member of given group.
     *
     * @return Whether user is member of group.
     */
    boolean inGroup(String username, String groupname);

    /**
     * List all groups that contain a user.
     *
     * @return List containing Strings of groupnames. If no groups found,
     *         empty list should be returned. If feature not supported
     *         by UserProvider, null shall be returned. This List should
     *         be immutable.
     */
    List<String> listGroupsContainingUser(String username);

    /**
     * List all users that are contained within a group.
     *
     * @return List containing Strings of usernames. If no users found,
     *         empty list should be returned. If feature not supported
     *         by UserProvider, null shall be returned. This List should
     *         be immutable.
     */
    List<String> listUsersInGroup(String groupname);

    /**
     * Remove user from group.
     *
     * @return Whether user was successfully removed from group.
     */
    boolean removeFromGroup(String username, String groupname);
}
