package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.watchers.IssueWatcherAccessor;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import java.util.Locale;

public class WatcherIndexer extends UserFieldIndexer
{
    private final IssueWatcherAccessor watcherAccessor;

    public WatcherIndexer(final FieldVisibilityManager fieldVisibilityManager, final IssueWatcherAccessor watcherAccessor)
    {
        super(fieldVisibilityManager);
        this.watcherAccessor = watcherAccessor;
    }

    public String getId()
    {
        return SystemSearchConstants.forWatchers().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forWatchers().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        // You can not hide the Votes via the FieldConfiguration so we only need to check if it is enabled or disabled
        return watcherAccessor.isWatchingEnabled();
    }

    public void addIndex(final Document doc, final Issue issue)
    {
        for (final String username : watcherAccessor.getWatcherNames(issue))
        {
            indexUsername(doc, getDocumentFieldId(), username, issue);
        }
    }
}
