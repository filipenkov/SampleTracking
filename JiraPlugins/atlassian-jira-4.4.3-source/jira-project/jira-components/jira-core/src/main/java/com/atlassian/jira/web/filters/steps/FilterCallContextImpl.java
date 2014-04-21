package com.atlassian.jira.web.filters.steps;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilterCallContextImpl implements FilterCallContext
{
    private final FilterChain filterChain;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    public FilterCallContextImpl(FilterCallContext callContext) {
        this.httpServletRequest = callContext.getHttpServletRequest();
        this.httpServletResponse = callContext.getHttpServletResponse();
        this.filterChain = callContext.getFilterChain();

    }

    public FilterCallContextImpl(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
    {
        this.filterChain = filterChain;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public FilterChain getFilterChain()
    {
        return filterChain;
    }

    @Override
    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

    @Override
    public HttpServletResponse getHttpServletResponse()
    {
        return httpServletResponse;
    }
}
