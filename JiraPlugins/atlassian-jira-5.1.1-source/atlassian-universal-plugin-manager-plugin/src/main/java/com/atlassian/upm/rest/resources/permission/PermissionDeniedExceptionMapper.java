package com.atlassian.upm.rest.resources.permission;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.atlassian.upm.rest.MediaTypes;


@Provider
public class PermissionDeniedExceptionMapper implements ExceptionMapper<PermissionDeniedException>
{
    public Response toResponse(PermissionDeniedException exception)
    {
        return Response.status(Response.Status.UNAUTHORIZED) //401
            .entity("Must have permission to access this resource.")
            .type(MediaTypes.ERROR_JSON)
            .build();
    }
}
