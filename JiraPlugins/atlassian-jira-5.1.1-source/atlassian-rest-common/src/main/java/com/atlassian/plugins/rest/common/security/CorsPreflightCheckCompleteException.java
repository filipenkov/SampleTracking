package com.atlassian.plugins.rest.common.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Thrown when a Cors preflight check has completed and is ready to be returned to the browser
 *
 * @since 2.6
 */
public class CorsPreflightCheckCompleteException extends WebApplicationException
{
    public CorsPreflightCheckCompleteException(Response response)
    {
        super(response);
    }
}
