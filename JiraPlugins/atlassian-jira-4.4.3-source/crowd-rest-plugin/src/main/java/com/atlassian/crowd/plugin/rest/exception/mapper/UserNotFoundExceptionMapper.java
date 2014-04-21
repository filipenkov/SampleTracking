package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;
import com.atlassian.plugins.rest.common.Status;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException>
{
    public Response toResponse(UserNotFoundException exception)
    {
        return Response.status(Response.Status.NOT_FOUND).entity(ErrorEntity.of(exception)).build();
    }
}
