package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.plugin.rest.entity.ApplicationErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an {@link ApplicationManagerException} to a {@link javax.ws.rs.core.Response}.
 */
@Provider
public class ApplicationManagerExceptionMapper implements ExceptionMapper<ApplicationManagerException>
{
    public Response toResponse(final ApplicationManagerException exception)
    {
        final ApplicationErrorEntity errorEntity = new ApplicationErrorEntity(ApplicationErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorEntity).build();
    }
}
