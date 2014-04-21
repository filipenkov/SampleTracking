package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been merged into another
 *
 * @since v4.4
 */
public class VersionMergeEvent extends AbstractVersionEvent
{
    private final long mergedVersionId;

    public VersionMergeEvent(long versionId, long mergedVersionId)
    {
        super(versionId);
        this.mergedVersionId = mergedVersionId;
    }

    /**
     * Get the ID of the version that was merged into this version. Note that the version for this ID will no longer
     * exist when this event is published.
     *
     * @return The ID of the version that was merged into this version
     */
    public long getMergedVersionId()
    {
        return mergedVersionId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        VersionMergeEvent that = (VersionMergeEvent) o;

        if (mergedVersionId != that.mergedVersionId) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (mergedVersionId ^ (mergedVersionId >>> 32));
        return result;
    }
}
