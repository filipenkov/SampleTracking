package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Runs through a saved filter to determine if there is any self-reference anywhere in the nested filter.
 *
 * @since v4.0
 */
@InjectableComponent
public class SavedFilterCycleDetector
{
    private final SavedFilterResolver savedFilterResolver;
    private final JqlOperandResolver jqlOperandResolver;

    public SavedFilterCycleDetector(final SavedFilterResolver savedFilterResolver, final JqlOperandResolver jqlOperandResolver)
    {
        this.savedFilterResolver = notNull("savedFilterResolver", savedFilterResolver);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    }

    /**
     * Checks if the specified saved {@link com.atlassian.jira.issue.search.SearchRequest} contains a reference to another saved filter
     * by using the <code>savedFilter</code> clause.
     *
     * @param searcher the user performing the search
     * @param overrideSecurity false if we should check permissions
     * @param savedFilter the saved filter to check if it holds a reference to the filter with the filterId
     * @param filterId the id of the filter to check if it can be referenced from the savedFilter, can be null.
     *        if null it takes the filter id from the savedFilter, thus checking if it can reference itself
     * @return true if the filter does contains a reference to the filter with filterId; false otherwise.
     */
    public boolean containsSavedFilterReference(User searcher, final boolean overrideSecurity, SearchRequest savedFilter, Long filterId)
    {
        notNull("savedFilter", savedFilter);
        final Query savedFilterQuery = savedFilter.getQuery();
        if(filterId == null)
        {
            filterId = savedFilter.getId();
        }

        return containsFilterRef(searcher, overrideSecurity, filterId, savedFilterQuery.getWhereClause());
    }

    private boolean containsFilterRef(User searcher, final boolean overrideSecurity, Long filterId, Clause filterClause)
    {
        if (filterClause == null)
        {
            return false;
        }

        NamedTerminalClauseCollectingVisitor collectingVisitor = new NamedTerminalClauseCollectingVisitor(SystemSearchConstants.forSavedFilter().getJqlClauseNames().getJqlFieldNames());
        filterClause.accept(collectingVisitor);

        final List<TerminalClause> nestedFilterClauses = collectingVisitor.getNamedClauses();

        final QueryCreationContextImpl creationContext = new QueryCreationContextImpl(searcher, overrideSecurity);
        for (TerminalClause nestedFilterClause : nestedFilterClauses)
        {
            final List<QueryLiteral> filterValues = jqlOperandResolver.getValues(creationContext, nestedFilterClause.getOperand(), nestedFilterClause);
            final List<SearchRequest> matchingSearchRequests = overrideSecurity ? savedFilterResolver.getSearchRequestOverrideSecurity(filterValues) : savedFilterResolver.getSearchRequest(searcher, filterValues);
            for (SearchRequest matchingSearchRequest : matchingSearchRequests)
            {
                // Is this filter the one we are worried about?
                if (filterId.equals(matchingSearchRequest.getId()))
                {
                    return true;
                }
                else
                {
                    // Recurse through that filter to make sure it has no saved filters
                    final Query query = matchingSearchRequest.getQuery();
                    if (containsFilterRef(searcher, overrideSecurity, filterId, query.getWhereClause()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    
}
