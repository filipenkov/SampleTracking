package com.atlassian.core.filters.legacy;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Legacy response wrapper which prevents the Content-Location header being set on the wrapped
 * response.
 * <p/>
 * This was necessary for Orion application servers earlier than 2.0.2, which set the Content-
 * Location header on JSPs automatically. Opera versions 7.x and 8.x used the header to resolve
 * relative URLs, but this was reverted in Opera 9.
 * <p/>
 * This is here for legacy functionality, but its use is <b>not</b> recommended. Our applications
 * no longer support the versions of Opera and Orion that need this fix.
 *
 * @since 4.0
 */
public final class NoContentLocationHeaderResponseWrapper extends HttpServletResponseWrapper
{
    public NoContentLocationHeaderResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    /**
     * If the header name is "Content-Location", the header is not set. Otherwise, delegates
     * to the wrapped response.
     */
    public void setHeader(String name, String value)
    {
        if (isContentLocationHeader(name))
            return;
        super.setHeader(name, value);
    }

    /**
     * If the header name is "Content-Location", the header is not added to. Otherwise,
     * delegates to the wrapped response.
     */
    public void addHeader(String name, String value)
    {
        if (isContentLocationHeader(name))
            return;
        super.addHeader(name, value);
    }

    private boolean isContentLocationHeader(String headerName)
    {
        return headerName != null && "content-location".equalsIgnoreCase(headerName);
    }
}
