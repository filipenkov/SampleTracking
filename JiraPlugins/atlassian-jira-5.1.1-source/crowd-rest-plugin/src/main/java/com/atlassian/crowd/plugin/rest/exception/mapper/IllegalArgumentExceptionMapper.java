package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

/**
 * Maps an {@link IllegalArgumentException} to a {@link Response}.
 */
@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException>
{
    public Response toResponse(final IllegalArgumentException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorEntity.of(exception)).build();
    }
}
