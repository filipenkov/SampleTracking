package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanStatus;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.plugin.ext.bamboo.release.ReleaseFinalisingAction;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class BambooReleaseServiceImpl implements BambooReleaseService, LifecycleAware
{
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger log = Logger.getLogger(BambooReleaseServiceImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    private static final String PLUGIN_PREFIX = "com.atlassian.bamboo.plugin.jira";
    private static final String TRIGGER_REASON = PLUGIN_PREFIX + ":jiraReleaseTriggerReason";
    private static final String PLAN_TRIGGER = PLUGIN_PREFIX + ":jiraReleasePlanTrigger";

    private static final String CUSTOM_TRIGGER_REASON_KEY = "bamboo.customTriggerReasonKey";
    private static final String CUSTOM_PLAN_TRIGGER_KEY = "bamboo.customPlanTriggerKey";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_USERNAME = "bamboo.triggerReason.jiraUsername";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_USER_DISPLAY_NAME = "bamboo.triggerReason.jiraUserDisplayName";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_PROJECT_NAME = "bamboo.triggerReason.jiraProjectName";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_PROJECT_KEY = "bamboo.triggerReason.jiraProjectKey";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_VERSION_NAME = "bamboo.triggerReason.jiraVersion";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_VERSION_ID = "bamboo.triggerReason.jiraVersionId";
    private static final String BAMBOO_TRIGGER_REASON_JIRA_BASE_URL = "bamboo.triggerReason.jiraBaseUrl";

    // ------------------------------------------------------------------------------------------------- Type Properties

    private final Function<Version, ManagedLock> versionReleaseLocks = ManagedLocks.weakManagedLockFactory();

    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final BambooRestService bambooRestService;
    private final ReleaseErrorReportingService releaseErrorReportingService;
    private final PlanStatusUpdateService planStatusUpdateService;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final VersionManager versionManager;
    private final IssueManager issueManager;
    private final SearchProvider searchProvider;
    private final UserManager userManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BambooReleaseServiceImpl(final BambooRestService bambooRestService, final ReleaseErrorReportingService releaseErrorReportingService,
                                    final PlanStatusUpdateService planStatusUpdateService, final PluginSettingsFactory pluginSettingsFactory,
                                    final VersionManager versionManager, final IssueManager issueManager, final SearchProvider searchProvider,
                                    final UserManager userManager, final ProjectManager projectManager,
                                    final PermissionManager permissionManager,
                                    final JiraAuthenticationContext authenticationContext)
    {
        this.bambooRestService = bambooRestService;
        this.releaseErrorReportingService = releaseErrorReportingService;
        this.planStatusUpdateService = planStatusUpdateService;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.versionManager = versionManager;
        this.issueManager = issueManager;
        this.searchProvider = searchProvider;
        this.userManager = userManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods

    @NotNull
    public PlanExecutionResult triggerPlanForRelease(@NotNull ApplicationLink applicationLink, @NotNull Version version, @NotNull Map<String, String> settings) throws CredentialsRequiredException
    {
        final Project project = version.getProjectObject();
        final String projectKey = project.getKey();

        clearErrors(version);
        clearBuildData(projectKey, version.getId());
        saveConfigData(projectKey, version, settings);

        final Map<String, String> params = populateParams(version, settings, project);

        PlanKey planKey = PlanKeys.getPlanKey(settings.get(PluginConstants.PS_CONFIG_PLAN));
        String stage = settings.get(PluginConstants.PS_CONFIG_STAGE);
        final RestResult<PlanResultKey> restResult = bambooRestService.triggerPlan(applicationLink, planKey, stage, params);

        PlanResultKey planResultKey = restResult.getResult();
        if (planResultKey != null && restResult.getErrors().isEmpty())
        {
            String userName = settings.get(PluginConstants.PS_CONFIG_USER_NAME);
            subscribeToReleaseStatus(version, userName, planResultKey);
            registerReleaseStarted(projectKey, version.getId(), planResultKey);
        }

        return new PlanExecutionResult(restResult.getResult(), restResult.getErrors());
    }

    @NotNull
    public PlanExecutionResult triggerExistingBuildForRelease(@NotNull final ApplicationLink applicationLink, @NotNull Version version, @NotNull final PlanResultKey planResultKey, @NotNull final Map<String, String> settings) throws CredentialsRequiredException
    {
        Project project = version.getProjectObject();

        final String projectKey = project.getKey();

        clearErrors(version);
        clearBuildData(projectKey, version.getId());
        saveConfigData(projectKey, version, settings);

        final Map<String, String> params = populateParams(version, settings, project);

        String stage = settings.get(PluginConstants.PS_CONFIG_STAGE);
        final RestResult<PlanResultKey> restResult = bambooRestService.continuePlan(applicationLink, planResultKey, stage, params);

        if (restResult.getErrors().isEmpty())
        {
            String userName = settings.get(PluginConstants.PS_CONFIG_USER_NAME);
            subscribeToReleaseStatus(version, userName, planResultKey);
            registerReleaseStarted(projectKey, version.getId(), planResultKey);
        }

        return new PlanExecutionResult(restResult.getResult(), restResult.getErrors());
    }

    public void releaseWithNoBuild(final Version version, final Map<String, String> releaseConfig)
    {
        final Project project = version.getProjectObject();
        final String projectKey = project.getKey();
        final Long versionId = version.getId();

        clearErrors(version);
        clearConfigData(projectKey, versionId);
        clearBuildData(projectKey, versionId);
        saveConfigData(projectKey, version, releaseConfig);
        doJiraRelease(version, releaseConfig);
    }

    public boolean releaseIfRequired(@NotNull final PlanStatus planStatus, @NotNull final Version version)
    {
        try
        {
            return versionReleaseLocks.get(version).withLock(new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    return releaseWithoutLock(version, planStatus);
                }
            });
        }
        catch (Exception e)
        {
            log.error("Unexpected Error Has Occured Performing Release", e);
        }

        return false;
    }

    public void onStart()
    {
        final Thread thread = new Thread() {
            @Override
            public void run()
            {
                for (final Project project : projectManager.getProjectObjects())
                {
                    final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(project.getKey());
                    for (final Version version : project.getVersions())
                    {
                        final Map<String, String> config = (Map<String, String>)settingsForKey.get(PluginConstants.PS_BUILD_DATA_KEY + version.getId());
                        if (config != null)
                        {
                            final String resultKey = config.get(PluginConstants.PS_BUILD_RESULT);
                            final boolean isCompleted = config.containsKey(PluginConstants.PS_BUILD_COMPLETED_STATE);
                            final String username = config.get(PluginConstants.PS_CONFIG_USER_NAME);

                            if (StringUtils.isNotEmpty(resultKey) && username!=null && !isCompleted)
                            {
                                final PlanResultKey planResultKey = PlanKeys.getPlanResultKey(resultKey);
                                subscribeToReleaseStatus(version, username, planResultKey);
                            }
                        }
                    }
                }
            }
        };
        thread.setName("Bamboo Release Management Startup");
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            public void uncaughtException(final Thread thread, final Throwable throwable)
            {
                log.error("Could not start Bamboo Release Management Plugin", throwable);
            }
        });
        thread.start();
    }

    public void resetReleaseStateIfVersionWasUnreleased(@NotNull Version version)
    {
        Project project = version.getProjectObject();

        final String projectKey = project.getKey();
        final Long versionId = version.getId();

        if (!version.isReleased() && getBuildState(projectKey, versionId) == BuildState.SUCCESS && isReleaseCompleted(projectKey, versionId))
        {
            clearConfigData(projectKey, versionId);
            clearBuildData(projectKey, versionId);
        }
    }

    public void clearConfigData(@NotNull String projectKey, long versionId)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        settingsForKey.remove(PluginConstants.PS_CONFIG_DATA_KEY + versionId);
    }

    @Nullable
    public Map<String, String> getConfigData(@NotNull String projectKey, long versionId)
    {
        return getConfigMap(projectKey, PluginConstants.PS_CONFIG_DATA_KEY + versionId);
    }

    @NotNull
    public Map<String, String> getDefaultSettings(@NotNull final String projectKey)
    {
        Map<String, String> settings = getConfigMap(projectKey, PluginConstants.PS_CONFIG_DEFAUTS_KEY);
        return (settings != null) ? settings : Maps.<String, String>newHashMap();
    }

    @Nullable
    public Map<String, String> getBuildData(@NotNull final String projectKey, long versionId)
    {
        return getConfigMap(projectKey, PluginConstants.PS_BUILD_DATA_KEY + versionId);
    }

    public void registerReleaseStarted(@NotNull String projectKey, long versionId, @NotNull PlanResultKey planResultKey)
    {
        Map<String, String> data = new HashMap<String,String>();
        data.put(PluginConstants.PS_BUILD_RESULT, planResultKey.getKey());

        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        settingsForKey.put(PluginConstants.PS_BUILD_DATA_KEY + versionId, data);
    }

    /**
     * Records the end of a release build for this version.  Only called after entire release is complete.
     * @param projectKey of the project the version belongs to
     * @param versionId of the version to remove associated configuration
     * @param planResultKey of the release build associated with this version
     * @param buildState - the status of the completed bamboo build.
     */
    private void registerReleaseFinished(@NotNull String projectKey, long versionId, @NotNull PlanResultKey planResultKey, BuildState buildState)
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put(PluginConstants.PS_BUILD_RESULT, planResultKey.getKey());
        data.put(PluginConstants.PS_BUILD_COMPLETED_STATE, buildState.name());
        data.put(PluginConstants.PS_RELEASE_COMPLETE, Boolean.TRUE.toString());

        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        settingsForKey.put(PluginConstants.PS_BUILD_DATA_KEY + versionId, data);
    }

    public boolean hasPermissionToRelease(@NotNull User user, @NotNull Project project)
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user) || permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    @NotNull
    public Map<String, String> getBambooVariablesFromMap(@NotNull Map<String, String> toFilter, @NotNull String prefixSubstitute)
    {
        Map<String, String> variables = new HashMap<String, String>();
        for (String key : toFilter.keySet())
        {
            if (key.startsWith(PluginConstants.VARIABLE_PARAM_PREFIX))
            {
                String variableKey = key.substring(PluginConstants.VARIABLE_PARAM_PREFIX.length());
                String variableValue = toFilter.get(key);
                if (StringUtils.isNotBlank(variableValue))
                {
                    variables.put(prefixSubstitute + variableKey, variableValue);
                }
            }
        }
        return variables;
    }

    private void doJiraRelease(Version version, Map<String, String> releaseConfig)
    {
        if (!version.isReleased())
        {
            //move issues if required
            final String openIssuesAction = releaseConfig.get(PluginConstants.PS_CONFIG_OPEN_ISSUES);
            if (PluginConstants.ISSUE_ACTION_MOVE.equals(openIssuesAction))
            {
                String userName = releaseConfig.get(PluginConstants.PS_CONFIG_USER_NAME);
                User user = userName != null ? userManager.getUserObject(userName) : null;

                String newVersionId = releaseConfig.get(PluginConstants.PS_CONFIG_OPEN_ISSUES_VERSION);
                Version newVersion = versionManager.getVersion(Long.parseLong(newVersionId));

                try
                {
                    final Collection<Issue> issues = getUnresolvedIssues(version, user);
                    if (!issues.isEmpty())
                    {
                        for (final Issue issue : issues)
                        {
                            // Need to look this up from the DB since we have DocumentIssues from the search.
                            final MutableIssue mutableIssue = issueManager.getIssueObject(issue.getId());
                            final Collection<Version> versions = mutableIssue.getFixVersions();
                            versions.remove(version);
                            versions.add(newVersion);
                            mutableIssue.setFixVersions(versions);

                            issueManager.updateIssue(user, mutableIssue, EventDispatchOption.ISSUE_UPDATED, true);
                        }
                    }
                }
                catch (UpdateException e)
                {
                    String message = "Version was not release. Failed to transition unresolved issues (to version " + newVersion + ").  Error: " + e.getMessage();
                    releaseErrorReportingService.recordError(version.getProjectObject().getKey(), version.getId(), message);
                    log.error(message, e);
                    return;
                }
                catch (Exception e)
                {
                    String message = "Version was not release. Failed to transition unresolved issues (to version " + newVersion + ").  Error: " + e.getMessage();
                    releaseErrorReportingService.recordError(version.getProjectObject().getKey(), version.getId(), message);
                    log.error(message, e);
                    return;
                }
            }

            version.setReleaseDate(new Date());
            version.setReleased(true);
            versionManager.releaseVersion(version, true);
        }
    }


    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    private Map<String, String> populateParams(final Version version, final Map<String, String> settings, final Project project)
    {
        final Map<String, String> params = Maps.newHashMap();

        String userName = authenticationContext.getLoggedInUser().getName();
        String userDisplayName = authenticationContext.getLoggedInUser().getDisplayName();
        
        params.put(CUSTOM_PLAN_TRIGGER_KEY, PLAN_TRIGGER);
        params.put(CUSTOM_TRIGGER_REASON_KEY, TRIGGER_REASON);
        params.put(BAMBOO_TRIGGER_REASON_JIRA_USERNAME, userName);
        params.put(BAMBOO_TRIGGER_REASON_JIRA_USER_DISPLAY_NAME, userDisplayName);
        params.put(BAMBOO_TRIGGER_REASON_JIRA_PROJECT_NAME, project.getName());
        params.put(BAMBOO_TRIGGER_REASON_JIRA_PROJECT_KEY, project.getKey());
        params.put(BAMBOO_TRIGGER_REASON_JIRA_VERSION_NAME, version.getName());
        params.put(BAMBOO_TRIGGER_REASON_JIRA_VERSION_ID, version.getId().toString());

        String baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL); //ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
        params.put(BAMBOO_TRIGGER_REASON_JIRA_BASE_URL, baseUrl);

        params.putAll(getBambooVariablesFromMap(settings, PluginConstants.VARIABLE_REST_PREFIX));
        return params;
    }

    private boolean releaseWithoutLock(final Version version, final PlanStatus planStatus)
    {
        final Project project = version.getProjectObject();

        boolean released = false;
        if (!version.isReleased())
        {
            if (BuildState.SUCCESS == planStatus.getBuildState())
            {
                final Map<String, String> releaseConfig = getConfigData(project.getKey(), version.getId());

                log.info("Bamboo Release Plugin releasing version " + version.getName() + " of project " + project.getKey());

                if (releaseConfig != null)
                {
                    // release version
                    doJiraRelease(version, releaseConfig);
                    released = true;
                    // clear out config
                    clearConfigData(project.getKey(), version.getId());
                }
                else
                {
                    final String errorMessage = "Release Build " + planStatus.getPlanResultKey() + " has completed but not record can be " +
                                                "found of triggering the release.  Version was not released.";
                    releaseErrorReportingService.recordError(project.getKey(), version.getId(), errorMessage);
                    log.error(errorMessage);
                }
            }

            registerReleaseFinished(project.getKey(), version.getId(), planStatus.getPlanResultKey(), planStatus.getBuildState() != null ? planStatus.getBuildState() : BuildState.UNKNOWN);
        }
        return released;
    }

    private Collection<Issue> getUnresolvedIssues(Version version, User user)
    {
        try
        {
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(version.getProjectObject().getId()).and().unresolved();
            builder.and().fixVersion(version.getId());

            final SearchResults results = searchProvider.search(builder.buildQuery(), user, PagerFilter.getUnlimitedFilter());
            return results.getIssues();
        }
        catch (final Exception e)
        {
            log.error("Exception whilst getting unresolved issues " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @Nullable
    private Map<String, String> getConfigMap(String projectKey, String configKey)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        final Object o = settingsForKey.get(configKey);
        if (o != null && Map.class.isAssignableFrom(o.getClass()))
        {
            //noinspection unchecked
            return (Map<String, String>) o;
        }
        else
        {
            return null;
        }
    }

    private void subscribeToReleaseStatus(@NotNull final Version version, @NotNull final String username, @NotNull final PlanResultKey planResultKey)
    {
        planStatusUpdateService.subscribe(version, planResultKey, username, new ReleaseFinalisingAction(version.getId(), versionManager, this, releaseErrorReportingService));
    }

    private void saveConfigData(@NotNull String projectKey, @NotNull Version version, @NotNull Map<String, String> config)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        settingsForKey.put(PluginConstants.PS_CONFIG_DATA_KEY + version.getId(), config);
        settingsForKey.put(PluginConstants.PS_CONFIG_DEFAUTS_KEY, config);
    }

    private void clearErrors(final Version version)
    {
        releaseErrorReportingService.clearErrors(version.getProjectObject().getKey(), version.getId());
    }

    private boolean isReleaseCompleted(@NotNull String projectKey, long versionId)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        final Map<String, String> config = (Map<String, String>)settingsForKey.get(PluginConstants.PS_BUILD_DATA_KEY + versionId);
        return config != null && config.containsKey(PluginConstants.PS_RELEASE_COMPLETE);
    }

    private BuildState getBuildState(@NotNull String projectKey, long versionId)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        final Map<String, String> config = (Map<String, String>)settingsForKey.get(PluginConstants.PS_BUILD_DATA_KEY + versionId);
        if (config != null)
        {
            String state = config.get(PluginConstants.PS_BUILD_COMPLETED_STATE);
            if (StringUtils.isNotEmpty(state))
            {
                try
                {
                    return BuildState.valueOf(state);
                }
                catch (IllegalArgumentException e)
                {
                    log.debug("Could not determine BuildState from Plugin Settings. Falling back to 'UNKNOWN'", e);
                }
            }
        }
        return BuildState.UNKNOWN;
    }

    private void clearBuildData(@NotNull String projectKey, long versionId)
    {
        final PluginSettings settingsForKey = pluginSettingsFactory.createSettingsForKey(projectKey);
        settingsForKey.remove(PluginConstants.PS_BUILD_DATA_KEY + versionId);
    }
}
