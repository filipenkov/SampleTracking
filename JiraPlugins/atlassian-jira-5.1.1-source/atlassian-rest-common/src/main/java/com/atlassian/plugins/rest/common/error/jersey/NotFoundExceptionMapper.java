package com.atlassian.plugins.rest.common.error.jersey;

import com.atlassian.plugins.rest.common.Status;
import com.sun.jersey.api.NotFoundException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * A generic exception mapper that will map {@link NotFoundException not found exceptions}.
 * @since 1.0
 */
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException>
{
    @Context
    Request request;

    public Response toResponse(NotFoundException exception)
    {
        return Status.notFound().message(exception.getMessage()).responseBuilder().type(Status.variantFor(request)).build();
    }
}
