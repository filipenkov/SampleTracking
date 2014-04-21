/**
 *
 */
package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.User;

import java.util.HashSet;
import java.util.Set;

public class MockRoleActor implements ProjectRoleActor
{
    private final String descriptor;
    private final String type;
    private final Long projectId;
    private final Long projectRoleId;
    private final String parameter;

    public MockRoleActor(String descriptor, String parameter)
    {
        this(null, null, descriptor, parameter, null);
    }

    public MockRoleActor(Long projectRoleId, Long projectId, String descriptor, String parameter, String type)
    {
        this.projectId = projectId;
        this.projectRoleId = projectRoleId;
        this.descriptor = descriptor;
        this.parameter = parameter;
        this.type = type;
    }

    public MockRoleActor(Long projectRoleId, Long projectId, String parameter, String type)
    {
        this(projectRoleId, projectId, type + ":" + parameter, parameter, type);
    }

    public String getDescriptor()
    {
        return descriptor;
    }

    public String getPrettyName()
    {
        return null;
    }

    public String getType()
    {
        return type;
    }

    public Set getUsers()
    {
        return new HashSet();
    }

    public boolean contains(com.opensymphony.user.User user)
    {
        return contains((User) user);
    }

    public boolean contains(User user)
    {
        return false;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getProjectRoleId()
    {
        return projectRoleId;
    }

    public Long getId()
    {
        return null;
    }

    public String getParameter()
    {
        return parameter;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MockRoleActor that = (MockRoleActor) o;

        if (descriptor != null ? !descriptor.equals(that.descriptor) : that.descriptor != null)
        {
            return false;
        }
        if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null)
        {
            return false;
        }
        if (projectRoleId != null ? !projectRoleId.equals(that.projectRoleId) : that.projectRoleId != null)
        {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (descriptor != null ? descriptor.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (projectRoleId != null ? projectRoleId.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }
}