package com.atlassian.plugins.rest.common.error.jersey;

import com.atlassian.plugins.rest.common.Status;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * A generic exception mapper that will map any {@link Throwable throwable}.  Handles the special case of
 * {@link WebApplicationException}, which provides its own response.
 *
 * @since 1.0
 */
@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
    public Response toResponse(Throwable t)
    {
        if (t instanceof WebApplicationException)
        {
            return ((WebApplicationException)t).getResponse();
        }
        else
        {
            return Status.error().message(t.getMessage()).response();
        }

    }
}
