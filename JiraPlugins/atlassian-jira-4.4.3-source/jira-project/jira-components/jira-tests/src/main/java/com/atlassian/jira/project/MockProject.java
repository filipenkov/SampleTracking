package com.atlassian.jira.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.NotNull;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Bean implementation of Project interface but doesn't believe in GenericValues. Equals and hashCode are based on
 * id alone.
 */
public class MockProject implements Project
{

    private Long id;
    private String name;
    private String key;
    private String url;
    private com.opensymphony.user.User lead;
    private String description;
    private Long assigneeType;
    private Long counter;
    private Collection<GenericValue> components;
    private Collection<Version> versions;
    private GenericValue projectGV;
    private GenericValue projectCategoryGV;
    private Avatar avatar;
    private Collection<IssueType> types;

    public MockProject(GenericValue gv)
    {
        this(gv.getLong("id"), gv.getString("key"), gv.getString("name"), gv);
    }

    public MockProject()
    {
    }

    public MockProject(long id)
    {
        this(id, null, null, null);
    }

    public MockProject(Long id)
    {
        this(id, null, null, null);
    }

    public MockProject(long id, String key)
    {
        this(id, key, key, null);
    }

    public MockProject(long id, String key, String name)
    {
        this(id, key, name, null);
    }

    public MockProject(Long id, String key, String name)
    {
        this(id, key, name, null);
    }

    public MockProject(long id, String key, String name, GenericValue projectGV)
    {
        this(new Long(id), key, name, projectGV);
    }

    public MockProject(Long id, String key, String name, GenericValue projectGV)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.projectGV = projectGV;
    }

    public Long getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(Long assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public Collection<GenericValue> getComponents()
    {
        return components;
    }

    public Collection<ProjectComponent> getProjectComponents()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void setComponents(Collection<GenericValue> components)
    {
        this.components = components;
    }

    public Long getCounter()
    {
        return counter;
    }

    public void setCounter(Long counter)
    {
        this.counter = counter;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Collection<Version> getVersions()
    {
        return versions;
    }

    @Override
    public Collection<IssueType> getIssueTypes()
    {
        return types;
    }

    public MockProject setIssueTypes(IssueType...types)
    {
        return setIssueTypes(Arrays.asList(types));
    }

    public MockProject setIssueTypes(Collection<IssueType> types)
    {
        this.types = types;
        return this;
    }

    public MockProject setIssueTypes(String...types)
    {
        Collection<IssueType> output = new ArrayList<IssueType>(types.length);
        for (String type : types)
        {
            output.add(new MockIssueType(type, type));
        }
        return setIssueTypes(output);
    }

    public GenericValue getProjectCategory()
    {
        return projectCategoryGV;
    }

    public void setProjectCategoryGV(GenericValue projectCategoryGV)
    {
        this.projectCategoryGV = projectCategoryGV;
    }

    public void setVersions(Collection<Version> versions)
    {
        this.versions = versions;
    }

    public GenericValue getGenericValue()
    {
        if (projectGV != null)
            return projectGV;
        // Create one on the fly...
        // TODO: Add other fields.
        MockGenericValue gv = new MockGenericValue("Project");
        gv.set("id", getId());
        gv.set("name", getName());
        gv.set("key", getKey());
        gv.set("description", getDescription());
        return gv;
    }

    public com.opensymphony.user.User getLead()
    {
        return lead;
    }

    @Override
    public User getLeadUser()
    {
        return lead;
    }

    public void setLead(com.opensymphony.user.User lead)
    {
        this.lead = lead;
    }

    public String getLeadUserName()
    {
        return lead.getName();
    }

    @NotNull
    public Avatar getAvatar()
    {
        return avatar;
    }

    public void setAvatar(Avatar avatar)
    {
        this.avatar = avatar;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockProject that = (MockProject) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }

    public String toString()
    {
        return "Project: " + getName() + '(' + getId() + ')';
    }
}
