package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * Representation of a vote in the JIRA REST API.
 *
 * @since v4.3
 */
public class Vote
{
    public String self;
    public int votes;
    public boolean hasVoted;
    public List<User> voters;

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
