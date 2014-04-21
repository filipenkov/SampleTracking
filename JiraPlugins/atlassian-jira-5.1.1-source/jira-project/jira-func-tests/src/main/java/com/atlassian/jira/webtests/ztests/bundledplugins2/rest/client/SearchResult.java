package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of a search result in the JIRA REST API.
 *
 * @since v4.3
 */
public class SearchResult
{
    public String expand;
    public Integer startAt;
    public Integer maxResults;
    public Integer total;
    public List<Issue> issues;
    public Map<String, String> names;
    public Map<String, JsonTypeBean> schema;
    public Set<String> warningMessages;

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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
