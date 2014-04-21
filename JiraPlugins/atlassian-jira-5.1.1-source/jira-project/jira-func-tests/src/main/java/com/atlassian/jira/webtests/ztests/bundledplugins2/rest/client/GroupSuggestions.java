package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.List;

/**
 * Represent the GroupSuggestionsBean containing suggestions and headers returned by the GroupPickerResource
 *
 * @since v4.4
 */
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class GroupSuggestions
{
    public String header;
    public List<GroupSuggestion> groups;
    public Integer total;

    public GroupSuggestions header(final String header)
    {
        this.header = header;
        return this;
    }

    public GroupSuggestions total(final Integer total)
    {
        this.total = total;
        return this;
    }

    public GroupSuggestions groups(final List<GroupSuggestion> groups)
    {
        this.groups = groups;
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
