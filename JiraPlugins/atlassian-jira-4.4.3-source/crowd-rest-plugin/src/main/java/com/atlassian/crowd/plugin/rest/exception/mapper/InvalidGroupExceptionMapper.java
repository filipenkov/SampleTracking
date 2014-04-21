package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidGroupExceptionMapper implements ExceptionMapper<InvalidGroupException>
{
    public Response toResponse(InvalidGroupException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorEntity.of(exception)).build();
    }
}
