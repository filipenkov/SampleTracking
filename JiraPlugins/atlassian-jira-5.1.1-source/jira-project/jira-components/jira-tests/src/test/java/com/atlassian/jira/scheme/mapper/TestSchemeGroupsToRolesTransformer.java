package com.atlassian.jira.scheme.mapper;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.scheme.AbstractSchemeTest;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.roles.PluginDelegatingRoleActorFactory;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.tasks.UpgradeTask1_2;
import com.atlassian.jira.user.MockUser;
import com.atlassian.plugin.DefaultPluginManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 */
public class TestSchemeGroupsToRolesTransformer extends AbstractSchemeTest
{
    private SchemeFactory schemeFactory;
    private SchemeManagerFactory schemeManagerFactory;
    private SchemeGroupsToRolesTransformer schemeGroupsToRolesTransformer;
    private DefaultPluginManager pluginManager;

    @Override
    protected void overrideServices()
    {
        try
        {
            pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(), EasyList.build(new SinglePluginLoader(
                "system-projectroleactors-plugin.xml")), new JiraModuleDescriptorFactory(new JiraHostContainer()), new DefaultPluginEventManager());
            pluginManager.init();
            ManagerFactory.addService(PluginAccessor.class, pluginManager);
            ManagerFactory.addService(PluginController.class, pluginManager);
            ManagerFactory.addService(PluginSystemLifecycle.class, pluginManager);
        }
        catch (final PluginParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        schemeManagerFactory = ComponentManager.getComponentInstanceOfType(SchemeManagerFactory.class);
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        final GroupManager groupManager = ComponentManager.getComponentInstanceOfType(GroupManager.class);
        // Please note that the RoleActorFactory cannot be retrieved via the ComponentManager, since it needs to be
        // constructed with the mockPluginManager above.  The problem is that the ManagerFactory.quickRefresh()
        // (which is called by setup()) method creates a DefaultProjectManager which in turn requires a
        // ProjectRoleMapper, which eventually requires a RoleActorFactory.  So by the time we get here,
        // Pico already contains a RoleActorFactory, that has the original PluginManager injected, and not the one
        // that we provide here in the overrideServices() method. Yay for pico.  Yay for ugly hacks in quickRefresh().
        final RoleActorFactory roleActorFactory = new PluginDelegatingRoleActorFactory(pluginManager);
        schemeFactory = ComponentManager.getComponentInstanceOfType(SchemeFactory.class);
        schemeGroupsToRolesTransformer = new SchemeGroupsToRolesTransformerImpl(schemeManagerFactory, projectRoleManager, roleActorFactory, null, groupManager);
    }

    public void testDoTransform() throws OperationNotPermittedException, InvalidGroupException
    {
        // Create the groups
        createMockGroup("test param 1");
        createMockGroup("test param 2");
        createMockGroup("test param 3");

        final Scheme scheme = getSchemeForType(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER);

        final Set<GroupToRoleMapping> groupToRoleMappings = new HashSet<GroupToRoleMapping>();
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 1"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 2"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 3"));

        // We will test a single scheme which has three entites, all of which should be replaced with PROJECT_ROLE_TYPE_1
        final SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(scheme), groupToRoleMappings);

        // We only passed in 1 scheme, we should only get out 1
        final List unassociatedTransformedResults = transformResults.getUnassociatedTransformedResults();
        assertEquals(1, unassociatedTransformedResults.size());

        final SchemeTransformResult result = (SchemeTransformResult) unassociatedTransformedResults.get(0);

        // assert that the original scheme is correct in the return type
        assertEquals(scheme, result.getOriginalScheme());

        // assert that all the GroupRoleMappings have been created correctly
        assertEquals(1, result.getRoleToGroupsMappings().size());
        assertEquals(3, ((RoleToGroupsMapping) new ArrayList(result.getRoleToGroupsMappings()).get(0)).getMappedGroups().size());

