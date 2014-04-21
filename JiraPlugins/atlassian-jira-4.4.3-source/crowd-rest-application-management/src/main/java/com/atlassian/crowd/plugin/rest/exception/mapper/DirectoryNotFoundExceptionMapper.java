package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.plugin.rest.entity.ApplicationErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DirectoryNotFoundExceptionMapper implements ExceptionMapper<DirectoryNotFoundException>
{
    public Response toResponse(DirectoryNotFoundException exception)
    {
        final ApplicationErrorEntity errorEntity = new ApplicationErrorEntity(ApplicationErrorEntity.ErrorReason.of(exception), exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(errorEntity).build();
    }
}
