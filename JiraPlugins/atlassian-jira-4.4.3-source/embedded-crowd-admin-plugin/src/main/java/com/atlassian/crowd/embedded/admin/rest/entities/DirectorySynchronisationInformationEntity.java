package com.atlassian.crowd.embedded.admin.rest.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name="synchronisation")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectorySynchronisationInformationEntity
{
    @XmlElement(name="lastStartTime")
    private String lastSyncStartTime;
    @XmlElement(name="lastDuration")
    private long lastSyncDurationInSeconds;
    @XmlElement(name="currentStartTime")
    private long currentSyncStartTime;
    @XmlElement(name="currentDuration")
    private long currentDurationInSeconds;
    @XmlElement(name="syncStatus")
    private String syncStatus;

    public long getCurrentSyncStartTime()
    {
        return currentSyncStartTime;
    }

    public long getLastSyncDurationInSeconds()
    {
        return lastSyncDurationInSeconds;
    }

    public String getLastSyncStartTime()
    {
        return lastSyncStartTime;
    }

    public void setCurrentDurationInSeconds(long currentDurationInSeconds)
    {
        this.currentDurationInSeconds = currentDurationInSeconds;
    }

    public void setLastSyncStartTime(String lastSyncStartTime)
    {
        this.lastSyncStartTime = lastSyncStartTime;
    }

    public void setLastSyncDurationInSeconds(long lastSyncDurationInSeconds)
    {
        this.lastSyncDurationInSeconds = lastSyncDurationInSeconds;
    }

    public void setCurrentSyncStartTime(long currentSyncStartTime)
    {
        this.currentSyncStartTime = currentSyncStartTime;
    }

    public long getCurrentDurationInSeconds()
    {
        return currentDurationInSeconds;
    }

    public String getSyncStatus()
    {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus)
    {
        this.syncStatus = syncStatus;
    }
}
