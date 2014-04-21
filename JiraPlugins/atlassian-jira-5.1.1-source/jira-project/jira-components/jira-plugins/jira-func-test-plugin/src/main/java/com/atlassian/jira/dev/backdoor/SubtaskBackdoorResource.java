package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * Are you allowed to enable or disable subtasks.
 *
 * @since v5.0.1
 */
@Path ("subtask")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class SubtaskBackdoorResource
{
    private final SubTaskManager subTaskManager;

    public SubtaskBackdoorResource(SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }
    
    @GET
    public Response get()
    {
        return Response.ok(subTaskManager.isSubTasksEnabled()).cacheControl(never()).build(); 
    }

    @POST
    public Response set(Boolean enabled)
    {
        if (enabled != subTaskManager.isSubTasksEnabled())
        {
            if (enabled)
            {
                try
                {
                    subTaskManager.enableSubTasks();
                }
                catch (CreateException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else
            {
                subTaskManager.disableSubTasks();
            }
        }
        return Response.ok(subTaskManager.isSubTasksEnabled()).cacheControl(never()).build();
    }
}
