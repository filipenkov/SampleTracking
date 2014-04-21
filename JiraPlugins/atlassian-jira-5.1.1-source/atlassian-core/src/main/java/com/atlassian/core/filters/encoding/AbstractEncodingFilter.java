package com.atlassian.core.filters.encoding;

import com.atlassian.core.filters.AbstractHttpFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Sets the encoding of request and response to a value defined by the application, and prevents later
 * changes to it by wrapping the response in a {@link FixedHtmlEncodingResponseWrapper}.
 * <p/>
 * For unrelated functionality that used to be performed by the old filter, see the related classes below.
 *
 * @see com.atlassian.core.filters.cache.AbstractCachingFilter
 * @see com.atlassian.core.filters.legacy.NoContentLocationHeaderResponseWrapper
 * @see com.atlassian.core.filters.legacy.WordCurlyQuotesRequestWrapper
 * @since 4.0
 */
public abstract class AbstractEncodingFilter extends AbstractHttpFilter
{
    /**
     * Sets the encoding of the request and the content-type of the response (which includes the
     * charset parameter) based on the values returned from the template methods. Wraps the request
     * in a {@link FixedHtmlEncodingResponseWrapper} to ensure the content-type is not changed later.
     * <p/>
     * After setting the encoding and wrapping the request, the remainder of the filter chain is
     * processed normally.
     * <p/>
     * If your application wants to be sure that the encoding set by this filter is used for all
     * HTML responses, this filter should be the first filter in your filter chain.
     */
    protected final void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException
    {
        request.setCharacterEncoding(getEncoding());
        response.setContentType(getContentType());

        filterChain.doFilter(request, new FixedHtmlEncodingResponseWrapper(response));
    }

    /**
     * Return the content type to be used for the response, via {@link HttpServletResponse#setContentType(String)}.
     * The header should include a charset parameter. For example: "text/html; charset=UTF-8".
     */
    abstract protected String getContentType();

    /**
     * Return the encoding to be used on the request, via {@link HttpServletRequest#setCharacterEncoding(String)}.
     * For example: "UTF-8".
     */
    abstract protected String getEncoding();
}
