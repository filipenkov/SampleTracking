package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.util.JiraUrlCodec;

/**
 * @since v4.4
 */
public class DefaultUrlEncoder implements UrlEncoder
{
    public String encode(String value)
    {
        return JiraUrlCodec.encode(value);
    }
}
