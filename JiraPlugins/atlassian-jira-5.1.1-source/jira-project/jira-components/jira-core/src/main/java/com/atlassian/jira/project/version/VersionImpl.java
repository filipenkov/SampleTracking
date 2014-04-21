/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;

public class VersionImpl extends AbstractOfBizValueWrapper implements Version, Comparable
{
    private final ProjectManager projectManager;

    public VersionImpl(ProjectManager projectManager, GenericValue genericValue)
    {
        super(genericValue);
        this.projectManager = projectManager;
    }

    public GenericValue getProject()
    {
        return projectManager.getProject(genericValue.getLong("project"));
    }

    public Project getProjectObject()
    {
        return projectManager.getProjectObj(genericValue.getLong("project"));
    }

    public Long getId()
    {
        return genericValue.getLong("id");
    }

    public String getName()
    {
        return genericValue.getString("name");
    }

    public void setName(String name)
    {
        genericValue.setString("name", name);
    }

    public String getDescription()
    {
        return genericValue.getString("description");
    }

    public void setDescription(String description)
    {
        genericValue.setString("description", description);
    }

    public Long getSequence()
    {
        return genericValue.getLong("sequence");
    }

    public void setSequence(Long sequence)
    {
        genericValue.set("sequence", sequence);
    }

    public boolean isArchived()
    {
        return "true".equals(genericValue.getString("archived"));
    }

    public void setArchived(boolean archived)
    {
        genericValue.set("archived", archived ? "true" : null);
    }

    public boolean isReleased()
    {
        return "true".equals(genericValue.getString("released"));
    }

    public void setReleased(boolean released)
    {
        genericValue.set("released", released ? "true" : null);
    }

    public Date getReleaseDate()
    {
        return genericValue.getTimestamp("releasedate");
    }

    public void setReleaseDate(Date releasedate)
    {
        if (releasedate == null)
        {
            genericValue.set("releasedate", null);
        }
        else
        {
            genericValue.set("releasedate", new Timestamp(releasedate.getTime()));
        }
    }

    public int compareTo(Object o)
    {
        return getGenericValue().compareTo(((VersionImpl) o).getGenericValue());
    }

    public String toString()
    {
        return getName();
    }
}
