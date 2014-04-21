package com.atlassian.core.filters.encoding;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Prevents the encoding of the response being changed with {@link #setContentType(String)}. If
 * that method is called with "text/html; charset=...", it ignores the change.
 * <p/>
 * This is meant to ensure that the encoding of the response can't be changed after an encoding
 * filter sets it.
 *
 * @see AbstractEncodingFilter
 * @since 4.0
 */
public final class FixedHtmlEncodingResponseWrapper extends HttpServletResponseWrapper
{
    public FixedHtmlEncodingResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    public final void setContentType(String contentType)
    {
        // If something tries to change the content type of the response by setting a content type of
        // "text/html; charset=...", just ignore it. This happens in Tomcat and Jetty JSPs.
        if (StringUtils.startsWith(contentType, "text/html") && contentType.length() > "text/html".length())
        {
            return;
        }

        // Ensure that the charset parameter is appended if we're called with just "text/html".
        // This happens on WebLogic.
        if (StringUtils.trimToEmpty(contentType).equals("text/html"))
        {
            super.setContentType(contentType + ";charset=" + getResponse().getCharacterEncoding());
            return;
        }

        // for all other content types, just set the value
        super.setContentType(contentType);
    }
}
