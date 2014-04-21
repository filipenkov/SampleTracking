package com.atlassian.upm.rest.async;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.notification.cache.NotificationCacheUpdater;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask.Representation;
import com.atlassian.upm.rest.async.AsynchronousTaskManager.TaskFuture;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.Sys.isUpmDebugModeEnabled;
import static com.atlassian.upm.rest.MediaTypes.PENDING_TASKS_COLLECTION_JSON;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.status;

@Path("/pending")
public class AsynchronousTaskResource
{
    private final AsynchronousTaskManager taskManager;
    private final UpmUriBuilder uriBuilder;
    private final PermissionEnforcer permissionEnforcer;
    private final UserManager userManager;
    private final NotificationCacheUpdater notificationCacheUpdater;

    public AsynchronousTaskResource(AsynchronousTaskManager taskManager, UpmUriBuilder upmUriBuilder,
        PermissionEnforcer permissionEnforcer, UserManager userManager,
        NotificationCacheUpdater notificationCacheUpdater)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.taskManager = checkNotNull(taskManager, "taskManager");
        this.uriBuilder = checkNotNull(upmUriBuilder, "upmUriBuilder");
        this.userManager = checkNotNull(userManager, "userManager");
        this.notificationCacheUpdater = checkNotNull(notificationCacheUpdater, "notificationCacheUpdater");
    }

    @GET
    @Produces(PENDING_TASKS_COLLECTION_JSON)
    public Response getTasksCollection()
    {
        // Lets at least ensure that they are an admin
        permissionEnforcer.enforceAdmin();

        // UPM-983 - it does not make too much sense to stop admin users from seeing what tasks are currently
        // executing, we will just enforce the permission checks when submitting the tasks.
        AsynchronousTaskCollectionRepresentation result =
            new AsynchronousTaskCollectionRepresentation(taskManager.getTasks(), uriBuilder);

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("{taskId}")
    public Response getTask(@PathParam("taskId") String taskId)
    {
        // Lets at least ensure that they are an admin
        permissionEnforcer.enforceAdmin();

        TaskFuture<? extends TaskStatus> task = taskManager.getTaskFuture(taskId);
        if (task == null)
        {
            return status(NOT_FOUND).build();
        }
        Representation<? extends TaskStatus> representation = task.getRepresentation();
        if (representation.getStatus().isDone())
        {
            //in case this was a plugin install/update, let's update the cached notification value
            notificationCacheUpdater.updatePluginUpdateNotification();
            
            return done(task, representation);
        }
        else
        {
            return ok(representation);
        }
    }

    @POST
    public Response createCancellableTask()
    {
        permissionEnforcer.enforceAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return status(PRECONDITION_FAILED).build();
        }
        CancellableAsynchronousTask task = new CancellableAsynchronousTask(userManager.getRemoteUsername());
        return taskManager.executeAsynchronousTask(task);
    }

    @DELETE
    @Path("{taskId}")
    public Response cancelCancellableTask(@PathParam("taskId") String taskId)
    {
        permissionEnforcer.enforceAdmin();
        if (!isUpmDebugModeEnabled())
        {
            return status(PRECONDITION_FAILED).build();
        }
        TaskFuture<? extends TaskStatus> task = taskManager.getTaskFuture(taskId);
        if (task == null)
        {
            return status(NOT_FOUND).build();
        }
        Representation<? extends TaskStatus> representation = task.getRepresentation();
        TaskStatus status = representation.getStatus();
        if (!(status instanceof CancellableTaskStatus))
        {
            return status(PRECONDITION_FAILED).build();
        }
        else
        {
            CancellableTaskStatus cancellable = (CancellableTaskStatus) status;
            cancellable.cancel();
            return status(OK).build();
        }
    }

    private Response done(TaskFuture<? extends TaskStatus> task, Representation<? extends TaskStatus> representation)
    {
        try
        {
            URI uri = task.get();
            if (uri == null)
            {
                return ok(representation);
            }
            else
            {
                return Response.seeOther(uriBuilder.makeAbsolute(task.get())).build();
            }
        }
        catch (InterruptedException e)
        {
            // not sure what the best thing to do here is, so I'll just return the representation for now
            return ok(representation);
        }
        catch (ExecutionException e)
        {
            return ok(representation);
        }
    }

    private Response ok(Representation<? extends TaskStatus> representation)
    {
        return Response.status(representation.getStatus().getStatusCode())
            .entity(representation)
            .type(representation.getContentType()).build();
    }

    public static class AsynchronousTaskCollectionRepresentation
    {
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final Collection<AsynchronousTask.Representation<? extends TaskStatus>> tasks;

        @JsonCreator
        public AsynchronousTaskCollectionRepresentation(@JsonProperty("links") Map<String, URI> links, @JsonProperty("tasks") Collection<AsynchronousTask.Representation<? extends TaskStatus>> tasks)
        {
            this.links = ImmutableMap.copyOf(links);
            this.tasks = ImmutableList.copyOf(tasks);
        }

        AsynchronousTaskCollectionRepresentation(Map<String, AsynchronousTask<? extends TaskStatus>> tasks, UpmUriBuilder uriBuilder)
        {
            this.tasks = Maps.transformValues(tasks, toRepresentation(uriBuilder)).values();
            this.links = ImmutableMap.of("self", uriBuilder.buildPendingTasksUri());
        }

        public URI getSelf()
        {
            return links.get("self");
        }

        public Collection<AsynchronousTask.Representation<? extends TaskStatus>> getTasks()
        {
            return tasks;
        }

        private Function<AsynchronousTask<? extends TaskStatus>, AsynchronousTask.Representation<? extends TaskStatus>> toRepresentation(final UpmUriBuilder uriBuilder)
        {
            return new Function<AsynchronousTask<? extends TaskStatus>, AsynchronousTask.Representation<? extends TaskStatus>>()
            {
                public AsynchronousTask.Representation<? extends TaskStatus> apply(AsynchronousTask<? extends TaskStatus> from)
                {
                    return from.getRepresentation(uriBuilder);
                }
            };
        }

        ;
    }
}
