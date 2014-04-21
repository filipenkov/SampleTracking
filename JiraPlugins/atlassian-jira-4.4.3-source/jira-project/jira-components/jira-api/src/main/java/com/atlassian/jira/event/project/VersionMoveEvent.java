package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been moved
 *
 * @since v4.4
 */
public class VersionMoveEvent extends AbstractVersionEvent
{
    public VersionMoveEvent(long versionId)
    {
        super(versionId);
    }
}
