package com.atlassian.jira.security.roles.actor;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class GroupRoleActorFactory implements RoleActorFactory
{
    public static final String TYPE = "atlassian-group-role-actor";

    private static final Logger log = Logger.getLogger(GroupRoleActorFactory.class);
    private final CrowdService crowdService;
    private final GroupManager groupManager;

    public GroupRoleActorFactory(final CrowdService crowdService, final GroupManager groupManager)
    {
        this.crowdService = crowdService;
        this.groupManager = groupManager;
    }

    public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String groupName)
            throws RoleActorDoesNotExistException
    {
        if (!TYPE.equals(type))
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot create RoleActors of type: " + type);
        }
        final Group group = crowdService.getGroup(groupName);
        if (group == null)
        {
            throw new RoleActorDoesNotExistException("Group '" + groupName + "' does not exist.");
        }
        return new GroupRoleActor(id, projectRoleId, projectId, group);
    }

    public Set optimizeRoleActorSet(Set roleActors)
    {
        // no optimise for groups
        return roleActors;
    }

    public class GroupRoleActor extends AbstractRoleActor
    {
        private final Group group;

        GroupRoleActor(Long id, Long projectRoleId, Long projectId, Group group)
        {
            super(id, projectRoleId, projectId, group.getName());
            this.group = group;
        }

        public String getType()
        {
            return TYPE;
        }

        public String getDescriptor()
        {
            return getParameter();
        }

        public Set<User> getUsers()
        {
            final Set<User> users = new HashSet<User>();

            for (User user : groupManager.getUsersInGroup(group.getName()))
            {
                 users.add(user);
            }
            return users;
        }

        public boolean contains(User user)
        {
            if (user == null)
            {
                return false;
            }
            return crowdService.isUserMemberOfGroup(user, group);
        }

        /**
         * Returns a Group object that represents a valid (existing) group or throws an IllegalArgumentException
         * if the group does not exist
         *
         * @return group
         * @throws IllegalArgumentException if group does not exist
         */
        public Group getGroup() throws IllegalArgumentException
        {
            return group;
        }

    }
}
