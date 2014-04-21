package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been created
 *
 * @since v4.4
 */
public class VersionCreateEvent extends AbstractVersionEvent
{
    public VersionCreateEvent(long versionId)
    {
        super(versionId);
    }
}
