package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Representation of a status in the JIRA REST API.
 *
 * @since v4.3
 */
public class Status
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String self;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    public Status()
    {
    }

    public Status(String id, String name, String self, String description, String iconUrl)
    {
        this.id = id;
        this.name = name;
        this.self = self;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public String id()
    {
        return this.id;
    }

    public Status id(String id)
    {
        return new Status(id, name, self, description, iconUrl);
    }

    public String name()
    {
        return name;
    }

    public Status name(String name)
    {
        return new Status(id, name, self, description, iconUrl);
    }

    public String self()
    {
        return this.self;
    }

    public Status self(String self)
    {
        return new Status(id, name, self, description, iconUrl);
    }

    public String description()
    {
        return this.description;
    }

    public Status description(String description)
    {
        return new Status(id, name, self, description, iconUrl);
    }

    public String iconUrl()
    {
        return iconUrl;
    }

    public Status iconUrl(String iconUrl)
    {
        return new Status(id, name, self, description, iconUrl);
    }

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
