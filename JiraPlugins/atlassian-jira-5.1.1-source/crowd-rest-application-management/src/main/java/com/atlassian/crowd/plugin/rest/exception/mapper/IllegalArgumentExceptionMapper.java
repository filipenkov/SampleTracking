package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.plugin.rest.entity.ApplicationErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an {@link IllegalArgumentException} to a {@link javax.ws.rs.core.Response}.
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException>
{
    public Response toResponse(final IllegalArgumentException exception)
    {
        final ApplicationErrorEntity errorEntity = new ApplicationErrorEntity(ApplicationErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
    }
}