        for (final Iterator iterator = result.getTransformedScheme().getEntities().iterator(); iterator.hasNext();)
        {
            final SchemeEntity schemeEntity = (SchemeEntity) iterator.next();
            assertEquals(ProjectRoleService.PROJECTROLE_NOTIFICATION_TYPE, schemeEntity.getType());
            assertEquals(MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId().toString(), schemeEntity.getParameter());
        }
    }

    public void testDoTransformNoTransformationOccurred()
    {
        final Scheme scheme = getSchemeForType(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER);

        final Set groupToRoleMappings = new HashSet();

        // We will test a single scheme which has three entites, none of which should match our empty group to role mapping
        final SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(scheme), groupToRoleMappings);

        // We only passed in 1 scheme, we should only get out 1
        final List unassociatedUntransformedResults = transformResults.getUnassociatedUntransformedResults();
        assertEquals(1, unassociatedUntransformedResults.size());

        final SchemeTransformResult result = (SchemeTransformResult) unassociatedUntransformedResults.get(0);

        // assert that the original scheme is correct in the return type
        assertFalse(result.originalSchemeTransformed());
    }

    public void testDoTransformWithRealPermissionScheme() throws Exception
    {
        // Running the upgrade task will ensure that we have a real representation of the default notificaiton
        // scheme so that we can exercise all the possible strange bits in a scheme.
        final UpgradeTask1_2 upgradeTask1_2 = new UpgradeTask1_2();
        upgradeTask1_2.doUpgrade(false);

        createMockGroup("jira-administrators");
        createMockGroup("jira-developers");
        createMockGroup("jira-users");

        final SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);

        final GenericValue defaultSchemeGV = schemeManager.getScheme("Default Permission Scheme");

        final Scheme defaultScheme = schemeFactory.getScheme(defaultSchemeGV);

        final Set groupToRoleMappings = new HashSet();
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "jira-administrators"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_2, "jira-developers"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_3, "jira-users"));

        // We will test a single scheme which has three entites, all of which should be replaced with PROJECT_ROLE_TYPE_1
        final SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(defaultScheme), groupToRoleMappings);

        // We only passed in 1 scheme, we should only get out 1
        final List unassociatedTransformedResults = transformResults.getUnassociatedTransformedResults();
        assertEquals(1, unassociatedTransformedResults.size());

        final SchemeTransformResult result = (SchemeTransformResult) unassociatedTransformedResults.get(0);

        // assert that all the GroupRoleMappings have been created correctly
        assertEquals(3, result.getRoleToGroupsMappings().size());
        result.getTransformedScheme();

        for (final Iterator iterator = result.getTransformedScheme().getEntities().iterator(); iterator.hasNext();)
        {
            final SchemeEntity schemeEntity = (SchemeEntity) iterator.next();
            assertEquals("projectrole", schemeEntity.getType());
        }
    }

    //JRA-11692: This tests the scenario, where 2 transformations to the same role back-to-back end up with
    // the same role shown twice in the resulting scheme.
    public void testDoTransformWithoutDuplicates() throws Exception
    {
        // Create the groups
        CrowdService crowdService = StaticCrowdServiceFactory.getCrowdService();
        createMockGroup("test param 1");
        createMockGroup("test param 2");
        createMockGroup("test param 3");

        final Scheme scheme = getSchemeForType(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER);

        Set groupToRoleMappings = new HashSet();
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 1"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 2"));

        // We will test a single scheme which has three entites, all of which should be replaced with PROJECT_ROLE_TYPE_1
        SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(scheme), groupToRoleMappings);

        // We only passed in 1 scheme, we should only get out 1
        List unassociatedTransformedResults = transformResults.getUnassociatedTransformedResults();
        assertEquals(1, unassociatedTransformedResults.size());

        SchemeTransformResult result = (SchemeTransformResult) unassociatedTransformedResults.get(0);

        // assert that the original scheme is correct in the return type
        assertEquals(scheme, result.getOriginalScheme());

        // assert that all the GroupRoleMappings have been created correctly
        assertEquals(1, result.getRoleToGroupsMappings().size());
        assertEquals(2, ((RoleToGroupsMapping) new ArrayList(result.getRoleToGroupsMappings()).get(0)).getMappedGroups().size());

        // check that we've got the correct number of scheme entities.
        assertEquals(2, result.getTransformedScheme().getEntities().size());

        groupToRoleMappings = new HashSet();
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_1, "test param 3"));
        transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(result.getTransformedScheme()), groupToRoleMappings);

        unassociatedTransformedResults = transformResults.getUnassociatedTransformedResults();
        result = (SchemeTransformResult) unassociatedTransformedResults.get(0);
        assertEquals(1, result.getTransformedScheme().getEntities().size());
    }

    //This test ensures that only users part of the mapped scheme are actually added to the Role
    public void testUnpackUsers() throws Exception
    {
        // Running the upgrade task will ensure that we have a real representation of the default notificaiton
        // scheme so that we can exercise all the possible strange bits in a scheme.
        final UpgradeTask1_2 upgradeTask1_2 = new UpgradeTask1_2();
        upgradeTask1_2.doUpgrade(false);

        final SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);

        final GenericValue defaultSchemeGV = schemeManager.getScheme("Default Permission Scheme");

        final Scheme defaultScheme = schemeFactory.getScheme(defaultSchemeGV);

        final Set groupToRoleMappings = new HashSet();
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_2, "jira-developers"));
        groupToRoleMappings.add(new GroupToRoleMapping(MockProjectRoleManager.PROJECT_ROLE_TYPE_2, "homosapien-users"));

        // Create a project and get the Object representation of it.
        final ProjectFactory projectFactory = ComponentAccessor.getProjectFactory();

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST", "name", "project 1"));
        final Project project1 = projectFactory.getProject(projectGV);
        ManagerFactory.getProjectManager().refresh();

        // Associate the permission scheme with our project
        schemeManager.addSchemeToProject(project1, defaultScheme);

        // Create some users and add them to different groups
        CrowdService crowdService = StaticCrowdServiceFactory.getCrowdService();
        final User user1 = new MockUser("test1", "", "test1@email.com");
        final User user2 = new MockUser("test2", "", "test2@email.com");
        crowdService.addUser(user1, "");
        crowdService.addUser(user2, "");

        Group devGroup = new MockGroup("jira-developers");
        crowdService.addGroup(devGroup);
        crowdService.addUserToGroup(user1, devGroup);

        Group userGroup = new MockGroup("homosapien-users");
        crowdService.addGroup(userGroup);
        crowdService.addUserToGroup(user2, userGroup);

        // We will test a single scheme which has three entites, all of which should be replaced with PROJECT_ROLE_TYPE_1
        final SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(defaultScheme), groupToRoleMappings);

        final SchemeTransformResult schemeTransformResult = (SchemeTransformResult) transformResults.getAssociatedTransformedResults().get(0);
        for (final Iterator iterator = schemeTransformResult.getRoleToGroupsMappings().iterator(); iterator.hasNext();)
        {
            final RoleToGroupsMapping roleToGroupsMapping = (RoleToGroupsMapping) iterator.next();
            if (MockProjectRoleManager.PROJECT_ROLE_TYPE_2.getName().equals(roleToGroupsMapping.getProjectRole().getName()) && (roleToGroupsMapping.getUnpackedUsers().size() != 1))
            {
                fail("More than 1 user was mapped to the developers role!");
            }
        }

    }

    // This test is a 'round-trip' test that requires that a lot of persistent entities exist in the db (project,
    // scheme, project-scheme association, users, projectRole).
    public void testPersistUsersToProjectRoles()
            throws GenericEntityException, OperationNotPermittedException, InvalidGroupException, InvalidUserException, InvalidCredentialException
    {

        // Create a projectRole for Developers
        ProjectRole developers = new ProjectRoleImpl("Developers", "Project role for developers");
        final ProjectRoleManager projectRoleManager = ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);
        developers = projectRoleManager.createRole(developers);

        // Create a project and get the Object representation of it.
        final ProjectFactory projectFactory = ComponentAccessor.getProjectFactory();

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST", "name", "project 1"));
        final Project project1 = projectFactory.getProject(projectGV);
        ManagerFactory.getProjectManager().refresh();

        // Create a default permission scheme
        // Running the upgrade task will ensure that we have a real representation of the default notificaiton
        // scheme so that we can exercise all the possible strange bits in a scheme.
        final UpgradeTask1_2 upgradeTask1_2 = new UpgradeTask1_2();
        upgradeTask1_2.doUpgrade(false);

        CrowdService crowdService = StaticCrowdServiceFactory.getCrowdService();
        createMockGroup("jira-administrators");
        createMockGroup("jira-developers");
        createMockGroup("jira-users");

        final SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);
        final GenericValue defaultSchemeGV = schemeManager.getScheme("Default Permission Scheme");
        final Scheme defaultScheme = schemeFactory.getScheme(defaultSchemeGV);

        // Associate the permission scheme with our project
        schemeManager.addSchemeToProject(project1, defaultScheme);

        // Create some users and add them to the jira-developers group
        final User user1 = getUser("test1", "test1@email.com");
        final User user2 = getUser("test2", "test2@email.com");

        Group group = new MockGroup("jira-developers");
        crowdService.addGroup(group);
        crowdService.addUserToGroup(user1, group);
        crowdService.addUserToGroup(user2, group);

        final SchemeTransformResults transformResults = transformResults(developers, defaultScheme);

        final SchemeTransformResult schemeTransformResult = validateUserTransformationResults(transformResults);

        // Now persist the result
        schemeGroupsToRolesTransformer.persistTransformationResults(transformResults);

        validatePersistUsers(projectRoleManager, developers, project1, user1, user2);

        validatePersistScheme(schemeManager, schemeTransformResult);
    }

    private void validatePersistScheme(final SchemeManager schemeManager, final SchemeTransformResult schemeTransformResult) throws GenericEntityException
    {
        final GenericValue resultingSchemeGV = schemeManager.getScheme(schemeTransformResult.getResultingScheme().getId());
        final Scheme resultingScheme = schemeFactory.getScheme(resultingSchemeGV);

        final Collection groupEntities = resultingScheme.getEntities();
        int roleEntityCount = 0;
        for (final Iterator iterator = groupEntities.iterator(); iterator.hasNext();)
        {
            final SchemeEntity entity = (SchemeEntity) iterator.next();
            if (GroupDropdown.DESC.equals(entity.getType()) && entity.getParameter().equals("jira-developers"))
            {
                fail("jira-developers was not mapped!");
            }
            if (ProjectRoleSecurityAndNotificationType.PROJECT_ROLE.equals(entity.getType()))
            {
                roleEntityCount++;
            }
        }

        assertFalse("No of Roles", 0 == roleEntityCount);
        assertEquals("Scheme Name", "Default Permission Scheme", resultingScheme.getName());

        final GenericValue backupSchemeGV = schemeManager.getScheme("Backup of Default Permission Scheme");
        final Scheme backupScheme = schemeFactory.getScheme(backupSchemeGV);

        assertNotNull("Backup Scheme", backupScheme);
        final Scheme originalScheme = schemeTransformResult.getOriginalScheme();
        final Set originalEntities = new HashSet(originalScheme.getEntities());
        final Set backupEntities = new HashSet(backupScheme.getEntities());

        final Set unionEntities = new HashSet();
        unionEntities.add(originalEntities);
        unionEntities.add(backupEntities);
        assertEquals("Backup is eqaul to original", 1, unionEntities.size());
        assertEquals("Scheme Name", originalScheme.getName(), backupScheme.getName());
        assertEquals("Scheme Desc", originalScheme.getDescription(), backupScheme.getDescription());
    }

    private void validatePersistUsers(final ProjectRoleManager projectRoleManager, final ProjectRole developers, final Project project1, final User user1, final User user2)
    {
        // Now get the Actors for the role and make sure that the two we expect are there
        final ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(developers, project1);

        assertEquals(2, projectRoleActors.getRoleActors().size());
        assertTrue(projectRoleActors.getUsers().contains(user1));
        assertTrue(projectRoleActors.getUsers().contains(user2));
    }

    private SchemeTransformResult validateUserTransformationResults(final SchemeTransformResults transformResults)
    {
        // We know there should only be one schemeTransformResult since we only passed in one scheme
        final List associatedTransformedResults = transformResults.getAssociatedTransformedResults();
        assertEquals(1, associatedTransformedResults.size());

        final SchemeTransformResult schemeTransformResult = (SchemeTransformResult) associatedTransformedResults.get(0);

        assertTrue(schemeTransformResult.originalSchemeTransformed());

        // We know there should only be one GroupRoleMapping for jira-developer to Developers
        assertEquals(1, schemeTransformResult.getRoleToGroupsMappings().size());

        // We need to check that 2 users are about to be persisted
        final RoleToGroupsMapping roleToGroupsMapping = (RoleToGroupsMapping) new ArrayList(schemeTransformResult.getRoleToGroupsMappings()).get(0);
        assertEquals(2, roleToGroupsMapping.getUnpackedUsers().size());
        return schemeTransformResult;
    }

    private SchemeTransformResults transformResults(final ProjectRole developers, final Scheme defaultScheme)
    {
        // Now we should be able to transform the Default Permission Scheme, converting jira-developers to the
        // ProjectRole Developers, and having the two users above added to the role for the project above, phew...
        final Set groupToRoleMappings = new HashSet();
        groupToRoleMappings.add(new GroupToRoleMapping(developers, "jira-developers"));

        final SchemeTransformResults transformResults = schemeGroupsToRolesTransformer.doTransform(EasyList.build(defaultScheme), groupToRoleMappings);
        return transformResults;
    }

    private User getUser(final String userName, final String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        CrowdService crowdService = StaticCrowdServiceFactory.getCrowdService();
        User user = crowdService.getUser(userName);
        if (user == null)
        {
            user = new MockUser(userName, "", email);
            crowdService.addUser(user, "");
        }
        return user;
    }
}
