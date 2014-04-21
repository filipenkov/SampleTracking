package com.atlassian.jira.plugin.issuenav.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Kickass search utility service
 * @since v5.1
 */
public class DefaultSearcherService implements SearcherService
{
    private static final String JQL_PREFIX = "__jql_";

    private final SearchContextHelper searchContextHelper;
    private final IssueSearcherManager issueSearcherManager;
    private final SearchHandlerManager searchHandlerManager;
    private final JqlStringSupport jqlStringSupport;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;
    private static final String JQL_TOO_COMPLEX_ERROR_MESSAGE = "jqlTooComplex";


    public DefaultSearcherService(final SearchContextHelper searchContextHelper, final IssueSearcherManager issueSearcherManager,
          final JqlStringSupport jqlStringSupport, final JiraAuthenticationContext authenticationContext,
          final SearchHandlerManager searchHandlerManager, final SearchService searchService)
    {
        this.searchContextHelper = searchContextHelper;
        this.issueSearcherManager = issueSearcherManager;
        this.jqlStringSupport = jqlStringSupport;
        this.authenticationContext = authenticationContext;
        this.searchHandlerManager = searchHandlerManager;
        this.searchService = searchService;
    }

    @Override
    public ServiceOutcome<SearchRendererValueResults> getViewHtml(JiraWebActionSupport action, Map<String, String[]> params)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();

        final ServiceOutcome<Map<String, SearchRendererHolder>> clausesOutcome = generateQuery(params, user, searchers);
        if (!clausesOutcome.isValid())
        {
            return new ServiceOutcomeImpl<SearchRendererValueResults>(clausesOutcome.getErrorCollection());
        }
        Map<String, SearchRendererHolder> clauses = clausesOutcome.getReturnedValue();
        final Query query = buildQuery(clauses);
        SearchContext searchContext = searchService.getSearchContext(user, query);
        
