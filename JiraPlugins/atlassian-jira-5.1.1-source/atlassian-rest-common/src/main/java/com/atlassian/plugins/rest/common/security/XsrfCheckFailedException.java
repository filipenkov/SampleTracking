package com.atlassian.plugins.rest.common.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Thrown when an XSRF check fails
 *
 * @since 2.4
 */
public class XsrfCheckFailedException extends WebApplicationException
{
    public XsrfCheckFailedException()
    {
        super(Response.status(Response.Status.NOT_FOUND).entity("XSRF check failed").build());
    }
}
