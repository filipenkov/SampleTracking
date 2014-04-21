package com.atlassian.jira.event.issue.link;

/**
 * Fired when remote issue link has been deleted.
 *
 * @since v5.0
 */
public class RemoteIssueLinkDeleteEvent extends AbstractRemoteIssueLinkEvent
{
    public RemoteIssueLinkDeleteEvent(Long remoteLinkId)
    {
        super(remoteLinkId);
    }
}
