package com.atlassian.crowd.embedded.admin.internal;

public final class InternalDirectoryConfiguration
{
    private long directoryId;
    private boolean active = true;
    private String name = "Internal Directory";
    private boolean nestedGroupsEnabled = false;

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

    public boolean isNestedGroupsEnabled()
    {
        return nestedGroupsEnabled;
    }

    public void setNestedGroupsEnabled(final boolean nestedGroupsEnabled)
    {
        this.nestedGroupsEnabled = nestedGroupsEnabled;
    }
}
