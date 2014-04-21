package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility to assist in the migration of code from the OSUser to the Crowd User API.
 *
 * @since v4.3
 */
public class OSUserConverter
{

    /**
     * Convert a Crowd User collection to a Set of OSUser.
     * If any user is already an OSUser that user will be returned unchanged.
     * @param crowdUsers A bunch of Crowd users
     * @return A Set of OS User objects.
     */
    public static Set<User> convertToOSUserSet(final Iterable<com.atlassian.crowd.embedded.api.User> crowdUsers)
    {
        HashSet<com.opensymphony.user.User> allUsers = new HashSet<com.opensymphony.user.User>();
        for (com.atlassian.crowd.embedded.api.User crowdUser : crowdUsers)
        {
            allUsers.add(convertToOSUser(crowdUser));
        }
        return allUsers;
    }

    /**
     * Convert a Crowd User collection to a List of OSUser.
     * If any user is already an OSUser that user will be returned unchanged.
     * @param crowdUsers A bunch of Crowd users
     * @return A List of OS User objects (in the same order as the original collection).
     */
    public static List<User> convertToOSUserList(final Iterable<com.atlassian.crowd.embedded.api.User> crowdUsers)
    {
        ArrayList<User> allUsers = new ArrayList<com.opensymphony.user.User>();
        for (com.atlassian.crowd.embedded.api.User crowdUser : crowdUsers)
        {
            allUsers.add(convertToOSUser(crowdUser));
        }
        return allUsers;
    }

    /**
     * Convert a Crowd User to an OSUser
     * If a user is already an OSUser the user will be returned unchanged.
     * @param crowdUser  A Crowd User Object
     * @return OS User object.
     */
    public static User convertToOSUser(final com.atlassian.crowd.embedded.api.User crowdUser)
    {
        if (crowdUser instanceof User)
        {
            return (User) crowdUser;
        }
        return crowdUser == null ? null : new User(crowdUser, StaticCrowdServiceFactory.getCrowdService());
    }

    /**
     * Convert a collection of Crowd Groups to OS User Groups
     * If any group is already an OSGroup that group will be returned unchanged.
     * @param crowdGroups A bunch of Crowd groups
     * @return A collection of OS Group objects.
     */
    public static Set<Group> convertToOSGroups(final Iterable<com.atlassian.crowd.embedded.api.Group> crowdGroups)
    {
        HashSet<com.opensymphony.user.Group> allGroups = new HashSet<com.opensymphony.user.Group>();
        for (com.atlassian.crowd.embedded.api.Group crowdGroup : crowdGroups)
        {
            allGroups.add(convertToOSGroup(crowdGroup));
        }
        return allGroups;
    }

    /**
     * Convert a Crowd Group to an OSUser Group
     * If a group is already an OSGroup the group will be returned unchanged.
     * @param crowdGroup A Crowd Group Object
     * @return OS User Group object.
     */
    public static Group convertToOSGroup(final com.atlassian.crowd.embedded.api.Group crowdGroup)
    {
        if (crowdGroup instanceof Group)
        {
            return (Group) crowdGroup;
        }
        return crowdGroup == null ? null : new Group(crowdGroup);
    }

}
