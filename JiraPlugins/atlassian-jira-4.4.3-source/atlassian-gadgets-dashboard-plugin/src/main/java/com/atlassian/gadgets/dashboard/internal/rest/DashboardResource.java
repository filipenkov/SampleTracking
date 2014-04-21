package com.atlassian.gadgets.dashboard.internal.rest;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.rest.representations.DashboardRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides REST endpoints for using the dashboard.
 */
@Path("{dashboardId}")
public class DashboardResource
{
    private final Log log = LogFactory.getLog(getClass());

    private final DashboardPermissionService permissionService;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final AddGadgetHandler addGadgetHandler;
    private final ChangeLayoutHandler changeLayoutHandler;
    private final I18nResolver i18n;
    private final DashboardRepository repository;
    private final RepresentationFactory representationFactory;

    /**
     * Constructor.
     * @param permissionService the {@code PermissionService} implementation to use
     * @param gadgetRequestContextFactory the {@code GadgetRequestContextFactory} to use
     * @param addGadgetHandler the {@code AddGadgetHandler} to use
     * @param changeLayoutHandler the {@code ChangeLayoutHandler} to use
     * @param repository the {@code Repository} to use
     * @param representationFactory the {@code RepresentationFactory} to use to construct JAXB reps
     * @param i18n the {@code I18nResolver} from SAL
     */
    public DashboardResource(DashboardPermissionService permissionService,
                             GadgetRequestContextFactory gadgetRequestContextFactory,
                             AddGadgetHandler addGadgetHandler,
                             ChangeLayoutHandler changeLayoutHandler,
                             DashboardRepository repository,
                             RepresentationFactory representationFactory,
                             I18nResolver i18n)
    {
        this.permissionService = permissionService;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.addGadgetHandler = addGadgetHandler;
        this.changeLayoutHandler = changeLayoutHandler;
        this.repository = repository;
        this.representationFactory = representationFactory;
        this.i18n = i18n;
    }

    @GET
    @AnonymousAllowed
    @Produces ({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getDashboard(@PathParam("dashboardId") DashboardId dashboardId, @Context HttpServletRequest request)
    {
        log.debug("DashboardResource: GET received: dashboardId = " + dashboardId);

        final GadgetRequestContext gadgetRequestContext = gadgetRequestContextFactory.get(request);
        if (!permissionService.isReadableBy(dashboardId, gadgetRequestContext.getViewer()))
        {
            log.warn("DashboardResource: GET: prevented getting dashboard representation due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        final Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        final DashboardRepresentation representation = representationFactory.createDashboardRepresentation(dashboard,
                gadgetRequestContext, permissionService.isWritableBy(dashboardId, gadgetRequestContext.getViewer()));
        return Response.ok(representation).build();
    }

    @GET
    @AnonymousAllowed
    @Path("numGadgets")
    @Produces (MediaType.TEXT_PLAIN)
    public Response getNumGadgets(@PathParam("dashboardId") DashboardId dashboardId, @Context HttpServletRequest request)
    {
        log.debug("DashboardResource: GET numGadgets received: " + dashboardId);
        final GadgetRequestContext gadgetRequestContext = gadgetRequestContextFactory.get(request);
        if (!permissionService.isReadableBy(dashboardId, gadgetRequestContext.getViewer()))
        {
            log.warn("DashboardResource: GET numGadgets: prevented getting number of gadgets on dashboard due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        final Dashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        return Response.ok(String.valueOf(dashboard.getNumberOfGadgets())).build();
    }

    /**
     * Adds a gadget to a dashboard, returning the new gadget's entity representation
     * if successful.
     * <p>
     * If the request body contains a gadgetId and sourceDashboardId, then this will be treated as a 'move'
     * operation, where the gadget will be added to the dashboard specified and deleted from the sourceDashboard.
     * </p>
     *
     * @param dashboardId the dashboard to add the gadget to
     * @param request the {@code HttpServletRequest} that was routed here
     * @param jsonContent the body of the post in JSON
     * @return a {@code Response} with the resulting status code and
     * gadget URL if applicable
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addGadget(@PathParam("dashboardId") DashboardId dashboardId,
                              @Context HttpServletRequest request,
                              Reader jsonContent)
    {
        String gadgetUrl;
        int columnIndexAsInt = 0;
        DashboardState.ColumnIndex columnIndex;
        try
        {
            JSONObject jsonObject = new JSONObject(IOUtils.toString(jsonContent));
            gadgetUrl = jsonObject.optString("url");
            columnIndexAsInt = jsonObject.optInt("columnIndex");
            columnIndex = DashboardState.ColumnIndex.from(columnIndexAsInt);
        }
        catch (JSONException jsone)
        {
            return errorParsingJson(jsone);
        }
        catch (IllegalArgumentException iae)
        {
            log.error("DashboardResource: POST: invalid column index " + columnIndexAsInt + ". Valid values for Column are 0-2.");
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("dashboardResource.error.parsing.json", iae.getMessage())).
                    build();
        }
        catch (IOException ioe)
        {
            log.error("DashboardResource: POST: error reading json entity");
            return Response.serverError().
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("dashboardResource.error.reading.json", ioe.getMessage())).
                    build();
        }

        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        log.debug("DashboardResource: POST received: dashboardId=" + dashboardId + ", url=" + gadgetUrl);
        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer()))
        {
            log.warn("DashboardResource: POST: prevented gadget addition due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return addGadgetHandler.addGadget(dashboardId, requestContext, gadgetUrl, columnIndex);
    }

    /**
     * Changes the existing layout of the specified dashboard in response to a POST request.
     *
     * @param dashboardId ID of the dashboard on which to change the layout
     * @param request the {@code HttpServletRequest} that was routed here
     * @param entity the new layout data
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/layout")
    public Response changeLayoutViaPOST(@PathParam("dashboardId") DashboardId dashboardId,
                                        @Context HttpServletRequest request,
                                        String entity)
    {
        return changeLayout(dashboardId, request, entity);
    }

    /**
     * Changes the existing layout of the specified dashboard in response to a PUT request.
     *
     * @param dashboardId ID of the dashboard on which to change the layout
     * @param request the {@code HttpServletRequest} that was routed here
     * @param entity the new layout data
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @PUT
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/layout")
    public Response changeLayoutViaPUT(@PathParam("dashboardId") DashboardId dashboardId,
                                       @Context HttpServletRequest request,
                                       String entity)
    {
        return changeLayout(dashboardId, request, entity);
    }

    private Response changeLayout(DashboardId dashboardId, HttpServletRequest request, String entity)
    {
        try
        {
            final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);
            log.debug("DashboardResource: changeLayout: dashboardId=" + dashboardId +
                      " viewer=" + requestContext.getViewer());

            if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer()))
            {
                log.warn("DashboardResource: changeLayout: prevented layout change due to insufficient permission");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            return changeLayoutHandler.changeLayout(dashboardId, requestContext, new JSONObject(entity));
        }
        catch (JSONException e)
        {
            return errorParsingJson(e);
        }
    }

    private Response errorParsingJson(JSONException e)
    {
        log.warn("DashboardResource: POST: error parsing json", e);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.TEXT_PLAIN)
            .entity(i18n.getText("dashboardResource.error.parsing.json", e.getMessage()))
            .build();
    }
}
