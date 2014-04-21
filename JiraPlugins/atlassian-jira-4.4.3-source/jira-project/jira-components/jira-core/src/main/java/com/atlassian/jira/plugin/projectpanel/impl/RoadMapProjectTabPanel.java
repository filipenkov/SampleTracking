package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * Project Tab Panel displaying upcoming versions and their issues.
 *
 * @since v4.0
 */
public class RoadMapProjectTabPanel implements ProjectTabPanel
{
    private final VersionManager versionManager;
    private final VersionDrillDownRenderer panelRenderer;
    private final FieldVisibilityManager fieldVisibilityManager;
    private ProjectTabPanelModuleDescriptor descriptor;

    public RoadMapProjectTabPanel(final VersionManager versionManager, final VersionDrillDownRenderer panelRenderer,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.versionManager = versionManager;
        this.panelRenderer = panelRenderer;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public void init(ProjectTabPanelModuleDescriptor descriptor)
    {

        this.descriptor = descriptor;
    }

    public String getHtml(BrowseContext ctx)
    {
        return panelRenderer.getHtml(ctx, descriptor.getCompleteKey(), versionManager.getVersionsUnreleased(ctx.getProject().getId(), false));
    }

    public boolean showPanel(BrowseContext ctx)
    {
        if (versionManager.getVersions(ctx.getProject().getId()).isEmpty())
        {
            return false;
        }

        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS);
    }
}