package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represent a GroupSuggestionBean returned by the GroupPickerResource
 *
 * @since v4.4
 */
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class GroupSuggestion
{
    public String name;
    public String html;

    public GroupSuggestion name(final String name)
    {
        this.name = name;
        return this;
    }

    public GroupSuggestion html(final String html)
    {
        this.html = html;
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
