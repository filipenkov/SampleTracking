package com.atlassian.crowd.event;

/**
 * This event will be used if a Crowd instance is nearing its resource limit.
 */
public class LicenseResourceLimitEvent extends Event
{
    private final Integer currentUserCount;

    public LicenseResourceLimitEvent(Object source, Integer currentUserCount)
    {
        super(source);
        this.currentUserCount = currentUserCount;
    }

    public Integer getCurrentUserCount()
    {
        return currentUserCount;
    }
}
