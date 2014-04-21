package com.atlassian.core.filters.cache;

import com.atlassian.core.filters.AbstractHttpFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Uses a list of caching strategies provided by the subclass, applying the first one which matches.
 * After the caching headers are applied (or not, if no strategies match), the request is processed
 * as normal.
 *
 * @see CachingStrategy
 * @since 4.0
 */
public abstract class AbstractCachingFilter extends AbstractHttpFilter
{
    /**
     * Before processing the filter chain, iterates through the caching strategies returned by
     * {@link #getCachingStrategies()} and applies the first one that matches.
     */
    public final void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException
    {
        CachingStrategy strategy = getFirstMatchingStrategy(request);
        if (strategy != null)
            strategy.setCachingHeaders(response);

        filterChain.doFilter(request, response);
    }

    private CachingStrategy getFirstMatchingStrategy(HttpServletRequest request)
    {
        CachingStrategy[] strategies = getCachingStrategies();
        if (strategies == null)
            return null;

        for (CachingStrategy strategy : strategies)
        {
            if (strategy.matches(request))
                return strategy;
        }
        return null;
    }

    /**
     * Subclasses should return an array of caching strategies to use. The first one that matches
     * will be applied by this filter.
     */
    abstract protected CachingStrategy[] getCachingStrategies();


    // not to be overridden
    public final void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
    }

    // not to be overridden
    public final void destroy()
    {
        super.destroy();
    }
}
