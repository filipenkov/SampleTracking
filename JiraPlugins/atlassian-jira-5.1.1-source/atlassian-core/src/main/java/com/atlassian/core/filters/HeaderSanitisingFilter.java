package com.atlassian.core.filters;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that will wrap {@link javax.servlet.http.HttpServletResponse}s with a
 * {@link com.atlassian.core.filters.HeaderSanitisingResponseWrapper}, so that any mutations made to the response's
 * header are sanitised.
 *
 * @since v4.2
 */
public class HeaderSanitisingFilter extends AbstractFilter
{
    static final String ALREADY_FILTERED = HeaderSanitisingFilter.class.getName() + "_already_filtered";

    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
    {
        if (req.getAttribute(ALREADY_FILTERED) != null)
        {
            chain.doFilter(req, res);
            return;
        }
        else
        {
            req.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
        }

        if (req instanceof HttpServletRequest)
        {
            res = new HeaderSanitisingResponseWrapper((HttpServletResponse) res);
        }
        chain.doFilter(req, res);
    }
}