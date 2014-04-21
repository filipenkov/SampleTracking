package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.plugin.rest.entity.EventEntityList;
import com.atlassian.crowd.plugin.rest.service.controller.EventsController;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Crowd SSO Token Resource.
 */
@Path("event")
@AnonymousAllowed
@Produces({APPLICATION_XML, APPLICATION_JSON})
public class EventsResource extends AbstractResource
{
    private final EventsController eventsController;

    public EventsResource(EventsController eventsController)
    {
        this.eventsController = eventsController;
    }

    @GET
    public Response getEventCookie()
    {
        return Response.ok(eventsController.getCurrentEventToken(getApplicationName())).build();
    }

    @GET
    @Path("{eventToken}")
    public Response getEvents(@PathParam("eventToken") String eventToken) throws EventTokenExpiredException, IncrementalSynchronisationNotAvailableException, OperationFailedException
    {
        final EventEntityList events = eventsController.getEventsSince(getApplicationName(), eventToken, getBaseUri());
        return Response.ok(events).build();
    }
}
