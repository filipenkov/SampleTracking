package com.atlassian.jira.mock;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.*;
import com.atlassian.core.util.collection.EasyList;
import com.opensymphony.user.EntityNotFoundException;

import java.util.*;

public class MockProjectRoleManager implements ProjectRoleManager
{
    private static final Collection DEFAULT_ROLE_TYPES;
    public static final ProjectRole PROJECT_ROLE_TYPE_1 = new ProjectRoleImpl(new Long(1), "Project Administrator", "Can change settings about this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_2 = new ProjectRoleImpl(new Long(2), "Developer", "Works on this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_3 = new ProjectRoleImpl(new Long(3), "User", "Acts as a participant on this project");
    public static final ProjectRole PROJECT_ROLE_TYPE_NULL = new ProjectRoleImpl(null, null, null);

    static
    {
        DEFAULT_ROLE_TYPES = EasyList.build(PROJECT_ROLE_TYPE_1, PROJECT_ROLE_TYPE_2, PROJECT_ROLE_TYPE_3);
    }

    private Collection projectRoles = new ArrayList(DEFAULT_ROLE_TYPES);
    private long idCounter = projectRoles.size();

    public MockProjectRoleManager()
    {
    }

    public Collection getProjectRoles()
    {
        return projectRoles;
    }

    public Collection getProjectRoles(User user, Project project)
    {
        return null;
    }

    public Collection getProjectRoles(com.opensymphony.user.User user, Project project)
    {
        return null;
    }

    public ProjectRole getProjectRole(Long id)
    {
        for (Iterator iterator = getProjectRoles().iterator(); iterator.hasNext();)
        {
            ProjectRole role = (ProjectRole) iterator.next();
            if (role.getId().equals(id))
            {
                return role;
            }
        }
        return null;
    }

    public ProjectRole getProjectRole(String name)
    {
        for (Iterator iterator = getProjectRoles().iterator(); iterator.hasNext();)
        {
            ProjectRole role = (ProjectRole) iterator.next();
            if (role.getName().equals(name))
            {
                return role;
            }
        }
        return null;
    }

    public void addRole(ProjectRole projectRole)
    {
        checkRoleNameUnique(projectRole.getName());
        if (getProjectRole(projectRole.getId()) != null)
        {
            throw new IllegalArgumentException("Role with id '" + projectRole.getId() + "' already exists.");
        }

        projectRoles.add(projectRole);
    }

    public ProjectRole createRole(ProjectRole projectRole)
    {
        MockProjectRole role = new MockProjectRole(++idCounter, projectRole.getName(), projectRole.getDescription());
        addRole(projectRole);
        return role;
    }

    public boolean isRoleNameUnique(String name)
    {
        try
        {
            checkRoleNameUnique(name);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private void checkRoleNameUnique(String name)
    {
        for (Iterator iterator = getProjectRoles().iterator(); iterator.hasNext();)
        {
            ProjectRole role = (ProjectRole) iterator.next();
            if (name != null && name.equals(role.getName()))
            {
                throw new IllegalArgumentException("Cannot have two roles with the same name");
            }
        }
    }

    public void deleteRole(ProjectRole projectRole)
    {
        projectRoles.remove(projectRole);
    }

    public void updateRole(ProjectRole projectRole)
    {
        projectRoles.remove(projectRole);
        projectRoles.add(projectRole);
    }

    public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Mock bad argument");
        }
        Set actors = new HashSet();
        Set users = new HashSet();
        try
        {
            users.add(UserUtils.createUser("tester", "tester@test.com"));
        }
        catch (Exception e)
        {
            try
            {
                users.add(UserUtils.getUser("tester"));
            }
            catch (EntityNotFoundException e1)
            {
                // don't do anything...
            }
        }

        try
        {
            users.add(UserUtils.createUser("fred", "fred@test.com"));
        }
        catch (Exception e)
        {
            try
            {
                users.add(UserUtils.getUser("fred"));
            }
            catch (EntityNotFoundException e1)
            {
                // don't do anything...
            }
        }

        final Long roleId = projectRole.getId();
        final Long projectId = project.getId();
        try
        {
            actors.add(new MockRoleActor(new Long(1), roleId, projectId, users, MockRoleActor.TYPE, "tester"));
            actors.add(new MockRoleActor(new Long(2), roleId, projectId, users, MockRoleActor.TYPE, "fred"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        return new ProjectRoleActorsImpl(projectId, roleId, actors);
    }

    public void updateProjectRoleActors(ProjectRoleActors projectRoleActors)
    {
    }

    public DefaultRoleActors getDefaultRoleActors(ProjectRole projectRole)
    {
        Set actors = new HashSet();
        Set users = new HashSet();
        try
        {
            users.add(UserUtils.createUser("tester", "tester@test.com"));
        }
        catch (Exception e)
        {
            try
            {
                users.add(UserUtils.getUser("tester"));
            }
            catch (EntityNotFoundException e1)
            {
                // don't do anything...
            }
        }
        try
        {
            actors.add(new MockRoleActor(new Long(1), projectRole.getId(), null, users, MockRoleActor.TYPE, "tester"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        return new DefaultRoleActorsImpl(projectRole.getId(), actors);
    }

    public void updateDefaultRoleActors(DefaultRoleActors defaultRoleActors)
    {
    }

    public void applyDefaultsRolesToProject(Project project)
    {
    }

    public void removeAllRoleActorsByNameAndType(String name, String type)
    {
    }

    public void removeAllRoleActorsByProject(Project project)
    {
    }

    public boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project)
    {
        return false;
    }

    public boolean isUserInProjectRole(com.opensymphony.user.User user, ProjectRole projectRole, Project project)
    {
        return false;
    }

    public Collection getProjectIdsContainingRoleActorByNameAndType(String name, String type)
    {
        return Collections.EMPTY_LIST;
    }

    public List roleActorOfTypeExistsForProjects(List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter)
    {
        return null;
    }

    public Map getProjectIdsForUserInGroupsBecauseOfRole(List projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName)
    {
        return null;
    }

    public static class MockProjectRole implements ProjectRole
    {
        public MockProjectRole(long id, String name, String description)
        {
            this.name = name;
            this.description = description;
            this.id = new Long(id);
        }

        private String name;
        private String description;
        private Long id;

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String toString()
        {
            return "Project Role: " + name + '(' + id + ')';
        }
    }

    public static class MockRoleActor implements ProjectRoleActor
    {
        private Long projectRoleId;
        private Long projectId;
        private Set users;
        private String type;
        private Long id;
        private String parameter;
        public static final String INVALID_PARAMETER = "Invalid Parameter";
        public static final String TYPE = "mock type";

        public MockRoleActor(Long id, Long projectRoleId, Long projectId, Set users, String type, String parameter)
                throws RoleActorDoesNotExistException
        {
            this.id = id;
            this.projectId = projectId;
            this.projectRoleId = projectRoleId;
            this.users = users;
            this.type = type;
            setParameter(parameter);
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getProjectRoleId()
        {
            return projectRoleId;
        }

        public void setProjectRoleId(Long projectRoleId)
        {
            this.projectRoleId = projectRoleId;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        public void setProjectId(Long projectId)
        {
            this.projectId = projectId;
        }

        public String getPrettyName()
        {
            return "Mock Role Actor";
        }

        public String getDescriptor()
        {
            return type + ":" + parameter;
        }

        public String getType()
        {
            return type;
        }

        public String getParameter()
        {
            return parameter;
        }

        public Set getUsers()
        {
            return users;
        }

        public void setParameter(String parameter) throws RoleActorDoesNotExistException
        {
            if (INVALID_PARAMETER.equals(parameter))
            {
                throw new RoleActorDoesNotExistException("Invalid Param does not exist");
            }
            this.parameter = parameter;
        }

        public boolean contains(com.opensymphony.user.User user)
        {
            return contains((User) user);
        }

        public boolean contains(User user)
        {
            return users.contains(user);
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final MockRoleActor that = (MockRoleActor) o;

            return parameter.equals(that.parameter) && type.equals(that.type);
        }

        public int hashCode()
        {
            int result;
            result = type.hashCode();
            result = 29 * result + parameter.hashCode();
            return result;
        }
    }

    public static class MockRoleActorFactory implements RoleActorFactory
    {
        public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter)
                throws RoleActorDoesNotExistException
        {
            return new MockProjectRoleManager.MockRoleActor(id, projectRoleId, projectId, Collections.EMPTY_SET, type, parameter);
        }

        public Set optimizeRoleActorSet(Set roleActors)
        {
            return roleActors;
        }
    }

    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(User user, Collection projectIds)
    {
        ProjectIdToProjectRoleIdsMap map = new ProjectIdToProjectRoleIdsMap();
        if (projectIds != null && !projectIds.isEmpty())
        {
            ProjectManager projectManager = ComponentAccessor.getProjectManager();
            for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
            {
                Long projectId = (Long) iterator.next();
                Collection projectRoles = getProjectRoles(user, projectManager.getProjectObj(projectId));
                for (Iterator i = projectRoles.iterator(); i.hasNext();)
                {
                    ProjectRole projectRole = (ProjectRole) i.next();
                    map.add(projectId, projectRole.getId());
                }
            }
        }
        return map;
    }

    public ProjectIdToProjectRoleIdsMap createProjectIdToProjectRolesMap(com.opensymphony.user.User user, Collection projectIds)
    {
        return createProjectIdToProjectRolesMap((User) user, projectIds);
    }

}
