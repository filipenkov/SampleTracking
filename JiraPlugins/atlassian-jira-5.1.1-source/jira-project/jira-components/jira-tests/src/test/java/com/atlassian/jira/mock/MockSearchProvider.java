/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.apache.lucene.search.Collector;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MockSearchProvider implements SearchProvider
{
    List results = Collections.EMPTY_LIST;

    public MockSearchProvider()
    {
    }

    public SearchResults search(Query query, User searcher, PagerFilter pager) throws SearchException
    {
        return new SearchResults(results, new PagerFilter());
    }

    public long searchCount(Query query, User searcher) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public long searchCountOverrideSecurity(final Query query, final User searcher) throws SearchException
    {
        return 0;
    }

    public void search(Query query, User user, Collector collector) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void search(final Query query, final User searcher, final Collector collector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void searchOverrideSecurity(final Query query, final User user, final Collector collector)
            throws SearchException
    {
    }

    public void searchAndSort(Query query, User user, Collector collector, PagerFilter pagerFilter) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void searchAndSortOverrideSecurity(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public long searchCountIgnorePermissions(SearchRequest request, User searchUser) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void setResults(List results)
    {
        this.results = results;
    }

    public SearchResults search(Query query, User searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return null;
    }

}
