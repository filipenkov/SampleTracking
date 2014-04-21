package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationNotFoundExceptionMapper implements ExceptionMapper<ApplicationNotFoundException>
{
    public Response toResponse(ApplicationNotFoundException exception)
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorEntity.of(exception)).build();
    }
}
