package com.atlassian.gadgets.dashboard.internal.rest;

import java.util.Map;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetNotFoundException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGadgetUserPrefsHandlerImplTest
{
    @Mock private DashboardRepository repository;
    @Mock private I18nResolver i18n;
    private GadgetRequestContext gadgetRequestContext;

    private UpdateGadgetUserPrefsHandlerImpl handler;

    @Before
    public void setup()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        handler = new UpdateGadgetUserPrefsHandlerImpl(repository, i18n);
    }

    @Test
    public void assertThatNoContentStatusCodeIsSentOnSuccess() throws Exception
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        Map<String, String> params = ImmutableMap.of("up_pref1", "value1");
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);

        Response response = handler.updateUserPrefs(dashboardId,
                gadgetRequestContext, gadgetId, params);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).updateGadgetUserPrefs(gadgetId, ImmutableMap.of("pref1", "value1"));
        verify(repository).save(dashboard);
        verifyNoMoreInteractions(repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.NO_CONTENT.getStatusCode())));
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void assertThatBadRequestStatusCodeIsSentWhenIllegallySettingRequiredPref()
            throws Exception
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        Map<String, String> params = ImmutableMap.of("up_pref1", "");
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        doThrow(new IllegalArgumentException())
                .when(dashboard).updateGadgetUserPrefs(GadgetId.valueOf("1001"), ImmutableMap.of("pref1", ""));

        Response response = handler.updateUserPrefs(dashboardId,
                gadgetRequestContext, gadgetId, params);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).updateGadgetUserPrefs(GadgetId.valueOf("1001"), ImmutableMap.of("pref1", ""));
        verifyNoMoreInteractions(repository, dashboard);
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test(expected= DashboardNotFoundException.class)
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void assertThatHandlerReturnsImmediatelyIfDashboardNotFound() throws DashboardNotFoundException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1000");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).
                thenThrow(new DashboardNotFoundException(dashboardId));

        try
        {
            handler.updateUserPrefs(dashboardId, gadgetRequestContext, gadgetId, ImmutableMap.<String, String>of());
        }
        finally
        {
            verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test(expected= GadgetNotFoundException.class)
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void assertThatHandlerReturnsImmediatelyIfGadgetIsNotFound() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");
        Dashboard dashboard = mock(Dashboard.class);

        Map<String, String> params = ImmutableMap.of("up_pref1", "value1");

        when(repository.get(eq(DashboardId.valueOf("100")), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        doThrow(new GadgetNotFoundException(gadgetId)).when(dashboard).updateGadgetUserPrefs(gadgetId, ImmutableMap.of("pref1", "value1"));

        try
        {
            handler.updateUserPrefs(dashboardId, gadgetRequestContext, gadgetId, params);
        }
        finally
        {
            verify(repository).get(eq(DashboardId.valueOf("100")), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInconsistentDashboardStateExceptionReturnsConflict()
    {
        Dashboard dashboard = mock(Dashboard.class);
        DashboardId dashboardId = DashboardId.valueOf("100");
        GadgetId gadgetId = GadgetId.valueOf("1001");

        Map<String, String> params = ImmutableMap.of("up_pref1", "value1");
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        doThrow(new InconsistentDashboardStateException("")).when(repository).save(dashboard);

        Response response = handler.updateUserPrefs(dashboardId,
                gadgetRequestContext, gadgetId, params);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
    }
}
