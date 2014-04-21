package com.atlassian.plugins.rest.common.error.jersey;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import com.atlassian.plugins.rest.common.Status;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThrowableExceptionMapperTest
{
    @Test
    public void testSomeThrowable()
    {
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
        mapper.request = mock(Request.class);
        when(mapper.request.selectVariant(Mockito.<List<Variant>>anyObject())).thenReturn(
                new Variant(MediaType.TEXT_HTML_TYPE, null, null));

        RuntimeException e = new RuntimeException("foo");
        Response res = mapper.toResponse(e);
        assertEquals(res.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals("foo", ((Status)res.getEntity()).getMessage());
    }

    @Test
    public void testWebApplicationException()
    {
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
        WebApplicationException e = new WebApplicationException(444);
        Response res = mapper.toResponse(e);
        assertEquals(444, res.getStatus());
    }

    @Test
    public void getResponseAsHtml()
    {
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();
        Exception e = new Exception();
        mapper.request = mock(Request.class);
        when(mapper.request.selectVariant(Mockito.<List<Variant>>anyObject())).thenReturn(
                new Variant(MediaType.TEXT_HTML_TYPE, null, null));

        Response res = mapper.toResponse(e);
        assertEquals(Collections.singletonList(MediaType.TEXT_HTML_TYPE), res.getMetadata().get("Content-Type"));
        assertEquals(500, res.getStatus());
    }
}
