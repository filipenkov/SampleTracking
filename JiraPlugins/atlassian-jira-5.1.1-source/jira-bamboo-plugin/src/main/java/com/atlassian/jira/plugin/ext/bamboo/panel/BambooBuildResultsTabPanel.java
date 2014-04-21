package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;

import java.util.List;

public class BambooBuildResultsTabPanel extends AbstractIssueTabPanel
{
    private static final Logger log = Logger.getLogger(BambooBuildResultsTabPanel.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String VIEW_BUILD_RESULTS_ACTION_URL = "/ajax/build/viewBuildResultsByJiraKey.action";
    public static final String VIEW_PLANS_ACTION_URL = "/ajax/build/viewPlansByJiraKey.action";

    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final BambooPanelHelper bambooPanelHelper;
    private final PermissionManager permissionManager;
    private final BambooApplicationLinkManager bambooApplicationLinkManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public BambooBuildResultsTabPanel(PermissionManager permissionManager,
                                      BambooPanelHelper bambooPanelHelper,
                                      BambooApplicationLinkManager bambooApplicationLinkManager)
    {
        this.permissionManager = permissionManager;
        this.bambooPanelHelper = bambooPanelHelper;
        this.bambooApplicationLinkManager = bambooApplicationLinkManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public List getActions(Issue issue, User remoteUser)
    {
        return EasyList.build(new BambooBuildResultsAction(bambooPanelHelper, issue, descriptor));
    }

    public boolean showPanel(Issue issue, User remoteUser)
    {
        return bambooApplicationLinkManager.hasApplicationLinks()
                && permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, issue, remoteUser);
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods

}
