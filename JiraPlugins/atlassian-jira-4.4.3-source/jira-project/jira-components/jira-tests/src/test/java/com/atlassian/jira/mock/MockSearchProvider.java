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
import org.apache.lucene.search.HitCollector;

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

    public SearchResults search(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager)
            throws SearchException
    {
        return search(query, (User) searcher, pager);
    }


    public long searchCount(Query query, User searcher) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public long searchCount(final Query query, final com.opensymphony.user.User searcher)
            throws SearchException
    {
        return searchCount(query, (User) searcher);
    }




    public long searchCountOverrideSecurity(final Query query, final User searcher) throws SearchException
    {
        return 0;
    }

    public long searchCountOverrideSecurity(final Query query, final com.opensymphony.user.User searcher)
            throws SearchException
    {
        return searchCountOverrideSecurity(query, (User) searcher);
    }


    public void search(Query query, User user, HitCollector hitCollector) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void search(final Query query, final User searcher, final HitCollector hitCollector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void search(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void search(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector)
            throws SearchException
    {
        search(query, (User) searcher, hitCollector);
    }

    public void searchOverrideSecurity(final Query query, final User user, final HitCollector hitCollector)
            throws SearchException
    {
    }

    public void searchOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector)
            throws SearchException
    {
        searchOverrideSecurity(query, (User) searcher, hitCollector);
    }

    public void searchAndSort(Query query, User user, HitCollector hitCollector, PagerFilter pagerFilter) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void searchAndSort(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final PagerFilter pager)
            throws SearchException
    {
        searchAndSort(query, (User) searcher, hitCollector, pager);
    }

    public void searchAndSortOverrideSecurity(final Query query, final User user, final HitCollector hitCollector, final PagerFilter pagerFilter)
            throws SearchException
    {
    }

    public void searchAndSortOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final HitCollector hitCollector, final PagerFilter pager)
            throws SearchException
    {
        searchAndSortOverrideSecurity(query, (User) searcher, hitCollector, pager);
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

    public SearchResults search(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return search(query, (User) searcher, pager, andQuery);
    }

    public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return null;
    }

    public SearchResults searchOverrideSecurity(final Query query, final com.opensymphony.user.User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return searchOverrideSecurity(query, (User) searcher, pager, andQuery);
    }


}
