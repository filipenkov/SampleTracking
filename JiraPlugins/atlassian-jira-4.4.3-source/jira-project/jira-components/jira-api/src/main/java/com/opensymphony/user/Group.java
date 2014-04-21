package com.opensymphony.user;

import com.atlassian.crowd.embedded.api.GroupComparator;
import com.opensymphony.user.provider.AccessProvider;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Entity that represents a Group of Users.
 *
 * The group is backed by Crowd's Embedded Service.
 */
@Deprecated
public class Group extends Entity implements java.security.acl.Group, com.atlassian.crowd.embedded.api.Group
{
    public Group(final com.atlassian.crowd.embedded.api.Group crowdGroup)
    {
        super(crowdGroup.getName(), UserManager.getInstance().getAccessor());
    }

    /**
    * Constructor is only called by UserManager.
     *
     * @param name groupname.
     * @param providerAccessor accessors.
     */
    public Group(final String name, final ProviderAccessor providerAccessor)
    {
        super(name, providerAccessor);
    }

    /**
    * Returns true if the passed principal is a member of the group.
    * This method does a recursive search, so if a principal belongs to a
    * group which is a member of this group, true is returned.
    *
    * @param member the principal whose membership is to be checked.
    *
    * @return true if the principal is a member of this group,
    * false otherwise.
    */
    public boolean isMember(final Principal member)
    {
        if (member instanceof User)
        {
            return containsUser((User) member);
        }
        else
        {
            return containsUser(member.getName());
        }
    }

    /**
    * List all Users that Group contains as members.
    * Modifiying this Collection will not add or remove Users.
    *
    * @return List of usernames (String)
    */
    public List<String> getUsers()
    {
        return Collections.unmodifiableList(getAccessProvider().listUsersInGroup(name));
    }

    /**
    * Adds the specified member to the group.
    *
    * @param user the principal to add to this group.
    *
    * @return true if the member was successfully added,
    * false if the principal was already a member.
    */
    public boolean addMember(final Principal user)
    {
        if (user instanceof User)
        {
            return addUser((User) user);
        }
        else
        {
            try
            {
                return addUser(getUserManager().getUser(user.getName()));
            }
            catch (final EntityNotFoundException e)
            {
                return false;
            }
        }
    }

    /**
     * Add User to members of this Group. Returns whether any modifications were made.
     *
     * @param user the user to add.
     * @return <code>true</code> if user is added successfully to the group.
     */
    public boolean addUser(final User user)
    {
        if (user == null)
        {
            return false;
        }

        return getAccessProvider().addToGroup(user.getName(), name);
    }

    /**
     * Determine whether Group contains supplied User as member.
     *
     * @param user the user to check.
     * @return <code>true</code> if user is a member of the group.
     */
    public boolean containsUser(final User user)
    {
        return (user != null) && getAccessProvider().inGroup(user.getName(), name);
    }

    /**
    * Determine whether Group contains supplied User as member.
     *
     * @param user name of the user to check.
     * @return <code>true</code> if the user is a member of the group.
     */
    public boolean containsUser(final String user)
    {
        return (user != null) && getAccessProvider().inGroup(user, name);
    }

    /**
    * Returns an enumeration of the members in the group.
    * The returned objects can be instances of either Principal
    * or Group (which is a subclass of Principal).
    *
    * @return an enumeration of the group members.
    */
    public Enumeration<? extends Principal> members()
    {
        final List<String> users = getAccessProvider().listUsersInGroup(name);
        final Vector<Principal> list = new Vector<Principal>(users.size());

        for (final String s : users)
        {
            try
            {
                list.add(getUserManager().getUser(s));
            }
            catch (final EntityNotFoundException e)
            {}
        }
        return list.elements();
    }

    /**
     * Remove Group from associated AccessProvider.
     */
    @Override
    public void remove() throws ImmutableException
    {
        final AccessProvider accessProvider = getAccessProvider();

        if (!mutable)
        {
            throw new ImmutableException();
        }

        if (accessProvider == null)
        {
            throw new ImmutableException();
        }

        if (!accessProvider.remove(name))
        {
            throw new ImmutableException();
        }
    }

    /**
    * Removes the specified member from the group.
    *
    * @param user the principal to remove from this group.
    *
    * @return true if the principal was removed, or
    * false if the principal was not a member.
    */
    public boolean removeMember(final Principal user)
    {
        if (user instanceof User)
        {
            return removeUser((User) user);
        }
        else
        {
            try
            {
                return removeUser(getUserManager().getUser(user.getName()));
            }
            catch (final EntityNotFoundException e)
            {
                return false;
            }
        }
    }

    /**
     * Remove User from members of this Group. Returns whether any modifications were made.
     *
     * @param user the user to remove from the group.
     * @return <code>true</code> if user is successfully removed from the group.
     */
    public boolean removeUser(final User user)
    {
        if (user == null)
        {
            return false;
        }

        return getAccessProvider().removeFromGroup(user.getName(), name);
    }

    @Override
    public boolean equals(Object o)
    {
        // implement equals() that is compatible with other implementations of Crowd Group
        return (o instanceof com.atlassian.crowd.embedded.api.Group) && GroupComparator.equal(this, (com.atlassian.crowd.embedded.api.Group) o);
    }

    @Override
    public int hashCode()
    {
        // implement hashCode() that is compatible with other implementations of Crowd Group
        return GroupComparator.hashCode(this);
    }

    public int compareTo(final com.atlassian.crowd.embedded.api.Group other)
    {
        // implement compareTo() that is compatible with other implementations of Crowd Group
        return GroupComparator.compareTo(this, other);
    }
}
