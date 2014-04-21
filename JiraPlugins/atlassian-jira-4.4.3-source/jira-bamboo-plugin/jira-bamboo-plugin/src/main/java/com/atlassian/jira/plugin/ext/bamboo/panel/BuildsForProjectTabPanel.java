package com.atlassian.jira.plugin.ext.bamboo.panel;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.log4j.Logger;

public class BuildsForProjectTabPanel extends AbstractProjectTabPanel
{
    private static final Logger log = Logger.getLogger(BuildsForVersionTabPanel.class);

    private static final String BAMBOO_PLUGIN_MODULE_KEY = "bamboo-project-tabpanel";

    private final BambooPanelHelper bambooPanelHelper;
    private final PermissionManager permissionManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;

    public BuildsForProjectTabPanel(BambooPanelHelper bambooPanelHelper,
                                    PermissionManager permissionManager,
                                    FieldVisibilityManager fieldVisibilityManager,
                                    BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.bambooPanelHelper = bambooPanelHelper;
        this.permissionManager = permissionManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    
    // ----------------------------------------------------------------------------------------------- Interface Methods
    @Override
    public String getHtml(BrowseContext context)
    {

        final Project project = context.getProject();

        final Map velocityParams = EasyMap.build("fieldVisibility", fieldVisibilityManager, "portlet", this);

        final String baseLinkUrl = "/browse/" + project.getKey() +
                             "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + BAMBOO_PLUGIN_MODULE_KEY;
        final String queryString = "projectKey=" + project.getKey();

        bambooPanelHelper.prepareVelocityContext(velocityParams, BAMBOO_PLUGIN_MODULE_KEY, baseLinkUrl, queryString,
                BambooPanelHelper.SUB_TABS, project);

        return getDescriptor().getHtml("view", velocityParams);
    }
    
    protected ProjectTabPanelModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public boolean showPanel(final BrowseContext context)
    {
         return  bambooApplicationLinkManager.hasApplicationLinks() &&
                 permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, context.getProject(),  context.getUser());
    }


    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------------------- private Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}