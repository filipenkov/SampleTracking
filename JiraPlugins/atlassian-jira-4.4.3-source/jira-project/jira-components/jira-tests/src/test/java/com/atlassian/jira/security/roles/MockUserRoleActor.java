package com.atlassian.jira.security.roles;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;

import java.util.HashSet;
import java.util.Set;

public class MockUserRoleActor extends MockRoleActor
{
    private final User user;

    public MockUserRoleActor(Long projectRoleId, Long projectId, User user)
    {
        super(projectRoleId, projectId, user.getName(), UserRoleActorFactory.TYPE);
        this.user = user;
    }

    public Set getUsers()
    {
        return new HashSet(EasyList.build(user));
    }

    public boolean contains(User user)
    {
        return this.user.equals(user);
    }
}