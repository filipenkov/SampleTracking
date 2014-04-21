package com.atlassian.gadgets.dashboard.internal.rest;

import javax.ws.rs.core.Response;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.GadgetLayoutException;
import com.atlassian.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.Iterables;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.dashboard.GadgetLayoutFactory.column;
import static com.atlassian.gadgets.dashboard.GadgetLayoutFactory.newGadgetLayout;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangeLayoutHandlerImplTest
{
    @Mock private DashboardRepository repository;
    @Mock private I18nResolver i18n;
    @Mock private EventPublisher eventPublisher;
    private GadgetRequestContext gadgetRequestContext;

    ChangeLayoutHandler handler;
    
    @Before
    public void setUp()
    {
        gadgetRequestContext = gadgetRequestContext().build();
        handler = new ChangeLayoutHandlerImpl(repository, i18n, eventPublisher);
    }

    @Test
    public void assertThatLayoutParameterOnlyRearrangesGadgetsAndSetsNoContentStatus() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        Dashboard dashboard = mock(Dashboard.class);
        GadgetId gadgetId1 = GadgetId.valueOf("1000");
        GadgetId gadgetId2 = GadgetId.valueOf("1001");
        GadgetId gadgetId3 = GadgetId.valueOf("1002");
        GadgetId gadgetId4 = GadgetId.valueOf("1003");

        JSONObject json = new JSONObject();
        json.put("0", new JSONArray().put("1000").put("1001"));
        json.put("1", new JSONArray().put("1002").put("1003"));
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getLayout()).thenReturn(Layout.AA);

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).rearrangeGadgets(argThat(is(equalTo(newGadgetLayout(column(gadgetId1, gadgetId2), column(gadgetId3, gadgetId4))))));
        verify(dashboard, atLeastOnce()).getLayout();
        verify(repository).save(dashboard);
        assertThat(response.getStatus(), is(equalTo(Response.Status.NO_CONTENT.getStatusCode())));
        verifyNoMoreInteractions(dashboard, repository);
    }

    @Test
    public void assertThatDashboardLayoutIsChangedWhenLayoutParameterIsPresentAndNoContentStatusIsSet() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        Dashboard dashboard = mock(Dashboard.class);
        GadgetId gadgetId1 = GadgetId.valueOf("1000");
        GadgetId gadgetId2 = GadgetId.valueOf("1001");
        GadgetId gadgetId3 = GadgetId.valueOf("1002");
        GadgetId gadgetId4 = GadgetId.valueOf("1003");

        JSONObject json = new JSONObject();
        json.put("0", new JSONArray().put("1000").put("1001"));
        json.put("1", new JSONArray().put("1002").put("1003"));
        json.put("layout", Layout.AB.name());

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getLayout()).thenReturn(Layout.AA);

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).changeLayout(Matchers.eq(Layout.AB), argThat(is(equalTo(newGadgetLayout(column(gadgetId1, gadgetId2), column(gadgetId3, gadgetId4))))));
        verify(repository).save(dashboard);
        assertThat(response.getStatus(), is(equalTo(Response.Status.NO_CONTENT.getStatusCode())));
        verifyNoMoreInteractions(dashboard, repository);
    }

    @Test
    public void assertThatHandlerSendsBadRequestErrorWhenLayoutParameterIsInvalid() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        JSONObject json = new JSONObject();
        json.put("layout", "C");

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        verify(repository).get(eq(DashboardId.valueOf("100")), isA(GadgetRequestContext.class));
        verifyNoMoreInteractions(repository);        
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatThrowingGadgetLayoutExceptionWhenRearrangingGadgetsResultsInSendingBadRequestResponse() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        Dashboard dashboard = mock(Dashboard.class);
        GadgetId gadgetId1 = GadgetId.valueOf("1000");
        GadgetId gadgetId2 = GadgetId.valueOf("1001");
        GadgetId gadgetId3 = GadgetId.valueOf("1002");
        GadgetId gadgetId4 = GadgetId.valueOf("1003");
        GadgetLayout gadgetLayout = newGadgetLayout(column(gadgetId1, gadgetId2), column(gadgetId3, gadgetId4));

        JSONObject json = new JSONObject();
        json.put("0", new JSONArray().put("1000").put("1001"));
        json.put("1", new JSONArray().put("1002").put("1003"));

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getLayout()).thenReturn(Layout.AA);
        doThrow(new GadgetLayoutException("Invalid gadget layout")).when(dashboard).rearrangeGadgets(argThat(is(equalTo(gadgetLayout))));

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard, atLeastOnce()).getLayout();
        verify(dashboard).rearrangeGadgets(argThat(is(equalTo(gadgetLayout))));
        verifyNoMoreInteractions(dashboard, repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatThrowingGadgetLayoutExceptionWhenChangingDashboardLayoutResultsInSendingBadRequestResponse() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        Dashboard dashboard = mock(Dashboard.class);
        GadgetId gadgetId1 = GadgetId.valueOf("1000");
        GadgetId gadgetId2 = GadgetId.valueOf("1001");
        GadgetId gadgetId3 = GadgetId.valueOf("1002");
        GadgetId gadgetId4 = GadgetId.valueOf("1003");
        GadgetLayout gadgetLayout = newGadgetLayout(column(gadgetId1, gadgetId2), column(gadgetId3, gadgetId4));

        JSONObject json = new JSONObject();
        json.put("0", new JSONArray().put("1000").put("1001"));
        json.put("1", new JSONArray().put("1002").put("1003"));
        json.put("layout", Layout.AB.name());
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getLayout()).thenReturn(Layout.AA);
        doThrow(new GadgetLayoutException("Invalid gadget layout")).when(dashboard).changeLayout(eq(Layout.AB), argThat(is(equalTo(gadgetLayout))));

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
        verify(dashboard).changeLayout(eq(Layout.AB), argThat(is(equalTo(gadgetLayout))));
        verifyNoMoreInteractions(dashboard, repository);
        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
    }

    @Test(expected=DashboardNotFoundException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatHandlerReturnsImmediatelyIfDashboardNotFound() throws Exception
    {
        DashboardId dashboardId = DashboardId.valueOf("100");

        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).
                thenThrow(new DashboardNotFoundException(dashboardId));

        try
        {
            handler.changeLayout(dashboardId, gadgetRequestContext, new JSONObject());
        }
        finally
        {
            verify(repository).get(eq(dashboardId), isA(GadgetRequestContext.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Test
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatInconsistentStateExceptionReturnsConflict() throws JSONException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        Dashboard dashboard = mock(Dashboard.class);

        JSONObject json = new JSONObject();
        json.put("0", new JSONArray().put("1000").put("1001"));
        json.put("1", new JSONArray().put("1002").put("1003"));
        when(repository.get(eq(dashboardId), isA(GadgetRequestContext.class))).thenReturn(dashboard);
        when(dashboard.getLayout()).thenReturn(Layout.AA);

        doThrow(new InconsistentDashboardStateException("")).when(repository).save(dashboard);

        Response response = handler.changeLayout(dashboardId, gadgetRequestContext, json);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CONFLICT.getStatusCode())));
    }

    private static <T> Matcher<? super T> equalTo(T actual)
    {
        return org.hamcrest.Matchers.equalTo(actual);
    }

    private static Matcher<GadgetLayout> equalTo(GadgetLayout layout)
    {
        return new GadgetLayoutIsEqual(layout);
    }

    private static class GadgetLayoutIsEqual extends BaseMatcher<GadgetLayout>
    {
        private final GadgetLayout actualLayout;

        public GadgetLayoutIsEqual(GadgetLayout actualLayout)
        {
            this.actualLayout = actualLayout;
        }

        public boolean matches(Object o)
        {
            if (o == actualLayout)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (!(o instanceof GadgetLayout))
            {
                return false;
            }
            GadgetLayout expectedLayout = (GadgetLayout) o;
            if (actualLayout.getNumberOfColumns() != expectedLayout.getNumberOfColumns())
            {
                return false;
            }
            for (int i = 0; i < actualLayout.getNumberOfColumns(); i++)
            {
                if (!Iterables.elementsEqual(actualLayout.getGadgetsInColumn(i), expectedLayout.getGadgetsInColumn(i)))
                {
                    return false;
                }
            }
            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("equal to AddGadgetChange(numberOfColumns=");
            description.appendValue(actualLayout.getNumberOfColumns());
            description.appendText(", columns={");
            for (int i = 0; i < actualLayout.getNumberOfColumns(); ++i)
            {
                description.appendText("[");
                description.appendValue(i);
                description.appendText("] = ");

                description.appendValue(Iterables.toString(actualLayout.getGadgetsInColumn(i)));

                if (i != actualLayout.getNumberOfColumns() - 1)
                {
                    description.appendText(", ");
                }
            }
            description.appendText("})");
        }
    }
}
