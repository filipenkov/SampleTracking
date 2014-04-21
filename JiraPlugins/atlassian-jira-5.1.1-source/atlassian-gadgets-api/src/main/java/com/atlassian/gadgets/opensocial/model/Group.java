package com.atlassian.gadgets.opensocial.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.Immutable;

/**
 * Represents a group name. There are three distinguished groups: ALL, FRIENDS, and SELF. Groups are
 * uniquely specified by their name
 *
 *  @since 2.0
 */
@Immutable
public final class Group
{
    private final String name;
    private static final ConcurrentMap<String, Group> allGroups = new ConcurrentHashMap<String, Group>();

    private Group(String name)
    {
        this.name = name;
    }

    public static final Group ALL = Group.of("ALL");
    public static final Group FRIENDS = Group.of("FRIENDS");
    public static final Group SELF = Group.of("SELF");

    /**
     * Retrieve the group corresponding to the specified name
     * @param name The name of the group
     * @return a {@code Group} corresponding to the specified name. A new group will be created if one does not
     * already exist
     */
    public static Group of(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name parameter to Group must not be null");
        }
        name = name.intern();
        Group freshGroup = new Group(name);
        Group existingGroup = allGroups.putIfAbsent(name, freshGroup);
        return existingGroup == null ? freshGroup : existingGroup;
    }

    /**
     * Fetch the (unique) name of this group
     * @return the name of the group
     */
    public String valueOf()
    {
        return name;
    }

    /**
     * The {@code String} representation of a group (its name)
     * @return the name of the group
     */
    @Override
    public String toString()
    {
        return valueOf();
    }
}
