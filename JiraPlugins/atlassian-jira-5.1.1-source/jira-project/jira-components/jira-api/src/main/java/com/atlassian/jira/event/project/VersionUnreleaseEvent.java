package com.atlassian.jira.event.project;

/**
 * Event indicating a version has been unreleased
 *
 * @since v4.4
 */
public class VersionUnreleaseEvent extends AbstractVersionEvent
{
    public VersionUnreleaseEvent(long versionId)
    {
        super(versionId);
    }
}
