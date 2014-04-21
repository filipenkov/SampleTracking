package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.annotate.JsonProperty;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * Representation for group in the JIRA REST API.
 *
 * @since v4.3
 */
public class Group
{
    @JsonProperty
    private String name;

    public Group()
    {
    }

    public Group(String name)
    {
        this.name = name;
    }

    public String name()
    {
        return this.name;
    }

    public Group name(String name)
    {
        return new Group(name);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}
