package com.atlassian.jira.event.scheme;

import com.atlassian.jira.scheme.Scheme;

/**
 * Abstract event that captures the data relevant to scheme events, e.g. permission schemes, notification schemes etc.
 *
 * @since v5.0
 */
public class AbstractSchemeEvent
{
    private Long id;

    public AbstractSchemeEvent(Scheme scheme)
    {
        if (scheme != null)
        {
            this.id = scheme.getId();
        }
    }

    public Long getId()
    {
        return id;
    }
}
