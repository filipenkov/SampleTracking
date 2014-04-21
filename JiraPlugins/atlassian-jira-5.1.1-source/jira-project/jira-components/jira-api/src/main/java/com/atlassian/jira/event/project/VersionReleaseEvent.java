package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been released
 *
 * @since v4.4
 */
public class VersionReleaseEvent extends AbstractVersionEvent
{
    public VersionReleaseEvent(long versionId)
    {
        super(versionId);
    }
}
