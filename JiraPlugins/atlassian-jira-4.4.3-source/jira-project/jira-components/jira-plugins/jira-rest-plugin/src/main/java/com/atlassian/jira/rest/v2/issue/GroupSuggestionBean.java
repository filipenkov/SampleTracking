package com.atlassian.jira.rest.v2.issue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A suggestion for a group picker. Returned by {@link GroupPickerResource}
 *
 * @since v4.4
 */
@XmlRootElement (name = "group")
public class GroupSuggestionBean
{
    @XmlElement
    private String name;

    @XmlElement
    private String html;

    public GroupSuggestionBean()
    {
    }

    GroupSuggestionBean(final String name, final String html)
    {
        this.name = name;
        this.html = html;
    }

    public String getName()
    {
        return name;
    }

    public String getHtml()
    {
        return html;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object rhs)
    {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
