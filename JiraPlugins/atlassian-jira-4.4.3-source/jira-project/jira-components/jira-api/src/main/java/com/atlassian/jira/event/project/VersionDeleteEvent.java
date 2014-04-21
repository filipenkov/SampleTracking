package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been deleted
 *
 * @since v4.4
 */
public class VersionDeleteEvent extends AbstractVersionEvent
{
    public VersionDeleteEvent(long versionId)
    {
        super(versionId);
    }
}
