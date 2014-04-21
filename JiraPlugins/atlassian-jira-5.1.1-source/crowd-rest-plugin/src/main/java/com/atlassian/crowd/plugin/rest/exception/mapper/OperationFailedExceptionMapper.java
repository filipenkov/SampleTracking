package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;
import com.atlassian.plugins.rest.common.Status;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class OperationFailedExceptionMapper implements ExceptionMapper<OperationFailedException>
{
    public Response toResponse(OperationFailedException exception)
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ErrorEntity.of(exception)).build();
    }
}
