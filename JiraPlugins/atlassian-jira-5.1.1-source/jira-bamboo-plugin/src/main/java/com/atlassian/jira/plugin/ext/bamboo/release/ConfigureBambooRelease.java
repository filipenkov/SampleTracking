package com.atlassian.jira.plugin.ext.bamboo.release;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooPlan;
import com.atlassian.jira.plugin.ext.bamboo.model.BambooProject;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKey;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanKeys;
import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import com.atlassian.jira.plugin.ext.bamboo.model.RestResult;
import com.atlassian.jira.plugin.ext.bamboo.panel.BambooPanelHelper;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooRestService;
import com.atlassian.jira.plugin.ext.bamboo.service.PlanExecutionResult;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import webwork.action.ActionContext;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigureBambooRelease extends ProjectActionSupport
{

    // ------------------------------------------------------------------------------------------------- Type Properties
    private long versionId;
    private Version version;
    private String selectedPlanKey;
    private String selectedStages;
    private String buildType;
    private String buildResult; 

    private String variablesJson;

    private Map<BambooProject, List<BambooPlan>> plansByProject;

    private Collection<Version> versions;
    private int openIssueCount;
    private String unresolved;
    private Long moveUnfixedIssuesTo;
    private URI credentialsUrl;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private BambooApplicationLinkManager bambooApplicationLinkManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private VersionManager versionManager;
    private BambooRestService bambooRestService;
    private SearchProvider searchProvider;
    private BambooReleaseService bambooReleaseService;
    private I18nHelper i18nHelper;

    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doInput()
    {
        version = versionManager.getVersion(versionId);
        if (version == null)
        {
            addErrorMessage("Failed to release, no version with id '" + versionId + "' could be found");
            return INPUT;
        }

        Project projectObject = version.getProjectObject();
        final ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(projectObject.getKey());
        if (applicationLink == null)
        {
            addErrorMessage("No Bamboo Application Link has been defined for this JIRA Project.  Please configure one and try again.");
            return INPUT;
        }

        final ApplicationLinkRequestFactory authenticatedRequestFactory = applicationLink.createAuthenticatedRequestFactory();
        if (authenticatedRequestFactory == null)
        {
            addErrorMessage("JIRA cannot find a way to connect with Bamboo. Please configure an Outgoing Authentication Type in the Application Links configuration");
            return INPUT;
        }
        
        // setup data for config
        try
        {
            RestResult<Map<BambooProject, List<BambooPlan>>> result = bambooRestService.getPlanList(applicationLink, false);
            plansByProject = result.getResult();
            if (plansByProject == null || !result.getErrors().isEmpty())
            {
                addErrorMessages(result.getErrors());
                return ERROR;
            }
        }
        catch (CredentialsRequiredException e)
        {
            log.debug(e.getMessage(), e);

            //Seems this was not able to be injected via a setter.
            ApplicationProperties applicationProperties = ComponentManager.getOSGiComponentInstanceOfType(ApplicationProperties.class);

            String baseLinkUrl = getBaseUrl(projectObject, version);
            String jiraBaseUrl = applicationProperties.getBaseUrl();
            credentialsUrl = e.getAuthorisationURI(URI.create(jiraBaseUrl + baseLinkUrl));
            return INPUT;
        }

        versions = versionManager.getVersionsUnreleased(projectObject.getId(), true);
        versions.remove(version);
        openIssueCount = getUnresolvedIssues(projectObject.getId(), versionId).size();

        Map<String, String> previousConfiguration = bambooReleaseService.getConfigData(projectObject.getKey(), versionId);
        if (previousConfiguration != null)
        {
            useSettingsAsDefaults(previousConfiguration);
        }
        else
        {
            useSettingsAsDefaults(bambooReleaseService.getDefaultSettings(projectObject.getKey()));
        }

        //Set default build type
        if (StringUtils.isBlank(buildType))
        {
            buildType = PluginConstants.BUILD_TYPE_NEW_BUILD;
        }

        return INPUT;
    }

    private void useSettingsAsDefaults(@NotNull final Map<String, String> settings)
    {
        buildType = settings.get(PluginConstants.PS_CONFIG_BUILD_TYPE);
        selectedPlanKey = settings.get(PluginConstants.PS_CONFIG_PLAN);
        unresolved = settings.get(PluginConstants.PS_CONFIG_OPEN_ISSUES);
        moveUnfixedIssuesTo = NumberUtils.createLong(settings.get(PluginConstants.PS_CONFIG_OPEN_ISSUES_VERSION));
        selectedStages = settings.get(PluginConstants.PS_CONFIG_STAGE);

        Map<String, String> variables = bambooReleaseService.getBambooVariablesFromMap(settings, "");
        if (!variables.isEmpty())
        {
            JSONObject variablesJsonObject = new JSONObject();
            try
            {
                variablesJsonObject.put("variables", variables);
                variablesJson = variablesJsonObject.toString();
            }
            catch (JSONException e)
            {
                // don't do anything we don't really care.
            }
        }
    }

    public String doExecute()
    {
        version = versionManager.getVersion(versionId);
        Project projectObject = version.getProjectObject();
        Map<String, String> settings = new HashMap<String, String>();

        User user = jiraAuthenticationContext.getLoggedInUser();
        settings.put(PluginConstants.PS_CONFIG_USER_NAME, user.getName());
        settings.put(PluginConstants.PS_CONFIG_OPEN_ISSUES, unresolved);
        if (PluginConstants.ISSUE_ACTION_MOVE.equals(unresolved))
        {
            settings.put(PluginConstants.PS_CONFIG_OPEN_ISSUES_VERSION, moveUnfixedIssuesTo.toString());
        }
    
        //Set default build type
        settings.put(PluginConstants.PS_CONFIG_BUILD_TYPE, buildType);


        final boolean existingBuild = PluginConstants.BUILD_TYPE_EXISTING_BUILD.equals(buildType);
        final boolean newBuild = PluginConstants.BUILD_TYPE_NEW_BUILD.equals(buildType);
        if (newBuild || existingBuild)
        {
            PlanKey planKey = PlanKeys.getPlanKey(selectedPlanKey);


            settings.put(PluginConstants.PS_CONFIG_PLAN, planKey.getKey());
            if (StringUtils.isNotBlank(selectedStages))
            {
                settings.put(PluginConstants.PS_CONFIG_STAGE, selectedStages);
            }

            Map<String, String> variables = filterVariableParams();
            if (!variables.isEmpty())
            {
                settings.putAll(variables);
            }

            final ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(projectObject.getKey());
            if (applicationLink != null)
            {
                try
                {
                    PlanExecutionResult result;
                    if (newBuild)
                    {
                        result = bambooReleaseService.triggerPlanForRelease(applicationLink, version, settings);
                    }
                    else //existing build
                    {
                        final PlanResultKey planResultKey = PlanKeys.getPlanResultKey(buildResult);
                        result = bambooReleaseService.triggerExistingBuildForRelease(applicationLink, version, planResultKey, settings);
                    }

                    if (!result.getErrors().isEmpty())
                    {
                        for (String error : result.getErrors())
                        {
                            addErrorMessage(error);
                        }
                        return ERROR;
                    }
                }
                catch (CredentialsRequiredException e)
                {
                    log.error(e.getMessage());
                    addErrorMessage("Please Authenticate and try again: " + e.getAuthorisationURI());
                }
                catch (Exception e)
                {
                    log.error("Bamboo failed to trigger the release build for version " + version.getName(), e);
                    addErrorMessage("An error occurred when trying to trigger the release build. " + e.getMessage());
                }
            }
        }
        else
        {
            bambooReleaseService.releaseWithNoBuild(version, settings);
        }

        return returnComplete(getBaseUrl(projectObject, version));
    }

    public void doValidation()
    {
        version = versionManager.getVersion(versionId);
        if (version == null)
        {
            addErrorMessage("Failed to release, no version with id '" + versionId + "' could be found");
        }
        else
        {
            if (PluginConstants.BUILD_TYPE_NEW_BUILD.equals(buildType))
            {
                Project projectObject = version.getProjectObject();
                if (projectObject == null)
                {
                    addErrorMessage("Could not determine which JIRA Project the version " + version.getName() + " belongs to.");
                }
                else
                {
                    final ApplicationLink applicationLink = bambooApplicationLinkManager.getApplicationLink(projectObject.getKey());
                    if (applicationLink == null)
                    {
                        addErrorMessage("No Bamboo Application Link has been defined for this JIRA Project.  Please configure one and try again.");
                    }
                }

                if (StringUtils.isBlank(selectedPlanKey))
                {
                    addError("selectedPlanKey", "Please select a valid plan key");
                    //addErrorMessage("Please select a valid plan key");
                }
            }

            final Project project = version != null ? version.getProjectObject() : null;
            if (project != null && !bambooReleaseService.hasPermissionToRelease(jiraAuthenticationContext.getLoggedInUser(), project))
            {
                addErrorMessage(i18nHelper.getText("admin.errors.version.no.permission"));
            }
        }

        if (hasAnyErrors())
        {
            doInput(); //prep data
        }
    }
    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Helper Methods

    /**
     * @param projectId the version is in
     * @param versionId teh version if to find the issues for
     *
     * @return a list of issues that have not been resolved for the current fix for version selected for release
     */
    private List<Issue> getUnresolvedIssues(long projectId, long versionId)
    {
        try
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(projectId).and().unresolved();
            builder.and().fixVersion(versionId);

            final SearchResults results = searchProvider.search(builder.buildQuery(), getLoggedInUser(), PagerFilter.getUnlimitedFilter());
            final List<Issue> issues = results.getIssues();
            return (issues == null) ? Collections.<Issue>emptyList() : issues;
        }
        catch (final Exception e)
        {
            addErrorMessage(getText("admin.errors.project.exception", e));
            log.error("Exception whilst getting unresolved issues " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String getBaseUrl(Project project, Version version)
    {

        return "/browse/" + project.getKey() +
               "/fixforversion/" + version.getId() +
               "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + PluginConstants.BAMBOO_RELEASE_TABPANEL_MODULE_KEY;
    }

    public Map<String, String> filterVariableParams()
    {
        Map<String, String> variables = new HashMap<String, String>();
        Map<String, Object> parameters = ActionContext.getContext().getParameters();

        for (String key : parameters.keySet())
        {
            if (key.startsWith(PluginConstants.VARIABLE_PARAM_PREFIX))
            {
                final String[] value = (String[]) parameters.get(key);
                String variableValue = value != null && value.length > 0 ? value[0] : null;
                if (!StringUtils.isEmpty(variableValue))
                {
                    variables.put(key, variableValue);
                }
            }
        }

        return variables;
    }
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public Version getVersion()
    {
        return version;
    }

    public void setVersionId(long versionId)
    {
        this.versionId = versionId;
    }

    public long getVersionId()
    {
        return versionId;
    }

    public String getUnresolved()
    {
        return unresolved;
    }

    public void setUnresolved(String unresolved)
    {
        this.unresolved = unresolved;
    }

    public Long getMoveUnfixedIssuesTo()
    {
        return moveUnfixedIssuesTo;
    }

    public void setMoveUnfixedIssuesTo(Long moveUnfixedIssuesTo)
    {
        this.moveUnfixedIssuesTo = moveUnfixedIssuesTo;
    }

    public String getBuildType()
    {
        return buildType;
    }

    public void setBuildType(String buildType)
    {
        this.buildType = buildType;
    }

    public Map<BambooProject, List<BambooPlan>> getPlansByProject()
    {
        return plansByProject;
    }

    public Collection<Version> getVersions()
    {
        return versions;
    }

    public URI getCredentialsUrl()
    {
        return credentialsUrl;
    }

    public int getOpenIssueCount()
    {
        return openIssueCount;
    }

    public void setSelectedPlanKey(String selectedPlanKey)
    {
        this.selectedPlanKey = selectedPlanKey;
    }

    public String getSelectedPlanKey()
    {
        return selectedPlanKey;
    }

    public String getVariablesJson()
    {
        return variablesJson;
    }

    public void setBambooApplicationLinkManager(BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    public void setVersionManager(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public void setSearchProvider(SearchProvider searchProvider)
    {
        this.searchProvider = searchProvider;
    }

    public void setBambooRestService(final BambooRestService bambooRestService)
    {
        this.bambooRestService = bambooRestService;
    }

    public void setBambooReleaseService(final BambooReleaseService bambooReleaseService)
    {
        this.bambooReleaseService = bambooReleaseService;
    }

    public void setJiraAuthenticationContext(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public void setI18nHelper(final I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    public String getSelectedStages()
    {
        return selectedStages;
    }

    public void setSelectedStages(String selectedStages)
    {
        this.selectedStages = selectedStages;
    }

    public void setBuildResult(final String buildResult)
    {
        this.buildResult = buildResult;
    }
}
