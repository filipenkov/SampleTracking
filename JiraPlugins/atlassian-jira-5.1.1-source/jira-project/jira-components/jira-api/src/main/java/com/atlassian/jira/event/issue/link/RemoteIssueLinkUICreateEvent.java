package com.atlassian.jira.event.issue.link;

/**
 * Fired when remote issue link has been created through the UI.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUICreateEvent extends AbstractRemoteIssueLinkEvent
{
    private final String applicationType;

    public RemoteIssueLinkUICreateEvent(Long remoteLinkId, String applicationType)
    {
        super(remoteLinkId);
        this.applicationType = applicationType;
    }
}
