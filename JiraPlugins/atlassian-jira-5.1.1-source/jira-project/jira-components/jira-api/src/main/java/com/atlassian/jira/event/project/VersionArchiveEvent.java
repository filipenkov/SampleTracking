package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been archived
 *
 * @since v4.4
 */
public class VersionArchiveEvent extends AbstractVersionEvent
{
    public VersionArchiveEvent(long versionId)
    {
        super(versionId);
    }
}