        return getValueResults(action, user, searchers, clauses, query, searchContext);
    }

    @Override
    public ServiceOutcome<SearchResults> search(JiraWebActionSupport action, Map<String, String[]> params)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();

        final ServiceOutcome<Map<String, SearchRendererHolder>> clausesOutcome = generateQuery(params, user, searchers);
        if (!clausesOutcome.isValid())
        {
            return new ServiceOutcomeImpl<SearchResults>(clausesOutcome.getErrorCollection());
        }
        Map<String, SearchRendererHolder> clauses = clausesOutcome.getReturnedValue();
        final Query query = buildQuery(clauses);
        SearchContext searchContext = searchService.getSearchContext(user, query);

        return getSearchResults(action, user, searchers, clauses, query, searchContext);
    }

    @Override
    public ServiceOutcome<SearchResults> searchWithJql(JiraWebActionSupport action, String jqlContext)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Query jqlQuery = searchService.parseQuery(user, jqlContext).getQuery();

        if (!searchService.doesQueryFitFilterForm(user, jqlQuery)) {
            return ServiceOutcomeImpl.error(JQL_TOO_COMPLEX_ERROR_MESSAGE);
        }

        final SearchService.ParseResult parseResult = searchService.parseQuery(user, jqlContext);
        final SearchContext searchContext = searchService.getSearchContext(user, parseResult.getQuery());

        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();
        final Map<String, SearchRendererHolder> clauses = generateQuery(searchContext, user, jqlQuery, searchers);

        return getSearchResults(action, user, searchers, clauses, jqlQuery, searchContext);
    }

    private ServiceOutcome<SearchResults> getSearchResults(JiraWebActionSupport action, User user, Collection<IssueSearcher<?>> searchers,
                                                           Map<String, SearchRendererHolder> clauses, Query query, SearchContext searchContext)
    {
        final ServiceOutcome<SearchRendererValueResults> outcome = getValueResults(action, user, searchers, clauses, query, searchContext);
        if (!outcome.isValid())
        {
            return new ServiceOutcomeImpl<SearchResults>(outcome.getErrorCollection());
        }

        Searchers renderableSearchers = getSearchers(searchContext, user);

        return ServiceOutcomeImpl.ok(new SearchResults(renderableSearchers, outcome.getReturnedValue()));
    }

    @Override
    public ServiceOutcome<String> getEditHtml(String searcherId, String jqlContext, JiraWebActionSupport action)
    {
        final User user = authenticationContext.getLoggedInUser();
        SearchContextHelper.SearchContextWithFieldValues searchContextWithFieldValues = searchContextHelper.getSearchContextWithFieldValuesFromJqlString(jqlContext);
        
        return getEditHtml(searcherId, action, user, searchContextWithFieldValues, createDisplayParams());
    }

    @Override
    public Searchers getSearchers(String jqlContext)
    {
        SearchContext searchContext = searchContextHelper.getSearchContextFromJqlString(jqlContext);

        return getSearchers(searchContext, authenticationContext.getLoggedInUser());
    }

    private ServiceOutcome<SearchRendererValueResults> getValueResults(JiraWebActionSupport action, User user, Collection<IssueSearcher<?>> searchers, Map<String, SearchRendererHolder> clauses, Query query, SearchContext searchContext)
    {
        Map<String, String> displayParams = createDisplayParams();
        SearchRendererValueResults results = new SearchRendererValueResults();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        for (IssueSearcher issueSearcher : searchers)
        {
            String id = issueSearcher.getSearchInformation().getId();
            SearchRendererHolder clause = clauses.get(id);
            SearchRenderer searchRenderer = issueSearcher.getSearchRenderer();
            String name = i18nHelper.getText(issueSearcher.getSearchInformation().getNameKey());
            if (searchRenderer.isRelevantForQuery(user, query)) {

                // We don't inspect viewHtml or editHtml if the searcher is invalid
                boolean validSearcher = searchRenderer.isShown(user, searchContext);
                String viewHtml;
                String editHtml;
                if (validSearcher)
                {
                    SearchInputTransformer searchInputTransformer = issueSearcher.getSearchInputTransformer();

                    FieldValuesHolder params;
                    if (null == clause)
                    {
                        params = null;
                    }
                    else if (clause.valid)
                    {
                        params = clause.params;
                    }
                    else
                    {
                        params = new FieldValuesHolderImpl();
                        searchInputTransformer.populateFromQuery(user, params, new QueryImpl(clause.clause), searchContext);
                    }

                    searchInputTransformer.validateParams(user, searchContext, params, i18nHelper, action);

                    viewHtml = searchRenderer.getViewHtml(user, searchContext, params, null, action);
                    
                    SearchContextHelper.SearchContextWithFieldValues searchContextWithFieldValues = new SearchContextHelper.SearchContextWithFieldValues(searchContext, params);
                    ServiceOutcome<String> outcome = getEditHtml(id, action, user, searchContextWithFieldValues, displayParams);
                    if (outcome.isValid())
                    {
                        editHtml = outcome.getReturnedValue();
                    }
                    else
                    {
                        return new ServiceOutcomeImpl<SearchRendererValueResults>(outcome.getErrorCollection());
                    }
                }
                else
                {
                    viewHtml = null;
                    editHtml = null;
                }
                
                String jql = clause != null ? jqlStringSupport.generateJqlString(clause.clause) : null;
                results.put(id, new SearchRendererValue(name, jql, viewHtml, editHtml, validSearcher));
            }
            else if (clause != null && !clause.valid)
            {
                results.put(id, new SearchRendererValue(name, jqlStringSupport.generateJqlString(clause.clause), null, null, false));
            }
        }

        return ServiceOutcomeImpl.ok(results);
    }

    private Map<String, SearchRendererHolder> generateQuery(SearchContext searchContext, User user, Query query, Collection<IssueSearcher<?>> searchers)
    {
        Map<String, SearchRendererHolder> clauses = Maps.newHashMap();
        
        // TODO: copy from DefaultSearchRequestFactory.getClausesFromSearchers
        for (IssueSearcher issueSearcher : searchers)
        {
            SearchInputTransformer searchInputTransformer = issueSearcher.getSearchInputTransformer();
            FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
            
            
            searchInputTransformer.populateFromQuery(user, fieldValuesHolder, query, searchContext);
            Clause clause = searchInputTransformer.getSearchClause(user, fieldValuesHolder);
            if (null != clause)
            {
                String id = issueSearcher.getSearchInformation().getId();
                clauses.put(id, SearchRendererHolder.valid(clause, fieldValuesHolder));
            }
        }

        return clauses;
    }
    
    /**
     * @return invalid if any __jql parameter contains invalid jql
     */
    private ServiceOutcome<Map<String, SearchRendererHolder>> generateQuery(Map<String, String[]> params, User user, Collection<IssueSearcher<?>> searchers)
    {
        Map<String, SearchRendererHolder> clauses = Maps.newHashMap();

        ActionParams actionParams = getActionParameters(params);
        Map<String, String[]> jqlParams = getJqlParameters(params);

        // TODO: copy from DefaultSearchRequestFactory.getClausesFromSearchers
        for (IssueSearcher issueSearcher : searchers)
        {
            String id = issueSearcher.getSearchInformation().getId();
            SearchInputTransformer searchInputTransformer = issueSearcher.getSearchInputTransformer();
            // For invalid clauses, we send jql as a param as we are not able to retrieve form params from an invalid clause (as we don't
            // call getEditHtml() if a clause is invalid for context.
            if (jqlParams.containsKey(id))
            {
                // We will only ever be sent the 0th result
                final SearchService.ParseResult parseResult = searchService.parseQuery(user, jqlParams.get(id)[0]);
                if (parseResult.isValid())
                {
                    Clause clause = parseResult.getQuery().getWhereClause();
                    clauses.put(id, SearchRendererHolder.invalid(clause));
                }
                else
                {
                    // ignore invalid jql query from invalid searcher
                    ErrorCollection errors = new SimpleErrorCollection();
                    errors.addErrorMessages(parseResult.getErrors().getErrorMessages());
                    return new ServiceOutcomeImpl(errors);
                }
            }
            else
            {
                FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
                searchInputTransformer.populateFromParams(user, fieldValuesHolder, actionParams);
                Clause clause = searchInputTransformer.getSearchClause(user, fieldValuesHolder);
                if (null != clause)
                {
                    clauses.put(id, SearchRendererHolder.valid(clause, fieldValuesHolder));
                }
            }
        }

        return ServiceOutcomeImpl.ok(clauses);
    }

    private Query buildQuery(Map<String, SearchRendererHolder> clauses)
    {
        final Collection<Clause> actualClauses = Collections2.transform(clauses.values(), new Function<SearchRendererHolder, Clause>()
        {
            @Override
            public Clause apply(@Nullable SearchRendererHolder input)
            {
                return input.clause;
            }
        });

        Clause jqlClause = clauses.size() > 0 ? new AndClause(actualClauses) : null;
        return new QueryImpl(jqlClause);
    }

    /**
     * Container for returned values
     */
    private static class SearchRendererHolder
    {
        public final boolean valid;
        public final Clause clause;
        public final FieldValuesHolder params;

        public static SearchRendererHolder invalid(Clause clause)
        {
            return new SearchRendererHolder(false, clause, null);
        }

        public static SearchRendererHolder valid(Clause clause, FieldValuesHolder params)
        {
            return new SearchRendererHolder(true, clause, params);
        }
        
        private SearchRendererHolder(boolean valid, Clause clause, FieldValuesHolder params)
        {
            this.valid = valid;
            this.clause = clause;
            this.params = params;
        }
    }
    
    private Searchers getSearchers(SearchContext searchContext, User user)
    {
        final Collection<SearcherGroup> searcherGroups = searchHandlerManager.getSearcherGroups(searchContext);
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        Searchers searchers = new Searchers();
        for (SearcherGroup searcherGroup : searcherGroups)
        {
            if (searcherGroup.isShown(user, searchContext))
            {
                final String titleKey = searcherGroup.getTitleKey();
                final String type = searcherGroup.getType().name();
                FilteredSearcherGroup group = new FilteredSearcherGroup(type);
                if (titleKey != null)
                {
                    group.setTitle(i18nHelper.getText(titleKey));
                }
                for (IssueSearcher<?> searcher : searcherGroup.getSearchers())
                {
                    if (searcher.getSearchRenderer().isShown(user, searchContext))
                    {
                        final SearcherInformation<? extends SearchableField> info = searcher.getSearchInformation();
                        String name = i18nHelper.getText(info.getNameKey());
                        group.addSearcher(new Searcher(info.getId(), name));
                    }
                }
                if (!group.getSearchers().isEmpty())
                {
                    searchers.addGroup(group);
                }
            }
        }
        return searchers;
    }

    private ServiceOutcome<String> getEditHtml(String searcherId, Action action, User user, SearchContextHelper.SearchContextWithFieldValues searchContextWithFieldValues, Map<String, String> displayParams)
    {
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        if (null == searcherId)
        {
            return ServiceOutcomeImpl.error(i18nHelper.getText("searchrenderer.error.no.search.renderer"));
        }

        IssueSearcher<?> issueSearcher = issueSearcherManager.getSearcher(searcherId);
        if (null == issueSearcher)
        {
            return ServiceOutcomeImpl.error(i18nHelper.getText("searchrenderer.error.no.search.renderer"));
        }
        
        String editHtml = issueSearcher.getSearchRenderer().getEditHtml(user, searchContextWithFieldValues.searchContext, searchContextWithFieldValues.fieldValuesHolder, displayParams, action);
        return ServiceOutcomeImpl.ok(editHtml);
    }
    
    private Map<String, String> createDisplayParams()
    {
        Map<String,String> displayParams = new HashMap<String,String>();
        displayParams.put("theme","aui");
        return displayParams;
    }

    private ActionParams getActionParameters(Map<String, String[]> params)
    {
        return new ActionParamsImpl(Maps.filterKeys(params, new Predicate<String>()
        {
            @Override
            public boolean apply(@Nullable String input)
            {
                return !input.startsWith(JQL_PREFIX);
            }
        }));
    }

    private Map<String, String[]> getJqlParameters(Map<String, String[]> params)
    {
        Map<String, String[]> jqlParams = Maps.newHashMap();
        for (Map.Entry<String, String[]> entry : params.entrySet())
        {
            if (entry.getKey().startsWith(JQL_PREFIX))
            {
                jqlParams.put(entry.getKey().substring(JQL_PREFIX.length()), entry.getValue());
            }
        }
        return jqlParams;
    }
}
