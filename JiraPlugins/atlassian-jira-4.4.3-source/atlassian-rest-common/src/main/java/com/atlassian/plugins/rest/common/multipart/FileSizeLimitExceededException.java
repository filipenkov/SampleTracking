package com.atlassian.plugins.rest.common.multipart;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Exception indicating the file size limit was exceeded
 *
 * @since 2.4
 */
public class FileSizeLimitExceededException extends WebApplicationException
{
    public FileSizeLimitExceededException(String message)
    {
        super(Response.status(Response.Status.NOT_FOUND).entity(message).build());
    }
}
