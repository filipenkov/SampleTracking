package com.atlassian.jira.plugin.componentpanel.impl;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.impl.VersionDrillDownRenderer;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * This class implements change log tab panel.
 *
 * @since v3.10
 */
public class ChangeLogTabPanel implements ComponentTabPanel
{
    private final VersionManager versionManager;
    private final VersionDrillDownRenderer panelRenderer;
    private final FieldVisibilityManager fieldVisibilityManager;
    private ComponentTabPanelModuleDescriptor descriptor;

    public ChangeLogTabPanel(final VersionManager versionManager, final VersionDrillDownRenderer panelRenderer,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.versionManager = versionManager;
        this.panelRenderer = panelRenderer;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public void init(ComponentTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getHtml(BrowseComponentContext ctx)
    {
        return panelRenderer.getHtml(ctx, descriptor.getCompleteKey(), versionManager.getVersionsReleasedDesc(ctx.getProject().getId(), false));
    }

    public boolean showPanel(BrowseComponentContext ctx)
    {
        if (versionManager.getVersions(ctx.getProject().getId()).isEmpty())
        {
            return false;
        }

        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getComponent().getProjectId(), IssueFieldConstants.COMPONENTS, null)
                && !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS);
    }
}
