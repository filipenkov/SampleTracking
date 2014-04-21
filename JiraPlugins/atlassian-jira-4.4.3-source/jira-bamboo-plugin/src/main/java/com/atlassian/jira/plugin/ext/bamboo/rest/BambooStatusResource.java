package com.atlassian.jira.plugin.ext.bamboo.rest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.jira.plugin.ext.bamboo.model.ErrorMessage;
import com.atlassian.jira.plugin.ext.bamboo.model.OAuthErrorMessage;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooRestService;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/status")
public class BambooStatusResource
{
    private static final Logger log = Logger.getLogger(BambooStatusResource.class);
    private static final String BAMBOO = "bamboo";
    private static final String JIRA = "jira";
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final VersionManager versionManager;
    private final BambooRestService bambooRestService;
    private final BambooReleaseService bambooReleaseService;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BambooStatusResource(final VersionManager versionManager,
                                final BambooRestService bambooRestService,
                                final BambooReleaseService bambooReleaseService,
                                final BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.versionManager = versionManager;
        this.bambooRestService = bambooRestService;
        this.bambooReleaseService = bambooReleaseService;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{jiraVersionId}/{planResultKey}")
    public Response getStatus(@PathParam("planResultKey") String resultKey, @PathParam("jiraVersionId") long jiraVersionId)
    {
        final Version version = versionManager.getVersion(jiraVersionId);
        if (version == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ApplicationLink bambooApplicationLink = bambooApplicationLinkManager.getApplicationLink(version.getProjectObject().getKey());

        if (bambooApplicationLink == null)
        {
            return Response.serverError().build();
        }

        final PlanResultKey planResultKey = PlanKeys.getPlanResultKey(resultKey);

        try
        {
            RestResult<JSONObject> restResult = bambooRestService.getPlanResultJson(bambooApplicationLink.createAuthenticatedRequestFactory(), planResultKey);
            JSONObject bambooJsonObject = restResult.getResult();

            if (bambooJsonObject == null)
            {
                final ErrorMessage errorMessage = new ErrorMessage(PluginConstants.BAMBOO_UNREACHABLE_TITLE, restResult.getErrorMessage(PluginConstants.BAMBOO_UNREACHABLE_MSG));
                return errorMessage.createJSONEntity(Response.status(Response.Status.NOT_FOUND)).build();
            }

            PlanStatus planStatus = getPlanStatusFromJSON(planResultKey, bambooJsonObject);

            JSONObject jiraJsonObject = new JSONObject();
            if (!version.isReleased() && planStatus.getBuildState() != BuildState.UNKNOWN)
            {
                // check if we are waiting for it to be released
                Map<String, String> buildParams = bambooReleaseService.getBuildData(version.getProjectObject().getKey(), version.getId());
                if (buildParams != null)
                {
                    String state = buildParams.get(PluginConstants.PS_BUILD_COMPLETED_STATE);
                    // if state is not null then we have already release this plan, we dont have to do it again...

                    if (state == null)
                    {
                        bambooReleaseService.releaseIfRequired(planStatus, version);
                        jiraJsonObject.put("forceRefresh", true);
                    }
                }

                //Lets re-look this up to be sure we are consistent
                jiraJsonObject.put("released", versionManager.getVersion(jiraVersionId).isReleased());

            }
            else
            {
                jiraJsonObject.put("released", version.isReleased());
            }

            JSONObject result = new JSONObject();
            result.put(BAMBOO, bambooJsonObject);
            result.put(JIRA, jiraJsonObject);

            return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();
        }
        catch (CredentialsRequiredException e)
        {
            log.debug("Credentials required", e);
            return new OAuthErrorMessage(PluginConstants.LOGIN_AND_APPROVE_BEFORE_STATUS_VISIBLE, e.getAuthorisationURI()).createJSONEntity(Response.status(Response.Status.UNAUTHORIZED)).build();
        }
        catch (JSONException e)
        {
            log.error("Failed to parse status response from Bamboo", e);
            return Response.serverError().build();
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
    // for testing.
    protected PlanStatus getPlanStatusFromJSON(PlanResultKey planResultKey, JSONObject bambooJsonObject) throws JSONException
    {
        return  PlanStatus.fromJsonObject(planResultKey, bambooJsonObject);
    }
}
