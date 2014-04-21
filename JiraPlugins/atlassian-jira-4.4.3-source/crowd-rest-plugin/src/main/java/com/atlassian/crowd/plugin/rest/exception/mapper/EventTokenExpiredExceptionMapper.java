package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EventTokenExpiredExceptionMapper implements ExceptionMapper<EventTokenExpiredException>
{
    public Response toResponse(EventTokenExpiredException exception)
    {
        final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.GONE).entity(errorEntity).build();
    }
}
