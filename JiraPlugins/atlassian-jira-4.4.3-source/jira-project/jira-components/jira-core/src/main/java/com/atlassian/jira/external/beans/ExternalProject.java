package com.atlassian.jira.external.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class ExternalProject implements NamedExternalObject
{
    // @TODO refactor Remote RPC objects to use this

    private GenericValue projectGV;
    String id;
    String name;
    String key;
    String url;
    String projectUrl;
    String lead;
    String description;
    String projectCategoryName;
    String assigneeType;
    private String emailSender;
    private String counter;

    public ExternalProject()
    {
    }

    public Map toFieldsMap()
    {
        Map fields = new HashMap();

        // Don't put the id in
//        if (StringUtils.isNotEmpty(getId())) fields.put("id", getId());
        if (StringUtils.isNotEmpty(getName()))
        {
            fields.put("name", getName());
        }
        if (StringUtils.isNotEmpty(getUrl()))
        {
            fields.put("url", getUrl());
        }
        if (StringUtils.isNotEmpty(getLead()))
        {
            fields.put("lead", getLead());
        }
        if (StringUtils.isNotEmpty(getDescription()))
        {
            fields.put("description", getDescription());
        }
        if (StringUtils.isNotEmpty(getKey()))
        {
            fields.put("key", getKey());
        }
        if (StringUtils.isNotEmpty(getAssigneeType()))
        {
            fields.put("assigneetype", new Long(getAssigneeType()));
        }

        return fields;
    }



    public GenericValue getProjectGV()
    {
        return projectGV;
    }

    public void setProjectGV(GenericValue projectGV)
    {
        this.projectGV = projectGV;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    // TODO: this seems like dead code, url handles this, perhaps it should be removed one day
    public String getProjectUrl()
    {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl)
    {
        this.projectUrl = projectUrl;
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getProjectCategoryName()
    {
        return projectCategoryName;
    }

    public void setProjectCategoryName(String projectCategoryName)
    {
        this.projectCategoryName = projectCategoryName;
    }

    public String getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(final String assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public String getEmailSender()
    {
        return emailSender;
    }

    public void setEmailSender(final String emailSender)
    {
        this.emailSender = emailSender;
    }

    public String getCounter()
    {
        return counter;
    }

    public void setCounter(final String pcounter)
    {
        this.counter = pcounter;
    }

    public boolean equals(Object o)
    {
        if ( !(o instanceof ExternalProject) )
        {
            return false;
        }

        ExternalProject rhs = (ExternalProject) o;
        return new EqualsBuilder()
                .append(getId(), rhs.getId())
                .append(getKey(), rhs.getKey())
                .append(getName(), rhs.getName())
                .append(getDescription(), rhs.getDescription())
                .append(getEmailSender(), rhs.getEmailSender())
                .append(getCounter(), rhs.getCounter())
                .isEquals();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).
                append(getId()).
                append(getKey()).
                append(getName()).
                append(getDescription()).
                append(getEmailSender()).
                append(getCounter()).
                toHashCode();
    }

    public String toString()
    {
        return getName() == null ? getKey() : getName();
    }

    public boolean isValid()
    {
        return StringUtils.isNotEmpty(getKey());
    }
}
