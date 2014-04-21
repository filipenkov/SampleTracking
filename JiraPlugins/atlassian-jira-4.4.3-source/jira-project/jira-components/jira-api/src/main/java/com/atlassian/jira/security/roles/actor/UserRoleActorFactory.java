package com.atlassian.jira.security.roles.actor;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for construction of UserRoleActor instances. Also optimises the 
 * lookup where we have many users in a particular role for a project by doing
 * a map lookup based on the username.
 * <p>
 * Access to the actual User instance is via a UserFactory so we can unit-test.
 * The production dependency is set in the default ctor. 
 */
public class UserRoleActorFactory implements RoleActorFactory
{
    public static final String TYPE = "atlassian-user-role-actor";

    private final UserFactory userFactory;
    private final CrowdService crowdService;

    public UserRoleActorFactory(final CrowdService crowdService)
    {
        // plugin the production factory here
        this(new UserFactory()
        {
            public User getUser(String name)
            {
                return crowdService.getUser(name);
            }
        }, crowdService);
    }

    UserRoleActorFactory(UserFactory userFactory, final CrowdService crowdService)
    {
        if (userFactory == null)
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot be constructed without a UserFactory instance");
        }
        this.userFactory = userFactory;
        this.crowdService = crowdService;
    }

    public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter)
            throws RoleActorDoesNotExistException
    {
        if (!TYPE.equals(type))
        {
            throw new IllegalArgumentException(this.getClass().getName() + " cannot create RoleActors of type: " + type);
        }
        Assertions.notNull("parameter", parameter);
        User user = userFactory.getUser(parameter);
        if (user == null)
        {
            throw new RoleActorDoesNotExistException("User '" + parameter + "' does not exist.");
        }
        return new UserRoleActor(id, projectRoleId, projectId, user);
    }

    public Set optimizeRoleActorSet(Set roleActors)
    {
        Set originals = new HashSet(roleActors);
        Set userRoleActors = new HashSet(roleActors.size());
        for (Iterator it = originals.iterator(); it.hasNext();)
        {
            ProjectRoleActor roleActor = (ProjectRoleActor) it.next();
            if (roleActor instanceof UserRoleActor)
            {
                userRoleActors.add(roleActor);
                it.remove();
            }
        }
        if (!userRoleActors.isEmpty())
        {
            // no point aggregating if there's only one
            if (userRoleActors.size() > 1)
            {
                UserRoleActor roleActor = (UserRoleActor) userRoleActors.iterator().next();
                originals.add(new AggregateRoleActor(roleActor, userRoleActors));
            }
            else
            {
                // just one? throw it back...
                originals.addAll(userRoleActors);
            }
        }
        return Collections.unmodifiableSet(originals);
    }

    class UserRoleActor extends AbstractRoleActor
    {
        private final com.opensymphony.user.User user;

        private UserRoleActor(Long id, Long projectRoleId, Long projectId, User user)
        {
            super(id, projectRoleId, projectId, user.getName());
            this.user = OSUserConverter.convertToOSUser(user);
        }

        public String getType()
        {
            return TYPE;
        }

        public String getDescriptor()
        {
            return user.getDisplayName();
        }

        public Set<com.opensymphony.user.User> getUsers()
        {
            return CollectionBuilder.newBuilder(user).asSet();
        }

        public boolean contains(User user)
        {
            return user != null && getParameter().equals(user.getName());
        }

        public boolean contains(com.opensymphony.user.User user)
        {
            return contains((User) user);
        }
    }

    /**
     * Aggregate UserRoleActors and look them up based on the hashcode
     */
    static class AggregateRoleActor extends AbstractRoleActor
    {
        private final Map /* <String, UserRoleActor */userRoleActorMap;

        private AggregateRoleActor(ProjectRoleActor prototype, Set /* <UserRoleActor> */roleActors)
        {
            super(null, prototype.getProjectRoleId(), prototype.getProjectId(), null);
            Map map = new HashMap(roleActors.size());
            for (Iterator it = roleActors.iterator(); it.hasNext();)
            {
                UserRoleActor userRoleActor = (UserRoleActor) it.next();
                map.put(userRoleActor.getParameter(), userRoleActor);
            }
            this.userRoleActorMap = Collections.unmodifiableMap(map);
        }

        public boolean contains(com.opensymphony.user.User user)
        {
            return user != null && userRoleActorMap.containsKey(user.getName())
                    && ((RoleActor) userRoleActorMap.get(user.getName())).contains(user);
        }

        /*
         * not enormously efficient, could cache users maybe, we want contains to be fast...
         * 
         * @see com.atlassian.jira.security.roles.RoleActor#getUsers()
         */
        public Set<com.opensymphony.user.User> getUsers()
        {
            Set<com.opensymphony.user.User> result = new HashSet(userRoleActorMap.size());
            for (Iterator it = userRoleActorMap.values().iterator(); it.hasNext();)
            {
                // not the most efficient, but generally called in UI etc.
                UserRoleActor roleActor = (UserRoleActor) it.next();
                result.addAll(roleActor.getUsers());
            }
            return Collections.unmodifiableSet(result);
        }

        public String getType()
        {
            return TYPE;
        }
    }

    /**
     * Used to mock out calls to stupid OSUser.
     */
    interface UserFactory
    {
        User getUser(String name);
    }
}
