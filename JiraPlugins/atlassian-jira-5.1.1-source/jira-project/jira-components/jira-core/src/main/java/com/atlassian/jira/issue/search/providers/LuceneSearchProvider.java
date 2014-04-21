package com.atlassian.jira.issue.search.providers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.parameters.lucene.CachedWrappedFilterCache;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;
import com.atlassian.query.Query;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LuceneSearchProvider implements SearchProvider
{
    private static final Logger log = Logger.getLogger(LuceneSearchProvider.class);
    private static final Logger slowLog = Logger.getLogger(LuceneSearchProvider.class.getName() + "_SLOW");

    private final SearchProviderFactory searchProviderFactory;
    private final IssueFactory issueFactory;
    private final PermissionsFilterGenerator permissionsFilterGenerator;
    private final SearchHandlerManager searchHandlerManager;
    private final SearchSortUtil searchSortUtil;
    private final LuceneQueryBuilder luceneQueryBuilder;

    public LuceneSearchProvider(final IssueFactory issueFactory, final SearchProviderFactory searchProviderFactory,
            final PermissionsFilterGenerator permissionsFilterGenerator, final SearchHandlerManager searchHandlerManager,
            final SearchSortUtil searchSortUtil, final LuceneQueryBuilder luceneQueryBuilder)
    {
        this.issueFactory = issueFactory;
        this.searchProviderFactory = searchProviderFactory;
        this.permissionsFilterGenerator = permissionsFilterGenerator;
        this.searchHandlerManager = searchHandlerManager;
        this.searchSortUtil = searchSortUtil;
        this.luceneQueryBuilder = luceneQueryBuilder;
    }

    public SearchResults search(final Query query, final User searcher, final PagerFilter pager) throws SearchException
    {
        return search(query, searcher, pager, null);
    }

    public SearchResults search(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
    {
        return search(query, searcher, pager, andQuery, false);
    }

    public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
    {
        return search(query, searcher, pager, andQuery, true);
    }

    public long searchCount(final Query query, final User user) throws SearchException
    {
        final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        return getHitCount(query, user, null, null, false, issueSearcher, null);
    }

    public long searchCountOverrideSecurity(final Query query, final User user) throws SearchException
    {
        final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        return getHitCount(query, user, null, null, true, issueSearcher, null);
    }

    public void search(final Query query, final User user, final Collector collector) throws SearchException
    {
        search(query, user, collector, null, false);
    }

    public void search(final Query query, final User searcher, final Collector collector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        search(query, searcher, collector, andQuery, false);
    }

    public void searchOverrideSecurity(final Query query, final User user, final Collector collector) throws SearchException
    {
        search(query, user, collector, null, true);
    }

    public void searchAndSort(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter) throws SearchException
    {
        searchAndSort(query, user, collector, pagerFilter, false);
    }

    public void searchAndSortOverrideSecurity(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter) throws SearchException
    {
        searchAndSort(query, user, collector, pagerFilter, true);
    }

    /**
     * Returns 0 if there are no Lucene parameters (search request is null), otherwise returns the hit count
     * <p/>
     * The count is 0 if there are no matches.
     *
     * @param searchQuery    search request
     * @param searchUser user performing the search
     * @param sortField  array of fields to sort by
     * @param andQuery   a query to join with the request
     * @param overrideSecurity ignore the user security permissions
     * @param issueSearcher the IndexSearcher to be used when searching
     * @param pager a pager which holds information about which page of search results is actually required.
     * @return hit count
     * @throws SearchException if error occurs
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if query creates a lucene query that is too complex to be processed.
     */
    private long getHitCount(final Query searchQuery, final User searchUser, final SortField[] sortField, final org.apache.lucene.search.Query andQuery, boolean overrideSecurity, IndexSearcher issueSearcher, final PagerFilter pager) throws SearchException
    {
        if (searchQuery == null)
        {
            return 0;
        }
        try
        {
            final Filter permissionsFilter = getPermissionsFilter(overrideSecurity, searchUser);
            final org.apache.lucene.search.Query finalQuery = createLuceneQuery(searchQuery, andQuery, searchUser, overrideSecurity);
            final TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();
            issueSearcher.search(finalQuery, permissionsFilter, hitCountCollector);
            return hitCountCollector.getTotalHits();
        }
        catch (IOException e)
        {
            throw new SearchException(e);
        }
        
    }
    
    /**
     * Returns null if there are no Lucene parameters (search request is null), otherwise returns a collection of Lucene
     * Document objects.
     * <p/>
     * The collection has 0 results if there are no matches.
     *
     * @param searchQuery    search request
     * @param searchUser user performing the search
     * @param sortField  array of fields to sort by
     * @param andQuery   a query to join with the request
     * @param overrideSecurity ignore the user security permissions
     * @param issueSearcher the IndexSearcher to be used when searching
     * @param pager a pager which holds information about which page of search results is actually required.
     * @return hits
     * @throws SearchException if error occurs
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if query creates a lucene query that is too complex to be processed.
     */
    private TopDocs getHits(final Query searchQuery, final User searchUser, final SortField[] sortField, final org.apache.lucene.search.Query andQuery, boolean overrideSecurity, IndexSearcher issueSearcher, final PagerFilter pager) throws SearchException
    {
        if (searchQuery == null)
        {
            return null;
        }
        try {
            final Filter permissionsFilter = getPermissionsFilter(overrideSecurity, searchUser);
            final org.apache.lucene.search.Query finalQuery = createLuceneQuery(searchQuery, andQuery, searchUser, overrideSecurity);
            if (log.isInfoEnabled())
            {
                log.info("JQL sorts: " + Arrays.toString(sortField));
            }
            return runSearch(issueSearcher, finalQuery, permissionsFilter, sortField, searchQuery.toString(), pager);
        }
        catch (final IOException e)
        {
            throw new SearchException(e);
        }
    }

    private void search(final Query searchQuery, final User user, final Collector collector, org.apache.lucene.search.Query andQuery, boolean overrideSecurity) throws SearchException
    {
        final long start = System.currentTimeMillis();
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        org.apache.lucene.search.Query finalQuery = andQuery;

        if (searchQuery.getWhereClause() != null)
        {
            final QueryCreationContext context = new QueryCreationContextImpl(user, overrideSecurity);
            final org.apache.lucene.search.Query query = luceneQueryBuilder.createLuceneQuery(context, searchQuery.getWhereClause());
            if (query != null)
            {
                if (finalQuery != null)
                {
                    BooleanQuery join = new BooleanQuery();
                    join.add(finalQuery, BooleanClause.Occur.MUST);
                    join.add(query, BooleanClause.Occur.MUST);
                    finalQuery = join;
                }
                else
                {
                    finalQuery = query;
                }
                log.info("JQL query: " + searchQuery.toString());
                log.info("JQL lucene query: " + finalQuery);
            }
            else
            {
                log.info("Got a null query from the JQL Query.");
            }
        }

        final Filter permissionsFilter = getPermissionsFilter(overrideSecurity, user);
        UtilTimerStack.push("Searching with Collector");

        // NOTE: we do this because when you are searching for everything the query is EMPTY
        if (finalQuery == null)
        {
            finalQuery = new MatchAllDocsQuery();
        }
        try
        {
            searcher.search(finalQuery, permissionsFilter, collector);
        }
        catch (IOException e)
        {
            throw new SearchException("Exception whilst searching for issues " + e.getMessage(), e);
        }
        UtilTimerStack.pop("Searching with Collector");
        ThreadLocalQueryProfiler.store(ThreadLocalQueryProfiler.LUCENE_GROUP, finalQuery.toString(), (System.currentTimeMillis() - start));
    }

    private org.apache.lucene.search.Query createLuceneQuery(Query searchQuery, org.apache.lucene.search.Query andQuery, User searchUser, boolean overrideSecurity)
            throws SearchException
    {
        final String jqlSearchQuery = searchQuery.toString();

        org.apache.lucene.search.Query finalQuery = andQuery;

        if (searchQuery.getWhereClause() != null)
        {
            final QueryCreationContext context = new QueryCreationContextImpl(searchUser, overrideSecurity);
            final org.apache.lucene.search.Query query = luceneQueryBuilder.createLuceneQuery(context, searchQuery.getWhereClause());
            if (query != null)
            {
                if (log.isInfoEnabled())
                {
                    log.info("JQL query: " + jqlSearchQuery);
                }

                if (finalQuery != null)
                {
                    BooleanQuery join = new BooleanQuery();
                    join.add(finalQuery, BooleanClause.Occur.MUST);
                    join.add(query, BooleanClause.Occur.MUST);
                    finalQuery = join;
                }
                else
                {
                    finalQuery = query;
                }
            }
            else
            {
                if (log.isInfoEnabled())
                {
                    log.info("Got a null query from the JQL Query.");
                }
            }
        }

        // NOTE: we do this because when you are searching for everything the query is null
        if (finalQuery == null)
        {
            finalQuery = new MatchAllDocsQuery();
        }

        if (log.isInfoEnabled())
        {
            log.info("JQL lucene query: " + finalQuery);
        }
        return finalQuery;
    }

    private SearchResults search(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery, final boolean overrideSecurity) throws SearchException
    {
        final long start = System.currentTimeMillis();
        UtilTimerStack.push("Lucene Query");
        final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        final TopDocs luceneMatches = getHits(query, searcher, getSearchSorts(searcher, query), andQuery, overrideSecurity, issueSearcher, pager);
        UtilTimerStack.pop("Lucene Query");

        try
        {
            UtilTimerStack.push("Retrieve From cache/db and filter");
            final List matches;
            final int totalIssueCount = luceneMatches == null ? 0 : luceneMatches.totalHits;
            if ((luceneMatches != null) && (luceneMatches.totalHits >= pager.getStart()))
            {
                final int end = Math.min(pager.getEnd(), luceneMatches.totalHits);
                matches = new ArrayList();
                for (int i = pager.getStart(); i < end; i++)
                {
                    Document doc = issueSearcher.doc(luceneMatches.scoreDocs[i].doc);
                    matches.add(issueFactory.getIssue(doc));
                }
            }
            else
            {
                //if there were no lucene-matches, or the length of the matches is less than the page start index
                //return an empty list of issues.
                matches = Collections.emptyList();
            }
            UtilTimerStack.pop("Retrieve From cache/db and filter");

            return new SearchResults(matches, totalIssueCount, pager);
        }
        catch (final IOException e)
        {
            throw new SearchException("Exception whilst searching for issues " + e.getMessage(), e);
        }
    }

    private void searchAndSort(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter, final boolean overrideSecurity) throws SearchException
    {
        final long start = System.currentTimeMillis();

        UtilTimerStack.push("Searching and sorting with Collector");
        try
        {
            final IndexSearcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);

            final TopDocs hits = getHits(query, user, getSearchSorts(user, query), null, overrideSecurity, issueSearcher, pagerFilter);
            if ((hits != null) && (hits.totalHits >= pagerFilter.getStart()))
            {
                final int end = Math.min(pagerFilter.getEnd(), hits.totalHits);

                // big performance boost by making sure all the hits needed are pulled in.
//                if (end != 0)
//                {
//                    hits.id(end - 1);
//                }

                for (int i = pagerFilter.getStart(); i < end; i++)
                {
                    collector.collect(hits.scoreDocs[i].doc);
                }
            }
        }
        catch (IOException e)
        {
            throw new SearchException("Exception whilst searching for issues " + e.getMessage(), e);
        }

        UtilTimerStack.pop("Searching and sorting with Collector");
        ThreadLocalQueryProfiler.store(ThreadLocalQueryProfiler.LUCENE_GROUP, query.toString(), (System.currentTimeMillis() - start));
    }

    private CachedWrappedFilterCache getCachedWrappedFilterCache()
    {
        CachedWrappedFilterCache cache = (CachedWrappedFilterCache) JiraAuthenticationContextImpl.getRequestCache().get(
                RequestCacheKeys.CACHED_WRAPPED_FILTER_CACHE);

        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new CachedWrappedFilterCache");
            }
            cache = new CachedWrappedFilterCache();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.CACHED_WRAPPED_FILTER_CACHE, cache);
        }

        return cache;
    }

    private Filter getPermissionsFilter(final boolean overRideSecurity, final User searchUser)
    {
        if (!overRideSecurity)
        {
            // JRA-14980: first attempt to retrieve the filter from cache
            final CachedWrappedFilterCache cache = getCachedWrappedFilterCache();

            Filter filter = cache.getFilter(searchUser);
            if (filter != null)
            {
                return filter;
            }

            // if not in cache, construct a query (also using a cache)
            final org.apache.lucene.search.Query permissionQuery = permissionsFilterGenerator.getQuery(searchUser);
            filter = new CachingWrapperFilter(new QueryWrapperFilter(permissionQuery));

            // JRA-14980: store the wrapped filter in the cache
            // this is because the CachingWrapperFilter gives us an extra benefit of precalculating its BitSet, and so
            // we should use this for the duration of the request.
            cache.storeFilter(filter, searchUser);

            return filter;
        }
        else
        {
            return null;
        }
    }

    private TopDocs runSearch(final IndexSearcher searcher, final org.apache.lucene.search.Query query, final Filter filter, final SortField[] sortFields, final String searchQueryString, final PagerFilter pager) throws IOException
    {
        log.debug("Lucene boolean Query:" + query.toString(""));

        UtilTimerStack.push("Lucene Search");

        TopDocs hits;
        final OpTimer opTimer = Instrumentation.pullTimer(InstrumentationName.ISSUE_INDEX_READS);
        try {

            int maxHits;
            if (pager != null && pager.getEnd() > 0)
            {
                maxHits = pager.getEnd();
            }
            else
            {
                maxHits = Integer.MAX_VALUE;
            }
            if ((sortFields != null) && (sortFields.length > 0)) // a zero length array sorts in very weird ways! JRA-5151
            {
                hits = searcher.search(query, filter, maxHits, new Sort(sortFields));
            }
            else
            {
                hits = searcher.search(query, filter, maxHits);
            }
            // NOTE: this is only here so we can flag any queries in production that are taking long and try to figure out
            // why they are doing that.
            final long timeQueryTook = opTimer.end().getMillisecondsTaken();
            if (timeQueryTook > 400)
            {
                if (log.isDebugEnabled() || slowLog.isInfoEnabled())
                {
                    // truncate lucene query at 800 characters
                    String msg = String.format("JQL query '%s' produced lucene query '%-1.800s' and took '%d' ms to run.", searchQueryString, query.toString(), timeQueryTook);
                    if (log.isDebugEnabled())
                    {
                        log.debug(msg);
                    }
                    if (slowLog.isInfoEnabled())
                    {
                        slowLog.info(msg);
                    }
                }
            }
        } finally {

            UtilTimerStack.pop("Lucene Search");
        }
        return hits;
    }

    private SortField[] getSearchSorts(final User searcher, Query query)
    {
        if (query == null)
        {
            return null;
        }

        List<SearchSort> sorts = searchSortUtil.getSearchSorts(query);

        final List<SortField> luceneSortFields = new ArrayList<SortField>();
        // When the sorts have been specifically set to null then we run the search with no sorts
        if (sorts != null)
        {
            final FieldManager fieldManager = ComponentAccessor.getFieldManager();

            for (SearchSort searchSort : sorts)
            {
                // Lets figure out what field this searchSort is referring to. The {@link SearchSort#getField} method
                //actually a JQL name.
                final List<String> fieldIds = new ArrayList<String>(searchHandlerManager.getFieldIds(searcher, searchSort.getField()));
                // sort to get consistent ordering of fields for clauses with multiple fields
                Collections.sort(fieldIds);

                for (String fieldId : fieldIds)
                {
                    if (fieldManager.isNavigableField(fieldId))
                    {
                        final NavigableField field = fieldManager.getNavigableField(fieldId);
                        luceneSortFields.addAll(field.getSortFields(getSortOrder(searchSort, field)));
                    }
                    else
                    {
                        log.info("Search sort contains invalid field: " + searchSort);
                    }
                }
            }
        }

        return luceneSortFields.toArray(new SortField[luceneSortFields.size()]);
    }

    private boolean getSortOrder(final SearchSort searchSort, final NavigableField field)
    {
        boolean order;

        if (searchSort.getOrder() == null)
        {
            // We need to handle the case where the sort order is null, we will delegate off to the fields
            // default SearchSort for order in this case.
            String defaultSortOrder = field.getDefaultSortOrder();
            if (defaultSortOrder == null)
            {
                order = false;
            }
            else
            {
                order = SortOrder.parseString(defaultSortOrder) == SortOrder.DESC;
            }
        }
        else
        {
            order = searchSort.isReverse();
        }
        return order;
    }

}
