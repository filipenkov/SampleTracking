package com.atlassian.gadgets.dashboard.internal.rest;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.internal.Dashboard;
import com.atlassian.gadgets.dashboard.internal.DashboardRepository;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.rest.representations.GadgetRepresentation;
import com.atlassian.gadgets.dashboard.internal.rest.representations.RepresentationFactory;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides REST endpoints for manipulating a Gadget.
 */
@Path("{dashboardId}/gadget/{gadgetId}")
public class GadgetResource
{
    private static final int HTTP_METHOD_NOT_ALLOWED = 405;

    private final Log log = LogFactory.getLog(getClass());

    private final DashboardPermissionService permissionService;
    private final DashboardRepository repository;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final AddGadgetHandler addGadgetHandler;
    private final DeleteGadgetHandler deleteGadgetHandler;
    private final ChangeGadgetColorHandler changeGadgetColorHandler;
    private final UpdateGadgetUserPrefsHandler updateGadgetUserPrefsHandler;
    private final I18nResolver i18n;
    private RepresentationFactory representationFactory;

    /**
     * Constructor.
     * @param permissionService the {@code PermissionService} to use
     * @param repository the {@code DashboardRepository} to use
     * @param gadgetRequestContextFactory the {@code GadgetRequestContextFactory} to use
     * @param addGadgetHandler the {@code AddGadgetHandler} to use
     * @param deleteGadgetHandler the {@code DeleteGadgetHandler} to use
     * @param changeGadgetColorHandler the {@code ChangeGadgetColorHandler} to use
     * @param updateGadgetUserPrefsHandler the {@code UpdateGadgetUserPrefsHandler} to use
     * @param i18n the SAL {@code I18nResolver} to use
     * @param representationFactory Used to created JAXB Gadget representations
     */
    public GadgetResource(DashboardPermissionService permissionService,
                          DashboardRepository repository,
                          GadgetRequestContextFactory gadgetRequestContextFactory,
                          AddGadgetHandler addGadgetHandler,
                          DeleteGadgetHandler deleteGadgetHandler,
                          ChangeGadgetColorHandler changeGadgetColorHandler,
                          UpdateGadgetUserPrefsHandler updateGadgetUserPrefsHandler,
                          I18nResolver i18n,
                          RepresentationFactory representationFactory)
    {
        this.permissionService = permissionService;
        this.repository = repository;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.addGadgetHandler = addGadgetHandler;
        this.deleteGadgetHandler = deleteGadgetHandler;
        this.changeGadgetColorHandler = changeGadgetColorHandler;
        this.updateGadgetUserPrefsHandler = updateGadgetUserPrefsHandler;
        this.i18n = i18n;
        this.representationFactory = representationFactory;
    }

    /**
     * Returns a Gadget's JSON or XMl representation.
     *
     * @param dashboardId the id of the dashboard which it belongs to
     * @param gadgetId the ID of the gadget to return
     * @param request the {@code HttpServletRequest} that was routed here
     * @return The gadget representation
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getRenderedGadget(@PathParam("dashboardId") DashboardId dashboardId,
                                    @PathParam("gadgetId") GadgetId gadgetId,
                                    @Context HttpServletRequest request)
    {
        log.debug("GadgetResource: GET received: dashboardId=" + dashboardId + ", gadgetId = " + gadgetId);
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);
        if(!permissionService.isReadableBy(dashboardId, requestContext.getViewer()))
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        final Dashboard dashboard = repository.get(dashboardId, requestContext);

        Gadget myGadget = null;
        DashboardState.ColumnIndex myColumn = null;
        for (DashboardState.ColumnIndex column : dashboard.getLayout().getColumnRange()) {
            for (Gadget gadget : dashboard.getGadgetsInColumn(column))
            {
                if (gadget.getId().equals(gadgetId))
                {
                    myGadget = gadget;
                    myColumn = column;
                    log.debug("GadgetResource: GET: Found gadget ID '" + gadgetId + "' in column " + myColumn.index()
                        + "; state=" + myGadget.toString());
                    break;
                }
            }
            if (myGadget != null)
            {
                break;
            }
        }

        if(myGadget == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final GadgetRepresentation rep = representationFactory.createGadgetRepresentation(
                dashboardId,
                myGadget,
                requestContext,
                permissionService.isWritableBy(dashboardId, requestContext.getViewer()), myColumn);
        return Response.ok().entity(rep).build();
    }

    /**
     * Deletes or moves the specified gadget from the specified dashboard when invoked
     * as a POST request.
     * @param method the HTTP method to forward to ("delete" does a delete, "put" does a move)
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget
     * @param request the request object (used for providing the locale)
     * @return a {@code Response} with details on the request's success or
     * failure
     */
    @POST
    @AnonymousAllowed
    public Response deleteOrMoveGadgetViaPost(@HeaderParam("X-HTTP-Method-Override") String method,
                                              @PathParam("dashboardId") DashboardId dashboardId,
                                              @PathParam("gadgetId") GadgetId gadgetId,
                                              @Context HttpServletRequest request)
    {
        if (method.equalsIgnoreCase(HttpMethod.DELETE))
        {
            log.debug("GadgetResource: POST redirected to DELETE");
            return deleteGadget(dashboardId, gadgetId, request);
        }
        else if (method.equalsIgnoreCase(HttpMethod.PUT))
        {
        	log.debug("GadgetResource: POST redirected to PUT");
        	return moveGadget(dashboardId, gadgetId, request);
        }
        else
        {
            return Response.status(HTTP_METHOD_NOT_ALLOWED).build();
        }
    }

