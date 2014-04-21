package com.atlassian.jira.dev.backdoor;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


/**
 * Allows access
 *
 * @since v4.3
 */
@Path ("log")
public class LogAccess
{
    private static final Logger log = Logger.getLogger(LogAccess.class);

    /**
     * This can be called to cause an error log message to be placed in the JIRA log
     * <p/>
     * invoked like this "/jira/rest/func-test/1.0/log/error?msg=hi"
     *
     * @param msg the message to go into the application log
     * @return nothing meaningful but GET requires it
     */
    @GET
    @AnonymousAllowed
    @Path ("error")
    public Response logMessage(@QueryParam ("msg") String msg)
    {
        log.error(msg);
        return Response.ok(null).build();
    }

}
