package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
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
public class DeleteGadgetHandlerImplTest
{
    @Mock private DashboardRepository repository;
    @Mock private I18nResolver i18n;
    @Mock private EventPublisher eventPublisher;
    private GadgetRequestContext gadgetRequestContext;

    DeleteGadgetHandler handler;

    @Before
    public void setup()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        handler = new DeleteGadgetHandlerImpl(repository, i18n, eventPublisher);
    }

    @Test
    public void assertThatNoContentStatusIsSentOnSuccess() throws Exception
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);

        Response response = handler.deleteGadget(dashboardId, gadgetRequestContext, gadgetId);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).removeGadget(GadgetId.valueOf("1001"));
        verify(repository).save(dashboard);
        verifyNoMoreInteractions(dashboard, repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.NO_CONTENT.getStatusCode())));
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInconsistentDashboardStateExceptionReturnsConflict()
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getId()).thenReturn(dashboardId);

        doThrow(new InconsistentDashboardStateException("")).when(repository).save(dashboard);

        Response response = handler.deleteGadget(dashboardId, gadgetRequestContext, gadgetId);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
    }
}
