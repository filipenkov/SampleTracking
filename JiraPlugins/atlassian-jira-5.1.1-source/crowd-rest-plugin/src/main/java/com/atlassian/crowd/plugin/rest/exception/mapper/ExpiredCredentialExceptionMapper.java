package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExpiredCredentialExceptionMapper implements ExceptionMapper<ExpiredCredentialException>
{
    public Response toResponse(ExpiredCredentialException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorEntity.of(exception)).build();
    }
}
