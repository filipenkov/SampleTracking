package com.atlassian.jira.event.issue.link;

/**
 * Fired when remote issue link has been created.
 *
 * @since v5.0
 */
public class RemoteIssueLinkCreateEvent extends AbstractRemoteIssueLinkEvent
{
    private final String applicationType;

    public RemoteIssueLinkCreateEvent(Long remoteLinkId, String applicationType)
    {
        super(remoteLinkId);
        this.applicationType = applicationType;
    }

    public String getApplicationType()
    {
        return applicationType;
    }
}
