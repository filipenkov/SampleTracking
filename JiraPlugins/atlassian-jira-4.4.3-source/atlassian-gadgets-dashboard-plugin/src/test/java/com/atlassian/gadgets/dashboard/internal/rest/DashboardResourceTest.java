package com.atlassian.gadgets.dashboard.internal.rest;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.sal.api.message.I18nResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class DashboardResourceTest
{
    @Mock private DashboardPermissionService permissionService;
    @Mock private GadgetRequestContextFactory gadgetRequestContextFactory;
    @Mock private AddGadgetHandler addGadgetHandler;
    @Mock private ChangeLayoutHandler changeLayoutHandler;
    @Mock private DashboardRepository repository;
    @Mock private I18nResolver i18n;

    private DashboardResource resource;

    @Before
    public void setup()
    {
        when(gadgetRequestContextFactory.get(isA(HttpServletRequest.class)))
                .thenReturn(gadgetRequestContext().viewer("user").build());
        resource = new DashboardResource(permissionService, gadgetRequestContextFactory, addGadgetHandler,
                changeLayoutHandler, repository, null, i18n);
    }

    @Test
    public void addGadgetReturnsBadRequestOnMissingUrlParameter()
    {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Reader jsonContent = new StringReader("");

        Response response = resource.addGadget(DashboardId.valueOf("100"), mockRequest, jsonContent);

        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
        verifyZeroInteractions(changeLayoutHandler);
    }

    @Test
    public void addGadgetReturnsServerErrorOnJsonError()
    {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Reader brokenReader = new Reader()
        {
            @Override
            public int read(char[] chars, int i, int i1) throws IOException
            {
                throw new IOException();
            }

            @Override
            public void close() throws IOException
            {
                throw new IOException();
            }
        };

        Response response = resource.addGadget(DashboardId.valueOf("100"), mockRequest, brokenReader);

        assertThat(response.getStatus(), is(equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
        verifyZeroInteractions(changeLayoutHandler);
    }

    @Test
    public void addGadgetReturnsUnauthorizedIfDashboardIsNotWritable()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(eq(dashboardId), anyString())).thenReturn(false);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Reader json = new StringReader("{url:'http://example.com/url/gadget.xml'}");

        Response response = resource.addGadget(DashboardId.valueOf("100"), mockRequest, json);

        assertThat(response.getStatus(), is(equalTo(Response.Status.UNAUTHORIZED.getStatusCode())));
        verifyZeroInteractions(addGadgetHandler);
    }

    @Test
    public void changeLayoutReturnsUnauthorizedIfDashboardIsNotWritable()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(eq(dashboardId), anyString())).thenReturn(false);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        Response response = resource.changeLayoutViaPUT(DashboardId.valueOf("100"), mockRequest, "{layout:'A'}");

        assertThat(response.getStatus(), is(equalTo(Response.Status.UNAUTHORIZED.getStatusCode())));
        verifyZeroInteractions(changeLayoutHandler);
    }
}
