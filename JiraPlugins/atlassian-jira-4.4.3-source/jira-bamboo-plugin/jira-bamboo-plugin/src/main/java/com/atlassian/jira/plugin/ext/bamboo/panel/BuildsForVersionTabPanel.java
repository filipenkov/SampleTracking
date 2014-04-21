package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooReleaseService;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.impl.GenericTabPanel;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;

import java.util.Map;

public class BuildsForVersionTabPanel extends GenericTabPanel
{
    private static final Logger log = Logger.getLogger(BuildsForVersionTabPanel.class);


    private static final String BAMBOO_PLUGIN_MODULE_KEY = "bamboo-version-tabpanel";

    private final PermissionManager permissionManager;
    private final BambooPanelHelper bambooPanelHelper;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;
    private final BambooReleaseService bambooReleaseService;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public BuildsForVersionTabPanel(JiraAuthenticationContext authenticationContext,
                                    SearchProvider searchProvider,
                                    BambooPanelHelper bambooPanelHelper,
                                    PermissionManager permissionManager,
                                    BambooApplicationLinkManager bambooApplicationLinkManager,
                                    final BambooReleaseService bambooReleaseService)
    {
        super(authenticationContext, searchProvider);
        this.bambooPanelHelper = bambooPanelHelper;
        this.permissionManager = permissionManager;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
        this.bambooReleaseService = bambooReleaseService;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    @Override
    protected Map<String, Object> createVelocityParams(BrowseVersionContext context)
    {
        final Map<String, Object> velocityParams = getVelocityParamsFromParent(context);
        final Version version = context.getVersion();

        String baseLinkUrl = "/browse/" + context.getProject().getKey() +
                             "/fixforversion/" + version.getId() +
                             "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + BAMBOO_PLUGIN_MODULE_KEY;
        final String queryString = "versionId=" + version.getId();
        if (version.isReleased() && version.getReleaseDate() != null)
        {
            velocityParams.put("extraDescriptionKey", "released.");
        }

        bambooPanelHelper.prepareVelocityContext(velocityParams, BAMBOO_PLUGIN_MODULE_KEY, baseLinkUrl, queryString, BambooPanelHelper.SUB_TABS,
                context.getProject());

        return velocityParams;
    }
    
    @Override
    public boolean showPanel(BrowseVersionContext context)
    {
        //This needs to happen before the release panel is shown
        bambooReleaseService.resetReleaseStateIfVersionWasUnreleased(context.getVersion());

        return shouldShowPanelAccordingToSuperClass(context) && bambooApplicationLinkManager.hasApplicationLinks() &&
               permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, context.getProject(), authenticationContext.getUser());
    }

    protected Map<String, Object> getVelocityParamsFromParent(BrowseVersionContext context)
    {
        return super.createVelocityParams(context);
    }
    
    protected boolean shouldShowPanelAccordingToSuperClass(BrowseVersionContext context)
    {
        return super.showPanel(context);
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Private Methods

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

}
