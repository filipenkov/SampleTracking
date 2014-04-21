package com.atlassian.jira.security.roles;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import org.easymock.MockControl;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the DefaultProjectRoleManager.
 */
public class TestDefaultProjectRoleManager extends LegacyJiraMockTestCase
{
    private static final String I_EXIST_DESC = "I exist desc";
    private static final String I_EXIST = "I EXIST";
    private Mock mockProjectRoleAndActorStore = null;

    public TestDefaultProjectRoleManager(final String name)
    {
        super(name);
        mockProjectRoleAndActorStore = new Mock(ProjectRoleAndActorStore.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        // TODO REMOVE THIS!!!!!!
        // Need this here to remove the mocked out project manager
        ManagerFactory.quickRefresh();
        super.tearDown();

    }

    public void testCreateProjectIdToProjectRolesMap()
    {
        ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager((ProjectRoleAndActorStore) mockProjectRoleAndActorStore.proxy());

        ProjectRoleManager.ProjectIdToProjectRoleIdsMap map;

        // null project IDs returns an empty map
        map = projectRoleManager.createProjectIdToProjectRolesMap(null, null);
        assertTrue(map.isEmpty());

        // empty collection of project IDs returns an empty map
        map = projectRoleManager.createProjectIdToProjectRolesMap(null, new ArrayList());
        assertTrue(map.isEmpty());

        final List expectedProjectIds = Collections.unmodifiableList(EasyList.build(new Long(10), new Long(20), new Long(150)));

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        final MockProject mockProject1 = new MockProject(new Long(10));
        mockProjectManagerControl.expectAndReturn(mockProjectManager.getProjectObj(new Long(10)), mockProject1);
        final MockProject mockProject2 = new MockProject(new Long(20));
        mockProjectManagerControl.expectAndReturn(mockProjectManager.getProjectObj(new Long(20)), mockProject2);
        final MockProject mockProject3 = new MockProject(new Long(150));
        mockProjectManagerControl.expectAndReturn(mockProjectManager.getProjectObj(new Long(150)), mockProject3);
        mockProjectManagerControl.replay();

        ManagerFactory.addService(ProjectManager.class, mockProjectManager);

        final List project1RoleIds = EasyList.build(new Long(11), new Long(21), new Long(31));
        final List project2RoleIds = EasyList.build(new Long(101), new Long(201), new Long(301));
        final List project3RoleIds = EasyList.build(new Long(1100), new Long(2100), new Long(3100));

        final AtomicInteger getProjectRolesCalled = new AtomicInteger(0);

        final com.opensymphony.user.User testUser = UtilsForTests.getTestUser("dushan");

        projectRoleManager = new DefaultProjectRoleManager(null)
        {
            @Override
            public Collection getProjectRoles(final User user, final Project project)
            {
                getProjectRolesCalled.incrementAndGet();

                // Test that the correct user object was passed
                assertTrue(user == testUser);

                if (project == mockProject1)
                {
                    return getMockProjectRoles(project1RoleIds);
                }
                else if (project == mockProject2)
                {
                    return getMockProjectRoles(project2RoleIds);
                }
                else if (project == mockProject3)
                {
                    return getMockProjectRoles(project3RoleIds);
                }

                throw new IllegalArgumentException("Unexpected project passed.");
            }

            private Collection getMockProjectRoles(final Collection mockProjectRoledIds)
            {
                final Collection mockProjectRoles = new ArrayList();
                for (final Iterator iterator = mockProjectRoledIds.iterator(); iterator.hasNext();)
                {
                    final Long roleId = (Long) iterator.next();
                    mockProjectRoles.add(new MockProjectRoleManager.MockProjectRole(roleId.longValue(), null, null));
                }

                return mockProjectRoles;
            }
        };

        // Do the right thing
        map = projectRoleManager.createProjectIdToProjectRolesMap(testUser, expectedProjectIds);

        // Ensure the getProjectRoles was called 3 times
        assertEquals(3, getProjectRolesCalled.get());

        // Check that the map has correct contebnts
        assertFalse(map.isEmpty());

        int called = 0;
        for (final ProjectRoleManager.ProjectIdToProjectRoleIdsMap.Entry entry : map)
        {
            called++;
            if (new Long(10).equals(entry.getProjectId()))
            {
                assertEquals(project1RoleIds, entry.getProjectRoleIds());
            }
            else if (new Long(20).equals(entry.getProjectId()))
            {
                assertEquals(project2RoleIds, entry.getProjectRoleIds());
            }
            else if (new Long(150).equals(entry.getProjectId()))
            {
                assertEquals(project3RoleIds, entry.getProjectRoleIds());
            }
            else
            {
                fail("Unexpected project id '" + entry.getProjectId() + "'.");
            }
        }

        // Ensure we have only 3 keys in the map
        assertEquals(3, called);

        // Ensure all the right methods were called on the ProjectManager
        mockProjectManagerControl.verify();
    }

    public void testProjectIdToProjectRoleIdsMap()
    {
        final ProjectRoleManager.ProjectIdToProjectRoleIdsMap map = new ProjectRoleManager.ProjectIdToProjectRoleIdsMap();
        assertTrue(map.isEmpty());

        final Long PROJECT_ID_1 = new Long(1);
        final Long PROJECT_ID_2 = new Long(2);

        final Long PROJECT_ROLE_ID_1 = new Long(11);
        final Long PROJECT_ROLE_ID_2 = new Long(22);
        final Long PROJECT_ROLE_ID_3 = new Long(33);

        // adding nulls should not modify the map
        map.add(null, null);
        assertTrue(map.isEmpty());

        // null project ID should not affect the map
        map.add(null, PROJECT_ROLE_ID_1);
        assertTrue(map.isEmpty());

        // adding a null project role ID should not affect the map
        map.add(PROJECT_ID_1, null);
        assertTrue(map.isEmpty());

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_1);
        assertFalse(map.isEmpty());
        ProjectRoleManager.ProjectIdToProjectRoleIdsMap.Entry entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        assertTrue(entry.getProjectRoleIds().contains(PROJECT_ROLE_ID_1));

        // adding a null project role ID should not affect the map
        map.add(PROJECT_ID_1, null);
        // same test as above
        assertFalse(map.isEmpty());
        entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        assertTrue(entry.getProjectRoleIds().contains(PROJECT_ROLE_ID_1));

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_2);
        assertFalse(map.isEmpty());
        entry = map.iterator().next();
        assertEquals(PROJECT_ID_1, entry.getProjectId());
        Collection projectRoleIds = entry.getProjectRoleIds();
        assertEquals(2, projectRoleIds.size());
        assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_1));
        assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_2));

        map.add(PROJECT_ID_1, PROJECT_ROLE_ID_3);
        map.add(PROJECT_ID_2, PROJECT_ROLE_ID_3);
        assertFalse(map.isEmpty());
        for (int i = 0; i < 2; i++)
        {
            entry = map.iterator().next();
            if (PROJECT_ID_1.equals(entry.getProjectId()))
            {
                projectRoleIds = entry.getProjectRoleIds();
                assertEquals(3, projectRoleIds.size());
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_1));
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_2));
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_3));
            }
            else if (PROJECT_ID_2.equals(entry.getProjectId()))
            {
                projectRoleIds = entry.getProjectRoleIds();
                assertEquals(1, projectRoleIds.size());
                assertTrue(projectRoleIds.contains(PROJECT_ROLE_ID_3));
            }
            else
            {
                fail();
            }
        }

    }

    public void testCreateProjectRoleDuplicateNameError()
    {
        mockProjectRoleAndActorStore.expectAndReturn("getProjectRoleByName", P.ANY_ARGS, new ProjectRoleImpl(I_EXIST, I_EXIST_DESC));

        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager((ProjectRoleAndActorStore) mockProjectRoleAndActorStore.proxy());
        try
        {
            projectRoleManager.createRole(new ProjectRoleImpl(I_EXIST, I_EXIST_DESC));
            fail();
        }
        catch (final IllegalArgumentException iae)
        {
            assertEquals("A project role with the provided name: " + I_EXIST + ", already exists in the system.", iae.getMessage());
        }
    }

    public void testGetDefaultRoleActors()
    {
        // Since we are just testing the illegal args we can use a null store
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);

        try
        {
            projectRoleManager.getDefaultRoleActors(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    public void testUpdateDefaultRoleActors()
    {
        // Since we are just testing the illegal args we can use a null store
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);
        try
        {
            // testing that the defaultRoleActors cannot be null
            projectRoleManager.updateDefaultRoleActors(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }

        try
        {
            // Testing that the projectRole cannot be null
            final DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(null, Collections.EMPTY_SET);
            projectRoleManager.updateDefaultRoleActors(defaultRoleActors);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    public void applyDefaultsRolesToProject(final Project project)
    {
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(null);
        try
        {
            // testing that the project cannot be null and exception is thrown
            projectRoleManager.applyDefaultsRolesToProject(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //everything is good
        }
    }

    public void testIsUserInRole()
    {
        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager((ProjectRoleAndActorStore) mockProjectRoleAndActorStore.proxy());

        // Prepare the mock object to pass in and return
        final MockProject mockProject = new MockProject();
        mockProject.setId(new Long(123));
        final com.opensymphony.user.User user1 = UtilsForTests.getTestUser("roleactoruser");
        final com.opensymphony.user.User user2 = UtilsForTests.getTestUser("roleactoruser2");
        final HashSet users = new HashSet();
        users.add(user1);

        final RoleActor actor = new MyRoleActor(users);
        final Set actors = new HashSet();
        actors.add(actor);
        final ProjectRoleActors projectRoleActors = new ProjectRoleActorsImpl(mockProject.getId(),
            MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId(), actors);

        mockProjectRoleAndActorStore.expectAndReturn("getProjectRoleActors", new Constraint[] { new IsEqual(
            MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId()), new IsEqual(mockProject.getId()) }, projectRoleActors);

        final boolean user1InRole = projectRoleManager.isUserInProjectRole(user1, MockProjectRoleManager.PROJECT_ROLE_TYPE_1, mockProject);
        final boolean user2InRole = projectRoleManager.isUserInProjectRole(user2, MockProjectRoleManager.PROJECT_ROLE_TYPE_1, mockProject);

        assertTrue(user1InRole);
        assertFalse(user2InRole);
    }

    public void testNullInputsForIsUserInRole()
    {
        try
        {
            final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(
                (ProjectRoleAndActorStore) mockProjectRoleAndActorStore.proxy());

            final boolean user1InRole = projectRoleManager.isUserInProjectRole(null, null, null);
            fail("We should have thrown an IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {

        }
    }

    public void testGetProjectRoles()
    {
        final MockProject mockProject = new MockProject();
        mockProject.setKey("PRJ");
        mockProject.setId(new Long(456));
        mockProject.setName("Projectini");

        final com.opensymphony.user.User userInRole = UtilsForTests.getTestUser("userinrole");
        final com.opensymphony.user.User userInGroupInRole = UtilsForTests.getTestUser("useringroupinrole");
        final Group groupInRole = UtilsForTests.getTestGroup("groupinrole");
        groupInRole.addUser(userInGroupInRole);
        final com.opensymphony.user.User userNotInRole = UtilsForTests.getTestUser("usernotinrole");
        final Group groupNotInRole = UtilsForTests.getTestGroup("groupnotinrole");
        groupNotInRole.addUser(userNotInRole);

        final Set actors = new HashSet();
        final Long roleId = MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId();
        final Long projectId = mockProject.getId();
        final RoleActor userRoleActor = new MockUserRoleActor(roleId, projectId, userInRole);
        actors.add(userRoleActor);
        final RoleActor groupRoleActor = new MockGroupRoleActor(roleId, projectId, groupInRole);
        actors.add(groupRoleActor);

        final ProjectRoleActors projectRoleActorsWithUsers = new ProjectRoleActorsImpl(projectId, roleId, actors);

        final ProjectRoleManager projectRoleManager = new DefaultProjectRoleManager(new MockProjectRoleAndActorStore()
        {
            @Override
            public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId) throws DataAccessException
            {
                if (projectRoleId.equals(roleId) && projectId.equals(projectId))
                {
                    return projectRoleActorsWithUsers;
                }

                return new ProjectRoleActorsImpl(projectId, projectRoleId, Collections.EMPTY_SET);
            }

            @Override
            public Collection getAllProjectRoles() throws DataAccessException
            {
                return EasyList.build(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, MockProjectRoleManager.PROJECT_ROLE_TYPE_2,
                    MockProjectRoleManager.PROJECT_ROLE_TYPE_3);
            }
        });

        assertContainsOnly(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, projectRoleManager.getProjectRoles(userInRole, mockProject));
        assertContainsOnly(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, projectRoleManager.getProjectRoles(userInGroupInRole, mockProject));
        assertTrue(projectRoleManager.getProjectRoles(userNotInRole, mockProject).isEmpty());

    }

    /**
     * A do-nothing mock for testing.
     */
    private static class MockProjectRoleAndActorStore implements ProjectRoleAndActorStore
    {
        public ProjectRole addProjectRole(final ProjectRole projectRole) throws DataAccessException
        {
            return null;
        }

        public void applyDefaultsRolesToProject(final Project project) throws DataAccessException
        {}

        public void deleteProjectRole(final ProjectRole projectRole) throws DataAccessException
        {}

        public Collection getAllProjectRoles() throws DataAccessException
        {
            return null;
        }

        public DefaultRoleActors getDefaultRoleActors(final Long projectRoleId) throws DataAccessException
        {
            return null;
        }

        public ProjectRole getProjectRole(final Long id) throws DataAccessException
        {
            return null;
        }

        public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId) throws DataAccessException
        {
            return null;
        }

        public ProjectRole getProjectRoleByName(final String name) throws DataAccessException
        {
            return null;
        }

        public void removeAllRoleActorsByNameAndType(final String name, final String type) throws DataAccessException
        {}

        public void removeAllRoleActorsByProject(final Project project) throws DataAccessException
        {}

        public Collection getProjectIdsContainingRoleActorByNameAndType(final String name, final String type) throws DataAccessException
        {
            return null;
        }

        public List roleActorOfTypeExistsForProjects(final List projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String projectRoleParameter) throws DataAccessException
        {
            return null;
        }

        public Map getProjectIdsForUserInGroupsBecauseOfRole(final List projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String userName) throws DataAccessException
        {
            return null;
        }

        public void updateDefaultRoleActors(final DefaultRoleActors defaultRoleActors) throws DataAccessException
        {}

        public void updateProjectRole(final ProjectRole projectRole) throws DataAccessException
        {}

        public void updateProjectRoleActors(final ProjectRoleActors projectRoleActors) throws DataAccessException
        {}
    }

    /**
     * Mock that could be promoted in the test source tree for making roles stuff
     * easier to test.
     */
    private static class MyRoleActor implements RoleActor
    {
        private final HashSet users;

        public MyRoleActor(final HashSet users)
        {
            this.users = users;
        }

        public String getDescriptor()
        {
            return null;
        }

        public Long getId()
        {
            return null;
        }

        public String getParameter()
        {
            return null;
        }

        public String getPrettyName()
        {
            return null;
        }

        public Long getProjectRoleId()
        {
            return null;
        }

        public String getType()
        {
            return null;
        }

        public Set getUsers()
        {
            return users;
        }

        public boolean contains(com.opensymphony.user.User user)
        {
            return contains((User) user);
        }

        public boolean contains(final User user)
        {
            return users.contains(user);
        }

        public void setId(final Long id)
        {}

        public void setParameter(final String parameter)
        {}

        public void setProjectRole(final ProjectRole projectRole)
        {}
    }
}
