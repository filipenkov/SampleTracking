package com.atlassian.core.filters.cache;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Applies caching headers to ensures JSP responses are not cached by the client. A JSP request is
 * one that has the string ".jsp" somewhere in the request URI.
 *
 * @since 4.0
 */
public final class JspCachingStrategy implements CachingStrategy
{
    public final boolean matches(HttpServletRequest request)
    {
        final String uri = request.getRequestURI();
        return StringUtils.indexOf(uri, ".jsp") > 0;
    }

    public final void setCachingHeaders(HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // http 1.1
        response.setHeader("Pragma", "no-cache"); // http 1.0
        response.setDateHeader("Expires", 0); // prevent proxy caching
    }
}
