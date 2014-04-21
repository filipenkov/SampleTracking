package com.atlassian.jira.event.issue.link;

/**
 * Abstract base class for remote issue linking related events.
 *
 * @since v5.0
 */
public class AbstractRemoteIssueLinkEvent
{
    private final Long remoteIssueLinkId;

    public AbstractRemoteIssueLinkEvent(Long remoteIssueLinkId) {
        this.remoteIssueLinkId = remoteIssueLinkId;
    }

    public Long getRemoteIssueLinkId()
    {
        return remoteIssueLinkId;
    }
}
