package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;

/**
 * Representation of a version move json object in the JIRA REST API.
 *
 * @since v4.4
 */
public class VersionMove
{
    public URI after;
    public String position;

    public VersionMove after(URI after)
    {
        this.after = after;
        return this;
    }

    public VersionMove position(String position)
    {
        this.position = position;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
