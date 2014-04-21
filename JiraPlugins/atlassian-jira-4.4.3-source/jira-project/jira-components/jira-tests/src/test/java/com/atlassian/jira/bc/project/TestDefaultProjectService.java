package com.atlassian.jira.bc.project;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
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
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestDefaultProjectService extends MockControllerTestCase
{
    @Before
    public void setup()
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Test
    public void testRetrieveProjectByIdProjectDoesntExist()
    {
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.not.found.for.id", 1L);
        mockI18nBeanControl.setReturnValue("Project not found for id '1'");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };


        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(null, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals("Project not found for id '1'", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testRetrieveProjectByIdNoBrowsePermission()
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(mockProject);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");
        permissionManager.hasPermission(Permissions.BROWSE, mockProject, user);
        mockController.setReturnValue(false);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.no.browse.permission");
        mockI18nBeanControl.setReturnValue("No Permission to browse project");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, 1L);
        assertNotNull(projectResult);
        assertNull(projectResult.getProject());
        assertEquals("No Permission to browse project",
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testRetrieveProjectById()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(mockProject);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");
        permissionManager.hasPermission(Permissions.BROWSE, mockProject, user);
        mockController.setReturnValue(true);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, 1L);
        assertNotNull(projectResult);
        assertEquals(mockProject, projectResult.getProject());
        assertFalse(projectResult.getErrorCollection().hasAnyErrors());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testRetrieveProjectByKeyAndActionProjectDoesntExist()
    {
        String projectKey = "projectKey";
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        expect(projectManager.getProjectObjByKey(projectKey)).andReturn(null);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null)
        {
            I18nHelper getI18nBean(final User user)
            {
                return new NoopI18nHelper();
            }
        };


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

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null)
        {

            @Override
            boolean checkActionPermission(User user, Project project, ProjectAction action)
            {
                if (project.equals(project1))
                {
                    assertEquals(ProjectAction.EDIT_PROJECT_CONFIG, action);
                    assertEquals(expectedUser, user);
                    return false;
                }
                else
                {
                    assertEquals(ProjectAction.VIEW_PROJECT, action);
                    assertNull(user);
                    return true;

                }
            }

            I18nHelper getI18nBean(final User user)
            {
                return new NoopI18nHelper();
            }
        };


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
                null, null, null, null, null, null, null, null, null, null, null)
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
                null, null, null, null, null, null, null, null, null, null, null)
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
                null, null, null, null, null, null, null, null, null, null, null)
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
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null, null, null,
                null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return new MockI18nBean();
            }
        };

        final ProjectService.CreateProjectValidationResult projectResult = projectService.validateCreateProject(user,
                "projectName", "invalidKey", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertEquals("You must have global administrator rights in order to modify projects.",
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
    }

    @Test
    public void testValidateUpdateProjectNoPermission()
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project",
                EasyMap.build("id", new Long(1000), "key", "HSP", "name", "homosapien"));
        final Project mockProject = new ProjectImpl(mockProjectGV);

        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey("HSP");
        mockController.setReturnValue(mockProject);

        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);
        permissionManager.hasPermission(Permissions.PROJECT_ADMIN, mockProject, user);
        mockController.setReturnValue(false);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.no.config.permission");
        mockI18nBeanControl.setReturnValue("You cannot edit the configuration of this project.");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null, null, null,
                null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

        };

        final ProjectService.UpdateProjectValidationResult projectResult = projectService.validateUpdateProject(user,
                "projectName", "HSP", "description", "admin", null, null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertEquals("You cannot edit the configuration of this project.",
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateCreateProjectNullValues()
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey(null);
        mockController.setReturnValue(null);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING);
        mockController.setReturnValue("You must specify a valid project key.");

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateUpdateProjectNullValues()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ApplicationProperties applicationProperties = (ApplicationProperties) mockController
                .getMock(ApplicationProperties.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        projectManager.getProjectObjByKey(null);
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.not.found.for.key", null);
        mockI18nBeanControl.setReturnValue("You must specify a valid project key.");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("You must specify a valid project key.",
                projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateCreateProjectAlreadyExists()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateUpdateProjectNotExists()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.not.found.for.key", "KEY");
        mockI18nBeanControl.setReturnValue("No project could be found with key 'KEY'");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("No project could be found with key 'KEY'",
                projectResult.getErrorCollection().getErrorMessages().iterator().next());
        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateProjectInvalidKey()
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_WARNING);
        mockController.setReturnValue("projectKeyWarning");
        projectManager.getProjectObjByKey("invalidKey");
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, applicationProperties,
                permissionManager, null, null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateProjectReservedKeyword()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("invalidKey");
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateCreateProjectLeadNotexists()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);


        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
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

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);


        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.not.a.valid.user");
        mockI18nBeanControl.setReturnValue("The user you have specified as project lead does not exist.");

        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("The user you have specified as project lead does not exist.",
                projectResult.getErrorCollection().getErrors().get("projectLead"));

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateCreateProjectLongNameUrlAndKey()
    {
        String longProjectKey = StringUtils.repeat("B", 256);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByKey(longProjectKey);
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
                StringUtils.repeat("A", 151), longProjectKey, "description", "admin", StringUtils.repeat("C", 256), null);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        assertEquals(3, projectResult.getErrorCollection().getErrors().size());
        assertEquals("The URL must not exceed 255 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name must not exceed 150 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));
        assertEquals("The project key must not exceed 255 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectKey"));

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateCreateProjectInvalidUrl()
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final Project mockProject = new ProjectImpl(null);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateUpdateProjectInvalidUrl()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
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

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.url.specified.is.not.valid");
        mockI18nBeanControl.setReturnValue("The URL specified is not valid - it must start with http://");

        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("The URL specified is not valid - it must start with http://",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateUpdateProjectLongNameAndUrl()
    {
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);
        final User user = new MockUser("admin");
        final I18nBean mockI18nBean = mockController.getMock(I18nBean.class);
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
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true).times(2);
        expect(mockI18nBean.getText("admin.errors.project.name.too.long"))
                .andReturn("The project name must not exceed 150 characters in length.");
        expect(mockI18nBean.getText("admin.errors.project.url.too.long"))
                .andReturn("The URL must not exceed 255 characters in length.");

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("The URL must not exceed 255 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectUrl"));
        assertEquals("The project name must not exceed 150 characters in length.",
                projectResult.getErrorCollection().getErrors().get("projectName"));

        mockController.verify();
    }

    @Test
    public void testValidateCreateProjectAssigneeType()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);
        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);
        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(null);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateUpdateProjectAssigneeType()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
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
        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        projectManager.getProjectObjByKey("KEY");
        mockController.setReturnValue(mockProject);

        projectManager.getProjectObjByName("projectName");
        mockController.setReturnValue(mockProject);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.invalid.default.assignee");
        mockI18nBeanControl.setReturnValue("Invalid default Assignee.");
        mockI18nBeanControl.replay();
        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

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
        assertEquals("Invalid default Assignee.", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateSchemesNoPermission()
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.projects.service.error.no.admin.permission");
        mockI18nBeanControl.setReturnValue("You must have global administrator rights in order to modify projects.");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.UpdateProjectSchemesValidationResult result = projectService
                .validateUpdateProjectSchemes(user, 1L, 1L, 1L);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCollection().hasAnyErrors());
        assertEquals("You must have global administrator rights in order to modify projects.",
                result.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }


    @Test
    public void testValidateSchemesNullSchemes() throws Exception
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        null, null, null);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateSchemesMinusOne() throws Exception
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final Long schemeId = new Long(-1);
        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateSchemesNotExistEnterprise() throws Exception
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final PermissionSchemeManager permissionSchemeManager = (PermissionSchemeManager) mockController
                .getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notitNotificationSchemeManager = (NotificationSchemeManager) mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = (IssueSecuritySchemeManager) mockController
                .getMock(IssueSecuritySchemeManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        final Long schemeId = 1L;
        permissionSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(null);
        notitNotificationSchemeManager.getScheme(schemeId);
        mockController.setReturnValue(null);
        if (true)
        {
            issueSecuritySchemeManager.getScheme(schemeId);
            mockController.setReturnValue(null);
        }

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.validation.permission.scheme.not.retrieved");
        mockI18nBeanControl.setReturnValue("Unable to validate, permission scheme could not be retrieved.");
        mockI18nBean.getText("admin.errors.project.validation.notification.scheme.not.retrieved");
        mockI18nBeanControl.setReturnValue("Unable to validate, notification scheme could not be retrieved.");
        mockI18nBean.getText("admin.errors.project.validation.issuesecurity.scheme.not.retrieved");
        mockI18nBeanControl.setReturnValue("Unable to validate, issue security scheme could not be retrieved.");
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager,
                permissionSchemeManager, notitNotificationSchemeManager, issueSecuritySchemeManager, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService
                .validateUpdateProjectSchemes(user,
                        schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertFalse(projectResult.isValid());
        assertTrue(projectResult.getErrorCollection().hasAnyErrors());
        final Iterator iterator = projectResult.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Unable to validate, permission scheme could not be retrieved.", iterator.next());
        assertEquals("Unable to validate, notification scheme could not be retrieved.", iterator.next());
        assertEquals("Unable to validate, issue security scheme could not be retrieved.", iterator.next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateSchemesExistEnterprise() throws Exception
    {
        final PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final PermissionSchemeManager permissionSchemeManager = (PermissionSchemeManager) mockController
                .getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notitNotificationSchemeManager = (NotificationSchemeManager) mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = (IssueSecuritySchemeManager) mockController
                .getMock(IssueSecuritySchemeManager.class);
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

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

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager,
                permissionSchemeManager, notitNotificationSchemeManager, issueSecuritySchemeManager, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        final ProjectService.UpdateProjectSchemesValidationResult projectResult = projectService.validateUpdateProjectSchemes(user, schemeId, schemeId, schemeId);

        assertNotNull(projectResult);
        assertTrue(projectResult.isValid());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testCreateProjectNullResult() throws Exception
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

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
                null, null, null, null, null, null, null, null);

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
                null, null, null, null, null, null, null, null);

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
                null, null, null, null, null, null, null, null);

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
        issueTypeScreenSchemeManager.associateWithDefaultScheme(null);
        workflowSchemeManager.clearWorkflowCache();

        mockController.replay();

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.CreateProjectValidationResult result = new ProjectService.CreateProjectValidationResult(errorCollection,
                "projectName", "KEY", null, "admin", null, null, null);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, null, null, null, null,
                null, workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null);

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
                null, workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null);

        Project project = projectService.updateProject(result);
        assertNotNull(project);

        mockController.verify();
    }

    @Test
    public void testUpdateSchemesNullResult()
    {
        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

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
                null, null, null, null, null, null, null, null);

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
                null, null, null, null, null, null, null, null);
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
        final ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = (IssueTypeScreenSchemeManager) mockController
                .getMock(IssueTypeScreenSchemeManager.class);
        final WorkflowSchemeManager workflowSchemeManager = (WorkflowSchemeManager) mockController
                .getMock(WorkflowSchemeManager.class);
        final PermissionSchemeManager permissionSchemeManager = (PermissionSchemeManager) mockController
                .getMock(PermissionSchemeManager.class);
        final NotificationSchemeManager notificationSchemeManager = (NotificationSchemeManager) mockController
                .getMock(NotificationSchemeManager.class);
        final IssueSecuritySchemeManager issueSecuritySchemeManager = (IssueSecuritySchemeManager) mockController
                .getMock(IssueSecuritySchemeManager.class);
        final SchemeFactory schemeFactory = (SchemeFactory) mockController.getMock(SchemeFactory.class);

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
                workflowSchemeManager, issueTypeScreenSchemeManager, null, null, null, null, null, null);

        ErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectService.UpdateProjectSchemesValidationResult schemesResult
                = new ProjectService.UpdateProjectSchemesValidationResult(errorCollection, schemeId, schemeId, schemeId);
        projectService.updateProjectSchemes(schemesResult, mockProject);

        mockController.verify();
    }

    @Test
    public void testValidateDeleteProject()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");
        final Project mockProject = new ProjectImpl(null);

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjectObjByKey("HSP");
        mockController.setReturnValue(mockProject);

        DefaultProjectService projectService = new DefaultProjectService(null, projectManager, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return new MockI18nBean();
            }
        };

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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        final Project mockProject = new ProjectImpl(null);

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(false);

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.projects.service.error.no.admin.permission");
        mockI18nBeanControl.setReturnValue("No admin permission");
        mockI18nBeanControl.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }

        };

        mockController.replay();
        final ProjectService.DeleteProjectValidationResult result = projectService.validateDeleteProject(user, "HSP");

        assertFalse(result.isValid());
        assertEquals("No admin permission", result.getErrorCollection().getErrorMessages().iterator().next());
        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testValidateDeleteProjectNoProject()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.ADMINISTER, user);
        mockController.setReturnValue(true);

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, permissionManager, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            public GetProjectResult getProjectByKeyForAction(final User user, final String key, ProjectAction projectAction)
            {
                ErrorCollection errors = new SimpleErrorCollection();
                errors.addErrorMessage("Error retrieving project.");
                return new GetProjectResult(errors);
            }

            @Override
            I18nBean getI18nBean(final User user)
            {
                return new MockI18nBean();
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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return new MockI18nBean();
            }
        };

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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Something bad happend");
        ProjectService.DeleteProjectValidationResult result = new ProjectService.DeleteProjectValidationResult(errors, null);

        DefaultProjectService projectService = new DefaultProjectService(null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null) {
            @Override
            I18nBean getI18nBean(final User user)
            {
                return new MockI18nBean();
            }
        };

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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        Project mockProject = new ProjectImpl(null);
        ProjectManager mockProjectManager = getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);
        expectLastCall().andThrow(new RemoveException("Error deleting issues"));

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.exception.removing", "Error deleting issues");
        mockI18nBeanControl.setReturnValue("Errors removing project: 'Error deleting issues'");
        mockI18nBeanControl.replay();


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertEquals("Errors removing project: 'Error deleting issues'", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testDeleteProjectRemoveAssociationsThrowsException() throws RemoveException, GenericEntityException
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProjectGV);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        //TODO: This is broken in the mockController at the moment
//        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = (IssueTypeScreenSchemeManager) mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        AssociationManager mockAssociationManager = (AssociationManager) mockController.getMock(AssociationManager.class);
        mockAssociationManager.removeAssociationsFromSource(mockProjectGV);
        mockController.setThrowable(new GenericEntityException("Error removing associations"));

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.exception.removing", "Error removing associations");
        mockI18nBeanControl.setReturnValue("Errors removing project: 'Error removing associations'");
        mockI18nBeanControl.replay();


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, null, mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockAssociationManager,
                null, null, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertEquals("Errors removing project: 'Error removing associations'", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testDeleteProjectProjectComponentThrowsException()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProjectGV);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        //TODO: This is broken in the mockController at the moment
