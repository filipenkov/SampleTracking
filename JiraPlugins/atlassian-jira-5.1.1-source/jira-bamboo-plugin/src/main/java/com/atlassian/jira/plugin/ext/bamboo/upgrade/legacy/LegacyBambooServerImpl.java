package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for a legacy Bamboo server configured in JIRA. This should only be used by the upgrade tasks.
 */
public class LegacyBambooServerImpl implements LegacyBambooServer
{
    private int id;
    private String name;
    private String description;
    private String host;
    private String username;
    private String password;
    private Set<String> associatedProjectKeys;

    public LegacyBambooServerImpl()
    {
        id = 0;
        name = "";
        description = "";
        host = "";
        username = "";
        password = "";
        associatedProjectKeys = new HashSet();
    }
    
    public LegacyBambooServerImpl(LegacyBambooServer server)
    {
        id = server.getId();
        name = server.getName();
        description = server.getDescription();
        host = server.getHost();
        username = server.getUsername();
        password = server.getPassword();
        setAssociatedProjectKeys(server.getAssociatedProjectKeys());
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String serverName)
    {
        this.name = serverName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Set<String> getAssociatedProjectKeys()
    {
        return associatedProjectKeys;
    }

    public void setAssociatedProjectKeys(final Set<String> projectKeys)
    {
        associatedProjectKeys = new HashSet(projectKeys);
    }

    public void addAssociatedProjectKey(String projectKey)
    {
        associatedProjectKeys.add(projectKey);
    }

    public void removeAssociatedProjectKey(String projectKey)
    {
        associatedProjectKeys.remove(projectKey);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof LegacyBambooServerImpl)) return false;

        LegacyBambooServerImpl that = (LegacyBambooServerImpl) o;

        if(id != that.id) return false;
        if(associatedProjectKeys != null ? !associatedProjectKeys.equals(that.associatedProjectKeys) : that.associatedProjectKeys != null) return false;
        if(description != null ? !description.equals(that.description) : that.description != null) return false;
        if(getHost() != null ? !getHost().equals(that.getHost()) : that.getHost() != null) return false;
        if(name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (associatedProjectKeys != null ? associatedProjectKeys.hashCode() : 0);
        return result;
    }
}
