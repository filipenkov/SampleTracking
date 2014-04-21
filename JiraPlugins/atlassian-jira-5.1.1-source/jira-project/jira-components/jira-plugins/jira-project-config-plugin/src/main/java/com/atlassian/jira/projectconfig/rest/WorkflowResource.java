package com.atlassian.jira.projectconfig.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.projectconfig.workflow.ProjectConfigWorkflowDispatcher;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.api.util.ErrorCollection.of;

/**
 * Called by the Project Config Workflow Panel to create and associate a new workflow with a project if that project is
 * using the default workflow scheme.
 *
 * @since v5.1
 */
@Path ("workflow")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class WorkflowResource
{
    private final ProjectConfigWorkflowDispatcher wfDispatcher;
    private final TaskManager taskManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authCtx;

    public WorkflowResource(ProjectConfigWorkflowDispatcher wfDispatcher, TaskManager taskManager,
            PermissionManager permissionManager, JiraAuthenticationContext authCtx)
    {
        this.wfDispatcher = wfDispatcher;
        this.taskManager = taskManager;
        this.permissionManager = permissionManager;
        this.authCtx = authCtx;
    }

    @POST
    @WebSudoRequired
    public Response editWorkflowDispatcher(long projectId)
    {
        final ServiceOutcome<Pair<String, Long>> outcome = wfDispatcher.editWorkflow(projectId);
        if (outcome.isValid())
        {
            Pair<String, Long> pair = outcome.getReturnedValue();
            return Response.ok(new Result(pair.first(), pair.second())).cacheControl(never()).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).entity(of(outcome.getErrorCollection()))
                    .cacheControl(never()).build();
        }
    }

    @GET
    @Path ("{id}")
    public Response getMigrationStatus(@PathParam ("id") long id)
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser()))
        {
            return forbidden();
        }

        TaskDescriptor<WorkflowMigrationResult> task = taskManager.getTask(id);
        if (task == null)
        {
            return notFound();
        }

        if (task.isFinished())
        {
            try
            {
                TaskStatus taskStatus = new TaskStatus(task.getResult());
                return Response.ok(taskStatus).cacheControl(never()).build();
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }
            catch (InterruptedException shouldNeverOccur)
            {
                throw new RuntimeException(shouldNeverOccur);
            }
        }
        else
        {
            TaskProgressEvent event = task.getTaskProgressIndicator().getLastProgressEvent();
            long progress = event == null ? 0 : event.getTaskProgress();
            TaskStatus taskStatus = new TaskStatus(progress);
            return Response.ok(taskStatus).cacheControl(never()).build();
        }
    }

    @DELETE
    @Path ("{id}")
    public Response deleteMigrationStatus(@PathParam ("id") long id)
    {
        User user = authCtx.getLoggedInUser();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return forbidden();
        }

        TaskDescriptor<?> task = taskManager.getTask(id);
        if (task == null)
        {
            return notFound();
        }

        User taskOwner = task.getUser();
        if (taskOwner != null && !taskOwner.equals(user))
        {
            return forbidden();
        }

        if (taskManager.removeTask(id))
        {
            return Response.ok().cacheControl(never()).build();
        }
        else
        {
            return notFound();
        }
    }

    private static Response notFound()
    {
        return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
    }

    private static Response forbidden()
    {
        return Response.status(Response.Status.FORBIDDEN).cacheControl(never()).build();
    }

    @JsonAutoDetect
    public static class Result
    {
        private final String workflowName;
        private final Long taskId;

        public Result(String workflowName, Long taskId)
        {
            this.workflowName = workflowName;
            this.taskId = taskId;
        }

        public String getWorkflowName()
        {
            return workflowName;
        }

        public Long getTaskId()
        {
            return taskId;
        }
    }

    @JsonAutoDetect
    public static class TaskStatus
    {
        private final boolean finished;
        private final Long progress;
        private final Boolean successful;
        private final ErrorCollection errorCollection;
        private final Integer numberOfFailedIssues;
        private final List<String> failedIssues;

        public TaskStatus(WorkflowMigrationResult result)
        {
            finished = true;
            progress = null;
            successful = result.getResult() == WorkflowMigrationResult.SUCCESS;
            errorCollection = of(result.getErrorCollection());
            numberOfFailedIssues = result.getNumberOfFailedIssues();
            failedIssues = ImmutableList.copyOf(result.getFailedIssues().values());
        }

        public TaskStatus(long progress)
        {
            finished = false;
            this.progress = progress;
            successful = null;
            errorCollection = null;
            numberOfFailedIssues = null;
            failedIssues = null;
        }

        public Boolean isSuccessful()
        {
            return successful;
        }

        public ErrorCollection getErrorCollection()
        {
            return errorCollection;
        }

        public Integer getNumberOfFailedIssues()
        {
            return numberOfFailedIssues;
        }

        public List<String> getFailedIssues()
        {
            return failedIssues;
        }

        public boolean isFinished()
        {
            return finished;
        }

        public Long getProgress()
        {
            return progress;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TaskStatus that = (TaskStatus) o;

            if (finished != that.finished) { return false; }
            if (errorCollection != null ? !errorCollection.equals(that.errorCollection) : that.errorCollection != null)
            { return false; }
            if (failedIssues != null ? !failedIssues.equals(that.failedIssues) : that.failedIssues != null)
            {
                return false;
            }
            if (numberOfFailedIssues != null ? !numberOfFailedIssues.equals(that.numberOfFailedIssues) : that.numberOfFailedIssues != null)
            { return false; }
            if (progress != null ? !progress.equals(that.progress) : that.progress != null) { return false; }
            if (successful != null ? !successful.equals(that.successful) : that.successful != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = (finished ? 1 : 0);
            result = 31 * result + (progress != null ? progress.hashCode() : 0);
            result = 31 * result + (successful != null ? successful.hashCode() : 0);
            result = 31 * result + (errorCollection != null ? errorCollection.hashCode() : 0);
            result = 31 * result + (numberOfFailedIssues != null ? numberOfFailedIssues.hashCode() : 0);
            result = 31 * result + (failedIssues != null ? failedIssues.hashCode() : 0);
            return result;
        }
    }
}
