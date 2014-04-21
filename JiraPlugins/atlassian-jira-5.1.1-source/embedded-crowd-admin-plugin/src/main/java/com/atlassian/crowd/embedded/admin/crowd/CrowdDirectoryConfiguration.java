package com.atlassian.crowd.embedded.admin.crowd;


public final class CrowdDirectoryConfiguration
{
    private long directoryId;
    private boolean active = true;
    private String name;
    private String crowdServerUrl;
    private String applicationName;
    private String applicationPassword;
    private CrowdPermissionOption crowdPermissionOption = CrowdPermissionOption.READ_ONLY;
    private boolean nestedGroupsEnabled;
    private boolean incrementalSyncEnabled = true;
    private long crowdServerSynchroniseIntervalInMin = 60; // in minutes

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

    public String getCrowdServerUrl()
    {
        return crowdServerUrl;
    }

    public void setCrowdServerUrl(String crowdServerUrl)
    {
        this.crowdServerUrl = crowdServerUrl;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getApplicationPassword()
    {
        return applicationPassword;
    }

    public void setApplicationPassword(String applicationPassword)
    {
        this.applicationPassword = applicationPassword;
    }

    public CrowdPermissionOption getCrowdPermissionOption()
    {
        return crowdPermissionOption;
    }

    public void setCrowdPermissionOption(final CrowdPermissionOption crowdPermissionOption)
    {
        this.crowdPermissionOption = crowdPermissionOption;
    }

    public boolean isNestedGroupsEnabled()
    {
        return nestedGroupsEnabled;
    }

    public void setNestedGroupsEnabled(final boolean nestedGroupsEnabled)
    {
        this.nestedGroupsEnabled = nestedGroupsEnabled;
    }

    public boolean isIncrementalSyncEnabled()
    {
        return incrementalSyncEnabled;
    }

    public void setIncrementalSyncEnabled(boolean incrementalSyncEnabled)
    {
        this.incrementalSyncEnabled = incrementalSyncEnabled;
    }

    public long getCrowdServerSynchroniseIntervalInMin()
    {
        return crowdServerSynchroniseIntervalInMin;
    }

    public void setCrowdServerSynchroniseIntervalInMin(long crowdServerSynchroniseIntervalInMin)
    {
        this.crowdServerSynchroniseIntervalInMin = crowdServerSynchroniseIntervalInMin;
    }

}
