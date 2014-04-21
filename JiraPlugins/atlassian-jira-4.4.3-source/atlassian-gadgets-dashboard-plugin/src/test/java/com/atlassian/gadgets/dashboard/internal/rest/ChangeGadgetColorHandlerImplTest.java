package com.atlassian.gadgets.dashboard.internal.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetNotFoundException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.sal.api.message.I18nResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangeGadgetColorHandlerImplTest
{
    @Mock private DashboardRepository repository;
    @Mock private I18nResolver i18n;
    private GadgetRequestContext gadgetRequestContext;
    ChangeGadgetColorHandler handler;

    @Before
    public void setUp()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        handler = new ChangeGadgetColorHandlerImpl(repository, i18n);
    }

    @Test
    public void assertThatNoContentStatusCodeIsSentOnSuccess() throws Exception
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);

        Response response = handler.setGadgetColor(dashboardId,
                gadgetRequestContext, gadgetId, Color.color1);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).changeGadgetColor(gadgetId, Color.color1);
        verify(repository).save(dashboard);
        assertThat(response.getStatus(), is(equalTo(Response.Status.NO_CONTENT.getStatusCode())));
    }

    @Test(expected=DashboardNotFoundException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatHandlerReturnsImmediatelyIfDashboardNotFound() throws DashboardNotFoundException, IOException, ServletException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).
                thenThrow(new DashboardNotFoundException(dashboardId));

        try
        {
            handler.setGadgetColor(dashboardId, gadgetRequestContext, GadgetId.valueOf("1001"), Color.color1);
        }
        finally
        {
            verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test(expected=GadgetNotFoundException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatHandlerReturnsImmediatelyIfGadgetIsNotFound() throws Exception
    {
        GadgetId gadgetId = GadgetId.valueOf("1001");
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        Color color = Color.color1;

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        doThrow(new GadgetNotFoundException(gadgetId)).when(dashboard).changeGadgetColor(gadgetId, Color.color1);

        try
        {
            handler.setGadgetColor(dashboardId, gadgetRequestContext, gadgetId, color);
        }
        finally
        {
            verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInconsistentDashboardStateExceptionReturnsConflict()
    {
        GadgetId gadgetId = GadgetId.valueOf("1001");
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        Color color = Color.color1;

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        doThrow(new InconsistentDashboardStateException("")).when(repository).save(dashboard);

        Response response = handler.setGadgetColor(dashboardId, gadgetRequestContext, gadgetId, color);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
    }
}
