package com.atlassian.crowd.embedded.admin.jirajdbc;

/**
 *
 */
public class JiraJdbcDirectoryConfiguration
{
    public static final String DIRECTORY_CLASS = "com.atlassian.confluence.user.crowd.jira.JiraJdbcRemoteDirectory";
    public static final String JNDI_NAME_ATTRIBUTE_KEY = "jirajdbc.datasource.url";

    private long directoryId;
    private boolean active = true;
    private String name = "Legacy JIRA User Database";
    private String datasourceJndiName;

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(long directoryId)
    {
        this.directoryId = directoryId;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDatasourceJndiName()
    {
        return datasourceJndiName;
    }

    public void setDatasourceJndiName(String datasourceJndiName)
    {
        this.datasourceJndiName = datasourceJndiName;
    }
}
