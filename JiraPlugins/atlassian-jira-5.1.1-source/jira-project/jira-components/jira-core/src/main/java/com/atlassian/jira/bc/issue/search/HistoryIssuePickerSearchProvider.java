package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;

/**
 * Implemenation of {@link com.atlassian.jira.bc.issue.search.IssuePickerSearchProvider} that searches the
 * user history in the session.
 * Query is tokenized and then matched against a key and summary.  All tokens must exist in summary OR key.
 */
public class HistoryIssuePickerSearchProvider extends AbstractIssuePickerSearchProvider
{
    public HistoryIssuePickerSearchProvider(SearchProvider searchProvider, ConstantsManager constantsManager, LuceneQueryModifier modifier)
    {
        super(searchProvider, constantsManager, modifier);
    }

    protected String getId()
    {
        return "hs";
    }

    protected String getLabelKey()
    {
        return "jira.ajax.autocomplete.history.search";
    }

    // method is protected to allow for unit testing
    protected SearchRequest getRequest(IssuePickerSearchService.IssuePickerParameters issuePickerParams)
    {
        final Clause clause = JqlQueryBuilder.newClauseBuilder().issueInHistory().buildClause();
        // The OrderBy clause must be null so that we will force a search with no sort
        final Query query = new QueryImpl(clause, null, null);

        return new SearchRequest(query);
    }

    public boolean handlesParameters(final User searcher, final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
    {
        return true;
    }
}
