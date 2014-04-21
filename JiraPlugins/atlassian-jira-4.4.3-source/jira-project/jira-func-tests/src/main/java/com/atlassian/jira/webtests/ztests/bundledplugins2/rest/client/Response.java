package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * An response, with optional error messages.
 *
 * @since v4.3
 */
public class Response
{
    public final int statusCode;
    public final Errors entity;

    public Response(int statusCode, Errors entity)
    {
        this.statusCode = statusCode;
        this.entity = entity;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
