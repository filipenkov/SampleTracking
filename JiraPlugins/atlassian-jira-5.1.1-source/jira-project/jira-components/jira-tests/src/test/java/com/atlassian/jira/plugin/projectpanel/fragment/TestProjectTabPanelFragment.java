package com.atlassian.jira.plugin.projectpanel.fragment;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueVersionsFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ProjectDescriptionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.RecentIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByFixVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByIssueTypeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByPriorityFragment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestProjectTabPanelFragment extends ListeningTestCase
{
    /**
     * Tests that all {@link ProjectTabPanelFragment implementations have a unique id.
     * NOTE: when adding a new ProjectTabPanelFragment, ensure you add it to this test!
     */
    @Test
    public void testFragmentsHaveUniqueIds()
    {
        List<ProjectTabPanelFragment> fragments = new ArrayList<ProjectTabPanelFragment>();
        fragments.add(new ProjectDescriptionFragment(null, null, null, null));
        fragments.add(new DueIssuesFragment(null, null, null, null, null));
        fragments.add(new RecentIssuesFragment(null, null, null, null, null));
        fragments.add(new DueVersionsFragment(null, null, null, null));
        fragments.add(new StatusSummaryFragment(null, null, null, null));
        fragments.add(new UnresolvedIssuesByAssigneeFragment(null, null, null, null));
        fragments.add(new UnresolvedIssuesByComponentFragment(null, null, null));
        fragments.add(new UnresolvedIssuesByFixVersionFragment(null, null, null));
        fragments.add(new UnresolvedIssuesByPriorityFragment(null, null, null, null));
        fragments.add(new UnresolvedIssuesByIssueTypeFragment(null, null, null, null));


        Set<String> idSet = new HashSet<String>();
        for (ProjectTabPanelFragment fragment : fragments)
        {
            assertTrue(idSet.add(fragment.getId()));
        }
    }
}