    /**
     * Deletes the specified gadget from the specified dashboard when invoked
     * as a DELETE request.
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget to remove
     * @param request the request object (used for providing the locale)
     * @return a {@code Response} with details on the request's success or
     * failure
     */
    @DELETE
    @AnonymousAllowed
    public Response deleteGadget(@PathParam("dashboardId") DashboardId dashboardId,
                                 @PathParam("gadgetId") GadgetId gadgetId,
                                 @Context HttpServletRequest request)
    {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer()))
        {
            log.warn("GadgetResource: DELETE: prevented gadget delete due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return deleteGadgetHandler.deleteGadget(dashboardId, requestContext, gadgetId);
    }

    /**
     * Moves the specified gadget to the specified dashboard. The gadget is
     * assumed to exist on some other (unspecified) dashboard. The gadget is safely removed
     * from that dashboard and added to the target dashboard.
     * @param targetDashboardId the dashboard id for the dashboard to which this gadget should be added
     * @param gadgetId the id of the gadget to move
     * @param request the request object
     * @return a {@code Response} with details on the request's success or failure
     */
    @PUT
    @AnonymousAllowed
    public Response moveGadget(@PathParam("dashboardId") DashboardId targetDashboardId,
                               @PathParam("gadgetId") GadgetId gadgetId,
                               @Context HttpServletRequest request)
    {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        if (!permissionService.isWritableBy(targetDashboardId, requestContext.getViewer()))
        {
            log.warn("GadgetResource: PUT: prevented gadget move due to insufficient permissions on target dashboard");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try
        {
            DashboardId sourceDashboardId = repository.findDashboardByGadgetId(gadgetId);
            if (!permissionService.isWritableBy(sourceDashboardId, requestContext.getViewer()))
            {
                log.warn("GadgetResource: PUT: prevented gadget move due to insufficient permissions on source dashboard");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            return addGadgetHandler.moveGadget(targetDashboardId,
                                        gadgetId,
                                        sourceDashboardId,
                                        DashboardState.ColumnIndex.ZERO,
                                        0,
                                        requestContext);
        }
        catch (DashboardNotFoundException dnfe)
        {
            log.error("DashboardResource: PUT: could not find a dashboard containing gadget " + gadgetId);
            return Response.status(Response.Status.BAD_REQUEST).
                    type(MediaType.TEXT_PLAIN).
                    entity(i18n.getText("gadgetResource.error.moving.gadget", dnfe.getMessage())).
                    build();
        }
    }
    
    /**
     * Forwards POST requests (coming from Ajax or web browsers) to the PUT
     * handler for color changing.
     * @param method the HTTP method to forward to (must be "put")
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget which will change color
     * @param request the request object (used for providing the locale)
     * @param entity JSON object containing one key, "color", mapped to the new color of the gadget
     * @return a {@code Response} with details on the request's success or failure
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/color")
    public Response changeGadgetColorViaPOST(@HeaderParam("X-HTTP-Method-Override") String method,
                                             @PathParam("dashboardId") DashboardId dashboardId,
                                             @PathParam("gadgetId") GadgetId gadgetId,
                                             @Context HttpServletRequest request,
                                             String entity)
    {
        if (method.equalsIgnoreCase(HttpMethod.PUT))
        {
            log.debug("GadgetResource: POST /color delegated to PUT");
            return changeGadgetColor(dashboardId, gadgetId, request, entity);
        }
        else
        {
            return Response.status(HTTP_METHOD_NOT_ALLOWED).build();
        }
    }

    /**
     * Changes the specified gadget's color in response to a PUT request.
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget which will change color
     * @param request the request object (used for providing the locale)
     * @param entity JSON object containing one key, "color", mapped to the new color of the gadget
     * @return a {@code Response} with details on the request's success or failure
     */
    @PUT
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/color")
    public Response changeGadgetColor(@PathParam("dashboardId") DashboardId dashboardId,
                                      @PathParam("gadgetId") GadgetId gadgetId,
                                      @Context HttpServletRequest request,
                                      String entity)
    {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer()))
        {
            log.warn("GadgetResource: PUT: prevented gadget color change due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JSONObject json;
        try
        {
            json = new JSONObject(entity);
        }
        catch (JSONException e)
        {
            return errorParsingJson(e);
        }
        String colorParam = null;
        Color color;
        try
        {
            colorParam = json.getString("color");
            color = Color.valueOf(colorParam);
        }
        catch (JSONException e)
        {
            return invalidColor(colorParam);
        }
        catch (IllegalArgumentException e)
        {
            return invalidColor(colorParam);
        }
        log.debug("GadgetResource: PUT /color: dashboardId=" + dashboardId +
                " gadgetId=" + gadgetId + " color=" + color);

        return changeGadgetColorHandler.setGadgetColor(dashboardId, requestContext, gadgetId, color);
    }

    private Response invalidColor(String colorParam)
    {
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.TEXT_PLAIN)
            .entity(i18n.getText("gadgetResource.invalid.color", colorParam, Arrays.toString(Color.values())))
            .build();
    }

    /**
     * <p>Forwards POST requests (coming from Ajax or web browsers) to the PUT
     * handler for user pref changes.<p>
     * @param method the HTTP method to forward to (must be "put")
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget to update the prefs for
     * @param request the request object (used for providing the locale)
     * @param entity the container for the form parameters
     * @return a {@code Response} with details on the request's success or failure
     */
    @POST
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/prefs")
    public Response updateUserPrefsViaPOST(@HeaderParam("X-HTTP-Method-Override") String method,
                                           @PathParam("dashboardId") DashboardId dashboardId,
                                           @PathParam("gadgetId") GadgetId gadgetId,
                                           @Context HttpServletRequest request,
                                           String entity)
    {
        if (method.equalsIgnoreCase(HttpMethod.PUT))
        {
            try
            {
                return updateUserPrefs(dashboardId, gadgetId, request, new JSONObject(entity));
            }
            catch (JSONException e)
            {
                return errorParsingJson(e);
            }
        }
        else
        {
            return Response.status(HTTP_METHOD_NOT_ALLOWED).build();
        }
    }

    /**
     * Updates the user prefs of the specified gadget.
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget to update the prefs for
     * @param request the request object (used for providing the locale)
     * @param entity the container for the user pref values
     * @return a {@code Response} with details on the request's success or failure
     */
    @PUT
    @AnonymousAllowed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/prefs")
    public Response updateUserPrefsViaPUT(@PathParam("dashboardId") DashboardId dashboardId,
                                          @PathParam("gadgetId") GadgetId gadgetId,
                                          @Context HttpServletRequest request,
                                          String entity)
    {
        try
        {
            return updateUserPrefs(dashboardId, gadgetId, request, new JSONObject(entity));
        }
        catch (JSONException e)
        {
            return errorParsingJson(e);
        }
    }

    private Response updateUserPrefs(DashboardId dashboardId,
                                     GadgetId gadgetId,
                                     HttpServletRequest request,
                                     JSONObject params)
    {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer()))
        {
            log.warn("GadgetResource: prevented gadget prefs change due to insufficient permission");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        log.debug("GadgetResource: update /prefs: dashboardId=" + dashboardId +
                  " gadgetId=" + gadgetId);

        return updateGadgetUserPrefsHandler.updateUserPrefs(dashboardId, requestContext, gadgetId, toMap(params));
    }

    private Map<String, String> toMap(JSONObject json)
    {
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (Iterator i = json.keys(); i.hasNext();)
        {
            String key = (String) i.next();
            try
            {
                map.put(key, json.getString(key));
            }
            catch (JSONException e)
            {
                throw new RuntimeException("key '" + key + "' not found in " + json);
            }
        }
        return map.build();
    }


    private Response errorParsingJson(JSONException e)
    {
        log.warn("GadgetResource: POST: error parsing json", e);
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.TEXT_PLAIN)
            .entity(i18n.getText("dashboardResource.error.parsing.json", e.getMessage()))
            .build();
    }
}
