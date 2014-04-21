package com.atlassian.crowd.embedded.admin.util;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Encode strings to escape html for xss protection.
 */
public class DefaultHtmlEncoder implements HtmlEncoder
{
    public String encode(String encodeMe)
    {
        return StringEscapeUtils.escapeHtml(encodeMe);
    }

}
