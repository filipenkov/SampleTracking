package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collection;

/**
 * Project Tab Panel displaying past versions and their issues.
 *
 * @since v4.0
 */
public class ChangeLogProjectTabPanel implements ProjectTabPanel
{
    private final VersionManager versionManager;
    private final VersionDrillDownRenderer panelRenderer;
    private final FieldVisibilityManager fieldVisibilityManager;
    private ProjectTabPanelModuleDescriptor descriptor;

    public ChangeLogProjectTabPanel(final VersionManager versionManager, final VersionDrillDownRenderer panelRenderer,
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
        return panelRenderer.getHtml(ctx, descriptor.getCompleteKey(), getVersionsToRenderFor(ctx));
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return !hasNoVersions(ctx.getProject()) && !isFixForVersionFieldHiddenForAllSchemesOf(ctx.getProject());
    }

    private boolean hasNoVersions(Project project)
    {
        return versionManager.getVersions(project.getId()).isEmpty();
    }

    private boolean isFixForVersionFieldHiddenForAllSchemesOf(Project project)
    {
        return fieldVisibilityManager.isFieldHiddenInAllSchemes(project.getId(), IssueFieldConstants.FIX_FOR_VERSIONS);
    }

    private Collection<Version> getVersionsToRenderFor(BrowseContext ctx)
    {
        return versionManager.getVersionsReleasedDesc(ctx.getProject().getId(), false);
    }
}