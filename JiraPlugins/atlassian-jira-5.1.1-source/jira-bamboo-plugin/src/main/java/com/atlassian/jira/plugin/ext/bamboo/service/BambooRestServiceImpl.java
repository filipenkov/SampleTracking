package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooPlan;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooProject;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BambooRestServiceImpl implements BambooRestService
{
    private static final Logger log = Logger.getLogger(BambooRestServiceImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    public static final int TRIGGER_CALL_TIMEOUT = 50000; //in miliseconds, this should be less than default JIRA AJAX call timeout of 60000

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods

    @NotNull
    public RestResult<Map<BambooProject, List<BambooPlan>>> getPlanList(@NotNull ApplicationLink applicationLink, boolean includeDisabled) throws CredentialsRequiredException
    {
        final String plansUrl = "/rest/api/latest/project.json?expand=projects.project.plans&withBuildPermissionOnly&max-results=10000";
        BambooRestResponse response = executeGetRequest(applicationLink, plansUrl, TRIGGER_CALL_TIMEOUT);

        List<String> errorCollection = new ArrayList<String>();
        Map<BambooProject, List<BambooPlan>> projectPlansMap = new TreeMap<BambooProject, List<BambooPlan>>();

        try
        {
            final JSONObject result = parseResponseForJsonObject(response, errorCollection);
            if (result != null)
            {
                final JSONArray projectArray = result.getJSONObject("projects").getJSONArray("project");
                for (int i = 0; i < projectArray.length(); i++)
                {
                    final JSONObject projectObject = projectArray.getJSONObject(i);
                    String projectName = projectObject.getString("name");
                    String projectKey = projectObject.getString("key");
                    BambooProject project = new BambooProject(projectKey, projectName);

                    List<BambooPlan> plans = new ArrayList<BambooPlan>();
                    JSONArray planArray = projectObject.getJSONObject("plans").getJSONArray("plan");
                    for (int j = 0; j < planArray.length(); j++)
                    {
                        final JSONObject planObject = planArray.getJSONObject(j);
                        String planName = planObject.getString("name");
                        String shortName = planObject.getString("shortName");
                        String planKey = planObject.getString("key");
                        boolean enabled = planObject.getBoolean("enabled");
                        if (enabled || includeDisabled)
                        {
                            plans.add(new BambooPlan(planKey, planName, shortName));
                        }
                    }
                    Collections.sort(plans);
                    if (!plans.isEmpty())
                    {
                        projectPlansMap.put(project, plans);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            log.error("Failed to parse Bamboo plan list", e);
            errorCollection.add("Failed to parse Bamboo plan list: " + e.getMessage());
        }

        return new RestResult(projectPlansMap, errorCollection);
    }

    @NotNull
    public RestResult<PlanStatus> getPlanStatus(@NotNull ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull final PlanResultKey planResultKey) throws CredentialsRequiredException
    {
        final String resultUrl = "/rest/api/latest/result/" + planResultKey.getPlanKey().getKey() + "/" + planResultKey.getBuildNumber() + ".json";
        final BambooRestResponse response = executeGetRequest(applicationLinkRequestFactory, resultUrl, TRIGGER_CALL_TIMEOUT);

        List<String> errorCollection = new ArrayList<String>();
        try
        {
            JSONObject result = parseResponseForJsonObject(response, errorCollection);
            if (result != null)
            {
                return new RestResult<PlanStatus>(PlanStatus.fromJsonObject(planResultKey, result), errorCollection);
            }
        }
        catch (JSONException e)
        {
            log.error("Failed to parse Bamboo result status", e);
            errorCollection.add("Failed to parse Bamboo result status: " + e.getMessage());
        }

        return new RestResult<PlanStatus>(new PlanStatus(planResultKey, null, null, false), errorCollection);
    }

    @NotNull
    public RestResult<JSONObject> getPlanResultJson(@NotNull final ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull final PlanResultKey planResultKey) throws CredentialsRequiredException
    {
        final String resultUrl = "/rest/api/latest/result/" + planResultKey.getPlanKey().getKey() + "/" + planResultKey.getBuildNumber() + ".json?expand=artifacts.artifact,labels.label";
        final BambooRestResponse response = executeGetRequest(applicationLinkRequestFactory, resultUrl, TRIGGER_CALL_TIMEOUT);

        final ArrayList<String> errorCollection = Lists.<String>newArrayList();
        return new RestResult<JSONObject>(parseResponseForJsonObject(response, errorCollection), errorCollection);
    }

    @NotNull
    public RestResult<JSONObject> getPlanJson(@NotNull final ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull final PlanKey planKey) throws CredentialsRequiredException
    {
        final String resultUrl = "/rest/api/latest/plan/" + planKey + ".json?expand=stages,variableContext";
        final BambooRestResponse response = executeGetRequest(applicationLinkRequestFactory, resultUrl, TRIGGER_CALL_TIMEOUT);

        final ArrayList<String> errorCollection = Lists.newArrayList();
        return new RestResult<JSONObject>(parseResponseForJsonObject(response, errorCollection), errorCollection);
    }

    public RestResult<JSONObject> getPlanHistory(@NotNull final ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull final PlanKey planKey, final int numberOfResults, @NotNull Map<String, String> urlParams) throws CredentialsRequiredException
    {
        if (numberOfResults < 0)
        {
            throw new IllegalArgumentException("Cannot request zero or negative number of results");
        }

        final StringBuilder resultUrl = new StringBuilder("/rest/api/latest/result/" + planKey + ".json?expand=results.result.stages&max-results=" + numberOfResults);

        for (final Map.Entry<String, String> entry : urlParams.entrySet())
        {
            resultUrl.append('&')
                     .append(entry.getKey())
                     .append('=')
                     .append(entry.getValue());
        }

        final BambooRestResponse response = executeGetRequest(applicationLinkRequestFactory, resultUrl.toString(), TRIGGER_CALL_TIMEOUT);

        final ArrayList<String> errorCollection = Lists.newArrayList();

        return new RestResult<JSONObject>(parseResponseForJsonObject(response, errorCollection), errorCollection);
    }

    @NotNull
    public RestResult<PlanResultKey> triggerPlan(@NotNull ApplicationLink applicationLink, @NotNull PlanKey planKey, @Nullable String stage, @NotNull Map<String, String> params) throws CredentialsRequiredException
    {
        return executeTrigger(applicationLink, planKey.getKey(), stage, params, TriggerType.NEW);
    }

    @NotNull
    public RestResult<PlanResultKey> continuePlan(@NotNull final ApplicationLink applicationLink, @NotNull final PlanResultKey planResultKey, @Nullable final String stage, @NotNull final Map<String, String> params) throws CredentialsRequiredException
    {
        return executeTrigger(applicationLink, planResultKey.getKey(), stage, params, TriggerType.EXISTING);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    private RestResult<PlanResultKey> executeTrigger(final ApplicationLink applicationLink, final String planKey, final String stage, final Map<String, String> params, final TriggerType triggerType) throws CredentialsRequiredException
    {
        List<String> errorCollection = new ArrayList<String>();

        String url = "/rest/api/latest/queue/" + planKey + ".json";
        if (StringUtils.isNotBlank(stage))
        {
            try
            {
                url = url + "?stage=" + URLCodec.encode(stage, true);
            }
            catch (UnsupportedEncodingException e)
            {
                String message = "Failed to trigger release build, requested stage name '" + stage + "' could not be encoded. ";
                errorCollection.add(message + e.getMessage());
                log.error(message, e);
            }
        }

        BambooRestResponse response = (triggerType == TriggerType.NEW)
                ? executePostRequest(applicationLink, url, params, TRIGGER_CALL_TIMEOUT)
                : executePutRequest(applicationLink,  url,  params, TRIGGER_CALL_TIMEOUT);

        JSONObject result = parseResponseForJsonObject(response, errorCollection);
        if (result != null)
        {
            try
            {
                String planResultKey = result.getString("buildResultKey");
                if (StringUtils.isNotBlank(planResultKey))
                {
                    return new RestResult<PlanResultKey>(PlanKeys.getPlanResultKey(planResultKey), errorCollection);
                }
                else
                {
                    String message = "Could not parse plan result key from Bamboo response: " + response.getResponseBody();
                    log.error(message);
                    errorCollection.add(message);
                }
            }
            catch (JSONException e)
            {
                String message = "Failed to trigger build, could not parse response from Bamboo";
                log.error(message, e);
                errorCollection.add(message + ": " + e.getMessage());
            }
        }

        return new RestResult<PlanResultKey>(null, errorCollection);
    }

    /**
     * Attempts to retrieve a json object from the response.  Checks for appropriate error codes and formatting in response.
     * Any errors will be added to the error collection.
     * This method should not a null {@link RestResult#getResult()} without adding a reason in the error collection
     *
     * @param response bamboo wrapped http response
     * @param errorCollection collection to add errors to
     * @return the high level json object retrieved from the response body.
     */
    @Nullable
    private JSONObject parseResponseForJsonObject(BambooRestResponse response, List<String> errorCollection)
    {
        if (!response.getErrors().isEmpty())
        {
           errorCollection.addAll(response.getErrors());
        }
        else if (!response.isValidStatusCode())
        {
            String responseBody = response.getResponseBody();
            String errorMessage = "Invalid return code received from Bamboo. " + response.getStatusCode() + ": ";
            if (responseBody != null && !responseBody.isEmpty())
            {
                errorMessage = errorMessage + responseBody;
            }
            else
            {
                errorMessage = errorMessage + response.getStatusMessage();
            }
            errorCollection.add(errorMessage);
            log.error(errorMessage);
        }
        else
        {
            String responseBody = response.getResponseBody();
            if (responseBody != null && !responseBody.isEmpty())
            {
                try
                {
                    return new JSONObject(responseBody);
                }
                catch (JSONException e)
                {
                    String message = "Bamboo Rest Request failed, could not parse response from Bamboo: ";
                    log.error(message, e);
                    errorCollection.add(message + e.getMessage());
                }
            }
            else
            {
                String message = "Bamboo Rest Request failed, no response received from Bamboo.";
                log.error(message);
                errorCollection.add(message);
            }
        }

        return null;
    }

    @NotNull
    protected BambooRestResponse executePostRequest(@NotNull ApplicationLink applicationLink, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
    {
        final ApplicationLinkRequestFactory authenticatedRequestFactory = applicationLink.createAuthenticatedRequestFactory();
        return executePostRequest(authenticatedRequestFactory, url, params, timeout);
    }

    @NotNull
    protected BambooRestResponse executePostRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
    {
        final ApplicationLinkRequest request = authenticatedRequestFactory.createRequest(Request.MethodType.POST, url);
        for (final Map.Entry<String, String> param : params.entrySet())
        {
            request.addRequestParameters(param.getKey(), param.getValue());
        }

        return executeRequest(request, authenticatedRequestFactory, timeout);
    }

    @NotNull
    protected BambooRestResponse executePutRequest(@NotNull ApplicationLink applicationLink, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
    {
        final ApplicationLinkRequestFactory authenticatedRequestFactory = applicationLink.createAuthenticatedRequestFactory();
        return executePutRequest(authenticatedRequestFactory, url, params, timeout);
    }

    @NotNull
    protected BambooRestResponse executePutRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, @NotNull Map<String, String> params, int timeout) throws CredentialsRequiredException
    {
        final UrlBuilder urlBuilder = new UrlBuilder(url);

        for (final Map.Entry<String, String> param : params.entrySet())
        {
            urlBuilder.addParameter(param.getKey(), param.getValue());
        }

        final ApplicationLinkRequest request = authenticatedRequestFactory.createRequest(Request.MethodType.PUT, urlBuilder.asUrlString());
        return executeRequest(request, authenticatedRequestFactory, timeout);
    }

    @NotNull
    protected BambooRestResponse executeGetRequest(@NotNull ApplicationLink applicationLink, @NotNull String url, final int timeout) throws CredentialsRequiredException
    {
        final ApplicationLinkRequestFactory authenticatedRequestFactory = applicationLink.createAuthenticatedRequestFactory();
        return executeGetRequest(authenticatedRequestFactory, url, timeout);
    }

    @NotNull
    protected BambooRestResponse executeGetRequest(@NotNull ApplicationLinkRequestFactory authenticatedRequestFactory, @NotNull String url, final int timeout) throws CredentialsRequiredException
    {
        final ApplicationLinkRequest request = authenticatedRequestFactory.createRequest(Request.MethodType.GET, url);
        return executeRequest(request, authenticatedRequestFactory, timeout);
    }

    @NotNull
    protected BambooRestResponse executeRequest(ApplicationLinkRequest request, ApplicationLinkRequestFactory authenticatedRequestFactory, int timeout) throws CredentialsRequiredException
    {
        request.setConnectionTimeout(timeout);
        request.setSoTimeout(timeout);
        return executeRequest(request, authenticatedRequestFactory);
    }

    @NotNull
    protected BambooRestResponse executeRequest(ApplicationLinkRequest request, ApplicationLinkRequestFactory authenticatedRequestFactory) throws CredentialsRequiredException
    {
        try
        {
            BambooRestResponse response = request.execute(new ApplicationLinkResponseHandler<BambooRestResponse>()
            {
                public BambooRestResponse handle(final Response response) throws ResponseException
                {
                    return new BambooRestResponse(response);
                }

                public BambooRestResponse credentialsRequired(Response response) throws ResponseException
                {
                    return new CredentialsRequiredResponse(response);
                }
            });

            if (response instanceof CredentialsRequiredResponse)
            {
                throw new CredentialsRequiredException(authenticatedRequestFactory, "Request failed. Credentials Required");
            }
            else
            {
                return response;
            }
        }
        catch (ResponseException e)
        {
            if (e.getCause() instanceof ConnectException)
            {
                return new BambooRestResponse("Could not connect to Bamboo. The server is unavailable at this time.");
            }
            else
            {
                log.error("Failed to execute request to Bamboo",e);
                return new BambooRestResponse("Failed to execute request: " + e.getMessage());
            }
        }
    }

    private static class CredentialsRequiredResponse extends BambooRestResponse
    {
        private CredentialsRequiredResponse(Response response)
        {
            super(response);
        }
    }

    enum TriggerType
    {
        NEW,
        EXISTING
    }
}
