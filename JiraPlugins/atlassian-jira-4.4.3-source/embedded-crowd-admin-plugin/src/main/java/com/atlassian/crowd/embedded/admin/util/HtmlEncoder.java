package com.atlassian.crowd.embedded.admin.util;

/**
 * Encode strings to escape html for xss protection.
 *
 */
public interface HtmlEncoder
{
    /**
     * Encode a string escaping html.
     * @param encodeMe String to be encoded.
     *
     * @return encoded string.
     */
    public String encode(String encodeMe);
}
