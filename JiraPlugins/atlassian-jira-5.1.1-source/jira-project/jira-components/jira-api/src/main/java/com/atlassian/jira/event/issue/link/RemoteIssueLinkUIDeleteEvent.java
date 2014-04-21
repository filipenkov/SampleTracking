package com.atlassian.jira.event.issue.link;

/**
 * Fired when remote issue link has been deleted through the UI.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUIDeleteEvent extends AbstractRemoteIssueLinkEvent
{
    public RemoteIssueLinkUIDeleteEvent(Long remoteLinkId)
    {
        super(remoteLinkId);
    }
}
