package com.atlassian.jira.issue.search;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import org.apache.lucene.search.IndexSearcher;

import static com.atlassian.plugin.util.Assertions.notNull;

public class SearchProviderFactoryImpl implements SearchProviderFactory
{
    private final IssueIndexManager issueIndexManager;

    public SearchProviderFactoryImpl(final IssueIndexManager issueIndexManager)
    {
        this.issueIndexManager = notNull("issueIndexManager", issueIndexManager);
    }

    public SearchProviderFactoryImpl()
    {
        this(ComponentManager.getComponentInstanceOfType(IssueIndexManager.class));
    }

    public IndexSearcher getSearcher(final String searcherName)
    {
        if (ISSUE_INDEX.equals(searcherName))
        {
            return issueIndexManager.getIssueSearcher();
        }
        else if (COMMENT_INDEX.equals(searcherName))
        {
            return issueIndexManager.getCommentSearcher();
        }
         else if (CHANGE_HISTORY_INDEX.equals(searcherName))
        {
            return issueIndexManager.getChangeHistorySearcher();
        }
        throw new UnsupportedOperationException("Only issue, comment and change history indexes are catered for currently");
    }
}
