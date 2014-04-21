package com.atlassian.jira.plugin.componentpanel.impl;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByFixVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByPriorityFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Issues component tab panel.
 *
 * @since v4.0
 */
public class IssuesComponentTabPanel extends AbstractFragmentBasedComponentTabPanel
{
    private final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment;
    private final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment;
    private final UnresolvedIssuesByFixVersionFragment unresolvedIssuesByFixVersionFragment;
    private final StatusSummaryFragment statusSummaryFragment;

    public IssuesComponentTabPanel(final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment,
            final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment,
            final UnresolvedIssuesByFixVersionFragment unresolvedIssuesByFixVersionFragment,
            final StatusSummaryFragment statusSummaryFragment)
    {
        this.unresolvedIssuesByPriorityFragment = unresolvedIssuesByPriorityFragment;
        this.unresolvedIssuesByAssigneeFragment = unresolvedIssuesByAssigneeFragment;
        this.unresolvedIssuesByFixVersionFragment = unresolvedIssuesByFixVersionFragment;
        this.statusSummaryFragment = statusSummaryFragment;
    }

    protected List<ComponentTabPanelFragment> getLeftColumnFragments(BrowseComponentContext ctx)
    {
        final List<ComponentTabPanelFragment> frags = new ArrayList<ComponentTabPanelFragment>();
        frags.add(unresolvedIssuesByPriorityFragment);
        frags.add(unresolvedIssuesByAssigneeFragment);
        frags.add(unresolvedIssuesByFixVersionFragment);

        return frags;
    }

    protected List<ComponentTabPanelFragment> getRightColumnFragments(BrowseComponentContext ctx)
    {
        final List<ComponentTabPanelFragment> frags = new ArrayList<ComponentTabPanelFragment>();
        frags.add(statusSummaryFragment);

        return frags;
    }

    public boolean showPanel(BrowseComponentContext ctx)
    {
        return true;
    }
}