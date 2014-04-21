package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestDefaultUserProjectHistoryManager extends MockControllerTestCase
{

    private UserHistoryManager historyManager;
    private PermissionManager permissionManager;
    private ProjectManager projectManager;

    private UserProjectHistoryManager projectHistoryManager;
    private User user;

    @Before
    public void setUp() throws Exception
    {

        MockProviderAccessor mpa = new MockProviderAccessor();
        user = new MockUser("admin");
        historyManager = mockController.getMock(UserHistoryManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        projectManager = mockController.getMock(ProjectManager.class);


        projectHistoryManager = new DefaultUserProjectHistoryManager(historyManager, projectManager, permissionManager);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        projectManager = null;
        permissionManager = null;
        projectHistoryManager = null;
    }

    @Test
    public void testAddProjectNullProject()
    {
        mockController.replay();

        try
        {
            projectHistoryManager.addProjectToHistory(user, null);
            fail("project can not be bull");
        }
        catch (IllegalArgumentException e)
        {
            // pass
        }

        mockController.verify();
    }

    @Test
    public void testAddProjectNullUser()
    {
        final Project project = mockController.getMock(Project.class);
        project.getId();
        mockController.setReturnValue(123L);

        historyManager.addItemToHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null, "123");

        mockController.replay();

        projectHistoryManager.addProjectToHistory(null, project);

        mockController.verify();
    }

    @Test
    public void testAddProject()
    {
        final Project project = mockController.getMock(Project.class);
        project.getId();
        mockController.setReturnValue(123L);

        historyManager.addItemToHistory(UserHistoryItem.PROJECT, user, "123");

        mockController.replay();

        projectHistoryManager.addProjectToHistory(user, project);

        mockController.verify();
    }

    @Test
    public void testHasProjectHistoryNullUserNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, null));

        mockController.verify();

    }


    @Test
    public void testHasProjectHistoryNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));

        mockController.verify();

    }

    @Test
    public void testHasProjectHistoryEmptyHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));

        mockController.verify();

    }

    @Test
    public void testHasProjectHistoryNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));

        mockController.verify();

    }

    @Test
    public void testHasProjectHistoryNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        final Project project = mockController.getMock(Project.class);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, user);
        mockController.setReturnValue(false);

        mockController.replay();

        assertFalse(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));

        mockController.verify();

    }

    @Test
    public void testHasProjectHistory()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);


        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, user);
        mockController.setReturnValue(false);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project2, user);
        mockController.setReturnValue(true);

        mockController.replay();

        assertTrue(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, user));

        mockController.verify();

    }

    @Test
    public void testHasProjectHistoryNullUserWithPermissions()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(list);


        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(false);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project2, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        assertTrue(projectHistoryManager.hasProjectHistory(Permissions.BROWSE, (com.atlassian.crowd.embedded.api.User) null));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProjectNullUserNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(null);

        mockController.replay();

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, null));

        mockController.verify();

    }


    @Test
    public void testGetCurrentProjectNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProjectEmptyHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProjectNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        mockController.replay();

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProjectNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        final Project project = mockController.getMock(Project.class);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project, user);
        mockController.setReturnValue(false);

        mockController.replay();

        assertNull(projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProject()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);


        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project, user);
        mockController.setReturnValue(false);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project2, user);
        mockController.setReturnValue(true);

        mockController.replay();

        assertEquals(project2, projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, user));

        mockController.verify();

    }

    @Test
    public void testGetCurrentProjectNullUserWithPermissions()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(list);


        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(false);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CLOSE_ISSUE, project2, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(true);

        mockController.replay();

        assertEquals(project2, projectHistoryManager.getCurrentProject(Permissions.CLOSE_ISSUE, (com.atlassian.crowd.embedded.api.User) null));

        mockController.verify();

    }

    @Test
    public void testGetProjectHistoryWithChecksNullUserNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, (com.atlassian.crowd.embedded.api.User) null).isEmpty());

        mockController.verify();

    }


    @Test
    public void testGetProjectHistoryWithChecksNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());

        mockController.verify();

    }

    @Test
    public void testGetProjectHistoryWithChecksEmptyHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());

        mockController.verify();

    }

    @Test
    public void testGetProjectHistoryWithChecksNullProject()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());

        mockController.verify();

    }

    @Test
    public void testGetProjectHistoryWithChecksNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.PROJECT, "123");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        final Project project = mockController.getMock(Project.class);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        mockController.setReturnValue(false);

        mockController.replay();

        assertTrue(projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user).isEmpty());

        mockController.verify();

    }

    @Test
    public void testGetProjectHistoryWithChecks()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);


        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
        mockController.setReturnValue(false);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.CREATE_ISSUE, project2, user);
        mockController.setReturnValue(true);

        final Project project3 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1236L);
        mockController.setReturnValue(project3);

        permissionManager.hasPermission(Permissions.CREATE_ISSUE, project3, user);
        mockController.setReturnValue(true);

        mockController.replay();

        List<Project> expectedList = CollectionBuilder.newBuilder(project2, project3).asList();

        List<Project> returnedList = projectHistoryManager.getProjectHistoryWithPermissionChecks(Permissions.CREATE_ISSUE, user);

        assertEquals(expectedList, returnedList);

        mockController.verify();

    }


    @Test
    public void testGetProjectHistoryWithOutChecks()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        mockController.replay();


        List<UserHistoryItem> returnedList = projectHistoryManager.getProjectHistoryWithoutPermissionChecks(user);

        assertEquals(list, returnedList);

        mockController.verify();
    }

    @Test
    public void testGetProjectHistoryWithOutChecksNullUser()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(list);

        mockController.replay();


        List<UserHistoryItem> returnedList = projectHistoryManager.getProjectHistoryWithoutPermissionChecks(null);

        assertEquals(list, returnedList);

        mockController.verify();
    }


    @Test
    public void testGetProjectHistoryWithOutChecksNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(null);

        mockController.replay();


        assertNull(projectHistoryManager.getProjectHistoryWithoutPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testGetProjectHistoryWithPermissionChecksUsingProjectAction()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.PROJECT, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.PROJECT, "1236");

        List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(list);

        projectManager.getProjectObj(123L);
        mockController.setReturnValue(null);

        final Project project1 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1234L);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);

        permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project1, user);
        mockController.setReturnValue(true);

        final Project project2 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1235L);
        mockController.setReturnValue(project2);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        final Project project3 = mockController.getMock(Project.class);
        projectManager.getProjectObj(1236L);
        mockController.setReturnValue(project3);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);

        permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project1, user);
        mockController.setReturnValue(false);

        mockController.replay();

        List<Project> expectedList = CollectionBuilder.newBuilder(project1, project2).asList();

        List<Project> returnedList = projectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.EDIT_PROJECT_CONFIG, user);

        assertEquals(expectedList, returnedList);

        mockController.verify();
    }

}
