package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been unarchived
 *
 * @since v4.4
 */
public class VersionUnarchiveEvent extends AbstractVersionEvent
{
    public VersionUnarchiveEvent(long versionId)
    {
        super(versionId);
    }
}
