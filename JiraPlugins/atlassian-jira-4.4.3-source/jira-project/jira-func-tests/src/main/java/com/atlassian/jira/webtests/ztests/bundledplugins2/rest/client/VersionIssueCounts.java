package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;

/**
 * Representation of a version in the JIRA REST API.
 *
 * @since v4.4
 */
public class VersionIssueCounts
{
    public String self;
    public long issuesFixedCount;
    public long issuesAffectedCount;

    public VersionIssueCounts self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public VersionIssueCounts self(String self)
    {
        this.self = self;
        return this;
    }

    public VersionIssueCounts issuesFixedCount(long issuesFixedCount)
    {
        this.issuesFixedCount = issuesFixedCount;
        return this;
    }

    public VersionIssueCounts issuesAffectedCount(long issuesAffectedCount)
    {
        this.issuesAffectedCount = issuesAffectedCount;
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
