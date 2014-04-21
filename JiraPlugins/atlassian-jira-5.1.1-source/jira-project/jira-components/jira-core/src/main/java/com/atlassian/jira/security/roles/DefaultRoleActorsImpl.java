package com.atlassian.jira.security.roles;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @see DefaultRoleActors
 */
public class DefaultRoleActorsImpl implements DefaultRoleActors {

    private final Set roleActors;
    private final Long projectRoleId;

    public DefaultRoleActorsImpl(Long projectRoleId, Set roleActors)
    {
        this.projectRoleId = projectRoleId;
        this.roleActors = Collections.unmodifiableSet(new HashSet(roleActors));
    }

    /**
     * This will allow you to add a single role actor to the internal Set used
     * for the role actors. This is a convience constructor to allow us to easily add
     * a single RoleActor
     *
     * @param projectRoleId the Project Role Id we are modelling
     * @param roleActor the Project Role we are modelling
     */
    public DefaultRoleActorsImpl(Long projectRoleId, RoleActor roleActor)
    {
        this(projectRoleId, new HashSet(EasyList.build(roleActor)));
    }

    public Set<User> getUsers()
    {
        Set<User> allUsers = new HashSet<User>();
        if (roleActors != null)
        {
            for (Iterator iterator = roleActors.iterator(); iterator.hasNext();)
            {
                RoleActor actor = (RoleActor) iterator.next();
                for (User user : actor.getUsers())
                {
                    allUsers.add(user);
                }
            }

        }
        return allUsers;
    }

    public Set getRoleActors()
    {
        return roleActors;
    }

    public Long getProjectRoleId()
    {
        return projectRoleId;
    }

    public Set getRoleActorsByType(String type)
    {
        // catagorize the roleActors by type and return all the users
        Set roleActorsForType = new TreeSet(RoleActorComparator.COMPARATOR);
        for (Iterator iterator = roleActors.iterator(); iterator.hasNext();)
        {
            RoleActor roleActor = (RoleActor) iterator.next();
            if (roleActor.getType().equals(type))
            {
                roleActorsForType.add(roleActor);
            }
        }
        return roleActorsForType;
    }

    public boolean contains(User user)
    {
        for (Iterator iterator = roleActors.iterator(); iterator.hasNext();)
        {
            RoleActor roleActor = (RoleActor) iterator.next();
            if (roleActor.contains(user))
            {
                return true;
            }
        }
        return false;
    }

    public DefaultRoleActors addRoleActor(RoleActor roleActor)
    {
        Set set = new HashSet(this.roleActors);
        set.add(roleActor);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors addRoleActors(Collection roleActors)
    {
        Set set = new HashSet(this.roleActors);
        set.addAll(roleActors);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors removeRoleActor(RoleActor roleActor)
    {
        if (!roleActors.contains(roleActor))
        {
            return this;
        }
        Set set = new HashSet(this.roleActors);
        set.remove(roleActor);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }

    public DefaultRoleActors removeRoleActors(Collection roleActors)
    {
        Set set = new HashSet(this.roleActors);
        set.removeAll(roleActors);
        return new DefaultRoleActorsImpl(projectRoleId, set);
    }
}
