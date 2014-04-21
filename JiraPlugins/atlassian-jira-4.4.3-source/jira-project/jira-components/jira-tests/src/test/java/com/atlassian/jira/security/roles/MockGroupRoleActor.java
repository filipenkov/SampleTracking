package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.user.util.UserUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MockGroupRoleActor extends MockRoleActor
{
    private final Group group;

    public MockGroupRoleActor(Long projectRoleId, Long projectId, Group group)
    {
        super(projectRoleId, projectId, group.getName(), GroupRoleActorFactory.TYPE);
        this.group = group;
    }

    public Set getUsers()
    {
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        return new HashSet(userUtil.getAllUsersInGroups(Collections.singleton(group)));
    }

    public boolean contains(com.opensymphony.user.User user)
    {
        return contains((User) user);
    }

    public boolean contains(User user)
    {
        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);
        return crowdService.isUserMemberOfGroup(user, group);
    }
}