package com.atlassian.jira.issue.index;

/**
* Raised when "reindex all" has completed.
* @since v5.0
*/
public class ReindexAllCompletedEvent
{
    /**
     * How long it took for the reindex to occur.
     */
    final public long time;

    public ReindexAllCompletedEvent(long time)
    {
        this.time = time;
    }
}
