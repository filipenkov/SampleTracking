package com.atlassian.jira.event.project;

/**
 * Abstract event for versions
 *
 * @since v4.4
 */
public class AbstractVersionEvent
{
    private final long versionId;

    public AbstractVersionEvent(long versionId)
    {
        this.versionId = versionId;
    }

    /**
     * Get the ID of the version this event occured on
     *
     * @return The ID of the version
     */
    public long getVersionId()
    {
        return versionId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AbstractVersionEvent that = (AbstractVersionEvent) o;

        if (versionId != that.versionId) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (versionId ^ (versionId >>> 32));
    }
}
