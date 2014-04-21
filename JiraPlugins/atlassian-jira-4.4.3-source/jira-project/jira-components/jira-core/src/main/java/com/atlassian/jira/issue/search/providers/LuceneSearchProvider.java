package com.atlassian.jira.issue.search.providers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
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
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;
import com.atlassian.query.Query;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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

    public final SearchResults search(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager)
            throws SearchException
    {
        return search(query, (User) searcher, pager);
    }

    public SearchResults search(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
    {
        return search(query, searcher, pager, andQuery, false);
    }

    public final SearchResults search(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return search(query, (User) searcher,  pager, andQuery);
    }

    public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery) throws SearchException
    {
        return search(query, searcher, pager, andQuery, true);
    }

    public final SearchResults searchOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return searchOverrideSecurity(query, (User)searcher,  pager, andQuery);
    }

    public long searchCount(final Query query, final User user) throws SearchException
    {
        return getHits(query, user, null, null, false).length();
    }

    public final long searchCount(final Query query, final com.opensymphony.user.User searcher)
            throws SearchException
    {
        return searchCount(query, (User) searcher);
    }

    public long searchCountOverrideSecurity(final Query query, final User user) throws SearchException
    {
        return getHits(query, user, null, null, true).length();
    }

    public final long searchCountOverrideSecurity(final Query query, final com.opensymphony.user.User searcher)
            throws SearchException
    {
        return searchCountOverrideSecurity(query, (User) searcher);
    }

    public void search(final Query query, final User user, final HitCollector hitCollector) throws SearchException
    {
        search(query, user, hitCollector, null, false);
    }

    public void search(final Query query, final User searcher, final HitCollector hitCollector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        search(query, searcher, hitCollector, andQuery, false);
    }

    public void search(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        search(query, (User) searcher, hitCollector, andQuery);
    }

    public final void search(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector)
            throws SearchException
    {
        search(query, (User) searcher,  hitCollector);
    }

    public void searchOverrideSecurity(final Query query, final User user, final HitCollector hitCollector) throws SearchException
    {
        search(query, user, hitCollector, null, true);
    }

    public final void searchOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector)
            throws SearchException
    {
        searchOverrideSecurity(query, (User) searcher,  hitCollector);
    }

    public void searchAndSort(final Query query, final User user, final HitCollector hitCollector, final PagerFilter pagerFilter) throws SearchException
    {
        searchAndSort(query, user, hitCollector, pagerFilter, false);
    }

    public final void searchAndSort(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final PagerFilter pager)
            throws SearchException
    {
        searchAndSort(query, (User) searcher, hitCollector, pager);
    }

    public void searchAndSortOverrideSecurity(final Query query, final User user, final HitCollector hitCollector, final PagerFilter pagerFilter) throws SearchException
    {
        searchAndSort(query, user, hitCollector, pagerFilter, true);
    }

    public final void searchAndSortOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final PagerFilter pager)
            throws SearchException
    {
        searchAndSortOverrideSecurity(query, (User) searcher, hitCollector, pager);
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
     * @return hits
     * @throws SearchException if error occurs
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if query creates a lucene query that is too complex to be processed.
     */
    private Hits getHits(final Query searchQuery, final User searchUser, final SortField[] sortField, final org.apache.lucene.search.Query andQuery, boolean overrideSecurity) throws SearchException
    {
        if (searchQuery == null)
        {
            return null;
        }

        final String jqlSearchQuery = searchQuery.toString();
        try
        {
            final Filter permissionsFilter = getPermissionsFilter(overrideSecurity, searchUser);
            final Searcher issueSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            org.apache.lucene.search.Query finalQuery = andQuery;

            if (searchQuery.getWhereClause() != null)
            {
                // TODO Change to use Crowd user directly
                final QueryCreationContext context = new QueryCreationContextImpl(OSUserConverter.convertToOSUser(searchUser), overrideSecurity);
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
                    log.info("Got a null query from the JQL Query.");
                }
            }

            // NOTE: we do this because when you are searching for everything the query is null
            if (finalQuery == null)
            {
                finalQuery = new MatchAllDocsQuery();
            }

            log.info("JQL lucene query: " + finalQuery);
            log.info("JQL sorts: " + Arrays.toString(sortField));
            return runSearch(issueSearcher, finalQuery, permissionsFilter, sortField, jqlSearchQuery);
        }
        catch (final IOException e)
        {
            throw new SearchException(e);
        }
    }

    private void search(final Query searchQuery, final User user, final HitCollector hitCollector, org.apache.lucene.search.Query andQuery, boolean overrideSecurity) throws SearchException
    {
        final long start = System.currentTimeMillis();
        final Searcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        org.apache.lucene.search.Query finalQuery = andQuery;

        if (searchQuery.getWhereClause() != null)
        {
            // TODO Change to use Crowd user directly
            final QueryCreationContext context = new QueryCreationContextImpl(OSUserConverter.convertToOSUser(user), overrideSecurity);
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
        UtilTimerStack.push("Searching with HitCollector");

        // NOTE: we do this because when you are searching for everything the query is EMPTY
        if (finalQuery == null)
        {
            finalQuery = new MatchAllDocsQuery();
        }
        try
        {
            searcher.search(finalQuery, permissionsFilter, hitCollector);
        }
        catch (IOException e)
        {
            throw new SearchException("Exception whilst searching for issues " + e.getMessage(), e);
        }
        UtilTimerStack.pop("Searching with HitCollector");
        ThreadLocalQueryProfiler.store(ThreadLocalQueryProfiler.LUCENE_GROUP, finalQuery.toString(), (System.currentTimeMillis() - start));
    }

    private SearchResults search(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery, final boolean overrideSecurity) throws SearchException
    {
        UtilTimerStack.push("Lucene Query");
        final Hits luceneMatches = getHits(query, searcher, getSearchSorts(searcher, query), andQuery, overrideSecurity);
        UtilTimerStack.pop("Lucene Query");

        try
        {
            UtilTimerStack.push("Retrieve From cache/db and filter");
            final List matches;
            final int totalIssueCount = luceneMatches == null ? 0 : luceneMatches.length();
            if ((luceneMatches != null) && (luceneMatches.length() >= pager.getStart()))
            {
                final int end = Math.min(pager.getEnd(), luceneMatches.length());
                matches = new ArrayList();
                for (int i = pager.getStart(); i < end; i++)
                {
                    matches.add(issueFactory.getIssue(luceneMatches.doc(i)));
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

    private void searchAndSort(final Query query, final User user, final HitCollector hitCollector, final PagerFilter pagerFilter, final boolean overrideSecurity) throws SearchException
    {
        final long start = System.currentTimeMillis();

        UtilTimerStack.push("Searching and sorting with HitCollector");
        try
        {
            final Hits hits = getHits(query, user, getSearchSorts(user, query), null, overrideSecurity);
            if ((hits != null) && (hits.length() >= pagerFilter.getStart()))
            {
                final int end = Math.min(pagerFilter.getEnd(), hits.length());

                // big performance boost by making sure all the hits needed are pulled in.
                if (end != 0)
                {
                    hits.id(end - 1);
                }

                for (int i = pagerFilter.getStart(); i < end; i++)
                {
                    hitCollector.collect(hits.id(i), 0);
                }
            }
        }
        catch (IOException e)
        {
            throw new SearchException("Exception whilst searching for issues " + e.getMessage(), e);
        }

        UtilTimerStack.pop("Searching and sorting with HitCollector");
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

            // TODO Change to use Crowd user directly
            com.opensymphony.user.User user = OSUserConverter.convertToOSUser(searchUser);
            Filter filter = cache.getFilter(user);
            if (filter != null)
            {
                return filter;
            }

            // if not in cache, construct a query (also using a cache)
            final org.apache.lucene.search.Query permissionQuery = permissionsFilterGenerator.getQuery(user);
            filter = new CachingWrapperFilter(new QueryWrapperFilter(permissionQuery));

            // JRA-14980: store the wrapped filter in the cache
            // this is because the CachingWrapperFilter gives us an extra benefit of precalculating its BitSet, and so
            // we should use this for the duration of the request.
            cache.storeFilter(filter, user);

            return filter;
        }
        else
        {
            return null;
        }
    }

    private Hits runSearch(final Searcher searcher, final org.apache.lucene.search.Query query, final Filter filter, final SortField[] sortFields, final String searchQueryString) throws IOException
    {
        log.debug("Lucene boolean Query:" + query.toString(""));

        UtilTimerStack.push("Lucene Search");
        Hits hits;
        final long start = System.currentTimeMillis();
        if ((sortFields != null) && (sortFields.length > 0)) // a zero length array sorts in very weird ways! JRA-5151
        {
            hits = searcher.search(query, filter, new Sort(sortFields));
        }
        else
        {
            hits = searcher.search(query, filter);
        }
        // NOTE: this is only here so we can flag any queries in production that are taking long and try to figure out
        // why they are doing that.
        final long end = System.currentTimeMillis();
        final long timeQueryTook = end - start;
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
        UtilTimerStack.pop("Lucene Search");

        return hits;
    }

    private List getIssueObjects(final Hits luceneMatches) throws IOException
    {
        if ((luceneMatches == null) || (luceneMatches.length() == 0))
        {
            return Collections.EMPTY_LIST;
        }

        final List issues = new ArrayList();
        for (final Iterator iterator = luceneMatches.iterator(); iterator.hasNext();)
        {
            final Hit hit = (Hit) iterator.next();
            issues.add(issueFactory.getIssue(hit.getDocument()));
        }
        return issues;
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
            final FieldManager fieldManager = ManagerFactory.getFieldManager();

            for (SearchSort searchSort : sorts)
            {
                // Lets figure out what field this searchSort is referring to. The {@link SearchSort#getField} method
                //actually a JQL name.
                // TODO Change to use Crowd user directly
                final List<String> fieldIds = new ArrayList<String>(searchHandlerManager.getFieldIds(OSUserConverter.convertToOSUser(searcher), searchSort.getField()));
                // sort to get consistent ordering of fields for clauses with multiple fields
                Collections.sort(fieldIds);

                for (String fieldId : fieldIds)
                {
                    if (fieldManager.isNavigableField(fieldId))
                    {
                        final NavigableField field = fieldManager.getNavigableField(fieldId);
                        final SortComparatorSource sorter = field.getSortComparatorSource();
                        if (sorter != null)
                        {
                            // lucene needs a field name. In some cases however, we don't have one. as it just caches the
                            // ScoreDocComparator for each field (and we can assume these are the same for a given field, we can
                            // just put the field name here if it isn't found.
                            String fieldName = field.getSorter() != null ? field.getSorter().getDocumentConstant() : "field_" + field.getId();
                            SortField sortField = new SortField(fieldName, sorter, getSortOrder(searchSort, field));
                            luceneSortFields.add(sortField);
                        }
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
