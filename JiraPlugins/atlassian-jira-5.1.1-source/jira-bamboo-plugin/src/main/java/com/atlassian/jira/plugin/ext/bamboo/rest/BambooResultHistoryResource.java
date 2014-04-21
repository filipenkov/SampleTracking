package com.atlassian.jira.plugin.ext.bamboo.rest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.model.ErrorMessage;
import com.atlassian.jira.plugin.ext.bamboo.model.OAuthErrorMessage;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooRestService;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/history")
public class BambooResultHistoryResource
{
    private static final Logger log = Logger.getLogger(BambooResultHistoryResource.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final BambooRestService bambooRestService;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BambooResultHistoryResource(final BambooRestService bambooRestService, final BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.bambooRestService = bambooRestService;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods

    @GET
    @Path("/{jiraProjectKey}/{planKey}/{numberOfResults}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory(@PathParam("jiraProjectKey") String projectKey, @PathParam("planKey") String key, @PathParam("numberOfResults") int numberOfResults, @Context UriInfo uriInfo)
    {
        ApplicationLink bambooApplicationLink = bambooApplicationLinkManager.getApplicationLink(projectKey);

        if (bambooApplicationLink == null)
        {
            return Response.serverError().build();
        }

        final PlanKey planKey = PlanKeys.getPlanKey(key);

        try
        {
            RestResult<JSONObject> result = bambooRestService.getPlanHistory(bambooApplicationLink.createAuthenticatedRequestFactory(), planKey, numberOfResults, getQueryParams(uriInfo));
            JSONObject jsonObject = result.getResult();
            if (jsonObject == null)
            {
                final ErrorMessage errorMessage = new ErrorMessage(PluginConstants.BAMBOO_UNREACHABLE_TITLE, result.getErrorMessage(PluginConstants.BAMBOO_UNREACHABLE_MSG));
                return errorMessage.createJSONEntity(Response.status(Response.Status.NOT_FOUND)).build();
            }
            return Response.ok(jsonObject.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }
        catch (IllegalArgumentException e)
        {
            log.info("Unable to get history for plan " + planKey + " numberOfResults: " + numberOfResults, e);
            JSONObject jsonObject = new JSONObject();
            try
            {
                jsonObject.put("message", e.getMessage());
            }
            catch (JSONException e1)
            {
                return Response.serverError().build();
            }

            return Response.status(Response.Status.BAD_REQUEST).entity(jsonObject.toString()).build();
        }
        catch (CredentialsRequiredException e)
        {
            log.debug(PluginConstants.CREDENTIALS_REQUIRED, e);
            return new OAuthErrorMessage(PluginConstants.LOGIN_AND_APPROVE_BEFORE_CONTINUING, e.getAuthorisationURI()).createJSONEntity(Response.status(Response.Status.UNAUTHORIZED)).build();
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public static Map<String, String> getQueryParams(final UriInfo uriInfo)
    {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        Map<String, String> result = Maps.newHashMap();
        if (!params.isEmpty())
        {
            for (String key : params.keySet())
            {
                result.put(key, params.getFirst(key));
            }
        }
        return result;
    }
}
