package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.annotate.JsonProperty;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Representation of a priority in the JIRA REST API.
 *
 * @since v4.3
 */
public class Priority
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String statusColor;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    public Priority()
    {
    }

    public Priority(String self, String id, String name, String statusColor, String description, String iconUrl)
    {
        this.self = self;
        this.id = id;
        this.name = name;
        this.statusColor = statusColor;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public String self()
    {
        return this.self;
    }

    public Priority self(String self)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public String id()
    {
        return this.id;
    }

    public Priority id(String id)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public String name()
    {
        return this.name;
    }

    public Priority name(String name)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public String statusColor()
    {
        return this.statusColor;
    }

    public Priority statusColor(String statusColor)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public String description()
    {
        return this.description;
    }

    public Priority description(String description)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public String iconUrl()
    {
        return this.iconUrl;
    }

    public Priority iconUrl(String iconUrl)
    {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    @Override
    public boolean equals(Object o) { return reflectionEquals(this, o); }

    @Override
    public int hashCode() { return reflectionHashCode(this); }
}
