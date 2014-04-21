package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class SimpleLink
{
    public String id;
    public String styleClass;
    public String iconClass;
    public String label;
    public String title;
    public String href;


    public SimpleLink id(String id)
    {
        this.id = id;
        return this;
    }

    public SimpleLink styleClass(String styleClass)
    {
        this.styleClass = styleClass;
        return this;
    }

    public SimpleLink iconClass(String iconClass)
    {
        this.iconClass = iconClass;
        return this;
    }

    public SimpleLink label(String label)
    {
        this.label = label;
        return this;
    }

    public SimpleLink title(String title)
    {
        this.title = title;
        return this;
    }

    public SimpleLink href(String href)
    {
        this.href = href;
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
