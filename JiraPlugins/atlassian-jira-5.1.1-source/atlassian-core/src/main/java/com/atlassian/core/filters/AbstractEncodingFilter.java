package com.atlassian.core.filters;

import com.atlassian.core.filters.cache.CachingStrategy;
import com.atlassian.core.filters.cache.JspCachingStrategy;
import com.atlassian.core.filters.legacy.WordCurlyQuotesRequestWrapper;
import com.atlassian.core.filters.legacy.NoContentLocationHeaderResponseWrapper;
import com.atlassian.core.filters.encoding.FixedHtmlEncodingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractEncodingFilter extends AbstractHttpFilter
{
    private final CachingStrategy jspCachingStrategy = new JspCachingStrategy();

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        request.setCharacterEncoding(getEncoding());
        response.setContentType(getContentType());

        // prevent caching of JSPs
        if (isNonCachableUri(request))
            setNonCachingHeaders(response);

        filterChain.doFilter(
            new WordCurlyQuotesRequestWrapper(request, getEncoding()),
            new FixedHtmlEncodingResponseWrapper(new NoContentLocationHeaderResponseWrapper(response)));
    }

    protected void setNonCachingHeaders(HttpServletResponse response)
    {
        jspCachingStrategy.setCachingHeaders(response);
    }

    // override this method to prevent caching of other file types
    protected boolean isNonCachableUri(HttpServletRequest request)
    {
        return jspCachingStrategy.matches(request);
    }

    protected abstract String getEncoding();

    protected abstract String getContentType();

}
