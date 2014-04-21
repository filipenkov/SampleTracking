package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByFixVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByPriorityFragment;
import com.atlassian.jira.project.browse.BrowseContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Issues project tab panel.
 *
 * @since v4.0
 */
public class IssuesProjectTabPanel extends AbstractFragmentBasedProjectTabPanel
{
    private final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment;
    private final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment;
    private final UnresolvedIssuesByFixVersionFragment unresolvedIssuesByFixVersionFragment;
    private final StatusSummaryFragment statusSummaryFragment;
    private final UnresolvedIssuesByComponentFragment unresolvedIssuesByComponentFragment;

    public IssuesProjectTabPanel(final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment,
            final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment,
            final UnresolvedIssuesByFixVersionFragment unresolvedIssuesByFixVersionFragment,
            final StatusSummaryFragment statusSummaryFragment,
            final UnresolvedIssuesByComponentFragment unresolvedIssuesByComponentFragment)
    {
        this.unresolvedIssuesByPriorityFragment = unresolvedIssuesByPriorityFragment;
        this.unresolvedIssuesByAssigneeFragment = unresolvedIssuesByAssigneeFragment;
        this.unresolvedIssuesByFixVersionFragment = unresolvedIssuesByFixVersionFragment;
        this.statusSummaryFragment = statusSummaryFragment;
        this.unresolvedIssuesByComponentFragment = unresolvedIssuesByComponentFragment;
    }

    protected List<ProjectTabPanelFragment> getLeftColumnFragments(BrowseContext ctx)
    {
        final List<ProjectTabPanelFragment> frags = new ArrayList<ProjectTabPanelFragment>();
        frags.add(unresolvedIssuesByPriorityFragment);
        frags.add(unresolvedIssuesByAssigneeFragment);
        frags.add(unresolvedIssuesByFixVersionFragment);

        return frags;
    }

    protected List<ProjectTabPanelFragment> getRightColumnFragments(BrowseContext ctx)
    {
        final List<ProjectTabPanelFragment> frags = new ArrayList<ProjectTabPanelFragment>();
        frags.add(statusSummaryFragment);
        frags.add(unresolvedIssuesByComponentFragment);

        return frags;
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }
}