package com.atlassian.jira.security.roles;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestCachingProjectRoleAndActorStore extends ListeningTestCase
{
    private CachingProjectRoleAndActorStore actorStore = null;
    private Mock mockProjectRoleActorStore = null;

    @Before
    public void setUp() throws Exception
    {
        mockProjectRoleActorStore = new Mock(ProjectRoleAndActorStore.class);

        actorStore = new CachingProjectRoleAndActorStore((ProjectRoleAndActorStore) mockProjectRoleActorStore.proxy(),
            new MockProjectRoleManager.MockRoleActorFactory());
    }

    @Test
    public void testAddProjectRole()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
        mockProjectRoleActorStore.expectAndReturn("addProjectRole", projectRoleType1, projectRoleType1);
        mockProjectRoleActorStore.expectNotCalled("getProjectRole");
        mockProjectRoleActorStore.expectNotCalled("getProjectRoleByName");

        actorStore.addProjectRole(projectRoleType1);

        // Call the cache and make sure that the store is not hit
        actorStore.getProjectRole(projectRoleType1.getId());
        actorStore.getProjectRoleByName(projectRoleType1.getName());

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testUpdateProjectRole()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        mockProjectRoleActorStore.expectAndReturn("getAllProjectRoles", Lists.newArrayList(projectRoleType1));
        mockProjectRoleActorStore.expectAndReturn("addProjectRole", projectRoleType1, projectRoleType1);

        mockProjectRoleActorStore.expectVoid("updateProjectRole", projectRoleType1);

        mockProjectRoleActorStore.expectAndReturn("getProjectRole", P.ANY_ARGS, projectRoleType1);
        mockProjectRoleActorStore.expectAndReturn("getProjectRoleByName", P.ANY_ARGS, projectRoleType1);

        // Make sure the cache is filled
        actorStore.addProjectRole(projectRoleType1);
        actorStore.getAllProjectRoles();

        // Clear the cache with the update call
        actorStore.updateProjectRole(projectRoleType1);

        // Call the cache and make sure that the store is hit
        actorStore.getProjectRole(projectRoleType1.getId());
        actorStore.getProjectRoleByName(projectRoleType1.getName());

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectAndReturn("getAllProjectRoles", Lists.newArrayList(projectRoleType1));

        final Collection allProjectRoles = actorStore.getAllProjectRoles();
        assertEquals(1, allProjectRoles.size());

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testUpdate() throws Exception
    {
        final Object delegate = new Object()
        {
            private ProjectRole currentRole;

            @SuppressWarnings("unused")
            public void updateProjectRole(final ProjectRole projectRole)
            {
                currentRole = projectRole;
            }

            @SuppressWarnings("unused")
            public ProjectRole getProjectRole(final Long id)
            {
                return currentRole;
            }

            @SuppressWarnings("unused")
            public ProjectRole getProjectRoleByName(final String name)
            {
                if (!currentRole.getName().equals(name))
                {
                    return null;
                }
                return currentRole;
            }
        };

        final ProjectRoleAndActorStore store = new CachingProjectRoleAndActorStore((ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
            ProjectRoleAndActorStore.class, delegate), null);

        ProjectRole projectRole = new ProjectRoleImpl(1L, "name", "description");
        store.updateProjectRole(projectRole);
        assertNotNull(store.getProjectRole(1L));
        assertNotNull(store.getProjectRoleByName("name"));
        projectRole = new ProjectRoleImpl(1L, "newname", "newdescription");
        store.updateProjectRole(projectRole);
        assertNotNull(store.getProjectRole(1L));
        assertNull(store.getProjectRoleByName("name"));
        assertNotNull(store.getProjectRoleByName("newname"));
        assertEquals("newname", store.getProjectRole(1L).getName());
        assertEquals(Long.valueOf(1L), store.getProjectRoleByName("newname").getId());
        projectRole = new ProjectRoleImpl(1L, "name", "description");
        store.updateProjectRole(projectRole);
        assertNotNull(store.getProjectRole(1L));
        assertEquals("name", store.getProjectRole(1L).getName());
    }

    @Test
    public void testGetAllProjectRoles()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        mockProjectRoleActorStore.expectAndReturn("getAllProjectRoles", Lists.newArrayList(projectRoleType1));

        Collection projectRoles = actorStore.getAllProjectRoles();

        assertTrue(projectRoles.contains(projectRoleType1));

        mockProjectRoleActorStore.verify();

        // Now call it a second time and make sure that the cache handles the call
        mockProjectRoleActorStore.expectNotCalled("getAllProjectRoles");

        projectRoles = actorStore.getAllProjectRoles();

        assertTrue(projectRoles.contains(projectRoleType1));

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testGetProjectRole()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        mockProjectRoleActorStore.expectAndReturn("getProjectRole", P.ANY_ARGS, projectRoleType1);

        ProjectRole projectRole = actorStore.getProjectRole(projectRoleType1.getId());

        assertEquals(projectRoleType1, projectRole);

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getProjectRole");

        projectRole = actorStore.getProjectRole(projectRoleType1.getId());

        assertEquals(projectRoleType1, projectRole);

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testGetProjectRoleByName()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        mockProjectRoleActorStore.expectAndReturn("getProjectRoleByName", P.ANY_ARGS, projectRoleType1);

        ProjectRole projectRole = actorStore.getProjectRoleByName(projectRoleType1.getName());

        assertEquals(projectRoleType1, projectRole);

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getProjectRoleByName");

        projectRole = actorStore.getProjectRoleByName(projectRoleType1.getName());

        assertEquals(projectRoleType1, projectRole);

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testDeleteProjectRole()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        mockProjectRoleActorStore.expectAndReturn("addProjectRole", projectRoleType1, projectRoleType1);

        mockProjectRoleActorStore.expectVoid("deleteProjectRole", projectRoleType1);

        mockProjectRoleActorStore.expectAndReturn("getAllProjectRoles", Lists.newArrayList(projectRoleType1));
        mockProjectRoleActorStore.expectAndReturn("getProjectRole", P.ANY_ARGS, projectRoleType1);
        mockProjectRoleActorStore.expectAndReturn("getProjectRoleByName", P.ANY_ARGS, projectRoleType1);

        // Make sure the cache is filled
        actorStore.getAllProjectRoles();
        actorStore.addProjectRole(projectRoleType1);

        // Clear the cache with the delete call
        actorStore.deleteProjectRole(projectRoleType1);

        // Call the cache and make sure that the store is hit
        actorStore.getProjectRole(projectRoleType1.getId());
        actorStore.getProjectRoleByName(projectRoleType1.getName());

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectAndReturn("getAllProjectRoles", Lists.newArrayList(projectRoleType1));

        final Collection allProjectRoles = actorStore.getAllProjectRoles();
        assertEquals(1, allProjectRoles.size());

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testGetProjectRoleActors()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set actors = new HashSet();
        final Long roleId = projectRoleType1.getId();
        try
        {
            actors.add(new MockProjectRoleManager.MockRoleActor(1L, roleId, null, Collections.EMPTY_SET, UserRoleActorFactory.TYPE, "testuser"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        final ProjectRoleActorsImpl projectRoleActors = new ProjectRoleActorsImpl(null, roleId, actors);
        mockProjectRoleActorStore.expectAndReturn("getProjectRoleActors", P.ANY_ARGS, projectRoleActors);

        ProjectRoleActors projectRoleActorsRetVal = actorStore.getProjectRoleActors(roleId, 1l);
        assertEquals(1, projectRoleActorsRetVal.getRoleActors().size());
        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getProjectRoleActors");
        projectRoleActorsRetVal = actorStore.getProjectRoleActors(roleId, 1l);
        assertEquals(1, projectRoleActorsRetVal.getRoleActors().size());

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testGetDefaultRoleActors()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set actors = new HashSet();
        final Long roleId = projectRoleType1.getId();
        try
        {
            actors.add(new MockProjectRoleManager.MockRoleActor(1L, roleId, null, Collections.EMPTY_SET, UserRoleActorFactory.TYPE, "testuser"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        final ProjectRoleActorsImpl projectRoleActors = new ProjectRoleActorsImpl(null, roleId, actors);
        mockProjectRoleActorStore.expectAndReturn("getDefaultRoleActors", P.ANY_ARGS, projectRoleActors);

        DefaultRoleActors defaultRoleActorsRetVal = actorStore.getDefaultRoleActors(roleId);
        assertEquals(1, defaultRoleActorsRetVal.getRoleActors().size());
        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getDefaultRoleActors");
        defaultRoleActorsRetVal = actorStore.getDefaultRoleActors(roleId);
        assertEquals(1, defaultRoleActorsRetVal.getRoleActors().size());

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testUpdateProjectRoleActors() throws RoleActorDoesNotExistException
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set actors = new HashSet();
        final Long roleId = projectRoleType1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(1L, roleId, null, Collections.EMPTY_SET, UserRoleActorFactory.TYPE, "testuser"));
        final ProjectRoleActorsImpl projectRoleActors = new ProjectRoleActorsImpl(null, roleId, actors);
        mockProjectRoleActorStore.expectAndReturn("getProjectRoleActors", P.ANY_ARGS, projectRoleActors);
        mockProjectRoleActorStore.expectVoid("updateProjectRoleActors", projectRoleActors);

        // puts it in the cache
        final ProjectRoleActors projectRoleActorsRetVal = actorStore.getProjectRoleActors(roleId, 1l);
        assertEquals(1, projectRoleActorsRetVal.getRoleActors().size());
        // clear the cache for this project role actor
        actorStore.updateProjectRoleActors(projectRoleActors);

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getProjectRoleActors");

        // This should not hit the db again because we called update in between and update recaches whatever is updated
        actorStore.getProjectRoleActors(roleId, 1l);

        mockProjectRoleActorStore.verify();
    }

    @Test
    public void testUpdateDefaultRoleActors()
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set actors = new HashSet();
        final Long roleId = projectRoleType1.getId();
        try
        {
            actors.add(new MockProjectRoleManager.MockRoleActor(1L, roleId, null, Collections.EMPTY_SET, UserRoleActorFactory.TYPE, "testuser"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        final DefaultRoleActorsImpl defaultRoleActors = new DefaultRoleActorsImpl(roleId, actors);
        mockProjectRoleActorStore.expectAndReturn("getDefaultRoleActors", P.ANY_ARGS, defaultRoleActors);
        mockProjectRoleActorStore.expectVoid("updateDefaultRoleActors", defaultRoleActors);

        // puts it in the cache
        final DefaultRoleActors defaultRoleActorsRetVal = actorStore.getDefaultRoleActors(roleId);
        assertEquals(1, defaultRoleActorsRetVal.getRoleActors().size());
        // clear the cache for this project role actor
        actorStore.updateDefaultRoleActors(defaultRoleActors);

        mockProjectRoleActorStore.verify();

        mockProjectRoleActorStore.expectNotCalled("getDefaultRoleActors");

        // This should hit the db again because we called update in between
        actorStore.getDefaultRoleActors(roleId);

        mockProjectRoleActorStore.verify();
    }

}
