package com.atlassian.plugins.rest.common.error.jersey;

import com.atlassian.plugins.rest.common.Status;
import junit.framework.TestCase;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 */
public class TestThrowableExceptionMapper extends TestCase
{
    public void testSomeThrowable()
    {
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
        RuntimeException e = new RuntimeException("foo");
        Response res = mapper.toResponse(e);
        assertEquals(res.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals("foo", ((Status)res.getEntity()).getMessage());
    }
    public void testWebApplicationException()
    {
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
        WebApplicationException e = new WebApplicationException(444);
        Response res = mapper.toResponse(e);
        assertEquals(res.getStatus(), 444);
    }
}
