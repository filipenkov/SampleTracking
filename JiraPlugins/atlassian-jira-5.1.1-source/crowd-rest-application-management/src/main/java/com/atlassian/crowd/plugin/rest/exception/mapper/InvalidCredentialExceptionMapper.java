package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.plugin.rest.entity.ApplicationErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an {@link InvalidCredentialException} to a {@link javax.ws.rs.core.Response}.
 */
@Provider
public class InvalidCredentialExceptionMapper implements ExceptionMapper<InvalidCredentialException>
{
    public Response toResponse(final InvalidCredentialException exception)
    {
        final ApplicationErrorEntity errorEntity = new ApplicationErrorEntity(ApplicationErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
    }
}
