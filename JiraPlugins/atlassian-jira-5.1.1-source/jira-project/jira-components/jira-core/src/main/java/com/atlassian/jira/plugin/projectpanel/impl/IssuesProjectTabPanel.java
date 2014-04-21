package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByFixVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByIssueTypeFragment;
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
    private final UnresolvedIssuesByIssueTypeFragment unresolvedIssuesByIssueTypeFragment;

    public IssuesProjectTabPanel(final UnresolvedIssuesByPriorityFragment unresolvedIssuesByPriorityFragment,
            final UnresolvedIssuesByAssigneeFragment unresolvedIssuesByAssigneeFragment,
            final UnresolvedIssuesByFixVersionFragment unresolvedIssuesByFixVersionFragment,
            final StatusSummaryFragment statusSummaryFragment,
            final UnresolvedIssuesByComponentFragment unresolvedIssuesByComponentFragment,
            final UnresolvedIssuesByIssueTypeFragment unresolvedIssuesByIssueTypeFragment)
    {
        this.unresolvedIssuesByPriorityFragment = unresolvedIssuesByPriorityFragment;
        this.unresolvedIssuesByAssigneeFragment = unresolvedIssuesByAssigneeFragment;
        this.unresolvedIssuesByFixVersionFragment = unresolvedIssuesByFixVersionFragment;
        this.statusSummaryFragment = statusSummaryFragment;
        this.unresolvedIssuesByComponentFragment = unresolvedIssuesByComponentFragment;
        this.unresolvedIssuesByIssueTypeFragment = unresolvedIssuesByIssueTypeFragment;
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
        frags.add(unresolvedIssuesByIssueTypeFragment);

        return frags;
    }

    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }
}