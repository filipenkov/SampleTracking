package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Re-indexing tasks return this as their result.  Its either a collection of errors or an index time.
 *
 * @since 3.13
 */
public class IndexCommandResult
{
    private final ErrorCollection errorCollection;
    private final long reindexTime;

    public IndexCommandResult(final ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);
        this.errorCollection = errorCollection;
        reindexTime = 0;
    }

    public IndexCommandResult(final long reindexTime)
    {
        this.reindexTime = reindexTime;
        errorCollection = new SimpleErrorCollection();
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public long getReindexTime()
    {
        return reindexTime;
    }

    public boolean isSuccessful()
    {
        return !errorCollection.hasAnyErrors();
    }
}
