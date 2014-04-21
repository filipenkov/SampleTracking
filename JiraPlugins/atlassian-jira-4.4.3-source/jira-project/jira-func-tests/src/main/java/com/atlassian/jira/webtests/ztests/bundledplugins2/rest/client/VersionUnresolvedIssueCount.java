package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;

/**
 * Representation of an unresolved issue count in the JIRA REST API.
 *
 * @since v4.4
 */
public class VersionUnresolvedIssueCount
{
    public String self;
    public long issuesUnresolvedCount;

    public VersionUnresolvedIssueCount self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public VersionUnresolvedIssueCount self(String self)
    {
        this.self = self;
        return this;
    }

    public VersionUnresolvedIssueCount issuesUnresolvedCount(long issuesUnresolvedCount)
    {
        this.issuesUnresolvedCount = issuesUnresolvedCount;
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
