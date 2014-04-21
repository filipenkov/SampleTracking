package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.*;

/**
 * Test the stores ability to convert to proper objects.
 */
public class TestOfBizProjectRoleAndActorStore extends LegacyJiraMockTestCase
{
    private static final String DEVS_DESC = "devs desc";
    private static final String DEVS = "devs";
    private static final String NEW_DEVS_DESC = "new devs desc";

    private ProjectRoleAndActorStore projectRoleAndActorStore;
    private static final String TEST_TYPE = "test type";
    private static final String ACTOR_NAME_TEST_1 = "test 1";
    private static final String ACTOR_NAME_TEST_2 = "test 2";
    private GroupManager mockGroupManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);
        mockGroupManager = new MockGroupManager();
        projectRoleAndActorStore = new OfBizProjectRoleAndActorStore((OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class),
                new MockProjectRoleManager.MockRoleActorFactory(), mockGroupManager);
    }

    protected void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(true);
        super.tearDown();
    }

    public TestOfBizProjectRoleAndActorStore(String name)
    {
        super(name);
    }

    public void testSimpleProjectRole()
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Test the getAll with no values in db
        assertEquals(0, projectRoleAndActorStore.getAllProjectRoles().size());

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);
        assertEquals(DEVS, developers.getName());
        assertEquals(DEVS_DESC, developers.getDescription());
        assertNotNull(developers.getId());
        // Now test the getAll

        ArrayList allProjectRoles = new ArrayList(projectRoleAndActorStore.getAllProjectRoles());
        assertEquals(1, allProjectRoles.size());
        assertTrue(allProjectRoles.get(0) instanceof ProjectRole);

        // Now test an update
        developers = new ProjectRoleImpl(developers.getId(), getName(), NEW_DEVS_DESC);
        projectRoleAndActorStore.updateProjectRole(developers);

        // Implicitly test the get method
        developers = projectRoleAndActorStore.getProjectRole(developers.getId());

        assertEquals(NEW_DEVS_DESC, developers.getDescription());

        // Now test a delete
        projectRoleAndActorStore.deleteProjectRole(developers);

        developers = projectRoleAndActorStore.getProjectRole(developers.getId());

        assertNull(developers);
    }

    public void testRoleActorPersistence() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));

        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        Set roleActors1 = new HashSet();
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, "test 3");

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        Set roleActors2 = new HashSet();
        roleActors2.add(actor1);
        roleActors2.add(actor3);

        ProjectRoleActors projectRoleActors = new ProjectRoleActorsImpl(projectId, roleId, roleActors1);
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);

        projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(roleId, projectId);

        assertContainsOnly(roleActors1, projectRoleActors.getRoleActors());
        assertEquals(projectId, projectRoleActors.getProjectId());
        assertEquals(roleId, projectRoleActors.getProjectRoleId());

        // Shift our role actor set so that an update will be forced to do an add and remove
        projectRoleActors = (ProjectRoleActors) projectRoleActors.removeRoleActor(actor2);
        projectRoleActors = (ProjectRoleActors) projectRoleActors.addRoleActor(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);

        projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(roleId, projectId);

        assertContainsOnly(roleActors2, projectRoleActors.getRoleActors());
    }

    public void testDefaultRoleActorPersistence() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Set roleActors1 = new HashSet();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, null, Collections.EMPTY_SET, TEST_TYPE, "test 3");

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        Set roleActors2 = new HashSet();
        roleActors2.add(actor1);
        roleActors2.add(actor3);

        DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(roleId, roleActors1);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        defaultRoleActors = projectRoleAndActorStore.getDefaultRoleActors(roleId);

        assertContainsOnly(defaultRoleActors.getRoleActors(), roleActors1);
        assertEquals(roleId, defaultRoleActors.getProjectRoleId());

        // Shift our role actor set so that an update will be forced to do an add and remove
        defaultRoleActors = defaultRoleActors.removeRoleActor(actor2);
        defaultRoleActors = defaultRoleActors.addRoleActor(actor3);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        defaultRoleActors = projectRoleAndActorStore.getDefaultRoleActors(roleId);

        assertFalse(defaultRoleActors instanceof ProjectRoleActors);

        assertContainsOnly(defaultRoleActors.getRoleActors(), roleActors2);
    }

    public void testApplyDefaultsRolesToProject() throws RoleActorDoesNotExistException
    {
        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));

        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Set roleActors1 = new HashSet();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(roleId, roleActors1);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        // Put the defaults into the project we are passing
        projectRoleAndActorStore.applyDefaultsRolesToProject(project);

        for (Iterator iterator = projectRoleAndActorStore.getAllProjectRoles().iterator(); iterator.hasNext();)
        {
            ProjectRole projectRole = (ProjectRole) iterator.next();
            ProjectRoleActors projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(projectRole.getId(), project.getId());
            assertContainsOnly(projectRoleActors.getRoleActors(), roleActors1);
        }
    }

    public void testRemoveAllRoleActorsByNameAndType() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));

        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        Set roleActors1 = new HashSet();
        final Long projectId = project.getId();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor2);

        // Also include a default role actor of the same type and name
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor4 = new MockProjectRoleManager.MockRoleActor(new Long(4), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        Set defaultRoleActors = new HashSet();
        defaultRoleActors.add(actor3);
        defaultRoleActors.add(actor4);

        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));
        projectRoleAndActorStore.updateDefaultRoleActors(new DefaultRoleActorsImpl(roleId, defaultRoleActors));

        // make sure that adding worked so we can be sure removing works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());

        projectRoleAndActorStore.removeAllRoleActorsByNameAndType(ACTOR_NAME_TEST_1, TEST_TYPE);

        // after the previous call we should have removed 1 defaultRoleActor and 1 projectRoleActor
        assertEquals(1, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(1, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());
    }

    public void testRemoveAllRoleActorsByProject() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));

        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        Set roleActors1 = new HashSet();
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor2);

        // Also include a default role actor of the same type and name
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor4 = new MockProjectRoleManager.MockRoleActor(new Long(4), roleId, null, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        Set defaultRoleActors = new HashSet();
        defaultRoleActors.add(actor3);
        defaultRoleActors.add(actor4);

        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));
        projectRoleAndActorStore.updateDefaultRoleActors(new DefaultRoleActorsImpl(roleId, defaultRoleActors));

        // make sure that adding worked so we can be sure removing works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());

        projectRoleAndActorStore.removeAllRoleActorsByProject(project);

        // after the previous call we should have removed 1 defaultRoleActor and 1 projectRoleActor
        assertEquals(0, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());
    }

    public void testGetProjectIdsForUserInGroupsBecauseOfRoleAllProjects() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to the group
        User testUser = createMockUser("dude");
        Group group = createMockGroup("mygroup");
        mockGroupManager.addUserToGroup(testUser, group);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));
        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        GenericValue projectGV2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "key", "ANA"));
        Project project2 = ComponentAccessor.getProjectFactory().getProject(projectGV2);

        Set roleActors1 = new HashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        Set roleActors2 = new HashSet();
        // Add a role actor that will match for project 2
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, project2.getId(), Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group.getName());
        roleActors2.add(actor2);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(project2.getId(), roleId, roleActors2));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        Map groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(Collections.EMPTY_LIST, developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(2, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertNotNull(groupsByProject.get(project2.getId()));
        assertEquals(1, ((List) groupsByProject.get(projectId)).size());
        assertEquals(group.getName(), ((List) groupsByProject.get(projectId)).get(0));
        assertEquals(1, ((List) groupsByProject.get(project2.getId())).size());
        assertEquals(group.getName(), ((List) groupsByProject.get(project2.getId())).get(0));
    }

    public void testGetProjectIdsForUserInGroupsBecauseOfRoleOneProject() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to the group
        User testUser = createMockUser("dude");
        Group group = createMockGroup("mygroup");
        mockGroupManager.addUserToGroup(testUser, group);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));
        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        GenericValue projectGV2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "key", "ANA"));
        Project project2 = ComponentAccessor.getProjectFactory().getProject(projectGV2);

        Set roleActors1 = new HashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, projectId, Collections.EMPTY_SET, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        Set roleActors2 = new HashSet();
        // Add a role actor that will match for project 2
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(new Long(2), roleId, project2.getId(), Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group.getName());
        roleActors2.add(actor2);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(project2.getId(), roleId, roleActors2));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        // Make sure we limit the query by projects so that we only get one result, even though we could get two if we
        // queried for all.
        Map groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(EasyList.build(projectId), developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(1, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertNull(groupsByProject.get(project2.getId()));
        assertEquals(1, ((List) groupsByProject.get(projectId)).size());
        assertEquals(group.getName(), ((List) groupsByProject.get(projectId)).get(0));
    }

    public void testGetProjectIdsForUserInGroupsBecauseOfRoleBatchGroupInClause() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to two groups
        User testUser = createMockUser("dude");
        Group group = createMockGroup("mygroup");
        Group group2 = createMockGroup("myothergroup");
        mockGroupManager.addUserToGroup(testUser, group);
        mockGroupManager.addUserToGroup(testUser, group2);

        // Set the batch size to 1 so we force a batch
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "1");

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST"));
        Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);

        Set roleActors1 = new HashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(new Long(1), roleId, projectId, Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(new Long(3), roleId, projectId, Collections.EMPTY_SET, GroupRoleActorFactory.TYPE, group2.getName());
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        Map groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(Collections.EMPTY_LIST, developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(1, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertEquals(2, ((List) groupsByProject.get(projectId)).size());
        List groupNames = (List) groupsByProject.get(projectId);
        assertTrue(groupNames.contains(group.getName()));
        assertTrue(groupNames.contains(group2.getName()));
    }
}
