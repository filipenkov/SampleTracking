package com.atlassian.jira.project;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.NotNull;
import com.opensymphony.user.EntityNotFoundException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Represents an immutable Project domain object for JIRA.
 */
public class ProjectImpl implements Project
{
    private final GenericValue projectGV;
    private final String key;
    private com.opensymphony.user.User lead;

    public ProjectImpl(GenericValue projectGv)
    {
        this.projectGV = projectGv;
        key = getStringFromGV("key");
    }

    public Long getId()
    {
        return getLongFromGV("id");
    }

    public String getName()
    {
        return getStringFromGV("name");
    }

    public String getKey()
    {
        return key;
    }

    public String getUrl()
    {
        return getStringFromGV("url");
    }

    public com.opensymphony.user.User getLead()
    {
        if (getLeadUserName() != null && lead == null)
        {
            lead = getUser(getLeadUserName());
        }
        return lead;
    }

    @Override
    public User getLeadUser()
    {
        return getLead();
    }

    public String getLeadUserName()
    {
        return getStringFromGV("lead");
    }

    public String getDescription()
    {
        return getStringFromGV("description");
    }

    public Long getAssigneeType()
    {
        return getLongFromGV("assigneetype");
    }

    public Long getCounter()
    {
        return getLongFromGV("counter");
    }

    public Collection<GenericValue> getComponents()
    {
        //noinspection deprecation
        return ComponentAccessor.getProjectManager().getComponents(projectGV);
    }

    public Collection<ProjectComponent> getProjectComponents()
    {
        return ComponentAccessor.getProjectComponentManager().findAllForProject(getId());
    }

    public Collection<Version> getVersions()
    {
        return ComponentAccessor.getVersionManager().getVersions(projectGV.getLong("id"));
    }

    @Override
    public Collection<IssueType> getIssueTypes()
    {
        return ComponentManager.getComponent(IssueTypeSchemeManager.class).getIssueTypesForProject(this);
    }

    // This should really return a projectCategory object, but alas it does not exist :'(
    public GenericValue getProjectCategory()
    {
        return ComponentAccessor.getProjectManager().getProjectCategoryFromProject(projectGV);
    }

    @NotNull
    public Avatar getAvatar()
    {
        return ComponentAccessor.getAvatarManager().getById(getLongFromGV("avatar"));
    }

    public GenericValue getGenericValue()
    {
        return projectGV;
    }

    private String getStringFromGV(String key)
    {
        if (projectGV != null)
        {
            return projectGV.getString(key);
        }
        return null;
    }

    private Long getLongFromGV(String key)
    {
        if (projectGV != null)
        {
            return projectGV.getLong(key);
        }
        return null;
    }

    private com.opensymphony.user.User getUser(String username)
    {
        try
        {
            return UserUtils.getUser(username);
        }
        catch (EntityNotFoundException e)
        {
            throw new DataAccessException("Error occurred while retrieving user with id '" + username + "'.", e);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Project))
        {
            return false;
        }

        final Project other = (Project) o;
        // JRA-20184: All the Project properties can change except KEY (and ID).

        if (getKey() == null)
        {
            return other.getKey() == null;
        }
        else
        {
            return getKey().equals(other.getKey());
        }
    }

    @Override
    public int hashCode()
    {
        return key != null ? key.hashCode() : 0;
    }

    public String toString()
    {
        return "Project: " + getKey();
    }
}
