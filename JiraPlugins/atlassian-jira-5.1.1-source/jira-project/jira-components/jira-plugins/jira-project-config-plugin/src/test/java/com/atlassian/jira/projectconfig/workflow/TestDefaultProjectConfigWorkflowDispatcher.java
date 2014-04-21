package com.atlassian.jira.projectconfig.workflow;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.MockTaskDescriptor;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.jira.workflow.migration.WorkflowMigrationHelper;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static com.atlassian.jira.util.NoopI18nHelper.makeTranslation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @since v5.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultProjectConfigWorkflowDispatcher
{
    @Mock
    private JiraWorkflow jiraDefaultWorkflow;

    @Mock
    private JiraWorkflow jiraWorkflow;

    @Mock
    private WorkflowSchemeManager workflowSchemeManager;

    @Mock
    private ProjectService projectService;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private MigrationHelperFactory migrationHelperFactory;

    private JiraAuthenticationContext jac;
    private DefaultProjectConfigWorkflowDispatcher dispatcher;
    private MockUser user;
    private Scheme scheme;

    @Before
    public void setup()
    {
        user = new MockUser("user");
        scheme = new Scheme(12345678L, "something", "Ignored", Collections.<SchemeEntity>emptyList());
        jac = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        dispatcher = new DefaultProjectConfigWorkflowDispatcher(workflowSchemeManager, projectService,
                jac, permissionManager, workflowService, migrationHelperFactory);

        stub(permissionManager.hasPermission(Permissions.ADMINISTER, user)).toReturn(true);
        stub(workflowService.getWorkflow(any(JiraServiceContext.class), eq(JiraWorkflow.DEFAULT_WORKFLOW_NAME)))
                .toReturn(jiraDefaultWorkflow);
        stub(jiraDefaultWorkflow.getName()).toReturn(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
        stub(jiraWorkflow.getName()).toReturn("something");
        stub(workflowSchemeManager.createSchemeAndEntities(any(Scheme.class)))
                .toReturn(scheme);
    }

    @Test
    public void testCopyWorkflowFailed()
    {
        final String expectedMessage = "our error message";
        final long projectId = 462873L;
        final MockProject realProject = new MockProject(projectId);

        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(realProject));
        stub(workflowSchemeManager.isUsingDefaultScheme(realProject)).toReturn(true);

        DefaultProjectConfigWorkflowDispatcher dispatcher = new DefaultProjectConfigWorkflowDispatcher(workflowSchemeManager, projectService, jac, permissionManager, workflowService, migrationHelperFactory)
        {
            @Override
            ServiceOutcome<String> copySystemWorkflow(Project project)
            {
                assertEquals(realProject, project);
                return ServiceOutcomeImpl.error(expectedMessage);
            }

            @Override
            ServiceOutcome<Long> createNewSchemeFromWorkflow(String workflowName, Project project)
            {
                throw new RuntimeException("THis method sould not be called.");
            }
        };

        ServiceOutcome<?> outcome = dispatcher.editWorkflow(projectId);
        assertErrorMessages(expectedMessage, outcome);
    }

    @Test
    public void testCopySchemeFailed()
    {
        final String expectedWorkflow = "expected workflow";
        final String expectedMessage = "our error message";

        final long projectId = 462873L;
        final MockProject realProject = new MockProject(projectId);

        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(realProject));
        stub(workflowSchemeManager.isUsingDefaultScheme(realProject)).toReturn(true);

        DefaultProjectConfigWorkflowDispatcher dispatcher = new DefaultProjectConfigWorkflowDispatcher(workflowSchemeManager, projectService, jac, permissionManager, workflowService, migrationHelperFactory)
        {
            @Override
            ServiceOutcome<String> copySystemWorkflow(Project project)
            {
                assertEquals(realProject, project);
                return ServiceOutcomeImpl.ok(expectedWorkflow);
            }

            @Override
            ServiceOutcome<Long> createNewSchemeFromWorkflow(String workflowName, Project project)
            {
                assertEquals(realProject, project);
                assertEquals(expectedWorkflow, workflowName);
                return ServiceOutcomeImpl.error(expectedMessage);
            }
        };

        ServiceOutcome<?> outcome = dispatcher.editWorkflow(projectId);
        assertErrorMessages(expectedMessage, outcome);
    }

    @Test
    public void testMigrationFailed()
    {
        final String expectedWorkflow = "expected workflow";
        final String expectedMessage = "our error message";
        final Long expectedSchemeId = 347429847823L;

        final long projectId = 462873L;
        final MockProject realProject = new MockProject(projectId);

        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(realProject));
        stub(workflowSchemeManager.isUsingDefaultScheme(realProject)).toReturn(true);

        DefaultProjectConfigWorkflowDispatcher dispatcher = new DefaultProjectConfigWorkflowDispatcher(workflowSchemeManager, projectService, jac, permissionManager, workflowService, migrationHelperFactory)
        {
            @Override
            ServiceOutcome<String> copySystemWorkflow(Project project)
            {
                assertEquals(realProject, project);
                return ServiceOutcomeImpl.ok(expectedWorkflow);
            }

            @Override
            ServiceOutcome<Long> createNewSchemeFromWorkflow(String workflowName, Project project)
            {
                assertEquals(realProject, project);
                assertEquals(expectedWorkflow, workflowName);
                return ServiceOutcomeImpl.ok(expectedSchemeId);
            }

            @Override
            ServiceOutcome<Long> migrateWorkflow(Project project, Long targetSchemeId)
            {
                assertEquals(realProject, project);
                assertEquals(expectedSchemeId, targetSchemeId);
                return ServiceOutcomeImpl.error(expectedMessage);
            }
        };

        ServiceOutcome<?> outcome = dispatcher.editWorkflow(projectId);
        assertErrorMessages(expectedMessage, outcome);
    }

    @Test
    public void testHappy()
    {
        final String expectedWorkflow = "expected workflow";
        final Long expectedSchemeId = 347429847823L;
        final Long expectedTaskId = 472574983L;

        final long projectId = 462873L;
        final MockProject realProject = new MockProject(projectId);

        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(realProject));
        stub(workflowSchemeManager.isUsingDefaultScheme(realProject)).toReturn(true);

        DefaultProjectConfigWorkflowDispatcher dispatcher = new DefaultProjectConfigWorkflowDispatcher(workflowSchemeManager, projectService, jac, permissionManager, workflowService, migrationHelperFactory)
        {
            @Override
            ServiceOutcome<String> copySystemWorkflow(Project project)
            {
                assertEquals(realProject, project);
                return ServiceOutcomeImpl.ok(expectedWorkflow);
            }

            @Override
            ServiceOutcome<Long> createNewSchemeFromWorkflow(String workflowName, Project project)
            {
                assertEquals(realProject, project);
                assertEquals(expectedWorkflow, workflowName);
                return ServiceOutcomeImpl.ok(expectedSchemeId);
            }

            @Override
            ServiceOutcome<Long> migrateWorkflow(Project project, Long targetSchemeId)
            {
                assertEquals(realProject, project);
                assertEquals(expectedSchemeId, targetSchemeId);
                return ServiceOutcomeImpl.ok(expectedTaskId);
            }
        };

        ServiceOutcome<Pair<String, Long>> outcome = dispatcher.editWorkflow(projectId);
        assertTrue(outcome.isValid());
        assertEquals(expectedWorkflow, outcome.getReturnedValue().first());
        assertEquals(expectedTaskId, outcome.getReturnedValue().second());
    }

    @Test
    public void testCurrentUserDoesNotHaveAdminPermission()
    {
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, user)).toReturn(false);
        final ServiceOutcome<?> serviceOutcome = dispatcher.editWorkflow(263721L);
        assertErrorMessages(makeTranslation("admin.project.workflow.no.edit.permission"),
                serviceOutcome);
    }

    @Test
    public void testCurrentProjectDoesNotExist()
    {
        final long projectId = 462873L;

        final SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage("some kind f");
        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(collection));

        final ServiceOutcome<?> serviceOutcome = dispatcher.editWorkflow(projectId);
        assertErrorMessages(makeTranslation("admin.project.project.no.edit.permission"),
                serviceOutcome);
    }

    @Test
    public void testNotUsingDefaultWorkflowScheme()
    {
        final long projectId = 462873L;
        stub(projectService.getProjectByIdForAction(user, projectId, ProjectAction.EDIT_PROJECT_CONFIG))
                .toReturn(new ProjectService.GetProjectResult(new MockProject(projectId)));
        final ServiceOutcome<?> stringServiceOutcome = dispatcher.editWorkflow(projectId);
        assertFalse(stringServiceOutcome.isValid());
        assertErrorMessages(makeTranslation("admin.project.workflow.not.default.scheme"),
                stringServiceOutcome);
    }

    @Test
    public void testCreateNewWorkflowValidNameButCopyFailed()
    {
        final MockProject project = new MockProject(462873L, "TEST", "Test");
        final String expected = project.getName() + " Workflow";
        stub(workflowService.copyWorkflow(any(JiraServiceContext.class), eq(expected),
                Mockito.<String>eq(null), eq(jiraDefaultWorkflow))).toReturn(null);
        final ServiceOutcome<String> stringServiceOutcome = dispatcher.copySystemWorkflow(project);
        assertErrorMessages(makeTranslation("admin.project.workflow.unable.to.copy.workflow"),
                stringServiceOutcome);
    }

    @Test
    public void testCreateNewWorkflowSchemeButCreateFailed() throws GenericEntityException
    {
        stub(workflowSchemeManager.schemeExists(any(String.class))).toThrow(new GenericEntityException());
        final ServiceOutcome<Long> stringServiceOutcome = dispatcher.createNewSchemeFromWorkflow("workflow", new MockProject(1, "key"));
        assertErrorMessages(makeTranslation("admin.project.workflow.scheme.unable.to.copy.workflow.scheme"),
                stringServiceOutcome);
    }

    @Test
    public void testGetWorkflowNameForProjectWithGoodProjectName()
    {
        checkWorkflowName("Test Workflow", "Test", "TEST");
    }


    @Test
    public void testGetWorkflowSchemeNameValidName()
    {
        checkWorkflowSchemeName(getWorkflowSchemeName("some"), "some", "SOM");
    }

    @Test
    public void testGetWorkflowNameForProjectWithI18NName()
    {
        checkWorkflowName("TBest Workflow", "T\u1E02est", "TEST");
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongMultibyteNameAndGoodKey()
    {
        checkWorkflowName("TEST Workflow", StringUtils.repeat("\u0F03", 256), "TEST");
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongMultibyteNameAndLongMultibyteKey()
    {
        checkWorkflowName("Workflow 1", StringUtils.repeat("\u0F03", 256), StringUtils.repeat("\u0F03", 256));
    }

    @Test
    public void testGetWorkflowNameForProjectWithMultibyteName()
    {
        checkWorkflowName("TEST Workflow", "T\u0F03est", "TEST");
    }

    @Test
    public void testGetWorkflowNameForProjectWithMultibyteNameAndMultibyteKey()
    {
        checkWorkflowName("Workflow 1", "T\u0F03est", "T\u0F03EST");
    }

    @Test
    public void testGetWorkflowNameForProjectWithDuplicateName()
    {
        Collection<String> workflows = ImmutableList.of("Test Workflow");
        checkWorkflowName("Test Workflow 2", "Test", "TEST", workflows);
    }

    @Test
    public void testGetWorkflowSchemeNameWithDuplicateName()
    {
        Collection<String> schemes = ImmutableList.of(getWorkflowSchemeName("some"));
        checkWorkflowSchemeName(getWorkflowSchemeName("some", 2), "some", "SOM", schemes);
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongNameAndGoodKey()
    {
        checkWorkflowName("TEST Workflow", StringUtils.repeat("*", 256), "TEST");
    }

    @Test
    public void testGetWorkflowSchemeNameWithLongNameAndGoodKey()
    {
        checkWorkflowSchemeName(getWorkflowSchemeName("SOM"), StringUtils.repeat("*", 256), "SOM");
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongNameAndI18NKey()
    {
        checkWorkflowName("TBEST Workflow", StringUtils.repeat("*", 256), "T\u1E02EST");
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongNameAndDuplicateKey()
    {
        Collection<String> workflows = ImmutableList.of("TEST Workflow");
        checkWorkflowName("TEST Workflow 2", StringUtils.repeat("*", 256), "TEST", workflows);
    }

    @Test
    public void testGetWorkflowSchemeNameWithLongNameAndDuplicateKey()
    {
        Collection<String> schemes = ImmutableList.of(getWorkflowSchemeName("SOM"));
        checkWorkflowSchemeName(getWorkflowSchemeName("SOM", 2), StringUtils.repeat("*", 256), "SOM", schemes);
    }

    @Test
    public void testGetWorkflowNameForProjectWithLongNameAndLongKey()
    {
        checkWorkflowName("Workflow 1", StringUtils.repeat("*", 256), StringUtils.repeat("*", 256));
    }

    @Test
    public void testGetWorkflowSchemeNameWithLongNameAndLongKey()
    {
        checkWorkflowSchemeName(getWorkflowSchemeName(1), StringUtils.repeat("*", 256), StringUtils.repeat("*", 256));
    }

    @Test
    public void testQuickMigrateWorkflow() throws GenericEntityException
    {
        MockProject project = new MockProject(12345L, "KEY", "Name");
        MockGenericValue scheme = new MockGenericValue("WorkflowScheme");
        WorkflowMigrationHelper migrationHelper = mock(WorkflowMigrationHelper.class);
        stub(migrationHelper.doQuickMigrate()).toReturn(true);
        stub(migrationHelperFactory.createMigrationHelper(project.getGenericValue(), scheme)).toReturn(migrationHelper);
        long id = 123456L;
        stub(workflowSchemeManager.getScheme(id)).toReturn(scheme);

        ServiceOutcome<?> serviceOutcome = dispatcher.migrateWorkflow(project, id);
        assertTrue(serviceOutcome.isValid());
        verify(migrationHelper, never()).migrateAsync();
        assertNull(serviceOutcome.getReturnedValue());
    }

    @Test
    public void testSlowMigrateWorkflow() throws GenericEntityException
    {
        MockProject project = new MockProject(12345L, "KEY", "Name");
        MockGenericValue scheme = new MockGenericValue("WorkflowScheme");

        MockTaskDescriptor<WorkflowMigrationResult> descriptor = new MockTaskDescriptor<WorkflowMigrationResult>();
        descriptor.setTaskId(38439L);

        WorkflowMigrationHelper migrationHelper = mock(WorkflowMigrationHelper.class);
        stub(migrationHelper.doQuickMigrate()).toReturn(false);
        stub(migrationHelperFactory.createMigrationHelper(project.getGenericValue(), scheme)).toReturn(migrationHelper);
        long id = 123456L;
        stub(workflowSchemeManager.getScheme(id)).toReturn(scheme);
        stub(migrationHelper.migrateAsync()).toReturn(descriptor);

        ServiceOutcome<Long> serviceOutcome = dispatcher.migrateWorkflow(project, id);
        assertTrue(serviceOutcome.isValid());
        verify(migrationHelper, times(1)).migrateAsync();
        assertEquals(descriptor.getTaskId(), serviceOutcome.getReturnedValue());
    }

    @Test
    public void testMigrateWorkflowFailed() throws GenericEntityException
    {
        MockProject project = new MockProject(12345L, "KEY", "Name");
        MockGenericValue scheme = new MockGenericValue("WorkflowScheme");
        WorkflowMigrationHelper migrationHelper = mock(WorkflowMigrationHelper.class);
        stub(migrationHelper.doQuickMigrate()).toThrow(new GenericEntityException());
        stub(migrationHelperFactory.createMigrationHelper(project.getGenericValue(), scheme)).toReturn(migrationHelper);
        long id = 123456L;
        stub(workflowSchemeManager.getScheme(id)).toReturn(scheme);

        ServiceOutcome<?> serviceOutcome = dispatcher.migrateWorkflow(project, id);
        verify(migrationHelper, never()).migrateAsync();
        assertErrorMessages(makeTranslation("admin.project.workflow.scheme.unable.to.migrate.workflow.scheme"), serviceOutcome);
    }

    private static void assertErrorMessages(String expectedError, ServiceOutcome<?> serviceOutcome)
    {
        assertFalse(serviceOutcome.isValid());
        assertTrue(serviceOutcome.getErrorCollection().getErrors().isEmpty());
        assertTrue(serviceOutcome.getErrorCollection().getErrorMessages().contains(expectedError));
    }

    private void checkWorkflowName(String expected, String projectName, String projectKey)
    {
        checkWorkflowName(expected, projectName, projectKey, Collections.<String>emptyList());
    }

    private void checkWorkflowName(String expected, String projectName, String projectKey, Collection<String> existingWorkflows)
    {
        final long projectId = 462873L;

        final MockProject project = new MockProject(projectId, projectKey, projectName);
        stub(workflowService.copyWorkflow(any(JiraServiceContext.class), eq(expected), Mockito.<String>eq(null),
                eq(jiraDefaultWorkflow))).toReturn(jiraWorkflow);
        for (String name : existingWorkflows)
        {
            doAnswer(new Answer<Void>()
            {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable
                {
                    final Object[] arguments = invocation.getArguments();
                    final JiraServiceContext argument = (JiraServiceContext)arguments[0];
                    argument.getErrorCollection().addErrorMessage("This is an error and you ....");
                    return null;
                }
            }).when(workflowService).validateCopyWorkflow(any(JiraServiceContext.class), eq(name));
        }
        final ServiceOutcome<String> outcome = dispatcher.copySystemWorkflow(project);

        assertTrue(outcome.isValid());
        assertEquals(jiraWorkflow.getName(), outcome.getReturnedValue());

        ArgumentCaptor<JiraServiceContext> argument = ArgumentCaptor.forClass(JiraServiceContext.class);
        verify(workflowService).copyWorkflow(argument.capture(), eq(expected), Mockito.<String>eq(null), eq(jiraDefaultWorkflow));
        assertEquals(user, argument.getValue().getLoggedInUser());
    }

    private void checkWorkflowSchemeName(String expected, String projectName, String projectKey)
    {
        checkWorkflowSchemeName(expected, projectName, projectKey, Collections.<String>emptyList());
    }

    private void checkWorkflowSchemeName(String expected, String projectName, String projectKey, Collection<String> existingSchemes)
    {
        final long id = 647326438L;
        stub(workflowSchemeManager.createSchemeAndEntities(any(Scheme.class))).toAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation)
            {
                Scheme o = (Scheme)invocation.getArguments()[0];
                return new Scheme(id, o.getType(), o.getName(), o.getDescription(), o.getEntities());
            }
        });

        for (String existing : existingSchemes)
        {
            try
            {
                stub(workflowSchemeManager.schemeExists(existing)).toReturn(true);
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException();
            }
        }

        ServiceOutcome<Long> serviceOutcome = dispatcher.createNewSchemeFromWorkflow("name", new MockProject(18181L, projectKey, projectName));

        assertTrue(serviceOutcome.isValid());
        assertEquals(id, (long)serviceOutcome.getReturnedValue());

        ArgumentCaptor<Scheme> argument = ArgumentCaptor.forClass(Scheme.class);
        verify(workflowSchemeManager).createSchemeAndEntities(argument.capture());

        Scheme createdScheme = argument.getValue();
        assertEquals(expected, createdScheme.getName());
        assertEquals(Collections.singletonList(new SchemeEntity("name", "0")), createdScheme.getEntities());
    }

    private static String getWorkflowSchemeName(String name)
    {
        return makeTranslation("admin.project.workflow.scheme.name.template", name);
    }

    private static String getWorkflowSchemeName(String name, int id)
    {
        return makeTranslation("admin.project.workflow.scheme.name.template.with.number", name, id);
    }

    private static String getWorkflowSchemeName(int id)
    {
        return makeTranslation("admin.project.workflow.scheme.name.template.generic", id);
    }
}
