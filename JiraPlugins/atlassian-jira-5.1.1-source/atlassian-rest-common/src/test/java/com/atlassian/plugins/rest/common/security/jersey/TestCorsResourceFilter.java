package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.plugins.rest.common.security.CorsPreflightCheckCompleteException;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaultsModuleDescriptor;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith (MockitoJUnitRunner.class)
public class TestCorsResourceFilter
{
    @Mock
    private CorsDefaults corsDefaults;

    @Mock
    private CorsDefaults corsDefaults2;

    private CorsResourceFilter corsResourceFilter;
    @Mock
    private ContainerRequest request;
    private Map<String,Object> requestProps;
    @Mock
    private ContainerResponse response;
    @Mock
    private PluginModuleTracker<CorsDefaults, CorsDefaultsModuleDescriptor> tracker;

    @Before
    public void setUp()
    {
        when(tracker.getModules()).thenReturn(newHashSet(corsDefaults));
        when(response.getResponse()).thenReturn(Response.ok().build());
        requestProps = newHashMap();
        when(request.getProperties()).thenReturn(requestProps);
        corsResourceFilter = new CorsResourceFilter(tracker, "GET");
    }

    @Test
    public void testSimplePreflightForGet()
    {
        String origin = "http://localhost";
        requestProps.put("Cors-Preflight-Requested", "true");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(request.getHeaderValue("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        MultivaluedMap<String, Object> headers = execPreflight();
        assertEquals(headers.getFirst("Access-Control-Allow-Origin"), origin);
    }

    @Test
    public void testPreflightSucceedsWhenOneCorsDefaultsAllowsOrigin()
    {
        String origin = "http://localhost";
        MultivaluedMap<String, Object> headers = execPreflightWithTwoCorsDefaults(origin);
        assertEquals(headers.getFirst("Access-Control-Allow-Origin"), origin);
    }

    @Test
    public void testSecondCorsDefaultsIsNotHitIfDoesntAllowOrigin()
    {
        String origin = "http://localhost";
        MultivaluedMap<String, Object> headers = execPreflightWithTwoCorsDefaults(origin);
        verify(corsDefaults2, never()).allowsCredentials(Matchers.<String>any());
        verify(corsDefaults2, never()).getAllowedRequestHeaders(Matchers.<String>any());
        verify(corsDefaults2, never()).getAllowedResponseHeaders(Matchers.<String>any());
    }

    @Test
    public void testSimplePreflightForGetWrongDomain()
    {
        String origin = "http://localhost";
        requestProps.put("Cors-Preflight-Requested", "true");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(false);
        when(request.getHeaderValue("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        execBadPreflight();
    }

    @Test
    public void testSimplePreflightForGetWrongMethod()
    {
        String origin = "http://localhost";
        requestProps.put("Cors-Preflight-Requested", "true");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(false);
        when(request.getHeaderValue("Access-Control-Request-Method")).thenReturn("POST");
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        execBadPreflight();
    }

    @Test
    public void testSimplePreflightForGetWrongHeaders()
    {
        String origin = "http://localhost";
        requestProps.put("Cors-Preflight-Requested", "true");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(corsDefaults.getAllowedRequestHeaders(origin)).thenReturn(ImmutableSet.<String>of("Foo-Header"));
        when(request.getHeaderValue("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getRequestHeader("Access-Control-Request-Headers")).thenReturn(Arrays.asList("Bar-Header"));
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        execBadPreflight();
    }

    @Test
    public void testSimpleGet()
    {
        String origin = "http://localhost";
        when(request.getMethod()).thenReturn("GET");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        MultivaluedMap<String, Object> headers = execNoPreflightWithHeaders();
        assertEquals(headers.getFirst("Access-Control-Allow-Origin"), origin);
    }

    @Test
    public void testSimpleGetWhenOneCorsDefaultsAllowsOrigin()
    {
        String origin = "http://localhost";
        MultivaluedMap<String, Object> headers = execNoPreflightWithHeadersForTwoCorsDefaults(origin);
        assertEquals(headers.getFirst("Access-Control-Allow-Origin"), origin);
    }

    @Test
    public void testSecondCorsDefaultIsNotCalledWhenItDoesntAllowOrigin()
    {
        String origin = "http://localhost";
        execNoPreflightWithHeadersForTwoCorsDefaults(origin);
        verify(corsDefaults2, never()).allowsCredentials(Matchers.<String>any());
        verify(corsDefaults2, never()).getAllowedRequestHeaders(Matchers.<String>any());
        verify(corsDefaults2, never()).getAllowedResponseHeaders(Matchers.<String>any());
    }

    @Test
    public void testSimpleGetWrongOrigin()
    {
        String origin = "http://localhost";
        when(request.getMethod()).thenReturn("GET");
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(request.getHeaderValue("Origin")).thenReturn("http://foo.com");
        execNoPreflightNoHeaders();
    }

    @Test
    public void testSimpleGetNoOrigin()
    {
        when(request.getMethod()).thenReturn("GET");
        execNoPreflightNoHeaders();
    }

    private MultivaluedMap<String, Object> execPreflight()
    {
        try
        {
            corsResourceFilter.filter(request);
            fail("Should have thrown preflight exception");
            return null;
        }
        catch (CorsPreflightCheckCompleteException ex)
        {
            return ex.getResponse().getMetadata();
        }
    }

    private MultivaluedMap<String, Object> execNoPreflightWithHeaders()
    {
        try
        {
            corsResourceFilter.filter(request);
            ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
            corsResourceFilter.filter(request, response);
            verify(response).setResponse(argument.capture());
            return argument.getValue().getMetadata();
        }
        catch (CorsPreflightCheckCompleteException ex)
        {
            fail("Shouldn't have thrown preflight exception");
            return null;
        }
    }

    private void execNoPreflightNoHeaders()
    {
        try
        {
            corsResourceFilter.filter(request);
            ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
            corsResourceFilter.filter(request, response);
            verify(response, never()).setResponse(argument.capture());
        }
        catch (CorsPreflightCheckCompleteException ex)
        {
            fail("Shouldn't have thrown preflight exception");
        }
    }

    private CorsPreflightCheckCompleteException execBadPreflight()
    {
        try
        {
            corsResourceFilter.filter(request);
            corsResourceFilter.filter(request, response);
            fail("Should have thrown preflight exception");
            return null;
        }
        catch (CorsPreflightCheckCompleteException ex)
        {
            return ex;
        }
    }

    private MultivaluedMap<String, Object> execPreflightWithTwoCorsDefaults(String origin)
    {
        requestProps.put("Cors-Preflight-Requested", "true");
        when(tracker.getModules()).thenReturn(newHashSet(corsDefaults, corsDefaults2));
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(corsDefaults2.allowsOrigin(origin)).thenReturn(false);
        when(request.getHeaderValue("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        MultivaluedMap<String, Object> headers = execPreflight();
        return headers;
    }

    private MultivaluedMap<String, Object> execNoPreflightWithHeadersForTwoCorsDefaults(String origin)
    {
        when(request.getMethod()).thenReturn("GET");
        when(tracker.getModules()).thenReturn(newHashSet(corsDefaults, corsDefaults2));
        when(corsDefaults.allowsOrigin(origin)).thenReturn(true);
        when(corsDefaults2.allowsOrigin(origin)).thenReturn(false);
        when(request.getHeaderValue("Origin")).thenReturn(origin);
        MultivaluedMap<String, Object> headers = execNoPreflightWithHeaders();
        return headers;
    }
}
