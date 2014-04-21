/*
 * Copyright (c) 2002-2004 All rights reserved.
 */
package com.atlassian.jira.plugin.searchrequestview.auth;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.issueview.IssueViewFieldParamsImpl;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.ListeningTestCase;

/**
 * test that the SearchResultSizeAuthorizer will delegate if size is less than configured limit,
 * and return false otherwise.
 */
public class TestSearchResultSizeAuthorizer extends ListeningTestCase
{
    private SearchRequest request;
    private final SearchRequestParams params = new SearchRequestParams()
    {
        public PagerFilter getPagerFilter()
        {
            return new PagerFilter(10);
        }

        public IssueViewFieldParamsImpl getIssueViewFieldParams()
        {
            return null;
        }

        public Map<?, ?> getSession()
        {
            return new HashMap<Object, Object>();
        }

        public String getUserAgent()
        {
            return null;
        }
    };
    private final SearchRequestParams searchCountParams = new SearchRequestParams()
    {
        public PagerFilter getPagerFilter()
        {
            return new PagerFilter(50);
        }

        public IssueViewFieldParamsImpl getIssueViewFieldParams()
        {
            return null;
        }

        public Map<?, ?> getSession()
        {
            final HashMap<String, String> session = new HashMap<String, String>();
            session.put("searchCount", "45");
            return Collections.unmodifiableMap(session);
        }

        public String getUserAgent()
        {
            return null;
        }
    };
    private final AtomicBoolean delegateCalled = new AtomicBoolean();
    private final Authorizer delegate = new Authorizer()
    {
        @Override
        public Result isSearchRequestAuthorized(com.atlassian.crowd.embedded.api.User user, SearchRequest searchRequest, SearchRequestParams params)
        {
            delegateCalled.set(true);
            return Result.OK;
        }
    };

    private final Object searchProvider = new Object()
    {
        @SuppressWarnings("unused")
        public long searchCount(final Query query, final User user)
        {
            return 20;
        }
    };
    private final SearchProvider searchProviderProxy = (SearchProvider) DuckTypeProxy.getProxy(SearchProvider.class, searchProvider);

    @Test
    public void testSizeOK()
    {
        final SearchResultSizeAuthorizer authorizer = new SearchResultSizeAuthorizer(searchProviderProxy, 50, delegate);

        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(null, request, params));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testLimited()
    {
        final SearchResultSizeAuthorizer authorizer = new SearchResultSizeAuthorizer(searchProviderProxy, 5, delegate);

        // authoriser will return false
        assertFalse(authorizer.isSearchRequestAuthorized(null, request, params).isOK());
        assertFalse(delegateCalled.get());
    }

    @Test
    public void testNullSearchProvider()
    {
        try
        {
            new SearchResultSizeAuthorizer(null, 5, delegate);
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Test
    public void testNegativeResultCount()
    {
        try
        {
            new SearchResultSizeAuthorizer(searchProviderProxy, -1, delegate);
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Test
    public void testNullDelegate()
    {
        try
        {
            new SearchResultSizeAuthorizer(searchProviderProxy, 5, null);
            fail("should have thrown something");
        }
        catch (final Exception yay)
        {}
    }

    @Test
    public void testSearchCountParamOK()
    {
        final SearchResultSizeAuthorizer authorizer = new SearchResultSizeAuthorizer(searchProviderProxy, 50, delegate);

        assertEquals(Authorizer.Result.OK, authorizer.isSearchRequestAuthorized(null, request, searchCountParams));
        assertTrue(delegateCalled.get());
    }

    @Test
    public void testSearchCountParamLimited()
    {
        final SearchResultSizeAuthorizer authorizer = new SearchResultSizeAuthorizer(searchProviderProxy, 40, delegate);

        // authoriser will return false
        assertFalse(authorizer.isSearchRequestAuthorized(null, request, searchCountParams).isOK());
        assertFalse(delegateCalled.get());
    }
}
