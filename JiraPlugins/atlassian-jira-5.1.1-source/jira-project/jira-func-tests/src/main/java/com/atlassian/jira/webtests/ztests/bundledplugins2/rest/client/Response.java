package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

/**
 * An response, with optional error messages.
 *
 * @since v4.3
 */
public class Response<T>
{
    public final int statusCode;
    public final Errors entity;
    public final T body;

    public Response(int statusCode, Errors entity)
    {
        this.statusCode = statusCode;
        this.entity = entity;
        this.body = null;
    }

    public Response(int statusCode, Errors entity, T body)
    {
        this.statusCode = statusCode;
        this.entity = entity;
        this.body = body;
    }

    @Override
    public boolean equals(Object obj)
    {
        return reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
