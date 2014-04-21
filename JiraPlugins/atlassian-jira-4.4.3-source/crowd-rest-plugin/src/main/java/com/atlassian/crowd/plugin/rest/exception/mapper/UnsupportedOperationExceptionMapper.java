package com.atlassian.crowd.plugin.rest.exception.mapper;

import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.plugin.rest.entity.ErrorEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps an {@link UnsupportedOperationException} to a {@link javax.ws.rs.core.Response}.
 *
 * @since v2.1
 */
@Provider
public class UnsupportedOperationExceptionMapper  implements ExceptionMapper<UnsupportedOperationException>
{
    public Response toResponse(final UnsupportedOperationException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorEntity.of(exception)).build();
    }
}
