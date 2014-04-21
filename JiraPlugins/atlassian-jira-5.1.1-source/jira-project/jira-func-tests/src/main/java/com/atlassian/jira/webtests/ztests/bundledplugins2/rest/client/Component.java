package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.net.URI;
import java.util.List;

/**
 * Representation of a component in the JIRA REST API.
 *
 * @since v4.3
 */
public class Component
{
    public static final GenericType<List<Component>> COMPONENTS_TYPE = new GenericType<List<Component>>(){};

    public String self;
    public Long id;
    public String project;
    public String name;
    public String description;
    public User lead;
    public String leadUserName;
    public AssigneeType assigneeType;
    public AssigneeType realAssigneeType;
    public User assignee;
    public User realAssignee;
    public boolean isAssigneeTypeValid;

    public Component self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public Component id(Long id)
    {
        this.id = id;
        return this;
    }

    public Component self(String self)
    {
        this.self = self;
        return this;
    }

    public Component project(String project)
    {
        this.project = project;
        return this;
    }

    public Component name(String name)
    {
        this.name = name;
        return this;
    }

    public Component description(String description)
    {
        this.description = description;
        return this;
    }

    public Component lead(User lead)
    {
        this.lead = lead;
        return this;
    }

    public Component leadUserName(String leadUserName)
    {
        this.leadUserName = leadUserName;
        return this;
    }

    public Component assigneeType(AssigneeType assigneeType)
    {
        this.assigneeType = assigneeType;
        return this;
    }

    public Component assignee(User assignee)
    {
        this.assignee = assignee;
        return this;
    }

    public Component realAssigneeType(AssigneeType realAssigneeType)
    {
        this.realAssigneeType = realAssigneeType;
        return this;
    }

    public Component realAssignee(User realAssignee)
    {
        this.realAssignee = realAssignee;
        return this;
    }

    public Component isAssigneeTypeValid(boolean isAssigneeTypeValid)
    {
        this.isAssigneeTypeValid = isAssigneeTypeValid;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    public enum AssigneeType
    {
        PROJECT_DEFAULT,
        COMPONENT_LEAD,
        PROJECT_LEAD,
        UNASSIGNED;
    }

}
