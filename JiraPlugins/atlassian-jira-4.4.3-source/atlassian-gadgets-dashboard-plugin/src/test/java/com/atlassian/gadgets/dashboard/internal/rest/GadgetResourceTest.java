package com.atlassian.gadgets.dashboard.internal.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.sal.api.message.I18nResolver;

import org.json.JSONException;
import org.json.JSONObject;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetResourceTest
{
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;

    @Mock private DashboardPermissionService permissionService;
    @Mock private DashboardRepository repository;
    @Mock private GadgetRequestContextFactory gadgetRequestContextFactory;
    @Mock private DeleteGadgetHandler deleteGadgetHandler;
    @Mock private AddGadgetHandler addGadgetHandler;
    @Mock private ChangeGadgetColorHandler changeGadgetColorHandler;
    @Mock private UpdateGadgetUserPrefsHandler updateGadgetUserPrefsHandler;
    @Mock private I18nResolver i18n;
    @Mock private HttpServletRequest request;
    @Mock private UriInfo uriInfo;
    @Mock private RepresentationFactory representationFactory;

    private GadgetResource resource;

    @Before
    public void setup()
    {
        when(gadgetRequestContextFactory.get(isA(HttpServletRequest.class)))
            .thenReturn(gadgetRequestContext().viewer("user").build());
        resource = new GadgetResource(permissionService, repository, gadgetRequestContextFactory,
                addGadgetHandler, deleteGadgetHandler, changeGadgetColorHandler, updateGadgetUserPrefsHandler, i18n,
                representationFactory);
    }

    //
    // Delete gadget tests
    //

    @Test
    public void assertthatDeleteGadgetReturnsUnauthorizedIfDashboardIsNotWritable()
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(dashboardId, "not-authorized")).thenReturn(false);

        Response response = resource.deleteGadget(DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request);

        assertThat(response.getStatus(), is(equalTo(Response.Status.UNAUTHORIZED.getStatusCode())));
        verifyZeroInteractions(deleteGadgetHandler);
    }

    @Test
    public void assertThatDeleteOrMoveGadgetViaPOSTRejectsRequestWithEmptyMethodParameter()
    {
        Response response = resource.deleteOrMoveGadgetViaPost("", DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request);
        assertThat(response.getStatus(), is(equalTo(HTTP_METHOD_NOT_ALLOWED)));
        verifyZeroInteractions(deleteGadgetHandler);
    }

    //
    // Color change tests
    //

    @Test
    public void assertThatColorChangeReturnsUnauthorizedIfDashboardIsNotWritable() throws JSONException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(dashboardId, "not-authorized")).thenReturn(false);

        Response response =
            resource.changeGadgetColor(DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request,
                                       new JSONObject().put("color", "color1").toString());

        assertThat(response.getStatus(), is(equalTo(Response.Status.UNAUTHORIZED.getStatusCode())));
        verifyZeroInteractions(changeGadgetColorHandler);
    }

    @Test
    public void assertThatInvalidColorParameterCausesBadRequestResponse() throws JSONException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(eq(dashboardId), anyString())).thenReturn(true);

        Response response =
            resource.changeGadgetColor(DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request,
                                       new JSONObject().put("color", "invalid-color").toString());

        assertThat(response.getStatus(), is(equalTo(Response.Status.BAD_REQUEST.getStatusCode())));
        verifyZeroInteractions(changeGadgetColorHandler);
    }

    @Test
    public void assertThatColorChangeViaPOSTRejectsRequestWithEmptyMethodParameter() throws JSONException
    {
        Response response =
            resource.changeGadgetColorViaPOST("", DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request,
                                              new JSONObject().put("color", "color1").toString());
        assertThat(response.getStatus(), is(equalTo(HTTP_METHOD_NOT_ALLOWED)));
        verifyZeroInteractions(changeGadgetColorHandler);
    }

    //
    // Pref change tests
    //

    @Test
    public void assertThatPrefChangeReturnsUnauthorizedIfDashboardIsNotWritable() throws JSONException
    {
        DashboardId dashboardId = DashboardId.valueOf("100");
        when(permissionService.isWritableBy(dashboardId, "not-authorized")).thenReturn(false);

        Response response =
            resource.updateUserPrefsViaPUT(DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request,
                                           new JSONObject().toString());

        assertThat(response.getStatus(), is(equalTo(Response.Status.UNAUTHORIZED.getStatusCode())));
        verifyZeroInteractions(updateGadgetUserPrefsHandler);
    }

    @Test
    public void assertThatPrefChangeViaPOSTRejectsRequestWithEmptyMethodParameter() throws JSONException
    {
        Response response =
            resource.updateUserPrefsViaPOST("", DashboardId.valueOf("100"), GadgetId.valueOf("1001"), request,
                                            new JSONObject().toString());
        assertThat(response.getStatus(), is(equalTo(HTTP_METHOD_NOT_ALLOWED)));
        verifyZeroInteractions(updateGadgetUserPrefsHandler);
    }
}
