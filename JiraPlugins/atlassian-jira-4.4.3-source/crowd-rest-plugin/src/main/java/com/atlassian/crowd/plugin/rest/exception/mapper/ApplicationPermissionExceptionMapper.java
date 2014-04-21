package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationPermissionExceptionMapper implements ExceptionMapper<ApplicationPermissionException>
{
    public Response toResponse(ApplicationPermissionException exception)
    {
        final ErrorEntity errorEntity = new ErrorEntity(ErrorEntity.ErrorReason.of(exception), ExceptionMapperUtil.stripNonValidXMLCharacters(exception.getMessage()));
        return Response.status(Response.Status.FORBIDDEN).entity(errorEntity).build();
    }
}
