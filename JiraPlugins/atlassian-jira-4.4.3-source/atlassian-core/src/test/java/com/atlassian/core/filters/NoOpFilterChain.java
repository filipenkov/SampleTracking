package com.atlassian.core.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class NoOpFilterChain implements FilterChain
{
    public static FilterChain getInstance()
    {
        return new NoOpFilterChain();
    }

    private NoOpFilterChain()
    {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
    {
        // do nothing
    }
}
