package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Manages the JIRA Release Bamboo integration
 */
public interface BambooReleaseService
{
    /**
     * Executes the specified Bamboo plan with the JIRA Release Trigger reason and Plan Trigger
     * @param applicationLink for the bamboo server to execute plan on
     * @param version jira version the release build is for
     * @param settings the configuration that is being used for the release
     * @return PlanExecutionResult containing any errors or planKey if successful.
     * @throws CredentialsRequiredException is authentication handshake is required
     */
    @NotNull
    PlanExecutionResult triggerPlanForRelease(@NotNull ApplicationLink applicationLink, @NotNull Version version, @NotNull Map<String, String> settings) throws CredentialsRequiredException;

    /**
     * Executes the specified bamboo result with the JIRA Release Trigger reason and Plan Trigger
     * @param applicationLink for the bamboo server to execute plan on
     * @param version jira version the release build is for
     * @param planResultKey of the build to trigger
     * @param settings the configuration that is being used for the release
     * @return PlanExecutionResult containing any errors or planKey if successful.
     * @throws CredentialsRequiredException is authentication handshake is required
     *
     */
    @NotNull
    PlanExecutionResult triggerExistingBuildForRelease(@NotNull ApplicationLink applicationLink, @NotNull Version version, @NotNull PlanResultKey planResultKey, @NotNull Map<String, String> settings) throws CredentialsRequiredException;

    /**
     * Performs the JIRA related components of the release.
     * @param version to be released
     * @param releaseConfig containing information on e.g. what to do with issues.
     */
    void releaseWithNoBuild(Version version, Map<String, String> releaseConfig);

    /**
     * Tries to release the provided {@link Version} when {@link PlanStatus#getBuildState()} is {@link BuildState#SUCCESS}
     * @param planStatus
     * @param version
     * @return whether release was performed or not
     */
    boolean releaseIfRequired(@NotNull final PlanStatus planStatus, @NotNull Version version);

    /**
     * Remove any release configuration data for th given version
     * @param projectKey of the project the version belongs to
     * @param versionId of the version to remove associated configuration
     */
    void clearConfigData(@NotNull String projectKey, long versionId);

    /**
     * Reset the state of the release management configuration and build data if the version is unreleased and the build state is {@link BuildState#SUCCESS}
     * @param version to reset
     */
    void resetReleaseStateIfVersionWasUnreleased(@NotNull Version version);

    /**
     * Retrieves the configuration data used for the release of that version.
     * Will only exist if release has been triggered and info may still be needed (i.e the release process is not yet complete)
     * @param projectKey of the project the version belongs to
     * @param versionId of the version to remove associated configuration
     * @return the configuration data used for the release of that version.
     */
    @Nullable
    Map<String, String> getConfigData(@NotNull String projectKey, long versionId);

    /**
     * Get the default project settings for release management
     * @param projectKey of the project
     * @return config
     */
    @NotNull
    Map<String, String> getDefaultSettings(@NotNull String projectKey);

    /**
     * Records the start of a release build for this version
     * @param projectKey of the project the version belongs to
     * @param versionId of the version to remove associated configuration
     * @param planResultKey of the release build associated with this version
     */
    void registerReleaseStarted(@NotNull String projectKey, long versionId, @NotNull PlanResultKey planResultKey);

    /**
     * Gets the Build Data for the given version, includes a result key if a build, and whether that build has been
     * detected as complete. Only exists if a build has previously been triggered for this release
     *
     * @param projectKey of the jira project the version belongs to
     * @param versionId  the version to get release info about
     *
     * @return planResultKey
     */
    @Nullable
    Map<String, String> getBuildData(@NotNull String projectKey, long versionId);

    /**
     * Checks if the given {@link User} has permission to release the specified {@link Project}
     * @param user
     * @param project
     * @return permissionToRelease
     */
    boolean hasPermissionToRelease(@NotNull User user, @NotNull Project project);

    /**
     * Filters a map for any key/values which have the variable prefix.  Variables are stored with "variable_" in front.
     * @param toFilter map to filter
     * @param prefixSubstitute the string to substitute bambooVeriable prefix with.
     * @return a map containing only bamboo variables.
     */
    @NotNull
    Map<String, String> getBambooVariablesFromMap(@NotNull Map<String, String> toFilter, @NotNull String prefixSubstitute);
}
