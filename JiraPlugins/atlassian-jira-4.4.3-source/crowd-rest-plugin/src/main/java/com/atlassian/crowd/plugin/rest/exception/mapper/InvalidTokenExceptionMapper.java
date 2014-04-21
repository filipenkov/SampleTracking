package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an {@link InvalidTokenException} to a 401 (Unauthorized) status.
 */
@Provider
public class InvalidTokenExceptionMapper implements ExceptionMapper<InvalidTokenException>
{
    public Response toResponse(InvalidTokenException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorEntity.of(exception)).build();
    }
}
