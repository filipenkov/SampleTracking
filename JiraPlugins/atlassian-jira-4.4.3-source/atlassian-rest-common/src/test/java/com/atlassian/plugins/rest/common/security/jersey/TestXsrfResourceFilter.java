package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;
import com.sun.jersey.spi.container.ContainerRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 */
@RunWith (MockitoJUnitRunner.class)
public class TestXsrfResourceFilter
{
    private XsrfResourceFilter xsrfResourceFilter;
    @Mock
    private ContainerRequest request;

    @Before
    public void setUp()
    {
        xsrfResourceFilter = new XsrfResourceFilter();
    }

    @Test(expected = XsrfCheckFailedException.class)
    public void testGetBlocked()
    {
        when(request.getMethod()).thenReturn("GET");
        xsrfResourceFilter.filter(request);
    }

    @Test
    public void testGetSuccess()
    {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderValue("X-Atlassian-Token")).thenReturn("nocheck");
        assertEquals(request, xsrfResourceFilter.filter(request));
    }

    @Test(expected = XsrfCheckFailedException.class)
    public void testPostBlocked()
    {
        when(request.getMethod()).thenReturn("POST");
        when(request.getMediaType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        xsrfResourceFilter.filter(request);
    }

    @Test
    public void testPostSuccess()
    {
        when(request.getMethod()).thenReturn("POST");
        when(request.getMediaType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        when(request.getHeaderValue("X-Atlassian-Token")).thenReturn("nocheck");
        assertEquals(request, xsrfResourceFilter.filter(request));
    }

    @Test
    public void testPostJsonSuccess()
    {
        when(request.getMethod()).thenReturn("POST");
        when(request.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        assertEquals(request, xsrfResourceFilter.filter(request));
    }

    @Test
    public void testPutFormSuccess()
    {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getMediaType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        assertEquals(request, xsrfResourceFilter.filter(request));
    }

}
