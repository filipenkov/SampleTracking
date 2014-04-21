package com.atlassian.core.filters;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A wrapper for a {@link javax.servlet.http.HttpServletResponse} that sanitises all mutations to the header of the
 * response to ensure that no suspect values are being written.
 *
 * The protocol for sanitising header values is essentially to replace any encountered carriage return or new line
 * characters with a single space.
 *
 * @since v4.2
 */
public class HeaderSanitisingResponseWrapper extends HttpServletResponseWrapper
{
    private static final char[] DISALLOWED_CHARS = new char[] {'\r', '\n'};
    private static final char REPLACEMENT_CHAR = ' ';

    private final char[] disallowedChars;
    private final char replacementChar;

    public HeaderSanitisingResponseWrapper(HttpServletResponse httpServletResponse)
    {
        this(httpServletResponse, DISALLOWED_CHARS, REPLACEMENT_CHAR);
    }

    /**
     * For testing purposes.
     *
     * @param httpServletResponse the response we are wrapping
     * @param disallowedChars the characters that will be replaced
     * @param replacementChar the replacement char
     */
    HeaderSanitisingResponseWrapper(final HttpServletResponse httpServletResponse, final char[] disallowedChars, final char replacementChar)
    {
        super(httpServletResponse);
        Arrays.sort(disallowedChars);
        this.disallowedChars = disallowedChars;
        this.replacementChar = replacementChar;
    }

    /**
     * Sanitises cookie value before adding it to the response. Note that cookie names are immutable and so cannot be
     * sanitised here.
     *
     * @param cookie the cookie to add to the header.
     */
    public void addCookie(Cookie cookie)
    {
        if (cookie != null)
        {
            cookie.setValue(cleanString(cookie.getValue()));
        }
        super.addCookie(cookie);
    }

    public void setContentType(final String contentType)
    {
        super.setContentType(cleanString(contentType));
    }

    public void setDateHeader(final String name, final long value)
    {
        super.setDateHeader(cleanString(name), value);
    }

    public void addDateHeader(final String name, final long value)
    {
        super.addDateHeader(cleanString(name), value);
    }

    public void setHeader(final String name, final String value)
    {
        super.setHeader(cleanString(name), cleanString(value));
    }

    public void addHeader(final String name, final String value)
    {
        super.addHeader(cleanString(name), cleanString(value));
    }

    public void setIntHeader(final String name, final int value)
    {
        super.setIntHeader(cleanString(name), value);
    }

    public void addIntHeader(final String name, final int value)
    {
        super.addIntHeader(cleanString(name), value);
    }

    public void sendRedirect(final String location) throws IOException
    {
        super.sendRedirect(cleanString(location));
    }

    public void sendError(final int code, final String message) throws IOException
    {
        super.sendError(code, cleanString(message));
    }

    public void setStatus(final int code, final String status)
    {
        super.setStatus(code, cleanString(status));
    }

    /**
     * Replaces all the chars in the input string lower than {@link #disallowedChars} with {@link #replacementChar}.
     *
     * @param value the string to clean
     * @return the "cleaned" string
     */
    String cleanString(String value)
    {
        if (value != null && !("".equals(value)))
        {
            char[] chars = value.toCharArray();
            for (int i = 0; i < chars.length; i++)
            {
                if (isDisallowedChar(chars[i]))
                {
                    chars[i] = replacementChar;
                }
            }
            value = new String(chars);
        }
        return value;
    }

    private boolean isDisallowedChar(final char c)
    {
        return Arrays.binarySearch(disallowedChars, c) >= 0;
    }
}
