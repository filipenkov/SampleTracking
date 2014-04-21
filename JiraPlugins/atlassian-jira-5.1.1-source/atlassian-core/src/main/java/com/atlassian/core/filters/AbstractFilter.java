package com.atlassian.core.filters;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * This class contains two deprecated methods to make filters compatible with old web application servers that are using
 * the old servlet spec
 *
 * @deprecated since 4.0 use {@link AbstractHttpFilter} instead, which also does some casting for you.
 * @see AbstractHttpFilter
 */
public abstract class AbstractFilter implements Filter
{
    /** @deprecated Not needed in final version of Servlet 2.3 API - replaced by init(). */
    // NOTE: Applications don't work in Orion 1.5.2 without this method
    public FilterConfig getFilterConfig()
    {
        return null;
    }

    /** @deprecated Not needed in final version of Servlet 2.3 API - replaced by init(). */
    // NOTE: Applications don't work with Orion 1.5.2 without this method
    public void setFilterConfig(FilterConfig filterConfig)
    {
        try
        {
            init(filterConfig);
        }
        catch (ServletException e)
        {
            //do nothing
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Implemented for convenience
    }

    public void destroy()
    {
        // Implemented for convenience
    }
}
