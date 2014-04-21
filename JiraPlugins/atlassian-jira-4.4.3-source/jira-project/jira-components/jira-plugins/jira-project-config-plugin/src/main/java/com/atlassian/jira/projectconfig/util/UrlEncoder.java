package com.atlassian.jira.projectconfig.util;

/**
 * Simple class to URL encode the passed string.
 *
 * @since v4.4
 */
public interface UrlEncoder
{
    /**
     * URL encode the passed string.
     *
     * @param value the value to URL encode.
     * @return the encoded value.
     */
    String encode(String value);
}
