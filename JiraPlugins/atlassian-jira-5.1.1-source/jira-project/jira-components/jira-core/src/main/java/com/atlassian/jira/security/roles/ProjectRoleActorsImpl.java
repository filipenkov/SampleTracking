package com.atlassian.jira.security.roles;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @see ProjectRoleActors
 */
public class ProjectRoleActorsImpl extends DefaultRoleActorsImpl implements ProjectRoleActors
{
    private final Long projectId;

    public ProjectRoleActorsImpl(Long projectId, Long projectRoleId, Set roleActors)
    {
        super(projectRoleId, roleActors);
        this.projectId = projectId;
    }

    public ProjectRoleActorsImpl(Long projectId, Long projectRoleId, RoleActor roleActor)
    {
        super(projectRoleId, roleActor);
        this.projectId = projectId;
    }

    public Long getProjectId()
    {
        return projectId;
    }


    public DefaultRoleActors addRoleActor(RoleActor roleActor)
    {
        Set set = new HashSet(getRoleActors());
        set.add(roleActor);
        return new ProjectRoleActorsImpl(this.projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors addRoleActors(Collection roleActors)
    {
        Set set = new HashSet(getRoleActors());
        set.addAll(roleActors);
        return new ProjectRoleActorsImpl(this.projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors removeRoleActor(RoleActor roleActor)
    {
        Set roleActors = getRoleActors();
        if (!roleActors.contains(roleActor))
        {
            return this;
        }
        Set set = new HashSet(roleActors);
        set.remove(roleActor);
        return new ProjectRoleActorsImpl(this.projectId, getProjectRoleId(), set);
    }

    public DefaultRoleActors removeRoleActors(Collection roleActors)
    {
        Set set = new HashSet(getRoleActors());
        set.removeAll(roleActors);
        return new ProjectRoleActorsImpl(this.projectId, getProjectRoleId(), set);
    }
}