package com.atlassian.applinks.core.util;

/**
 * Indicates that a URL is invalid.
 *
 * @since 3.0
 */
public class InvalidURLException extends Exception
{
    private final String invalidURL;
    private final String field;

    public InvalidURLException(final String invalidURL, final String field)
    {
        this.invalidURL= invalidURL;
        this.field = field;
    }

    public InvalidURLException(final String invalidURL, final String field, final Throwable cause)
    {
        super(cause);
        this.invalidURL = invalidURL;
        this.field = field;
    }

    public String getInvalidURL()
    {
        return invalidURL;
    }

    public String getField()
    {
        return field;
    }
}
