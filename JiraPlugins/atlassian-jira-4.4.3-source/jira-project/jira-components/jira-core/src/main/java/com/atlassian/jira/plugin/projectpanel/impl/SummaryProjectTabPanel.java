package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ActivityStreamFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.CreatedVsResolvedFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueVersionsFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ProjectAdminMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ProjectDescriptionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.RecentIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ReportsMenuFragment;
import com.atlassian.jira.project.browse.BrowseContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary project tab panel.
 *
 * @since v4.0
 */
public class SummaryProjectTabPanel extends AbstractFragmentBasedProjectTabPanel
{
    private final ProjectDescriptionFragment projectDescriptionFragment;
    private final DueIssuesFragment dueIssuesFragment;
    private final RecentIssuesFragment recentIssuesFragment;
    private final DueVersionsFragment dueVersionsFragment;
    private final ReportsMenuFragment reportsMenuFragment;
    private final FiltersMenuFragment filtersMenuFragment;
    private final CreatedVsResolvedFragment createdVsResolvedFragment;
    private final ActivityStreamFragment activityStreamFragment;
    private final ProjectAdminMenuFragment projectAdminMenuFragment;

    public SummaryProjectTabPanel(final ProjectDescriptionFragment projectDescriptionFragment,
            final DueIssuesFragment dueIssuesFragment,
            final RecentIssuesFragment recentIssuesFragment,
            final DueVersionsFragment dueVersionsFragment,
            final ReportsMenuFragment reportsMenuFragment,
            final FiltersMenuFragment filtersMenuFragment,
            final CreatedVsResolvedFragment createdVsResolvedFragment,
            final ActivityStreamFragment activityStreamFragment,
            final ProjectAdminMenuFragment projectAdminMenuFragment)
    {
        this.projectDescriptionFragment = projectDescriptionFragment;
        this.dueIssuesFragment = dueIssuesFragment;
        this.recentIssuesFragment = recentIssuesFragment;
        this.dueVersionsFragment = dueVersionsFragment;
        this.reportsMenuFragment = reportsMenuFragment;
        this.filtersMenuFragment = filtersMenuFragment;
        this.createdVsResolvedFragment = createdVsResolvedFragment;
        this.activityStreamFragment = activityStreamFragment;
        this.projectAdminMenuFragment = projectAdminMenuFragment;
    }

    public String createEmptyContent()
    {
        return moduleDescriptor.getI18nBean().getText("browseproject.empty.tab.summary");
    }

    protected List<ProjectTabPanelFragment> getLeftColumnFragments(BrowseContext ctx)
    {
        final List<ProjectTabPanelFragment> frags = new ArrayList<ProjectTabPanelFragment>();
        frags.add(projectDescriptionFragment);
        frags.add(dueIssuesFragment);
        frags.add(createdVsResolvedFragment);
        frags.add(recentIssuesFragment);

        return frags;
    }

    protected List<ProjectTabPanelFragment> getRightColumnFragments(BrowseContext ctx)
    {
        final List<ProjectTabPanelFragment> frags = new ArrayList<ProjectTabPanelFragment>();
        frags.add(dueVersionsFragment);
        frags.add(activityStreamFragment);

        return frags;
    }

    protected List<MenuFragment> getMenuFragments()
    {
        final List<MenuFragment> frags = new ArrayList<MenuFragment>();
        frags.add(projectAdminMenuFragment);
        frags.add(reportsMenuFragment);
        frags.add(filtersMenuFragment);
        return frags;
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }

}
