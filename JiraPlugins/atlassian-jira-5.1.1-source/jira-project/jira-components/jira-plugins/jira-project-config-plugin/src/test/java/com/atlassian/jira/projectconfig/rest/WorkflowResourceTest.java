package com.atlassian.jira.projectconfig.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.projectconfig.workflow.ProjectConfigWorkflowDispatcher;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.MockTaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.task.TaskProgressIndicator;
import com.atlassian.jira.task.TaskProgressListener;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationSuccess;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationTerminated;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;

/**
 *
 * @since v5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowResourceTest
{
    private static final String CACHE_CHECK = "Cache-Control";

    @Mock
    private ProjectConfigWorkflowDispatcher workflowDispatcher;

    @Mock
    private TaskManager taskManager;

    @Mock
    private PermissionManager permissionManager;

    private JiraAuthenticationContext authCtx;
    
    private WorkflowResource workflowResource;

    @Before
    public void setup()
    {
        authCtx = new MockSimpleAuthenticationContext(new MockUser("username"));
        workflowResource = new WorkflowResource(workflowDispatcher,taskManager, permissionManager, authCtx);
    }

    @Test
    public void testEditWorkflowDispatcherValid()
    {
        long projectId = 12345L;
        Pair<String, Long> pair = Pair.of("abc", 54321L);
        stub(workflowDispatcher.editWorkflow(projectId)).toReturn(ServiceOutcomeImpl.ok(pair));
        Response response = workflowResource.editWorkflowDispatcher(projectId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        WorkflowResource.Result actual = (WorkflowResource.Result)response.getEntity();
        assertEquals(pair.first(), actual.getWorkflowName());
        assertEquals(pair.second(), actual.getTaskId());
        assertResponseCacheNever(response);
    }

    @Test
    public void testEditWorkflowDispatcherInvalid()
    {
        long projectId = 12345L;
        String errorMessage = "error message";
        stub(workflowDispatcher.editWorkflow(projectId)).toReturn(ServiceOutcomeImpl.<Pair<String, Long>>error(errorMessage));
        Response response = workflowResource.editWorkflowDispatcher(projectId);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals(new ErrorCollection().addErrorMessage(errorMessage).reason(null), response.getEntity());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusNoPermission()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(false);
        Response response = workflowResource.getMigrationStatus(taskId);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusInvalidTask()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        stub(taskManager.getTask(taskId)).toReturn(null);
        Response response = workflowResource.getMigrationStatus(taskId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusFinished()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        WorkflowMigrationResult migrationResult = new WorkflowMigrationSuccess(Collections.<Long, String>emptyMap());
        taskDescriptor.setFinishedTime(new Date());
        taskDescriptor.setResult(migrationResult);
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        Response response = workflowResource.getMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new WorkflowResource.TaskStatus(migrationResult), response.getEntity());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusNotStarted()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        taskDescriptor.setTaskProgressIndicator(new TaskProgressIndicatorImpl(null));
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        Response response = workflowResource.getMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new WorkflowResource.TaskStatus(0L), response.getEntity());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusInProgress()
    {
        long taskId = 12345L;
        long progress = 50L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        taskDescriptor.setTaskProgressIndicator(new TaskProgressIndicatorImpl(new TaskProgressEvent(123L, 0, progress, null, null)));
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        Response response = workflowResource.getMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new WorkflowResource.TaskStatus(progress), response.getEntity());
        assertResponseCacheNever(response);
    }

    @Test
    public void testGetMigrationStatusWithException() throws ExecutionException, InterruptedException
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = spy(new MockTaskDescriptor<Object>());
        taskDescriptor.setFinishedTime(new Date());
        Throwable expectedException = new ExecutionException(new Exception());
        stub(taskDescriptor.getResult()).toThrow(expectedException);
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        try
        {
            workflowResource.getMigrationStatus(taskId);
            fail("Expecting exception but no exception thrown!");
        }
        catch (RuntimeException e)
        {
            assertEquals(expectedException, e.getCause());
        }
    }

    @Test
    public void testTaskStatusOnSuccess()
    {
        Map<Long, String> failedIssues = Collections.<Long, String>emptyMap();
        WorkflowMigrationResult result = new WorkflowMigrationSuccess(failedIssues);
        WorkflowResource.TaskStatus taskStatus = new WorkflowResource.TaskStatus(result);
        assertTrue(taskStatus.isFinished());
        assertTrue(taskStatus.isSuccessful());
        assertEquals(ErrorCollection.of(result.getErrorCollection()), taskStatus.getErrorCollection());
        assertEquals(result.getNumberOfFailedIssues(), taskStatus.getNumberOfFailedIssues().longValue());
        assertEquals(ImmutableList.copyOf(result.getFailedIssues().values()), taskStatus.getFailedIssues());
    }

    @Test
    public void testTaskStatusOnError()
    {
        Map<Long, String> failedIssues = ImmutableMap.of(1L, "string");
        WorkflowMigrationResult result = new WorkflowMigrationTerminated(failedIssues);
        WorkflowResource.TaskStatus taskStatus = new WorkflowResource.TaskStatus(result);
        assertTrue(taskStatus.isFinished());
        assertFalse(taskStatus.isSuccessful());
        assertEquals(ErrorCollection.of(result.getErrorCollection()), taskStatus.getErrorCollection());
        assertEquals(result.getNumberOfFailedIssues(), taskStatus.getNumberOfFailedIssues().longValue());
        assertEquals(ImmutableList.copyOf(result.getFailedIssues().values()), taskStatus.getFailedIssues());
        assertNull(taskStatus.getProgress());
    }

    @Test
    public void testTaskStatusInProgress()
    {
        long progress = 50L;
        WorkflowResource.TaskStatus taskStatus = new WorkflowResource.TaskStatus(progress);
        assertFalse(taskStatus.isFinished());
        assertEquals(progress, taskStatus.getProgress().longValue());
        assertNull(taskStatus.isSuccessful());
        assertNull(taskStatus.getErrorCollection());
        assertNull(taskStatus.getNumberOfFailedIssues());
        assertNull(taskStatus.getFailedIssues());
    }

    @Test
    public void testDeleteMigrationStatusNoPermission()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(false);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusInvalidTask()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        stub(taskManager.getTask(taskId)).toReturn(null);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusNotOwnerOfTask()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        User user = new MockUser("differentusername");
        taskDescriptor.setUser(user);
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusSuccess()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        User user = new MockUser("username");
        taskDescriptor.setUser(user);
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        stub(taskManager.removeTask(taskId)).toReturn(true);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusOwnerIsAnonymousUserIsLoggedIn()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        stub(taskManager.removeTask(taskId)).toReturn(true);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusOwnerIsAnonymousUserIsAnonymous()
    {
        long taskId = 12345L;
        JiraAuthenticationContext authCtx = new MockSimpleAuthenticationContext(null);
        WorkflowResource workflowResource = new WorkflowResource(workflowDispatcher, taskManager, permissionManager, authCtx);
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        stub(taskManager.removeTask(taskId)).toReturn(true);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    @Test
    public void testDeleteMigrationStatusRemoveFailed()
    {
        long taskId = 12345L;
        stub(permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser())).toReturn(true);
        MockTaskDescriptor<Object> taskDescriptor = new MockTaskDescriptor<Object>();
        stub(taskManager.getTask(taskId)).toReturn(taskDescriptor);
        stub(taskManager.removeTask(taskId)).toReturn(false);
        Response response = workflowResource.deleteMigrationStatus(taskId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertResponseCacheNever(response);
    }

    private static void assertCache(CacheControl control, Response response)
    {
        List<Object> object = response.getMetadata().get(CACHE_CHECK);
        assertNotNull("No cache control set.", object);
        assertFalse("No cache control set.", object.isEmpty());
        assertEquals("Unexpected cache control.", 1, object.size());
        assertEquals("Cache control is wrong.", control, object.get(0));
    }

    private static void assertResponseCacheNever(Response response)
    {
        assertCache(never(), response);
    }

    private static class TaskProgressIndicatorImpl implements TaskProgressIndicator
    {

        private TaskProgressEvent lastProgressEvent;

        public TaskProgressIndicatorImpl(TaskProgressEvent lastProgressEvent)
        {
            this.lastProgressEvent = lastProgressEvent;
        }

        @Override
        public void addListener(TaskProgressListener listener)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void removeListener(TaskProgressListener listener)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public TaskProgressEvent getLastProgressEvent()
        {
            return lastProgressEvent;
        }
    }
}
