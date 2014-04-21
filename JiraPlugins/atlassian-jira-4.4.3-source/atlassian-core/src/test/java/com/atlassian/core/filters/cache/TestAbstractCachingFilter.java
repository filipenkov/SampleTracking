package com.atlassian.core.filters.cache;

import junit.framework.TestCase;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.core.filters.ServletStubs;
import com.atlassian.core.filters.NoOpFilterChain;

public class TestAbstractCachingFilter extends TestCase
{
    private ServletStubs.Request request;
    private ServletStubs.Response response;

    protected void setUp() throws Exception
    {
        super.setUp();
        request = ServletStubs.getRequestInstance();
        response = ServletStubs.getResponseInstance();
    }

    public void testSubclassThatReturnsNull() throws Exception
    {
        Filter filter = new AbstractCachingFilter() {
            protected CachingStrategy[] getCachingStrategies()
            {
                return null;
            }
        };

        // no asserts, just checking that there's no exception
        filter.doFilter(request, response, NoOpFilterChain.getInstance());
    }

    public void testSubclassThatReturnsEmptyArray() throws Exception
    {
        Filter filter = new AbstractCachingFilter() {
            protected CachingStrategy[] getCachingStrategies()
            {
                return new CachingStrategy[0];
            }
        };

        // no asserts, just checking that there's no exception
        filter.doFilter(request, response, NoOpFilterChain.getInstance());
    }

    public void testSingleStrategy() throws Exception
    {
        final StubCachingStrategy strategy = new StubCachingStrategy();

        Filter filter = new AbstractCachingFilter() {
            protected CachingStrategy[] getCachingStrategies()
            {
                return new CachingStrategy[]{ strategy };
            }
        };

        filter.doFilter(request, response, NoOpFilterChain.getInstance());

        assertEquals("true", response.getHeader("cache"));
    }

    public void testTwoStrategies() throws Exception
    {
        final StubCachingStrategy strategy1 = new StubCachingStrategy();
        strategy1.matches = false;
        final StubCachingStrategy strategy2 = new StubCachingStrategy();
        strategy2.cacheValue = "strategy2";

        Filter filter = new AbstractCachingFilter() {
            protected CachingStrategy[] getCachingStrategies()
            {
                return new CachingStrategy[]{ strategy1, strategy2 };
            }
        };

        filter.doFilter(request, response, NoOpFilterChain.getInstance());

        assertEquals("strategy2", response.getHeader("cache"));
    }

    public void testFirstStrategyMatches() throws Exception
    {
        final StubCachingStrategy strategy1 = new StubCachingStrategy();
        strategy1.cacheValue = "strategy1";
        final StubCachingStrategy strategy2 = new StubCachingStrategy();
        strategy2.cacheValue = "strategy2";

        Filter filter = new AbstractCachingFilter() {
            protected CachingStrategy[] getCachingStrategies()
            {
                return new CachingStrategy[]{ strategy1, strategy2 };
            }
        };

        filter.doFilter(request, response, NoOpFilterChain.getInstance());

        assertEquals("strategy1", response.getHeader("cache"));
    }

    private class StubCachingStrategy implements CachingStrategy
    {
        boolean matches = true;
        String cacheValue = "true";

        public boolean matches(HttpServletRequest request)
        {
            return matches;
        }

        public void setCachingHeaders(HttpServletResponse response)
        {
            response.setHeader("cache", cacheValue);
        }
    }
}
