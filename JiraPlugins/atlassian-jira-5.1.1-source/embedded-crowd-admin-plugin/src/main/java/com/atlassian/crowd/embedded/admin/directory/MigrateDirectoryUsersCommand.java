package com.atlassian.crowd.embedded.admin.directory;

public final class MigrateDirectoryUsersCommand
{
    private long fromDirectoryId;
    private long toDirectoryId;
    private long totalCount;
    private long migratedCount;
    private boolean testSuccessful;

    public long getFromDirectoryId()
    {
        return fromDirectoryId;
    }

    public void setFromDirectoryId(final long fromDirectoryId)
    {
        this.fromDirectoryId = fromDirectoryId;
    }

    public long getToDirectoryId()
    {
        return toDirectoryId;
    }

    public void setToDirectoryId(final long toDirectoryId)
    {
        this.toDirectoryId = toDirectoryId;
    }

    public long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(final long totalCount)
    {
        this.totalCount = totalCount;
    }

    public long getMigratedCount()
    {
        return migratedCount;
    }

    public void setMigratedCount(final long migratedCount)
    {
        this.migratedCount = migratedCount;
    }

    public boolean isTestSuccessful()
    {
        return testSuccessful;
    }

    public void setTestSuccessful(final boolean testSuccessful)
    {
        this.testSuccessful = testSuccessful;
    }
}
