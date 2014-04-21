package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.Map;

/**
 * Mimicks UserJsonBean
 *
 * @since v5.0
 */
public class UserJson
{
    public String self;
    public String name;
    public String displayName;
    public boolean active;
    public Map<String, String> avatarUrls;
    public String emailAddress;

    public UserJson self(String self)
    {
        this.self = self;
        return this;
    }

    public UserJson self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public UserJson name(String name)
    {
        this.name = name;
        return this;
    }

    public UserJson displayName(String displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public UserJson active(boolean active)
    {
        this.active = active;
        return this;
    }

    public UserJson avatarUrls(Map<String, String> avatarUrls)
    {
        this.avatarUrls = avatarUrls;
        return this;
    }

    public UserJson emailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
        return this;
    }

    public enum Expand
    {
        groups
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
