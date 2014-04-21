/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockAbstractPermissionsManager;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.ProjectLead;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

//This class has been updated to reflect the fact that permisisons are now done on a scheme basis rather than a project basis
public class TestAbstractPermissionManager extends AbstractUsersTestCase
{
    private MockAbstractPermissionsManager apm;
    protected User bob;
    protected User joe;
    protected User paul;
    protected GenericValue project;
    protected GenericValue project2;
    protected GenericValue scheme;
    protected GenericValue issue;
    protected CrowdService crowdService;

    public TestAbstractPermissionManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        apm = new MockAbstractPermissionsManager();

        crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);

        bob = new MockUser("bob");
        joe = new MockUser("joe");
        paul = new MockUser("paul");
        crowdService.addUser(bob, "");
        crowdService.addUser(joe, "");
        crowdService.addUser(paul, "");

        scheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("id", new Long(10), "name", "Test Scheme", "description", "test"));

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "lead", "paul"));

        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(3)));

        //Only add one project to the permission scheme
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project, scheme);

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1", "project", new Long(2), "reporter", "bob", "assignee", "bob"));
    }

    /**
     * Cannot call hasPermission (project) for a global permission
     */
    public void testHasPermissionForProjectFailGlobalPermission()
    {
        AbstractPermissionManager permissionManager = new AbstractPermissionManager() {
            protected boolean isGlobalPermission(int permissionId)
            {
                return true;
            }
        };

        try
        {
            permissionManager.hasPermission(0, (Project) null, null, false);
            fail("Should throw IllegalArgument exception due to global permission id");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "PermissionType passed to this function must NOT be a global permission, 0 is global");
        }
    }

    /**
     * Cannot check permissions for null project
     */
    public void testHasPermissionForProjectFailNullProject()
    {
        AbstractPermissionManager permissionManager = new AbstractPermissionManager() {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }
        };

        try
        {
            permissionManager.hasPermission(0, (Project) null, null, false);
            fail("Should throw IllegalArgument exception due to null Project");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "The Project argument and its backing generic value must not be null");
        }
    }

    /**
     * A project with a null backing GV is a serious state problem for the domain object - also it's required by
     * underlying methods 
     */
    public void testHasPermissionForProjectFailNullProjectBackingGV()
    {
        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }
        };

        Project project = new MockProject()
        {
            public GenericValue getGenericValue()
            {
                return null;
            }
        };

        try
        {
            permissionManager.hasPermission(0, project, null, false);
            fail("Should throw IllegalArgument exception due to null backing GV for Project");
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals(e.getMessage(), "The Project argument and its backing generic value must not be null");
        }
    }

    /**
     * If issue object's GV is null, need to defer to the project object for permission check.
     */
    public void testHasPermissionForIssueCreation()
    {
        final int testPermission = 0;
        final User testUser = null;
        final MockIssue testIssue = new MockIssue()
        {
            private GenericValue genericValue;
            
            public GenericValue getGenericValue()
            {
                return genericValue;
            }

            public void setGenericValue(final GenericValue genericValue)
            {
                this.genericValue = genericValue;
            }
        };
        testIssue.setProject(project);
        final Project testProject = testIssue.getProjectObject();


        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            public boolean hasPermission(final int permissionsId, final GenericValue entity, final com.atlassian.crowd.embedded.api.User u)
            {
                //verify arguments are called through correctly
                assertEquals(testPermission, permissionsId);
                assertEquals(issue, entity);
                assertEquals(testUser, u);
                return true;
            }

            public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
            {
                //verify arguments are called through correctly
                assertEquals(testPermission, permissionsId);
                assertEquals(testProject, project);
                assertEquals(testUser, user);
                assertEquals(true, issueCreation);
                return true;
            }
        };

        assertTrue(permissionManager.hasPermission(testPermission, testIssue, testUser));
        testIssue.setGenericValue(issue);
        assertTrue(permissionManager.hasPermission(testPermission, testIssue, testUser));
    }

    /**
     * If the issueCreation boolean is not specified for hasPermission, it is called through with false from an
     * overloaded method 
     */
    public void testHasPermissionForProjectIssueCreationNotSpecifiedDefaultsToFalse()
    {
        final int testPermission = 0;
        final Project testProject = new MockProject();
        final User testUser = null;

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
            {
                //verify arguments are called through correctly
                assertEquals(testPermission, permissionsId);
                assertEquals(testProject, project);
                assertEquals(testUser, user);
                assertEquals(false, issueCreation);
                return true;
            }
        };

        assertTrue(permissionManager.hasPermission(testPermission, testProject, testUser));
    }

    /**
     * Check the underlying method hasProjectPermission is called with the correct args
     */
    public void testHasPermissionForProjectHappyPath()
    {
        final int testPermission = 0;
        final User testUser = null;
        final boolean testIssueCreation = true;
        final GenericValue projectGV = new MockGenericValue("project");
        final Project testProject = new MockProject()
        {
            public GenericValue getGenericValue()
            {
                return projectGV;
            }
        };

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            protected boolean hasProjectPermission(Long permissionTypeLong, GenericValue entity, com.atlassian.crowd.embedded.api.User u, boolean issueCreation)
            {
                //validate args & make sure "false / no permission" is propogated
                assertEquals(new Long(testPermission), permissionTypeLong);
                assertEquals(projectGV, entity);
                assertEquals(testUser, u);
                assertEquals(testIssueCreation, issueCreation);
                return false;
            }
        };

        assertFalse(permissionManager.hasPermission(testPermission, testProject, testUser, testIssueCreation));
    }

    public void testProtectedMethodHasProjectPermissionAnonymous()
    {
        final Long testPermission = new Long(0);
        final GenericValue testEntity = new MockGenericValue("entity");
        boolean testIssueCreation = false;

        final Mock mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockPermissionSchemeManager.expectAndReturn
                ("hasSchemeAuthority", P.args(new IsEqual(testPermission), new IsEqual(testEntity)), Boolean.TRUE);

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            /**
             * @return mocked out permission manager
             */
            protected PermissionSchemeManager getPermissionSchemeManager()
            {
                return (PermissionSchemeManager) mockPermissionSchemeManager.proxy();
            }
        };

        assertTrue(permissionManager.hasProjectPermission(testPermission, testEntity, null, testIssueCreation));
        mockPermissionSchemeManager.verify();
    }

    public void testProtectedMethodHasProjectPermissionSpecifiedUser()
    {
        final Long testPermission = new Long(0);
        final GenericValue testEntity = new MockGenericValue("entity");
        final User testUser = bob;
        boolean testIssueCreation = false;

        //assert permissionSchemeManager denies permission / return false is propogated
        final Mock mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockPermissionSchemeManager.expectAndReturn("hasSchemeAuthority",
                new Constraint[]{
                        new IsEqual(testPermission),
                        new IsEqual(testEntity),
                        new IsEqual(testUser),
                        new IsEqual(Boolean.valueOf(testIssueCreation)) }, 
                Boolean.FALSE);

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            /**
             * @return mocked out permission manager
             */
            protected PermissionSchemeManager getPermissionSchemeManager()
            {
                return (PermissionSchemeManager) mockPermissionSchemeManager.proxy();
            }
        };

        assertFalse(permissionManager.hasProjectPermission(testPermission, testEntity, testUser, testIssueCreation));
        mockPermissionSchemeManager.verify();
    }

    public void testUsersWithPermission() throws Exception
    {
        Collection users;

        Group group1 = new MockGroup("group1");
        crowdService.addGroup(group1);
        crowdService.addUserToGroup(bob, group1);

        PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        SchemeEntity schemeEntity = new SchemeEntity(SingleUser.DESC, "joe", new Long(Permissions.CREATE_ISSUE));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);

        PermissionContext ctx = ComponentAccessor.getPermissionContextFactory().getPermissionContext(project);
        users = permissionSchemeManager.getUsers(new Long(Permissions.CREATE_ISSUE), ctx);
        assertEquals(1, users.size());
        assertTrue(users.contains(joe));

        schemeEntity = new SchemeEntity(GroupDropdown.DESC, "group1", new Long(10));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        users = permissionSchemeManager.getUsers(new Long(10), ctx);
        assertEquals(1, users.size());
        assertFalse(users.contains(joe));
        assertTrue(users.contains(bob));

        schemeEntity = new SchemeEntity(ProjectLead.DESC, new Long(10));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        users = permissionSchemeManager.getUsers(new Long(10), ctx);
        assertEquals(2, users.size());
        assertFalse(users.contains(joe));
        assertTrue(users.contains(bob));
        assertTrue(users.contains(paul));
    }

    public void testHasUserGroupPermission()
            throws CreateException, DuplicateEntityException, ImmutableException, RemoveException, GenericEntityException, OperationNotPermittedException, InvalidGroupException
    {
        UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(1)));

        Group group1 = new MockGroup("group1");
        crowdService.addGroup(group1);
        crowdService.addUserToGroup(bob, group1);
        Group group2 = new MockGroup("group2");
        crowdService.addGroup(group2);
        crowdService.addUserToGroup(joe, group2);

        //Anonymous Permission Per Scheme - Anyone
        apm.addPermission(2, scheme, null, GroupDropdown.DESC);
        hasUserGroupAndUserGroup(2, project, bob);
        hasUserGroupAndUserGroup(2, project, joe);
        hasUserGroupAndUserGroup(2, project, null);
        hasUserGroupAndUserGroup(2, project, paul);

        ManagerFactory.getPermissionSchemeManager().removeEntities(scheme, new Long(2));
        hasntUserGroupAndUserGroup(2, project, bob);
        hasntUserGroupAndUserGroup(2, project, joe);
        hasntUserGroupAndUserGroup(2, project, null);
        hasntUserGroupAndUserGroup(2, project, paul);

        //These projects are not assocaiated with scheme so should not have permission
        hasntUserGroupAndUserGroup(2, project2, bob);
        hasntUserGroupAndUserGroup(2, project2, joe);

        // specific permission - only these groups and schemes should have permission
        apm.addPermission(5, scheme, "group1", GroupDropdown.DESC);
        hasUserGroupAndUserGroup(5, project, bob);
        hasntUserGroupAndUserGroup(5, project2, bob);
        hasntUserGroupAndUserGroup(5, project, joe);
    }

    public void testGetProjectsNoProjectsExist() throws Exception
    {
        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            protected ProjectManager getProjectManager()
            {
                Mock mockProjectManager = new Mock(ProjectManager.class);
                mockProjectManager.expectAndReturn("getProjects", Collections.EMPTY_SET);
                return (ProjectManager) mockProjectManager.proxy();
            }
        };

        assertFalse(permissionManager.hasProjects(0, bob));
    }

    public void testGetProjectsUserHasNoVisibleProjects() throws Exception
    {
        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            protected ProjectManager getProjectManager()
            {
                Mock mockProjectManager = new Mock(ProjectManager.class);
                mockProjectManager.expectAndReturn("getProjects", EasyList.build(new MockGenericValue("Project"), new MockGenericValue("Project")));
                return (ProjectManager) mockProjectManager.proxy();
            }

            public boolean hasPermission(int permissionsId, GenericValue projectGV, com.atlassian.crowd.embedded.api.User user)
            {
                return false;
            }
        };

        assertFalse(permissionManager.hasProjects(0, bob));
    }

    public void testGetProjectObjectsNoProjectsExist() throws Exception
    {
        final MockControl mockProjectManagerControl = MockControl.createControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjects();
        mockProjectManagerControl.setReturnValue(Collections.EMPTY_LIST);
        mockProjectManagerControl.replay();

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            @Override
            public boolean hasPermission(final int permissionsId, final Project project, final com.atlassian.crowd.embedded.api.User user)
            {
                return true;
            }

            protected ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };

        assertTrue(permissionManager.getProjectObjects(Permissions.BROWSE, bob).isEmpty());

        mockProjectManagerControl.verify();
    }

    public void testGetProjectObjectsProjectsExist() throws Exception
    {
        MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("name", "proj", "key", "key", "id", 1000L));
        final Project project = new MockProject(mockProjectGV);
        final List<Project> projects = Collections.singletonList(project);

        final MockControl mockProjectManagerControl = MockControl.createControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjects();
        mockProjectManagerControl.setReturnValue(projects);
        mockProjectManagerControl.replay();

        AbstractPermissionManager permissionManager = new AbstractPermissionManager()
        {
            protected boolean isGlobalPermission(int permissionId)
            {
                return false;
            }

            @Override
            public boolean hasPermission(final int permissionsId, final Project project, final com.atlassian.crowd.embedded.api.User user)
            {
                return true;
            }

            protected ProjectManager getProjectManager()
            {
                return mockProjectManager;
            }
        };

        assertEquals(projects, permissionManager.getProjectObjects(Permissions.BROWSE, bob));

        mockProjectManagerControl.verify();
    }

    protected void hasUserGroupAndUserGroup(int permtype, GenericValue project, User user)
    {
        userGroupAndUserGroup(permtype, project, user, true);
    }

    protected void hasntUserGroupAndUserGroup(int permtype, GenericValue project, User user)
    {
        userGroupAndUserGroup(permtype, project, user, false);
    }

    protected void userGroupAndUserGroup(int permtype, GenericValue project, User user, boolean assertTrue)
    {
        assertEquals(assertTrue, apm.hasPermission(permtype, project, user));
    }

    protected void userAndUserGroup(int permtype, GenericValue project, User user, boolean assertTrue)
    {
        assertEquals(assertTrue, apm.hasPermission(permtype, project, user));
    }
}
