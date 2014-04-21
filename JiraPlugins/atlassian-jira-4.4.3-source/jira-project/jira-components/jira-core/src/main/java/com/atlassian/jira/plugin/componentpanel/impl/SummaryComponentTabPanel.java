package com.atlassian.jira.plugin.componentpanel.impl;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;
import com.atlassian.jira.plugin.componentpanel.fragment.impl.ComponentDescriptionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueVersionsFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.RecentIssuesFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary component tab panel.
 *
 * @since v4.0
 */
public class SummaryComponentTabPanel extends AbstractFragmentBasedComponentTabPanel
{
    private final ComponentDescriptionFragment componentDescriptionFragment;
    private final DueIssuesFragment dueIssuesFragment;
    private final RecentIssuesFragment recentIssuesFragment;
    private final DueVersionsFragment dueVersionsFragment;
    private final FiltersMenuComponentFragment filtersMenuComponentFragment;

    public SummaryComponentTabPanel(final ComponentDescriptionFragment componentDescriptionFragment, final DueIssuesFragment dueIssuesFragment, final RecentIssuesFragment recentIssuesFragment, final DueVersionsFragment dueVersionsFragment, final FiltersMenuComponentFragment filtersMenuComponentFragment)
    {
        this.componentDescriptionFragment = componentDescriptionFragment;
        this.dueIssuesFragment = dueIssuesFragment;
        this.recentIssuesFragment = recentIssuesFragment;
        this.dueVersionsFragment = dueVersionsFragment;
        this.filtersMenuComponentFragment = filtersMenuComponentFragment;
    }

    public String createEmptyContent()
    {
        return moduleDescriptor.getI18nBean().getText("browsecomponent.empty.tab.summary");
    }

    protected List<ComponentTabPanelFragment> getLeftColumnFragments(BrowseComponentContext ctx)
    {
        final List<ComponentTabPanelFragment> frags = new ArrayList<ComponentTabPanelFragment>();
        frags.add(componentDescriptionFragment);
        frags.add(dueIssuesFragment);
        frags.add(recentIssuesFragment);

        return frags;
    }

    protected List<ComponentTabPanelFragment> getRightColumnFragments(BrowseComponentContext ctx)
    {
        final List<ComponentTabPanelFragment> frags = new ArrayList<ComponentTabPanelFragment>();
        frags.add(dueVersionsFragment);

        return frags;
    }

    protected List<MenuFragment> getMenuFragments()
    {
        final List<MenuFragment> frags = new ArrayList<MenuFragment>();
        frags.add(filtersMenuComponentFragment);
        return frags;
    }
}
