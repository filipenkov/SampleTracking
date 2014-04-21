package com.atlassian.jira.issue.index.managers;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.AffectedVersionsIndexer;
import com.atlassian.jira.issue.index.indexers.impl.AssigneeIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ComponentsIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CreatedDateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CurrentEstimateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DescriptionIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.EnvironmentIndexer;
import com.atlassian.jira.issue.index.indexers.impl.FixForVersionsIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueIdIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueKeyIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueLinkIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueTypeIndexer;
import com.atlassian.jira.issue.index.indexers.impl.LabelsIndexer;
import com.atlassian.jira.issue.index.indexers.impl.OriginalEstimateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ParentIssueIndexer;
import com.atlassian.jira.issue.index.indexers.impl.PriorityIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ProgressIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ProjectIdIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ReporterIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ResolutionIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SecurityIndexer;
import com.atlassian.jira.issue.index.indexers.impl.StatusIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SubTaskIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SummaryIndexer;
import com.atlassian.jira.issue.index.indexers.impl.TimeSpentIndexer;
import com.atlassian.jira.issue.index.indexers.impl.UpdatedDateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.VoterIndexer;
import com.atlassian.jira.issue.index.indexers.impl.VotesIndexer;
import com.atlassian.jira.issue.index.indexers.impl.WatcherIndexer;
import com.atlassian.jira.issue.index.indexers.impl.WatchesIndexer;
import com.atlassian.jira.issue.index.indexers.impl.WorkRatioIndexer;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.JiraUtils.loadComponent;

public class FieldIndexerManagerImpl implements FieldIndexerManager
{
    private final IssueSearcherManager searcherManager;
    private final List<FieldIndexer> knownIndexers;
    private volatile Collection<FieldIndexer> allIssueIndexers;

    static List<FieldIndexer> indexers(final Class<? extends FieldIndexer>... indexers)
    {
        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.newBuilder();
        for (final Class<? extends FieldIndexer> clazz : indexers)
        {
            builder.add(loadComponent(clazz));
        }
        return builder.asList();
    }

    public FieldIndexerManagerImpl(final IssueSearcherManager searcherManager)
    {
        this.searcherManager = searcherManager;
        knownIndexers = indexers(AffectedVersionsIndexer.class, AssigneeIndexer.class, ComponentsIndexer.class, CreatedDateIndexer.class,
            CurrentEstimateIndexer.class, DescriptionIndexer.class, DueDateIndexer.class, EnvironmentIndexer.class, FixForVersionsIndexer.class,
            IssueIdIndexer.class, IssueKeyIndexer.class, IssueTypeIndexer.class, OriginalEstimateIndexer.class, ParentIssueIndexer.class,
            PriorityIndexer.class, ProjectIdIndexer.class, ReporterIndexer.class, ResolutionIndexer.class, SecurityIndexer.class,
            StatusIndexer.class, SubTaskIndexer.class, SummaryIndexer.class, TimeSpentIndexer.class, UpdatedDateIndexer.class, VotesIndexer.class,
            VoterIndexer.class, WatchesIndexer.class, WatcherIndexer.class, WorkRatioIndexer.class, ProgressIndexer.class, LabelsIndexer.class,
                IssueLinkIndexer.class);
        init();
    }

    public Collection<FieldIndexer> getAllIssueIndexers()
    {
        if (allIssueIndexers == null)
        {
            init();
        }

        return allIssueIndexers;
    }

    // commons-collections can't be generified yet
    private synchronized void init()
    {
        allIssueIndexers = new ArrayList<FieldIndexer>();
        final Collection<IssueSearcher<?>> allSearchers = searcherManager.getAllSearchers();
        for (final IssueSearcher<?> searcher : allSearchers)
        {
            allIssueIndexers.addAll(searcher.getSearchInformation().getRelatedIndexers());
        }
        allIssueIndexers = CollectionUtils.union(knownIndexers, allIssueIndexers);
    }

    public void refresh()
    {
        allIssueIndexers = null;
        init();
    }
}
