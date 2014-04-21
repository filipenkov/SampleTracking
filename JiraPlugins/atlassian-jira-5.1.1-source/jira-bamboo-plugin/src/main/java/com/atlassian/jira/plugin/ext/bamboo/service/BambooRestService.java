package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooPlan;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooProject;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Accessor for the Bamboo REST API. All calls are synchronous and may block
 */
public interface BambooRestService
{
    /**
     * Lists all Projects and Plans on the Bamboo Server represented by {@link ApplicationLink}
     * @param applicationLink to retrieve plans from
     * @param includeDisabled if false will only retrieve enabled plans, otherwise all will be returned.
     * @return map of {@link BambooProject} to plans {@link BambooPlan}s
     * @throws CredentialsRequiredException if authentication dance required
     */
    @NotNull
    RestResult<Map<BambooProject, List<BambooPlan>>> getPlanList(@NotNull ApplicationLink applicationLink, boolean includeDisabled) throws CredentialsRequiredException;

    /**
     * Directly get the {@link PlanStatus} for the given {@link PlanResultKey} at the Bamboo instance using the provide requestFactory
     * If possible you should use the {@link PlanStatusUpdateService}
     *
     * @param applicationLinkRequestFactory - the request factory to be used to get Plan Status
     * @param planResultKey to get status of
     * @return buildState
     * @throws CredentialsRequiredException if authentication danc required
     */
    @NotNull
    RestResult<PlanStatus> getPlanStatus(@NotNull ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull PlanResultKey planResultKey) throws CredentialsRequiredException;

    /**
     * Directly get the JSON for the given {@link PlanResultKey} at the Bamboo instance using the provide requestFactory
     *
     * This call adds "?expand=artifacts.artifact,labels.label" to the request
     *
     * @param applicationLinkRequestFactory
     * @param planResultKey
     * @return jsonObject
     * @throws CredentialsRequiredException
     */
    @NotNull
    RestResult<JSONObject> getPlanResultJson(@NotNull ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull PlanResultKey planResultKey) throws CredentialsRequiredException;

    /**
     * Directly get the JSON for the given {@link PlanKey} at the Bamboo instance using the provide requestFactory
     *
     * This call adds "?expand=stages,variableContext" to the request
     *
     * @param applicationLinkRequestFactory
     * @param planKey
     * @return jsonObject
     * @throws CredentialsRequiredException
     */
    @NotNull
    RestResult<JSONObject> getPlanJson(@NotNull ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull PlanKey planKey) throws CredentialsRequiredException;

    /**
     * Load the latest results for the given {@link PlanKey}
     * @param applicationLinkRequestFactory
     * @param planKey
     * @param numberOfResults to load
     * @param urlParams to pass on
     * @return jsonObject
     * @throws CredentialsRequiredException
     * @throws IllegalArgumentException if numberOfResults less than or equal to 0
     */
    RestResult<JSONObject> getPlanHistory(@NotNull ApplicationLinkRequestFactory applicationLinkRequestFactory, @NotNull PlanKey planKey, int numberOfResults, @NotNull Map<String, String> urlParams) throws CredentialsRequiredException;

    /**
     * Directly execute the plan at the Bamboo instance represented by {@link ApplicationLink} with optional params
     * @param applicationLink to the Bamboo instance with the plan.
     * @param planKey of the plan to execute
     * @param stage the name of the manual stage (if any) that you want to run automatically.  If there is more than one manual stage, only the last one should be specified
     * @param params to send with the rest request to Bamboo
     * @return RestResult containing planResultKey if successful otherwise any error messages.
     * @throws CredentialsRequiredException if authentication is required.
     */
    @NotNull
    RestResult<PlanResultKey> triggerPlan(@NotNull ApplicationLink applicationLink, @NotNull PlanKey planKey, @Nullable String stage, @NotNull Map<String, String> params) throws CredentialsRequiredException;

    /**
     * Directly continue the plan at the Bamboo instance represented by {@link ApplicationLink} with optional params
     * @param applicationLink to the Bamboo instance with the plan.
     * @param planResultKey of the plan to continue
     * @param stage the name of the manual stage (if any) that you want to run automatically.  If there is more than one manual stage, only the last one should be specified
     * @param params to send with the rest request to Bamboo
     * @return RestResult containing planResultKey if successful otherwise any error messages.
     * @throws CredentialsRequiredException if authentication is required.
     */
    @NotNull
    RestResult<PlanResultKey> continuePlan(@NotNull ApplicationLink applicationLink, @NotNull PlanResultKey planResultKey, @Nullable String stage, @NotNull Map<String, String> params) throws CredentialsRequiredException;
}
