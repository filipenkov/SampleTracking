package com.atlassian.streams.jira.search;

import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.SortOrder;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsException;

import com.google.common.collect.ImmutableSet;

import static com.atlassian.streams.jira.search.Jql.MIN_PAGING_RESULTS;
import static com.atlassian.streams.jira.search.Jql.filterByDate;
import static com.atlassian.streams.jira.search.Jql.filterByIssueKey;
import static com.atlassian.streams.jira.search.Jql.filterByIssueType;
import static com.atlassian.streams.jira.search.Jql.filterByProject;
import static com.atlassian.streams.jira.search.Jql.filterByUser;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;

class IssueSearch
{
    private final SearchProvider searchProvider;
    private final JiraAuthenticationContext authenticationContext;

    public IssueSearch(SearchProvider searchProvider,
            JiraAuthenticationContext authenticationContext)
    {
        this.searchProvider = checkNotNull(searchProvider, "searchProvider");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
    }

    public Set<Issue> search(final ActivityRequest request)
    {
        final Query searchQuery = createSearchQuery(request);
        final PagerFilter<Issue> pagerFilter = new PagerFilter<Issue>(Math.max(MIN_PAGING_RESULTS, request.getMaxResults()));
        try
        {
            final SearchResults searchResults = searchProvider.search(searchQuery, authenticationContext.getLoggedInUser(), pagerFilter);
            return ImmutableSet.copyOf(searchResults.getIssues());
        }
        catch (final SearchException se)
        {
            throw new StreamsException("Error performing search", se);
        }
    }

    private Query createSearchQuery(final ActivityRequest activityRequest)
    {
        return selectIssues(
                filterByProject(activityRequest),
                filterByUser(activityRequest),
                filterByIssueKey(activityRequest),
                filterByDate(activityRequest),
                filterByIssueType(activityRequest)).
            orderBy().updatedDate(SortOrder.DESC).
            buildQuery();
    }

    private JqlQueryBuilder selectIssues(Clause... clauses)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        for (Clause clause : filter(asList(clauses), notNull()))
        {
            builder.addClause(clause);
        }
        return builder.endWhere();
    }
}
