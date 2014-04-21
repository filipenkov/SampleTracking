/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jun 8, 2004
 * Time: 4:39:54 PM
 */
package com.atlassian.core.filters;

import com.atlassian.core.logging.ThreadLocalErrorCollection;
import com.atlassian.util.profiling.filters.ProfilingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class ProfilingAndErrorFilter extends ProfilingFilter
{
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        ThreadLocalErrorCollection.clear();
        ThreadLocalErrorCollection.enable();

        try
        {
            super.doFilter(servletRequest, servletResponse, filterChain);
        }
        finally
        {
            ThreadLocalErrorCollection.disable();
            ThreadLocalErrorCollection.clear();
        }
    }
}