package com.atlassian.upm.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException>
{
    public Response toResponse(WebApplicationException exception)
    {
        Response r = exception.getResponse();
        if (r.getStatus() >= 500 && r.getEntity() == null)
        {
            // Write out the exception to a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            pw.flush();

            r = Response.status(r.getStatus()).entity(sw.toString()).
                type("text/plain").build();
        }
        return r;
    }
}
