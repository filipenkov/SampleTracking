package com.atlassian.jira.plugin.ext.bamboo.release;

import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.panel.BambooPanelHelper;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.ext.bamboo.service.ReleaseErrorReportingService;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.impl.GenericTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class ReleaseForVersionTabPanel extends GenericTabPanel
{
    // ------------------------------------------------------------------------------------------------------- Constants



    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final BambooApplicationLinkManager bambooApplicationLinkManager;
    private final PermissionManager permissionManager;
    private final BambooPanelHelper bambooPanelHelper;
    private final BambooReleaseService bambooReleaseService;
    private final ReleaseErrorReportingService releaseErrorReportingService;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ReleaseForVersionTabPanel(JiraAuthenticationContext authenticationContext,
                                     SearchProvider searchProvider,
                                     BambooApplicationLinkManager bambooApplicationLinkManager,
                                     PermissionManager permissionManager,
                                     BambooPanelHelper bambooPanelHelper,
                                     BambooReleaseService bambooReleaseService,
                                     ReleaseErrorReportingService releaseErrorReportingService)
    {
        super(authenticationContext, searchProvider);
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
        this.permissionManager = permissionManager;
        this.bambooPanelHelper = bambooPanelHelper;
        this.bambooReleaseService = bambooReleaseService;
        this.releaseErrorReportingService = releaseErrorReportingService;
    }
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean showPanel(BrowseVersionContext browseVersionContext)
    {
        return super.showPanel(browseVersionContext) &&
               permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, browseVersionContext.getProject(), authenticationContext.getLoggedInUser());
    }

    @Override
    protected Map<String, Object> createVelocityParams(BrowseVersionContext context)
    {
            //This needs to happen before the release panel is shown
        bambooReleaseService.resetReleaseStateIfVersionWasUnreleased(context.getVersion());

        // super puts context in map @ "versionContext"

        final Map<String, Object> velocityParams = super.createVelocityParams(context);
        final Version version = context.getVersion();

        String baseLinkUrl = getBaseUrl(context.getProject(), version);
        final Long versionId = version.getId();
        final String queryString = "versionId=" + versionId;

        final String projectKey = version.getProjectObject().getKey();
        Map<String, String> buildParams = bambooReleaseService.getBuildData(projectKey, versionId);
        if (buildParams != null)
        {
            velocityParams.put("buildTriggered", true);

            String planResultKey = buildParams.get(PluginConstants.PS_BUILD_RESULT);
            if (StringUtils.isNotBlank(planResultKey))
            {
                velocityParams.put("buildResultKey", planResultKey);
            }

            String state = buildParams.get(PluginConstants.PS_BUILD_COMPLETED_STATE);
            if (StringUtils.isNotBlank(state))
            {
                velocityParams.put("completedState", state);
            }
        }

        List<String> releaseErrors = releaseErrorReportingService.getErrors(projectKey, versionId);
        if (!releaseErrors.isEmpty())
        {
            velocityParams.put("releaseErrors", releaseErrors);
        }

        velocityParams.put("isProjectAdmin", bambooReleaseService.hasPermissionToRelease(context.getUser(), version.getProjectObject()));

        velocityParams.put("hasApplinks", bambooApplicationLinkManager.hasApplicationLinks());

        bambooPanelHelper.prepareVelocityContext(velocityParams, PluginConstants.BAMBOO_RELEASE_TABPANEL_MODULE_KEY, baseLinkUrl, queryString, context.getProject());

        return velocityParams;
    }
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    private String getBaseUrl(Project project, Version version)
    {

         return "/browse/" + project.getKey() +
                             "/fixforversion/" + version.getId() +
                             "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + PluginConstants.BAMBOO_RELEASE_TABPANEL_MODULE_KEY;
    }
}
