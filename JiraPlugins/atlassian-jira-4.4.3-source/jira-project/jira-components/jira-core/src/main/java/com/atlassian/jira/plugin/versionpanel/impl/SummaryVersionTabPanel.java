package com.atlassian.jira.plugin.versionpanel.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.RecentIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ReleaseNotesMenuFragment;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelFragment;
import com.atlassian.jira.plugin.versionpanel.fragment.impl.VersionDescriptionFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary version tab panel.
 *
 * @since v4.0
 */
public class SummaryVersionTabPanel extends AbstractFragmentBasedVersionTabPanel
{
    private final VersionDescriptionFragment versionDescriptionFragment;
    private final DueIssuesFragment dueIssuesFragment;
    private final RecentIssuesFragment recentIssuesFragment;
    private final FiltersMenuVersionFragment filtersMenuVersionFragment;
    private final ReleaseNotesMenuFragment releaseNotesMenuFragment;

    public SummaryVersionTabPanel(final VersionDescriptionFragment versionDescriptionFragment, final DueIssuesFragment dueIssuesFragment,
                                  final RecentIssuesFragment recentIssuesFragment, final FiltersMenuVersionFragment filtersMenuVersionFragment,
                                  final ReleaseNotesMenuFragment releaseNotesMenuFragment)
    {
        this.versionDescriptionFragment = versionDescriptionFragment;
        this.dueIssuesFragment = dueIssuesFragment;
        this.recentIssuesFragment = recentIssuesFragment;
        this.filtersMenuVersionFragment = filtersMenuVersionFragment;
        this.releaseNotesMenuFragment = releaseNotesMenuFragment;
    }

    public String createEmptyContent()
    {
        return moduleDescriptor.getI18nBean().getText("browseversion.empty.tab.summary");
    }

    protected List<VersionTabPanelFragment> getLeftColumnFragments(BrowseVersionContext ctx)
    {
        final List<VersionTabPanelFragment> frags = new ArrayList<VersionTabPanelFragment>();
        frags.add(versionDescriptionFragment);
        frags.add(dueIssuesFragment);
        frags.add(recentIssuesFragment);

        return frags;
    }

    protected List<VersionTabPanelFragment> getRightColumnFragments(BrowseVersionContext ctx)
    {
        final List<VersionTabPanelFragment> frags = new ArrayList<VersionTabPanelFragment>();
        return frags;
    }

    protected List<MenuFragment> getMenuFragments()
    {
        final List<MenuFragment> frags = new ArrayList<MenuFragment>();
        frags.add(releaseNotesMenuFragment);
        frags.add(filtersMenuVersionFragment);
        return frags;
    }
}