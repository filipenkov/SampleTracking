package com.atlassian.jira.bc.projectroles;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.workflow.MockJiraWorkflow;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Sets;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.collections.MultiMap;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Test the project role service, the validation of project roles.
 */
public class TestDefaultProjectRoleService extends LegacyJiraMockTestCase
{
    private MockProjectRoleManager projectRoleManager = null;
    private JiraAuthenticationContext jiraAuthenticationContext = null;
    private DefaultProjectRoleService defaultProjectRoleServicePermFalseFalse = null;
    private DefaultProjectRoleService defaultProjectRoleServicePermFalseTrue = null;

    private static final String PROJECTROLE_WORKFLOW_FILE = "com/atlassian/jira/bc/projectroles/test-projectrole-condition-workflow.xml";
    private static final String FRED = "TestDefaultProjectRoleService_fred";
    private static final String TESTER = "TestDefaultProjectRoleService_tester";

    protected void setUp() throws Exception
    {
        super.setUp();
        projectRoleManager = new MockProjectRoleManager();
        jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();

        defaultProjectRoleServicePermFalseFalse = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(false, false), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null, null, null, null, null);
        defaultProjectRoleServicePermFalseTrue = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(false, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null, null, null, null, null);
    }

    protected void tearDown() throws Exception
    {
        projectRoleManager = null;
        jiraAuthenticationContext = null;
        defaultProjectRoleServicePermFalseFalse = null;
        defaultProjectRoleServicePermFalseTrue = null;
        UtilsForTests.cleanUsers();
        super.tearDown();
    }


    private PermissionManager getPermissionManager(final boolean projectAdminPermission, final boolean adminPermission)
    {
        return new MockPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return projectAdminPermission;
            }

            public boolean hasPermission(int permissionsId, com.atlassian.crowd.embedded.api.User u)
            {
                return adminPermission;
            }
        };
    }

    /**
      * Will return the project role based off the passed in <code>id</code>, and checking the <code>currentUser</code>
      * has the correct permissions to perform the operation.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testGetProjectRoleWithNullId()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRole(null, null, collection);

         assertNull(projectRole);
         assertTrue(collection.hasAnyErrors());
         assertTrue(collection.getErrorMessages().contains("Can not get a project role for a null id."));
     }

    /**
     * Will return the project role based off the passed in <code>id</code>, and checking the <code>currentUser</code>
     * has the correct permissions to perform the operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    public void testGetProjectRole()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRole(null, null, collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("Can not get a project role for a null id."));
    }

    public void testGetProjectRoleByName()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.getProjectRoleByName(null, null, collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("Can not get a project role with a null name."));
    }

     /**
      * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
      * the <code>currentUser</code> has the correct permissions to perform the create operation.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testCreateRoleNullNameAndAdminPermission()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.createProjectRole(null, new ProjectRoleImpl(null, null), collection);

         assertNull(projectRole);
         assertTrue(collection.hasAnyErrors());
         assertTrue(collection.getErrorMessages().contains("Can not create a project role with a null name."));
         assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
     }

     /**
      * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
      * the <code>currentUser</code> has the correct permissions to perform the create operation.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testCreateRoleAdminPermissionFalse()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         ProjectRole projectRole = defaultProjectRoleServicePermFalseFalse.createProjectRole(null, new ProjectRoleImpl("name", null), collection);

         assertNull(projectRole);
         assertTrue(collection.hasAnyErrors());
         assertFalse(collection.getErrorMessages().contains("Can not create a project role with a null name."));
         assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
     }

     /**
      * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
      * the <code>currentUser</code> has the correct permissions to perform the create operation.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testCreateRoleAdminPermissionTrue()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         ProjectRole projectRole = defaultProjectRoleServicePermFalseTrue.createProjectRole(null, new ProjectRoleImpl("name", null), collection);

         assertNotNull(projectRole);
         assertFalse(collection.hasAnyErrors());
         assertFalse(collection.getErrorMessages().contains("Can not create a project role with a null name."));
         assertFalse(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
     }

    /**
     * Will create the project role based off the passed in <code>name</code>, <code>description</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the create operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    public void testCreateRoleAdminPermissionIllegalargumentException()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        ProjectRole projectRole = defaultProjectRoleServicePermFalseTrue.createProjectRole(null, new ProjectRoleImpl("Developer", null), collection);

        assertNull(projectRole);
        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrors().get("name").equals("A project role with name 'Developer' already exists."));
    }

    public void testIsRoleNameUnique()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        assertFalse(defaultProjectRoleServicePermFalseFalse.isProjectRoleNameUnique(null, "Random Name", collection));
        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

     /**
      * Will delete the project role based off the passed in <code>projectRole</code> and checking
      * the <code>currentUser</code> has the correct permissions to perform the delete operation.
      * This will also delete all ProjectRoleActor associations that it is the parent of.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testDeleteRole()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         defaultProjectRoleServicePermFalseFalse.deleteProjectRole(null, null, collection);

         assertTrue(collection.hasAnyErrors());
         assertTrue(collection.getErrorMessages().contains("Can not delete a project role with a null project role specified."));
         assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
     }

     /**
      * Will add project role actor associations based off the passed in <code>actors</code> and checking
      * the <code>currentUser</code> has the correct permissions to perform the update operation.
      * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
      */
     public void testAddActorsToProjectRoleNullParams()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();

         defaultProjectRoleServicePermFalseFalse.addActorsToProjectRole(null, null, null, null, null, collection);

         assertTrue(collection.hasAnyErrors());
         assertTrue(collection.getErrorMessages().contains("Can not update a null role actor."));
         assertTrue(collection.getErrorMessages().contains("Can not retrieve a role actor for a null project role."));
         assertTrue(collection.getErrorMessages().contains("Can not retrieve a project role actor for a null project."));
         assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, or is not running Enterprise and does not have the Project admin permission."));
     }

    // This is a REALLY horrible method that relies on some really loosly hung together mocks.
    public void testAddActorsToProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", EasyMap.build("id", new Long(1))));
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, mockProjectManager, null, null, null, null);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.addActorsToProjectRole(null, EasyList.build(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester"), projectRoleManager.getProjectRole(new Long(1)), mockProject, MockProjectRoleManager.MockRoleActor.TYPE, collection);

        assertTrue(collection.getErrorMessages().contains("'tester' is already a member of the project role."));
        assertTrue(collection.getErrorMessages().contains("'"+MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER+"' does not exist."));
    }

    /**
     * Slowly works throuh each parameter to ensure the right error messages are produced
     */
    public void testSetActorsForNullProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", EasyMap.build("id", new Long(1))));
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, mockProjectManager, null, null, null, null);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.setActorsForProjectRole(null, null,
                projectRoleManager.getProjectRole(new Long(1)), mockProject , collection);

        assertEquals(1, collection.getErrorMessages().size());
        assertTrue(collection.getErrorMessages().contains("Can not update project actors with a null value."));

        collection = new SimpleErrorCollection();

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                    MockProjectRoleManager.MockRoleActor.TYPE,
                        Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
                ), null, mockProject , collection);
        assertEquals(2, collection.getErrorMessages().size());
        assertTrue(collection.getErrorMessages().contains("Can not retrieve a role actor for a null project role."));
        assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, "
                + "or is not running Enterprise and does not have the Project admin permission."));

        collection = new SimpleErrorCollection();

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                    MockProjectRoleManager.MockRoleActor.TYPE,
                        Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
                ), projectRoleManager.getProjectRole(new Long(1)), null, collection);
        assertEquals(2, collection.getErrorMessages().size());
        assertTrue(collection.getErrorMessages().contains("Can not retrieve a project role actor for a null project."));
        assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, "
                + "or is not running Enterprise and does not have the Project admin permission."));
    }

    public void testSetInvalidActorForProjectRole()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        Project mockProject = new ProjectImpl(new MockGenericValue("project", EasyMap.build("id", new Long(1))));
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, mockProjectManager, null, null, null, null);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                    MockProjectRoleManager.MockRoleActor.TYPE,
                        Sets.<String>newHashSet(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester")
                ),
                projectRoleManager.getProjectRole(new Long(1)), mockProject , collection);

        assertTrue(collection.getErrorMessages().contains("'"+MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER+"' does not exist."));
    }

    public void testSetActorForProjectRoleDeterminesRightAdditionAndDeletion()
    {
        MockProjectManager mockProjectManager = new MockProjectManager();
        final Project mockProject = new ProjectImpl(new MockGenericValue("project", EasyMap.build("id", new Long(1))));
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true),
                jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(),
                null, null, null, mockProjectManager, null, null, null, null)
        {
            @Override
            public void addActorsToProjectRole(com.atlassian.crowd.embedded.api.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
            {
                assertNull(currentUser);
                assertContainsOnly(CollectionBuilder.list("someUser"), actors);
                assertEquals(projectRoleManager.getProjectRole(new Long(1)), projectRole);
                assertEquals(mockProject, project);
                assertEquals(MockProjectRoleManager.MockRoleActor.TYPE, actorType);
            }

            @Override
            public void removeActorsFromProjectRole(com.atlassian.crowd.embedded.api.User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
            {
                assertNull(currentUser);
                assertContainsOnly(CollectionBuilder.list("fred", "tester"), actors);
                assertEquals(projectRoleManager.getProjectRole(new Long(1)), projectRole);
                assertEquals(mockProject, project);
                assertEquals(MockProjectRoleManager.MockRoleActor.TYPE, actorType);

            }
        };
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an RoleActorDoesNotExistException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.

        // Als note that the projectRoleManager actually contains two role actors: fred and tester

        projectRoleService.setActorsForProjectRole(null, MapBuilder.<String, Set<String>>build(
                MockProjectRoleManager.MockRoleActor.TYPE,
                Sets.<String>newHashSet("someUser")
        ),
                projectRoleManager.getProjectRole(new Long(1)), mockProject, collection);

        assertFalse(collection.hasAnyErrors());
    }

    public void testRemoveSelfFromProjectRoleReferencedTwice() throws Exception
    {
        removeActorFromAProjectRole("tester", false, getPermissionManager(true, false), projectRoleManager);
    }

    public void testRemoveSelfFromProjectRoleReferencedOnce() throws Exception
    {
        // make sure the
        SimpleErrorCollection errorCollection = removeActorFromAProjectRole("tester", true, getPermissionManager(true, false), new MockProjectRoleManagerWithOneUserReference());
        assertTrue(errorCollection.getErrorMessages().toString(), errorCollection.getErrorMessages().contains("You can not remove a user/group that will result in completely removing yourself from this role."));
    }

    public void testRemoveSomeoneNoPermission()
    {
        SimpleErrorCollection errorCollection = removeActorFromAProjectRole("tester", true, getPermissionManager(false, false), projectRoleManager);
        assertTrue(errorCollection.getErrorMessages().toString(), errorCollection.getErrorMessages().contains("You do not have permission to remove a user/group from this role."));
    }

    public void testRemoveSomeoneElseFromProjectRole() throws Exception
    {
        removeActorFromAProjectRole(FRED, false, getPermissionManager(true, false), projectRoleManager);
    }

    public void testRemoveAllFromProjectRoleAsGlobalAdmin() throws Exception
    {
        removeActorFromAProjectRole("tester", false, getPermissionManager(false, true), projectRoleManager);
    }

    /**
     * Will update the project role based off the passed in <code>projectRole</code> and checking
     * the <code>currentUser</code> has the correct permissions to perform the update operation.
     * The passed in <code>errorCollection</code> will contain any errors that are generated, such as permission violations.
     */
    public void testUpdateRole()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.updateProjectRole(null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("Can not update a project role with a null project role specified."));
        assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

    public void testUpdateRoleNameAlreadyExistsInAnotherRole() throws Exception
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        final MockControl mockProjectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager mockProjectRoleManager = (ProjectRoleManager) mockProjectRoleManagerControl.getMock();
        mockProjectRoleManager.getProjectRole("Test");
        mockProjectRoleManagerControl.setReturnValue(new ProjectRoleImpl(new Long(567), "Test", "blah"));
        mockProjectRoleManagerControl.replay();

        final MockControl mockPermissionManagerControl = MockControl.createStrictControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.ADMINISTER, (com.atlassian.crowd.embedded.api.User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManagerControl.replay();

        final MockControl mockJiraAuthenticationContextControl = MockControl.createStrictControl(JiraAuthenticationContext.class);
        final JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
        mockJiraAuthenticationContext.getI18nHelper();
        mockJiraAuthenticationContextControl.setReturnValue(new MockI18nBean());
        mockJiraAuthenticationContextControl.replay();

        DefaultProjectRoleService service = new DefaultProjectRoleService(mockProjectRoleManager, mockPermissionManager, mockJiraAuthenticationContext, null, null, null, null, null, null, null, null, null);

        service.updateProjectRole(null, new ProjectRoleImpl(new Long(123), "Test", "blah"), collection);

        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("A project role with name 'Test' already exists."));

        mockProjectRoleManagerControl.verify();
        mockPermissionManagerControl.verify();
        mockJiraAuthenticationContextControl.verify();
    }

    public void testUpdateRoleNameAlreadyExistsInThisRole() throws Exception
    {
        final ProjectRoleImpl passedInRole = new ProjectRoleImpl(new Long(123), "Test", "blah");
        SimpleErrorCollection collection = new SimpleErrorCollection();

        final MockControl mockProjectRoleManagerControl = MockControl.createStrictControl(ProjectRoleManager.class);
        final ProjectRoleManager mockProjectRoleManager = (ProjectRoleManager) mockProjectRoleManagerControl.getMock();
        mockProjectRoleManager.getProjectRole("Test");
        mockProjectRoleManagerControl.setReturnValue(new ProjectRoleImpl(new Long(123), "Test", "blah"));
        mockProjectRoleManager.updateRole(passedInRole);
        mockProjectRoleManagerControl.replay();

        final MockControl mockPermissionManagerControl = MockControl.createStrictControl(PermissionManager.class);
        final PermissionManager mockPermissionManager = (PermissionManager) mockPermissionManagerControl.getMock();
        mockPermissionManager.hasPermission(Permissions.ADMINISTER, (com.atlassian.crowd.embedded.api.User) null);
        mockPermissionManagerControl.setReturnValue(true);
        mockPermissionManagerControl.replay();

        DefaultProjectRoleService service = new DefaultProjectRoleService(mockProjectRoleManager, mockPermissionManager, null, null, null, null, null, null, null, null, null, null);

        service.updateProjectRole(null, passedInRole, collection);

        assertFalse(collection.hasAnyErrors());

        mockProjectRoleManagerControl.verify();
        mockPermissionManagerControl.verify();
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullSchemeFactory()
    {
        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, (PermissionSchemeManager) mockPSM.proxy(), null, null, null, null, null, null);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null SchemeFactory");
        }
        catch (NullPointerException yay)
        {
            assertTrue(yay.getMessage().indexOf(SchemeFactory.class.getName()) >= 0);
        }
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullPermissionSchemeManager()
    {
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, null, null, null, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null PermissionSchemeManager");
        }
        catch (NullPointerException yay)
        {
            assertTrue(yay.getMessage().indexOf(PermissionSchemeManager.class.getName()) >= 0);
        }
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullProject()
    {
        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, (PermissionSchemeManager) mockPSM.proxy(), null, null, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(null, projectRoleManager.getProjectRole(new Long(1)));
            fail("should have throw NPE due to null Project");
        }
        catch (NullPointerException yay)
        {
            assertTrue(yay.getMessage().indexOf(Project.class.getName()) >= 0);
        }
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionChucksNPEWithNullProjectRole()
    {
        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, (PermissionSchemeManager) mockPSM.proxy(), null, null, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);
        try
        {
            projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), null);
            fail("should have throw NPE due to null ProjectRole");
        }
        catch (NullPointerException yay)
        {
            assertTrue(yay.getMessage().indexOf(ProjectRole.class.getName()) >= 0);
        }
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionHasPermission()
    {
        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        mockPSM.expectAndReturn("getSchemes", P.ANY_ARGS, EasyList.build(new MockGenericValue("Scheme")));

        Collection schemeEntities = EasyList.build(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.PROJECT_ADMIN)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, scheme);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, (PermissionSchemeManager) mockPSM.proxy(), null, null, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);

        assertTrue(projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1))));
    }

    public void testDoesProjectRoleExistForAdministerProjectsPermissionNoPermission()
    {
        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        mockPSM.expectAndReturn("getSchemes", P.ANY_ARGS, EasyList.build(new MockGenericValue("Scheme")));

        Collection schemeEntities = EasyList.build(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.EDIT_ISSUE)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, scheme);
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(null, null, null, null, null, (PermissionSchemeManager) mockPSM.proxy(), null, null, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);

        assertFalse(projectRoleService.doesProjectRoleExistForAdministerProjectsPermission(new ProjectImpl(null), projectRoleManager.getProjectRole(new Long(1))));
    }

    /**
      * Will return the project role actors based off the passed in <code>projectRole</code> and <code>project</code> checking
      * the <code>currentUser</code> has the correct permissions to perform the delete operation.
      */
     public void testGetProjectRoleActors()
     {
         SimpleErrorCollection collection = new SimpleErrorCollection();
         Project mockProject = new ProjectImpl(new MockGenericValue("Project"));

         // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
         // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
         // also implemented to throw an IllegalArgumentException for the parameter name INVALID_PARAMETER, we count on
         // this to generate the "does not exist" validation.
         defaultProjectRoleServicePermFalseFalse.getProjectRoleActors(null,  projectRoleManager.getProjectRole(1L), mockProject, collection);

         assertTrue(collection.getErrorMessages().contains("The user does not have the JIRA Administrator permission, or is not running Enterprise and does not have the Project admin permission."));
     }

    public void testGetDefaultRoleActors()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, false), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null, null, null, null, null);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        projectRoleService.getDefaultRoleActors(null,  null, collection);

        assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
        assertTrue(collection.getErrorMessages().contains("Can not retrieve a role actor for a null project role."));
    }

    public void testAddDefaultRoleActors()
    {
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, null, null, null, null, null, null);
        SimpleErrorCollection collection = new SimpleErrorCollection();

        // The MockProjectRoleManager will return a ProjectRoleActors object with a MockRoleActor with parameter name
        // 'tester', we count on this so that we can generate the "already a member" validation. The MockRoleActor is
        // also implemented to throw an IllegalArgumentException for the parameter name INVALID_PARAMETER, we count on
        // this to generate the "does not exist" validation.
        projectRoleService.addDefaultActorsToProjectRole(null, EasyList.build(MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER, "tester"), projectRoleManager.getProjectRole(new Long(1)), MockProjectRoleManager.MockRoleActor.TYPE, collection);

        assertTrue(collection.getErrorMessages().contains("'tester' is already a member of the project role."));
        assertTrue(collection.getErrorMessages().contains("'"+MockProjectRoleManager.MockRoleActor.INVALID_PARAMETER+"' does not exist."));
    }

    public void testAddDefaultRoleActorsNullParams()
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();

        defaultProjectRoleServicePermFalseFalse.addDefaultActorsToProjectRole(null, null, null, null, collection);

        assertTrue(collection.hasAnyErrors());
        assertTrue(collection.getErrorMessages().contains("Can not update a null role actor."));
        assertTrue(collection.getErrorMessages().contains("Can not retrieve a role actor for a null project role."));
        assertTrue(collection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

    /**
     * NOTE: This is not tested because this exercises a subset of the validation called by testAddActorsToProjectRole
     */
    public void testRemoveDefaultRoleActors()
    {
    }

    public void testRemoveAllRoleActosByNameAndType()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.removeAllRoleActorsByNameAndType(null, null, null, errorCollection);

        assertTrue(errorCollection.getErrorMessages().contains("Can not delete role actors without a name specified."));
        assertTrue(errorCollection.getErrorMessages().contains("Can not delete role actors without a type specified."));
        assertTrue(errorCollection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

    public void testRemoveAllRoleActorsByProject()
    {

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.removeAllRoleActorsByProject(null, null, errorCollection);

        assertTrue(errorCollection.getErrorMessages().contains("Can not delete role actors without a project specified."));
        assertTrue(errorCollection.getErrorMessages().contains("This user does not have the JIRA Administrator permission to perform this operation."));
    }

    public void testGetAssociatedNotificationSchemes()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.getAssociatedNotificationSchemes(null, null, errorCollection);
        assertTrue(errorCollection.getErrorMessages().contains("The project role can not be null."));
    }

    public void testGetAssociatedPermissionSchemes()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        defaultProjectRoleServicePermFalseFalse.getAssociatedPermissionSchemes(null, null, errorCollection);
        assertTrue(errorCollection.getErrorMessages().contains("The project role can not be null."));
    }

    public void testGetAssociatedWorkflows() throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        User user = UtilsForTests.getTestUser("workflowuser");

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);

        WorkflowManager workflowManager = (WorkflowManager) mockWorkflowManager.proxy();

        //Collection actions = new HashSet();
        MockJiraWorkflow roleConditionWorkflow = new MockJiraWorkflow(workflowManager, PROJECTROLE_WORKFLOW_FILE);
        final String roleConditionWorkflowName = "The default JIRA workflow.";
        roleConditionWorkflow.setName(roleConditionWorkflowName);

        MockJiraWorkflow anotherRoleConditionWorkflow = new MockJiraWorkflow(workflowManager, PROJECTROLE_WORKFLOW_FILE);
        final String anotherWorkflowName = "another " + roleConditionWorkflowName;
        anotherRoleConditionWorkflow.setName(anotherWorkflowName);

        MockJiraWorkflow unassociatedWorkflow = new MockJiraWorkflow(workflowManager, "com/atlassian/jira/upgrade/tasks/upgradetask_build155/simpleworkflow-broken.xml");
        mockWorkflowManager.expectAndReturn("getWorkflows", EasyList.build(roleConditionWorkflow, unassociatedWorkflow, anotherRoleConditionWorkflow));

        ProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, getPermissionManager(true, true), jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, null, (WorkflowManager) mockWorkflowManager.proxy(), null, null, null, null, null);

        ProjectRole developerRole = new ProjectRoleImpl(new Long(10001), "Developers", "A role that represents developers in a project");

        MultiMap workflows = projectRoleService.getAssociatedWorkflows(user, developerRole, errorCollection);

        mockWorkflowManager.verify();

        boolean foundRoleConditionWorkflow = false;
        boolean foundAnotherWorkflow = false;
        assertEquals(2, workflows.keySet().size());
        for (Iterator iterator = workflows.entrySet().iterator(); iterator.hasNext();)
        {
            // check the workflows are right
            Map.Entry e =  (Map.Entry) iterator.next();
            final String name = ((JiraWorkflow) e.getKey()).getName();
            if (name.equals(roleConditionWorkflowName))
            {
                foundRoleConditionWorkflow = true;
            } else if (name.equals(anotherWorkflowName))
            {
                foundAnotherWorkflow = true;
            }
            Collection actions = (Collection) e.getValue();
            for (Iterator actionsIter = actions.iterator(); actionsIter.hasNext();)
            {
                // check the action with the condition is the correct one
                ActionDescriptor actionDescriptor = (ActionDescriptor) actionsIter.next();
                assertTrue(actionDescriptor.getName().equals("Start Progress"));
            }
        }
        assertTrue(foundRoleConditionWorkflow);
        assertTrue(foundAnotherWorkflow);
    }

    private SimpleErrorCollection removeActorFromAProjectRole(String userToRemove, boolean errorsExpected, PermissionManager permissionManager, ProjectRoleManager projectRoleManager)
    {
        User user = getUser("tester", "tester@test.com");
        Project mockProject = new ProjectImpl(new MockGenericValue("Project", EasyMap.build("id", 1L)));

        Mock mockPSM = new Mock(PermissionSchemeManager.class);
        // Return one GenericValue that pretends to be a scheme
        mockPSM.expectAndReturn("getSchemes", P.ANY_ARGS, EasyList.build(new MockGenericValue("Scheme")));

        Collection schemeEntities = EasyList.build(new SchemeEntity(ProjectRoleSecurityAndNotificationType.PROJECT_ROLE, "1", new Long(Permissions.PROJECT_ADMIN)));
        Scheme scheme = new Scheme(null, "PermissionScheme", "Default Permission Scheme", schemeEntities);

        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, scheme);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(mockProject.getGenericValue());
        DefaultProjectRoleService projectRoleService = new DefaultProjectRoleService(projectRoleManager, permissionManager, jiraAuthenticationContext, new MockProjectRoleManager.MockRoleActorFactory(), null, (PermissionSchemeManager) mockPSM.proxy(), null, mockProjectManager, null, (SchemeFactory) mockSchemeFactory.proxy(), null, null);

        // tester is automagically included in the ProjectRoleManager mock, that's why it works
        projectRoleService.removeActorsFromProjectRole(user, EasyList.build(userToRemove), projectRoleManager.getProjectRole(1L), mockProject, "mock type", errorCollection);

        assertEquals(errorCollection.getErrorMessages().toString(), errorsExpected, errorCollection.hasAnyErrors());
        return errorCollection;
    }

    private User getUser(String username, String email)
    {
        User user = null;
        try
        {
            user = UserUtils.createUser(username, email);
        }
        catch (Exception e)
        {
            try
            {
                user = UserUtils.getUser("tester");
            }
            catch (EntityNotFoundException e1)
            {
                // don't do anything...
            }
        }
        return user;
    }

    private class MockProjectRoleManagerWithOneUserReference extends MockProjectRoleManager
    {

        public ProjectRoleActors getProjectRoleActors(ProjectRole projectRole, Project project)
        {
            if(project == null)
            {
                throw new IllegalArgumentException("Mock bad argument");
            }
            Set actors = new HashSet();
            User fred = null;
            User tester = null;
            try
            {
                tester = UserUtils.createUser(TESTER, "tester@test.com");
            }
            catch (Exception e)
            {
                try
                {
                    tester = UserUtils.getUser(TESTER);
                }
                catch (EntityNotFoundException e1)
                {
                    // don't do anything...
                }
            }
            try
            {
                fred = UserUtils.createUser(FRED, "fred@test.com");
            }
            catch (Exception e)
            {
                try
                {
                    fred = UserUtils.getUser(FRED);
                }
                catch (EntityNotFoundException e1)
                {
                    // don't do anything...
                }
            }

            try
            {
                actors.add(new MockRoleActor(new Long(1), projectRole.getId(), project.getId(), new HashSet(EasyList.build(tester)), MockRoleActor.TYPE, TESTER));
                actors.add(new MockRoleActor(new Long(2), projectRole.getId(), project.getId(), new HashSet(EasyList.build(fred)), MockRoleActor.TYPE, FRED));
            }
            catch (RoleActorDoesNotExistException e)
            {
                throw new RuntimeException(e);
            }
            return new ProjectRoleActorsImpl(project.getId(), projectRole.getId(), actors);
        }
    }
}
