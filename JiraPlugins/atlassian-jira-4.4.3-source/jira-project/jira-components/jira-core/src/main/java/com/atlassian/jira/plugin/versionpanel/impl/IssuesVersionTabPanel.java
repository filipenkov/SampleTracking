package com.atlassian.jira.plugin.versionpanel.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByPriorityFragment;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Issues version tab panel.
 *
 * @since v4.0
 */
public class IssuesVersionTabPanel extends AbstractFragmentBasedVersionTabPanel
{
    private final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment;
    private final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment;
    private final StatusSummaryFragment statusSummaryFragment;
    private final UnresolvedIssuesByComponentFragment unresolvedIssuesByComponentFragment;

    public IssuesVersionTabPanel(final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment,
            final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment,
            final StatusSummaryFragment statusSummaryFragment,
            final UnresolvedIssuesByComponentFragment unresolvedIssuesByComponentFragment)
    {
        this.unresolvedIssuesByPriorityFragment = unresolvedIssuesByPriorityFragment;
        this.unresolvedIssuesByAssigneeFragment = unresolvedIssuesByAssigneeFragment;
        this.statusSummaryFragment = statusSummaryFragment;
        this.unresolvedIssuesByComponentFragment = unresolvedIssuesByComponentFragment;
    }

    protected List<VersionTabPanelFragment> getLeftColumnFragments(BrowseVersionContext ctx)
    {
        final List<VersionTabPanelFragment> frags = new ArrayList<VersionTabPanelFragment>();
        frags.add(unresolvedIssuesByPriorityFragment);
        frags.add(unresolvedIssuesByAssigneeFragment);
        return frags;
    }

    protected List<VersionTabPanelFragment> getRightColumnFragments(BrowseVersionContext ctx)
    {
        final List<VersionTabPanelFragment> frags = new ArrayList<VersionTabPanelFragment>();
        frags.add(statusSummaryFragment);
        frags.add(unresolvedIssuesByComponentFragment);
        return frags;
    }

    public boolean showPanel(BrowseVersionContext ctx)
    {
        return true;
    }
}