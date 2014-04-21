package com.atlassian.jira.event.scheme;

import com.atlassian.jira.scheme.Scheme;

/**
 * Abstract event that captures the data relevant to a scheme being copied, e.g. permission schemes, notification schemes etc.
 *
 * @since v5.0
 */
public class AbstractSchemeCopiedEvent
{
    private Long copiedFromId;
    private Long copiedToId;
    
    public AbstractSchemeCopiedEvent(Scheme fromScheme, Scheme toScheme)
    {
        if (fromScheme != null)
        {
            this.copiedFromId = fromScheme.getId();
        }
        if (toScheme != null)
        {
            this.copiedToId = toScheme.getId();
        }
    }

    public Long getCopiedFromId()
    {
        return copiedFromId;
    }

    public Long getCopiedToId()
    {
        return copiedToId;
    }
}