//        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = (IssueTypeScreenSchemeManager) mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        AssociationManager mockAssociationManager = (AssociationManager) mockController.getMock(AssociationManager.class);
        mockAssociationManager.removeAssociationsFromSource(mockProjectGV);

        VersionManager mockVersionManager = (VersionManager) mockController.getMock(VersionManager.class);
        mockVersionManager.getVersions(new Long(10000));
        mockController.setReturnValue(Collections.EMPTY_LIST);

        ProjectComponent mockProjectComponent = (ProjectComponent) mockController.getMock(ProjectComponent.class);
        mockProjectComponent.getId();
        mockController.setReturnValue(new Long(-99));

        ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockController.getMock(ProjectComponentManager.class);
        mockProjectComponentManager.findAllForProject(new Long(10000));
        mockController.setReturnValue(EasyList.build(mockProjectComponent));
        mockProjectComponentManager.delete(new Long(-99));
        mockController.setThrowable(new EntityNotFoundException("Component could not be found!"));


        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBean.getText("admin.errors.project.exception.removing", "Component could not be found!");
        mockI18nBeanControl.setReturnValue("Errors removing project: 'Component could not be found!'");
        mockI18nBeanControl.replay();


        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, null, mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockAssociationManager,
                mockVersionManager, mockProjectComponentManager, null, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertFalse(projectResult.isValid());
        assertEquals("Errors removing project: 'Component could not be found!'", projectResult.getErrorCollection().getErrorMessages().iterator().next());

        mockController.verify();
        mockI18nBeanControl.verify();
    }

    @Test
    public void testDeleteProjectNoVersionsNorComponents()
            throws RemoveException, GenericEntityException, EntityNotFoundException
    {
        final User user = new MockUser("admin");


        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProjectGV);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        //TODO: This is broken in the mockController at the moment
        //IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = (IssueTypeScreenSchemeManager) mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        AssociationManager mockAssociationManager = (AssociationManager) mockController.getMock(AssociationManager.class);
        mockAssociationManager.removeAssociationsFromSource(mockProjectGV);

        VersionManager mockVersionManager = (VersionManager) mockController.getMock(VersionManager.class);
        mockVersionManager.getVersions(new Long(10000));
        mockController.setReturnValue(Collections.EMPTY_LIST);

        ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockController.getMock(ProjectComponentManager.class);
        mockProjectComponentManager.findAllForProject(new Long(10000));
        mockController.setReturnValue(Collections.EMPTY_LIST);

        SearchRequestAdminService mockSearchRequestAdminService = (SearchRequestAdminService) mockController.getNiceMock(SearchRequestAdminService.class);

        MockControl mockSharePermissionDeleteUtilsControl = MockClassControl.createControl(SharePermissionDeleteUtils.class);
        SharePermissionDeleteUtils mockSharePermissionDeleteUtils = (SharePermissionDeleteUtils) mockSharePermissionDeleteUtilsControl.getMock();
        mockSharePermissionDeleteUtils.deleteProjectSharePermissions(new Long(10000));
        mockSharePermissionDeleteUtilsControl.replay();

        mockProjectManager.removeProject(mockProject);

        mockProjectManager.refresh();

        WorkflowSchemeManager mockWorkflowSchemeManager = (WorkflowSchemeManager) mockController.getMock(WorkflowSchemeManager.class);
        mockWorkflowSchemeManager.clearWorkflowCache();

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, mockWorkflowSchemeManager, mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockAssociationManager,
                mockVersionManager, mockProjectComponentManager,
                mockSharePermissionDeleteUtils, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User user = new MockUser("admin");


        GenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("key", "HSP", "name", "homosapien", "id", new Long(10000)));
        Project mockProject = new ProjectImpl(mockProjectGV);

        ProjectManager mockProjectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        mockProjectManager.removeProjectIssues(mockProject);

        CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.removeProjectAssociations(mockProjectGV);

        final MockControl mockIssueTypeScreenSchemeControl = MockControl.createControl(IssueTypeScreenScheme.class);
        IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockIssueTypeScreenSchemeControl.getMock();
        mockIssueTypeScreenSchemeControl.replay();

        //TODO: This is broken in the mockController at the moment
        //IssueTypeScreenScheme mockIssueTypeScreenScheme = (IssueTypeScreenScheme) mockController.getMock(IssueTypeScreenScheme.class);

        IssueTypeScreenSchemeManager mockIssueTypeScreenSchemeManager = (IssueTypeScreenSchemeManager) mockController.getMock(IssueTypeScreenSchemeManager.class);
        mockIssueTypeScreenSchemeManager.getIssueTypeScreenScheme(mockProjectGV);
        mockController.setReturnValue(mockIssueTypeScreenScheme);
        mockIssueTypeScreenSchemeManager.removeSchemeAssociation(mockProjectGV, mockIssueTypeScreenScheme);

        AssociationManager mockAssociationManager = (AssociationManager) mockController.getMock(AssociationManager.class);
        mockAssociationManager.removeAssociationsFromSource(mockProjectGV);

        MockControl mockVersionControl = MockControl.createControl(Version.class);
        Version mockVersion = (Version) mockVersionControl.getMock();
        mockVersionControl.replay();

        VersionManager mockVersionManager = (VersionManager) mockController.getMock(VersionManager.class);
        mockVersionManager.getVersions(new Long(10000));
        mockController.setReturnValue(EasyList.build(mockVersion));
        mockVersionManager.deleteVersion(mockVersion);


        ProjectComponent mockProjectComponent = (ProjectComponent) mockController.getMock(ProjectComponent.class);
        mockProjectComponent.getId();
        mockController.setReturnValue(new Long(12));
        ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockController.getMock(ProjectComponentManager.class);
        mockProjectComponentManager.findAllForProject(new Long(10000));
        mockController.setReturnValue(EasyList.build(mockProjectComponent));
        mockProjectComponentManager.delete(new Long(12));

        SearchRequestAdminService mockSearchRequestAdminService = (SearchRequestAdminService) mockController.getNiceMock(SearchRequestAdminService.class);

        MockControl mockSharePermissionDeleteUtilsControl = MockClassControl.createControl(SharePermissionDeleteUtils.class);
        SharePermissionDeleteUtils mockSharePermissionDeleteUtils = (SharePermissionDeleteUtils) mockSharePermissionDeleteUtilsControl.getMock();
        mockSharePermissionDeleteUtils.deleteProjectSharePermissions(new Long(10000));
        mockSharePermissionDeleteUtilsControl.replay();

        mockProjectManager.removeProject(mockProject);

        mockProjectManager.refresh();

        WorkflowSchemeManager mockWorkflowSchemeManager = (WorkflowSchemeManager) mockController.getMock(WorkflowSchemeManager.class);
        mockWorkflowSchemeManager.clearWorkflowCache();

        MockControl mockI18nBeanControl = MockClassControl.createControl(I18nBean.class);
        final I18nBean mockI18nBean = (I18nBean) mockI18nBeanControl.getMock();
        mockI18nBeanControl.replay();

        mockController.replay();

        DefaultProjectService projectService = new DefaultProjectService(null, mockProjectManager, null, null, null,
                null, null, null, mockWorkflowSchemeManager, mockIssueTypeScreenSchemeManager, mockCustomFieldManager, mockAssociationManager,
                mockVersionManager, mockProjectComponentManager,
                mockSharePermissionDeleteUtils, null)
        {
            I18nBean getI18nBean(final User user)
            {
                return mockI18nBean;
            }
        };

        ProjectService.DeleteProjectValidationResult result =
                new ProjectService.DeleteProjectValidationResult(new SimpleErrorCollection(), mockProject);
        final ProjectService.DeleteProjectResult projectResult = projectService.deleteProject(user, result);

        assertTrue(projectResult.isValid());

        mockController.verify();
        mockI18nBeanControl.verify();
        mockSharePermissionDeleteUtilsControl.verify();
    }
}
