package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MembershipNotFoundExceptionMapper implements ExceptionMapper<MembershipNotFoundException>
{
    public Response toResponse(MembershipNotFoundException exception)
    {
        return Response.status(Response.Status.NOT_FOUND).entity(ErrorEntity.of(exception)).build();
    }
}
