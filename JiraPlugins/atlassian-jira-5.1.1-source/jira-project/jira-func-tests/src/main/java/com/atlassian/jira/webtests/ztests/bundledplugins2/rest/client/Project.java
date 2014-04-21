package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Representation of a Project in the JIRA REST API.
 *
 * @since v4.3
 */
public class Project
{
    public static final GenericType<List<Project>> PROJECTS_TYPE = new GenericType<List<Project>>(){};

    public String self;
    public String id;
    public String key;
    public String description;
    public User lead;
    public String name;
    public List<Component> components;
    public List<Version> versions;
    public List<IssueType> issueTypes;
    public String url;
    public AssigneeType assigneeType;
    public Map<String, String> roles;
    public Map<String, String> avatarUrls;

    public Project url(String url)
    {
        this.url = url;
        return this;
    }

    public Project id(String id)
    {
        this.id = id;
        return this;
    }

    public Project self(String self)
    {
        this.self = self;
        return this;
    }

    public Project self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public Project key(String key)
    {
        this.key = key;
        return this;
    }

    public Project description(String description)
    {
        this.description = description;
        return this;
    }

    public Project lead(User lead)
    {
        this.lead = lead;
        return this;
    }

    public Project assigneeType(AssigneeType assigneeType)
    {
        this.assigneeType = assigneeType;
        return this;
    }

    public Project name(String name)
    {
        this.name = name;
        return this;
    }

    public Project components(List<Component> components)
    {
        this.components = components;
        return this;
    }

    public Project versions(List<Version> versions)
    {
        this.versions = versions;
        return this;
    }

    public Project issueTypes(List<IssueType> types)
    {
        this.issueTypes = types;
        return this;
    }

    public Project roles(Map<String, String> roles)
    {
        this.roles = roles;
        return this;
    }

    public Project avatarUrls(Map<String, String> avatarUrls)
    {
        this.avatarUrls = avatarUrls;
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

    public enum AssigneeType
    {
        PROJECT_LEAD,
        UNASSIGNED
    }
}
