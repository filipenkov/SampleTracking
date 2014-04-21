package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.plugin.rest.entity.ApplicationErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationNotFoundExceptionMapper implements ExceptionMapper<ApplicationNotFoundException>
{
    public Response toResponse(ApplicationNotFoundException exception)
    {
        final ApplicationErrorEntity errorEntity = new ApplicationErrorEntity(ApplicationErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND).entity(errorEntity).build();
    }
}
