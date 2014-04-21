package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.Map;

/**
 * Representation of a user in the JIRA REST API. Example:
 * <pre>
 * {
 *   self: http://localhost:8090/jira/rest/api/2/user?username=admin
 *   name: admin
 *   displayName: Administrator
 * }
 * </pre>
 *
 * @since v4.3
 */
public class User
{
    public String expand;
    public String self;
    public String name;
    public String displayName;
    public boolean active;
    public Map<String, String> avatarUrls;
    public String emailAddress;
    public Expando<Group> groups;
    public String timeZone;

    public User timeZone(String timeZone)
    {
        this.timeZone = timeZone;
        return this;
    }

    public User expand(String expand)
    {
        this.expand = expand;
        return this;
    }

    public User self(String self)
    {
        this.self = self;
        return this;
    }

    public User self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public User name(String name)
    {
        this.name = name;
        return this;
    }

    public User displayName(String displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public User active(boolean active)
    {
        this.active = active;
        return this;
    }

    public User avatarUrls(Map<String, String> avatarUrls)
    {
        this.avatarUrls = avatarUrls;
        return this;
    }

    public User emailAddress(String emailAddress)
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
