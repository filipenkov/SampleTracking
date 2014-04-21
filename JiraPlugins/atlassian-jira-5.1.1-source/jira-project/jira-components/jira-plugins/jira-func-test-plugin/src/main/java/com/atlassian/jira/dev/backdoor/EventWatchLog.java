package com.atlassian.jira.dev.backdoor;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @since v5.0
 */
@Produces ({ MediaType.APPLICATION_JSON})
@Path ("events")
public class EventWatchLog
{
    private final EventWatcher watcher;

    public EventWatchLog(final EventWatcher watcher)
    {
        this.watcher = watcher;
    }

    @GET
    @AnonymousAllowed
    public Response events()
    {
        return Response.ok(watcher.getEvents()).build();
    }
}
