package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Representation in the JIRA REST API.
 *
 * @since v4.3
 */
public class IssueSecurityType
{
    public String self;
    public String id;
    public String name;
    public String description;

    public IssueSecurityType self(final String self)
    {
        this.self = self;
        return this;
    }

    public IssueSecurityType id(final String id)
    {
        this.id = id;
        return this;
    }

    public IssueSecurityType name(final String name)
    {
        this.name = name;
        return this;
    }

    public IssueSecurityType description(final String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
