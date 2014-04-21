package com.atlassian.jira.bc.project;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultProjectService extends MockControllerTestCase
{
    private I18nHelper.BeanFactory i18nFactory = new NoopI18nFactory();

    @Mock
    private PermissionManager permissionManager;

    @Before
    public void setup()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRetrieveProjectByIdProjectDoesntExist()
    {
        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        when(projectManager.getProjectObj(1L)).thenReturn(null);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, i18nFactory, null);


        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(null, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.id", 1L)));
    }

    @Test
    public void testRetrieveProjectByIdNoBrowsePermission()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(mockProject);
        final User user = new MockUser("admin");
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).thenReturn(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.id", 1L)));

        mockController.verify();
    }

    @Test
    public void testRetrieveProjectById()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(mockProject);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).thenReturn(true);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, 1L);
        assertNotNull(projectResult);
        assertEquals(mockProject, projectResult.getProject());
        assertFalse(projectResult.getErrorCollection().hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testRetrieveProjectByKeyAndActionProjectDoesntExist()
    {
        String projectKey = "projectKey";
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        expect(projectManager.getProjectObjByKey(projectKey)).andReturn(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, i18nFactory, null);


        final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(null, projectKey, ProjectAction.VIEW_ISSUES);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", projectKey),
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testRetrieveProjectByKeyAndActionPermissions()
    {
        final Project project1 = new MockProject(1818L, "ONE");
        final Project project2 = new MockProject(1819L, "TW0");
        final MockUser expectedUser = new MockUser("mockUser");
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        expect(projectManager.getProjectObjByKey(project1.getKey())).andReturn(project1);
        expect(projectManager.getProjectObjByKey(project2.getKey())).andReturn(project2);

        mockController.replay();

        // user can BROWSE but not PROJECT admin for project1
        when(permissionManager.hasPermission(Permissions.BROWSE, project1, expectedUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project1, expectedUser)).thenReturn(false);

        // anon can BROWSE project2
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, null)).thenReturn(true);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null, null,
                null, null, null, null, null, null, null, null, null, null, i18nFactory, null);


        ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(expectedUser, project1.getKey(), ProjectAction.EDIT_PROJECT_CONFIG);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals(NoopI18nHelper.makeTranslation("admin.errors.project.no.config.permission"),
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        projectResult = projectService.getProjectByKeyForAction(null, project2.getKey(), ProjectAction.VIEW_PROJECT);
        assertNotNull(projectResult);
        assertEquals(project2, projectResult.getProject());
        assertTrue(projectResult.isValid());

        mockController.verify();
    }

    @Test
    public void testRetrieveProjectKeyId()
    {
        final User expectedUser = new MockUser("admin");
        final String expectedKey = "KEY";
        final ProjectService.GetProjectResult expectedResult =
                new ProjectService.GetProjectResult(new SimpleErrorCollection());

        final DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            public GetProjectResult getProjectByKeyForAction(User user, String key, ProjectAction action)
            {
                assertEquals(expectedUser, user);
                assertEquals(expectedKey, key);
                assertSame(ProjectAction.VIEW_ISSUES, action);

                return expectedResult;
            }
        };

        ProjectService.GetProjectResult actualResult = projectService.getProjectByKey(expectedUser, expectedKey);
        assertSame(expectedResult, actualResult);
    }

    @Test
    public void testRetrieveProjectsForUserWithAction() throws Exception
    {
        final Project mockProject1 = new MockProject(11781L, "ABC");
        final Project mockProject2 = new MockProject(171718L, "EX");
        final List<Project> projects = Arrays.asList(mockProject1, mockProject2);
        final User mockUser = new MockUser("admin");
        final List<Project> checkedProjects = Lists.newArrayList();

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        expect(projectManager.getProjectObjects()).andReturn(projects);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            boolean checkActionPermission(User user, Project project, ProjectAction action)
            {
                checkedProjects.add(project);
                assertEquals(mockUser, user);
                assertEquals(ProjectAction.EDIT_PROJECT_CONFIG, action);
                return project.equals(mockProject1);
            }
        };

        final List<Project> expectedList = Lists.newArrayList(mockProject1);
        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjectsForAction(mockUser, ProjectAction.EDIT_PROJECT_CONFIG);
        assertTrue(outcome.isValid());
        assertEquals(expectedList, outcome.getReturnedValue());
        assertEquals(projects, checkedProjects);

        mockController.verify();
    }

    @Test
    public void testRetrieveProjectsForUser() throws Exception
    {
        final Project mockProject1 = createProjectObj("ABC");
        final Project mockProject2 = createProjectObj("EX");
        final List<Project> projectArrayList = Lists.newArrayList(mockProject1, mockProject2);

        final User mockUser = new MockUser("admin");

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            public ServiceOutcome<List<Project>> getAllProjectsForAction(User user, ProjectAction action)
            {
                assertEquals(mockUser, user);
                assertEquals(ProjectAction.VIEW_ISSUES, action);
                return ServiceOutcomeImpl.ok(projectArrayList);
            }
        };

        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjects(mockUser);
        assertEquals(projectArrayList, outcome.getReturnedValue());
    }

    public static Project createProjectObj(String projectKey)
    {
        return new MockProject(18181L, projectKey);
    }

    @Test
    public void testValidateCreateProjectNoPermission()
    {
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null, null, null,
                null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectNoPermissionToView()
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project",
                EasyMap.build("id", 1000L, "key", "HSP", "name", "homosapien"));
        final Project mockProject = new ProjectImpl(mockProjectGV);

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey("HSP");
        mockController.setReturnValue(mockProject);

        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.BROWSE, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).thenReturn(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null, null, null,
                null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "HSP", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", "HSP")));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectNoPermissionToEdit()
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project",
                EasyMap.build("id", 1000L, "key", "HSP", "name", "homosapien"));
        final Project mockProject = new ProjectImpl(mockProjectGV);

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey("HSP");
        mockController.setReturnValue(mockProject);

        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockProject, user)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user)).thenReturn(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null, null, null,
                null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "HSP", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.no.config.permission")));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectNullValues()
    {
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey(null);
        mockController.setReturnValue(null);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING);
        mockController.setReturnValue("You must specify a valid project key.");

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            boolean isProjectKeyValid(final String key)
            {
                return false;
            }

            @Override
            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            @Override
            boolean checkUserExists(final String user)
            {
                return false;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                null, null, null, null, null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("You must specify a valid project name.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("You must specify a valid project key.", projectResult.getErrorCollection().getErrors().get("projectKey"));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectNullValues()
    {
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");


        projectManager.getProjectObjByKey(null);
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return false;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return false;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                null, null, null, null, null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", (Object) null)));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectAlreadyExists()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("A project with that name already exists.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("A project with that project key already exists.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));


        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectNotExists()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.not.found.for.key", "KEY")));
        mockController.verify();
    }

    @Test
    public void testValidateProjectInvalidKey()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING);
        mockController.setReturnValue("projectKeyWarning");
        projectManager.getProjectObjByKey("invalidKey");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            boolean isProjectKeyValid(final String key)
            {
                return false;
            }

            @Override
            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("projectKeyWarning", projectResult.getErrorCollection().getErrors().get("projectKey"));

        mockController.verify();
    }

    @Test
    public void testValidateProjectReservedKeyword()
    {
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("invalidKey");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return true;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("This keyword is invalid as it is a reserved word on this operating system.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectLeadNotexists()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return false;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("The user you have specified as project lead does not exist.",
                projectResult.getErrorCollection().getErrors().get("projectLead"));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectLeadNotExist()
    {
        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return false;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "KEY", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectLead"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.not.a.valid.user")));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectLongNameUrlAndKey()
    {
        String longProjectKey = StringUtils.repeat("B", 11);

        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByKey(longProjectKey);
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                StringUtils.repeat("A", 81), longProjectKey, "description", "admin", StringUtils.repeat("C", 256), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(3, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL must not exceed 255 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name must not exceed 80 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("The project key must not exceed 10 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectInvalidUrl()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "KEY", "description", "admin", "invalidUrl", null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectInvalidUrl()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "KEY", "description", "admin", "invalidUrl", null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectUrl"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.url.specified.is.not.valid")));

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectLongNameAndUrl()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");
        final String projectKey = "KEY";
        final Project mockProject = new ProjectImpl(null)
        {
            @Override
            public String getKey()
            {
                return projectKey;
            }
        };

        expect(projectManager.getProjectObjByKey(projectKey)).andReturn(mockProject).times(2);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            @Override
            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                StringUtils.repeat("N", 151), projectKey, "description", "admin", StringUtils.repeat("U", 256), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(2, projectResult.getErrorCollection().getErrors().size());
        assertThat(projectResult.getErrorCollection().getErrors().get("projectUrl"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.url.too.long")));
        assertThat(projectResult.getErrorCollection().getErrors().get("projectName"), equalTo(NoopI18nHelper.makeTranslation("admin.errors.project.name.too.long", ProjectService.MAX_NAME_LENGTH)));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectAssigneeType()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }

            @Override
            protected JiraServiceContext getServiceContext(final User user, final ErrorCollection errorCollection)
            {
                return new MockJiraServiceContext(user, errorCollection);
            }            
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "KEY", "description", "admin", null, new Long(-1));

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals("Invalid default Assignee.", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectAssigneeType()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            boolean isProjectKeyValid(final String key)
            {
                return true;
            }

            boolean isReservedKeyword(final String key)
            {
                return false;
            }

            boolean checkUserExists(final String user)
            {
                return true;
            }
        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "KEY", "description", "admin", null, new Long(-1));

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.invalid.default.assignee")));

        mockController.verify();
    }

    @Test
    public void testValidateSchemesNoPermission()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectSchemesValidationResult result = projectService
                .validateUpdateProjectSchemes(user, 1L, 1L, 1L);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertThat(result.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));

        mockController.verify();
    }


    @Test
    public void testValidateSchemesNullSchemes() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        null, null, null);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
    }

    @Test
    public void testValidateSchemesMinusOne() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final Long schemeId = -1L;
        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
    }

    @Test
    public void testValidateSchemesNotExistEnterprise() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final PermissionSchemeManager permissionSchemeManager = mockController.getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notitNotificationSchemeManager = mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = mockController
                .getMock(IssueSecuritySchemeManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final Long schemeId = 1L;
        permissionSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(null);
        notitNotificationSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(null);
        issueSecuritySchemeManager.getScheme(schemeId);
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager,
                permissionSchemeManager, notitNotificationSchemeManager, issueSecuritySchemeManager, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());

        ErrorCollection errors = projectResult.getErrorCollection();
        assertTrue(errors.hasAnyErrors());
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.permission.scheme.not.retrieved")));
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.notification.scheme.not.retrieved")));
        assertThat(errors.getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.validation.issuesecurity.scheme.not.retrieved")));

        mockController.verify();
    }

    @Test
    public void testValidateSchemesExistEnterprise() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final PermissionSchemeManager permissionSchemeManager = mockController.getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notitNotificationSchemeManager = mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = mockController
                .getMock(IssueSecuritySchemeManager.class);
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final Long schemeId = 1L;
        MockGenericValue permissionScheme = new MockGenericValue("permissionScheme", new HashMap());
        MockGenericValue notificationScheme = new MockGenericValue("notificationScheme", new HashMap());
        MockGenericValue issueSecurityScheme = new MockGenericValue("issueSecurityScheme", new HashMap());

        permissionSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(permissionScheme);
        notitNotificationSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(notificationScheme);
        issueSecuritySchemeManager.getScheme(schemeId);
        mockController.setReturnValue(issueSecurityScheme);


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager,
                permissionSchemeManager, notitNotificationSchemeManager, issueSecuritySchemeManager, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService.validateUpdateProjectSchemes(user, schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
    }

    @Test
    public void testCreateProjectNullResult() throws Exception
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        try
        {
            projectService.createProject(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testUpdateProjectNullResult() throws Exception
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        try
        {
            projectService.updateProject(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testCreateProjectErrorResult() throws Exception
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(errorCollection);

        try
        {
            projectService.createProject(result);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
    }

    @Test
    public void testUpdateProjectErrorResult() throws Exception
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.UpdateProjectValidationResult result = new ProjectService.UpdateProjectValidationResult(errorCollection);

        try
        {
            projectService.updateProject(result);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
    }

    @Test
    public void testCreateProjectSuccess() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = mockController
                .getMock(IssueTypeScreenSchemeManager.class);
        final WorkflowSchemeManager workflowSchemeManager = mockController
                .getMock(WorkflowSchemeManager.class);

        final Project mockProject = new ProjectImpl(null);

        projectManager.createProject("projectName", "KEY", null, "admin", null, null, null);
        mockController.setReturnValue(mockProject);
        issueTypeScreenSchemeManager.associateWithDefaultScheme(mockProject);
        workflowSchemeManager.clearWorkflowCache();

        mockController.replay();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(errorCollection,
                "projectName", "KEY", null, "admin", null, null, null);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null, null,
                null, workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null, i18nFactory, null);

        Project project = projectService.createProject(result);
        assertNotNull(project);

        mockController.verify();
    }

    @Test
    public void testUpdateProjectSuccess() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = mockController
                .getMock(IssueTypeScreenSchemeManager.class);
        final WorkflowSchemeManager workflowSchemeManager = mockController.getMock(WorkflowSchemeManager.class);

        final Project mockProject = new ProjectImpl(null)
        {
            public String getKey()
            {
                return "KEY";
            }
        };

        projectManager.updateProject(mockProject, "projectName", null, "admin", null, null, null);
        mockController.setReturnValue(mockProject);
        mockController.replay();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.UpdateProjectValidationResult result = new ProjectService.UpdateProjectValidationResult(errorCollection,
                "projectName", "KEY", null, "admin", null, null, null, mockProject);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null, null,
                null, workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null, i18nFactory, null);

        Project project = projectService.updateProject(result);
        assertNotNull(project);

        mockController.verify();
    }

    @Test
    public void testUpdateSchemesNullResult()
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        final Project mockProject = new ProjectImpl(null);
        try
        {
            projectService.updateProjectSchemes(null, mockProject);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testUpdateSchemesErrorResult()
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);

        final Project mockProject = new ProjectImpl(null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("field", "error");
        ProjectService.UpdateProjectSchemesValidationResult result = new ProjectService.UpdateProjectSchemesValidationResult(
                errorCollection);

        try
        {
            projectService.updateProjectSchemes(result, mockProject);
            fail();
        }
        catch (IllegalStateException e)
        {
            //
        }
    }

    @Test
    public void testUpdateSchemesNullProject()
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, i18nFactory, null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        ProjectService.UpdateProjectSchemesValidationResult result = new ProjectService.UpdateProjectSchemesValidationResult(
                errorCollection);

        try
        {
            projectService.updateProjectSchemes(result, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testUpdateSchemesSuccessEnterprise() throws Exception
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = mockController.
                getMock(IssueTypeScreenSchemeManager.class);
        final WorkflowSchemeManager workflowSchemeManager = mockController.getMock(WorkflowSchemeManager.class);
        final PermissionSchemeManager permissionSchemeManager = mockController.getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notificationSchemeManager = mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = mockController
                .getMock(IssueSecuritySchemeManager.class);
        final SchemeFactory schemeFactory = mockController.getMock(SchemeFactory.class);

        final Project mockProject = new ProjectImpl(null);
        final Long schemeId = 1L;
        MockGenericValue permissionScheme = new MockGenericValue("permissionScheme", new HashMap());
        MockGenericValue notificationScheme = new MockGenericValue("notificationScheme", new HashMap());
        MockGenericValue issueSecurityScheme = new MockGenericValue("issueSecurityScheme", new HashMap());

        notificationSchemeManager.removeSchemesFromProject(mockProject);
        notificationSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(notificationScheme);
        schemeFactory.getScheme(notificationScheme);
        final Scheme nScheme = new Scheme();
        mockController.setReturnValue(nScheme);
        notificationSchemeManager.addSchemeToProject(mockProject, nScheme);

        permissionSchemeManager.removeSchemesFromProject(mockProject);
        permissionSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(permissionScheme);
        schemeFactory.getScheme(permissionScheme);
        final Scheme pScheme = new Scheme();
        mockController.setReturnValue(pScheme);
        permissionSchemeManager.addSchemeToProject(mockProject, pScheme);

        issueSecuritySchemeManager.removeSchemesFromProject(mockProject);
        issueSecuritySchemeManager.getScheme(schemeId);
        mockController.setReturnValue(issueSecurityScheme);
        schemeFactory.getScheme(issueSecurityScheme);
        final Scheme iScheme = new Scheme();
        mockController.setReturnValue(iScheme);
        issueSecuritySchemeManager.addSchemeToProject(mockProject, iScheme);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null,
                permissionSchemeManager, notificationSchemeManager, issueSecuritySchemeManager, schemeFactory,
                workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null, i18nFactory, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.UpdateProjectSchemesValidationResult schemesResult
                = new ProjectService.UpdateProjectSchemesValidationResult(errorCollection, schemeId, schemeId, schemeId);
        projectService.updateProjectSchemes(schemesResult, mockProject);

        mockController.verify();
    }

    @Test
    public void testValidateDeleteProject()
    {
        final User user = new MockUser("admin");
        final Project mockProject = new ProjectImpl(null);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        expect(projectManager.getProjectObjByKey("HSP")).andReturn(mockProject);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        mockController.replay();
        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(user, "HSP");

        assertTrue(result.isValid());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertEquals(mockProject, result.getProject());
        mockController.verify();
    }

    @Test
    public void testValidateDeleteProjectNoPermission()
    {
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);


        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        mockController.replay();
        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(user, "HSP");

        assertFalse(result.isValid());

        assertThat(result.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.projects.service.error.no.admin.permission")));
        mockController.verify();
    }

    @Test
    public void testValidateDeleteProjectNoProject()
    {
        final User user = new MockUser("admin");

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null)
        {
            public GetProjectResult getProjectByKeyForAction(final User user, final String key, ProjectAction projectAction)
            {
                ErrorCollection errors = new SimpleErrorCollection();
                errors.addErrorMessage("Error retrieving project.");
                return new GetProjectResult(errors);
            }
        };

        mockController.replay();
        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(user, "HSP");

        assertFalse(result.isValid());
        assertEquals("Error retrieving project.", result.getErrorCollection().getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testDeleteProjectNullResult()
    {
        final User user = new MockUser("admin");

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        try
        {
            projectService.deleteProject(user, null);
            fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("You can not delete a project with a null validation result.", e.getMessage());
        }
    }

    @Test
    public void testDeleteProjectInvalidResult()
    {
        final User user = new MockUser("admin");

        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Something bad happend");
        ProjectService.DeleteProjectValidationResult result = new ProjectService.DeleteProjectValidationResult(errors, null);

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        try
        {
            projectService.deleteProject(user, result);
            fail("Should have thrown exception");
        }
        catch (IllegalStateException e)
        {
            assertEquals("You can not delete a project with an invalid validation result.", e.getMessage());
        }
    }

    @Test
    public void testDeleteProjectRemoveIssuesException() throws RemoveException
    {
        final User user = new MockUser("admin");


        Project mockProject = new ProjectImpl(null);
        ProjectManager mockProjectManager = getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);
        expectLastCall().andThrow(new RemoveException("Error deleting issues"));


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, i18nFactory, null);

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertThat(projectResult.getErrorCollection().getErrorMessages(), hasItem(NoopI18nHelper.makeTranslation("admin.errors.project.exception.removing", "Error deleting issues")));

        mockController.verify();
    }

    @Test
    public void testDeleteProjectRemoveAssociationsThrowsException() throws RemoveException, GenericEntityException
    {
        final User user = new MockUser("admin");

        final GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", 10000L));
        Project mockProject = new ProjectImpl(mockProjectGV);

        final ProjectManager mockProjectManager = mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        final CustomFieldManager mockCustomFieldManager = mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProject);

        final IssueTypeScreenScheme mockIssueTypeScreenScheme = Mockito.mock(IssueTypeScreenScheme.class);

        final IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = mockController.getMock(IssueTypeScreenSchemeManager.class);
        expect(mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).andReturn(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        final NodeAssociationStore mockNodeAssociationStore = mockController.getMock(NodeAssociationStore.class);
        mockNodeAssociationStore.removeAssociationsFromSource(mockProjectGV);
        mockController.setThrowable(new DataAccessException("Error removing associations"));

        mockController.replay();

        final DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, Mockito.mock(WorkflowSchemeManager.class), mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockNodeAssociationStore,
                null, null, null, null, i18nFactory, Mockito.mock(WorkflowManager.class));

        final ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertThat
                (
                        projectResult.getErrorCollection().getErrorMessages(),
                        hasItem
                                (
                                        NoopI18nHelper.makeTranslation
                                                (
                                                        "admin.errors.project.exception.removing",
                                                        "Error removing associations")
                                )
                );

        mockController.verify();
    }

    @Test
    public void testDeleteProjectProjectComponentThrowsException()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        final User user = new MockUser("admin");

        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProject);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        NodeAssociationStore mockNodeAssociationStore = mockController.getMock(NodeAssociationStore.class);
        mockNodeAssociationStore.removeAssociationsFromSource(mockProjectGV);

        VersionManager mockVersionManager = mockController.getMock(VersionManager.class);
        mockVersionManager.getVersions(10000L);
        mockController.setReturnValue(Collections.EMPTY_LIST);

        ProjectComponent mockProjectComponent = mockController.getMock(ProjectComponent.class);
        mockProjectComponent.getId();
        mockController.setReturnValue(-99L);

        ProjectComponentManager mockProjectComponentManager = mockController.getMock(ProjectComponentManager.class);
        mockProjectComponentManager.findAllForProject(10000L);
        mockController.setReturnValue(EasyList.build(mockProjectComponent));
        mockProjectComponentManager.delete(-99L);
        mockController.setThrowable(new EntityNotFoundException("Component could not be found!"));

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, Mockito.mock(WorkflowSchemeManager.class), mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockNodeAssociationStore,
                mockVersionManager, mockProjectComponentManager, null, null, i18nFactory, Mockito.mock(WorkflowManager.class));

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertThat
                (
                        projectResult.getErrorCollection().getErrorMessages(),
                        hasItem
                                (
                                    NoopI18nHelper.makeTranslation
                                            (
                                                    "admin.errors.project.exception.removing",
                                                    "Component could not be found!")
                                )
                );

        mockController.verify();
    }

    @Test
    public void testDeleteProjectNoVersionsNorComponents()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        final User user = new MockUser("admin");

        GenericValue mockProjectGV = new MockGenericValue("Project", ImmutableMap.of("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProject);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        NodeAssociationStore mockNodeAssociationStore = mockController.getMock(NodeAssociationStore.class);
        mockNodeAssociationStore.removeAssociationsFromSource(mockProjectGV);

        VersionManager mockVersionManager = mockController.getMock(VersionManager.class);
        mockVersionManager.getVersions(10000L);
        mockController.setReturnValue(Collections.EMPTY_LIST);

        ProjectComponentManager mockProjectComponentManager = mockController.getMock(ProjectComponentManager.class);
        mockProjectComponentManager.findAllForProject(10000L);
        mockController.setReturnValue(Collections.EMPTY_LIST);

        MockControl mockSharePermissionDeleteUtilsControl = MockClassControl.createControl(SharePermissionDeleteUtils.class);
        SharePermissionDeleteUtils mockSharePermissionDeleteUtils = (SharePermissionDeleteUtils) mockSharePermissionDeleteUtilsControl.getMock();
        mockSharePermissionDeleteUtils.deleteProjectSharePermissions(10000L);
        mockSharePermissionDeleteUtilsControl.replay();

        mockProjectManager.removeProject(mockProject);

        mockProjectManager.refresh();

        WorkflowSchemeManager mockWorkflowSchemeManager = mockController.getMock(WorkflowSchemeManager.class);
        expect(mockWorkflowSchemeManager.getSchemeFor(mockProject)).andReturn(null);
        mockWorkflowSchemeManager.clearWorkflowCache();


        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, mockWorkflowSchemeManager, mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockNodeAssociationStore,
                mockVersionManager, mockProjectComponentManager,
                mockSharePermissionDeleteUtils, null, i18nFactory, Mockito.mock(WorkflowManager.class));

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertTrue(projectResult.isValid());

        mockController.verify();
        mockI18nBeanControl.verify();
        mockSharePermissionDeleteUtilsControl.verify();
    }

    @Test
    public void testDeleteProject() throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        final User user = ImmutableUser.newUser().name("admin").toUser();

        final GenericValue mockProjectGV = new MockGenericValue("Project", ImmutableMap.of("key", "HSP", "name", "homosapien", "id", 10000L));
        Project mockProject = new ProjectImpl(mockProjectGV);

        final ProjectManager projectManager = Mockito.mock(ProjectManager.class);

        final CustomFieldManager customFieldManager = Mockito.mock(CustomFieldManager.class);

        final IssueTypeScreenScheme mockIssueTypeScreenScheme = Mockito.mock(IssueTypeScreenScheme.class);

        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = Mockito.mock(IssueTypeScreenSchemeManager.class);
        when(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV)).thenReturn(mockIssueTypeScreenScheme);

        final NodeAssociationStore nodeAssociationStore = Mockito.mock(NodeAssociationStore.class);

        final Version mockVersion = new MockVersion(1000L, "the-version-to-delete");

        final VersionManager versionManager = Mockito.mock(VersionManager.class);
        when(versionManager.getVersions(10000L)).thenReturn(ImmutableList.of(mockVersion));

        final ProjectComponent mockProjectComponent = new MockProjectComponent(12L, "the-component-to-delete");

        final ProjectComponentManager projectComponentManager = Mockito.mock(ProjectComponentManager.class);
        when(projectComponentManager.findAllForProject(10000L)).thenReturn(ImmutableList.of(mockProjectComponent));

        final SharePermissionDeleteUtils sharePermissionDeleteUtils = Mockito.mock(SharePermissionDeleteUtils.class);

        final WorkflowSchemeManager workflowSchemeManager = Mockito.mock(WorkflowSchemeManager.class);
        when(workflowSchemeManager.getSchemeFor(mockProject)).thenReturn(null);

        final WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
        final List<JiraWorkflow> workflowsUsedByProject = Collections.emptyList();
        when(workflowManager.getWorkflowsFromScheme((Scheme) null)).thenReturn(workflowsUsedByProject);

        final DefaultProjectService projectService = new DefaultProjectService
                (
                        null, projectManager, null, null, null, null, null, null, workflowSchemeManager,
                        issueTypeScreenSchemeManager, customFieldManager, nodeAssociationStore, versionManager,
                        projectComponentManager, sharePermissionDeleteUtils, null, i18nFactory, workflowManager
                );

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertTrue(projectResult.isValid());

        Mockito.verify(projectManager).removeProjectIssues(mockProject);
        Mockito.verify(projectManager).removeProject(mockProject);
        Mockito.verify(projectManager).refresh();

        Mockito.verify(customFieldManager).removeProjectAssociations(mockProject);

        Mockito.verify(issueTypeScreenSchemeManager).removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        Mockito.verify(nodeAssociationStore).removeAssociationsFromSource(mockProjectGV);

        Mockito.verify(versionManager).deleteVersion(mockVersion);
        Mockito.verify(projectComponentManager).delete(12L);

        Mockito.verify(sharePermissionDeleteUtils).deleteProjectSharePermissions(10000L);
        Mockito.verify(workflowSchemeManager).clearWorkflowCache();
        Mockito.verify(workflowManager).copyAndDeleteDraftsForInactiveWorkflowsIn(user, workflowsUsedByProject);
    }
}
