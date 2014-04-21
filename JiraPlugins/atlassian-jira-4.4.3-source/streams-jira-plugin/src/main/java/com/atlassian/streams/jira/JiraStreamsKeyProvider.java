package com.atlassian.streams.jira;

import com.atlassian.streams.spi.StreamsKeyProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Returns a set of project keys from JIRA that the user has permission to see.
 *
 * @since v3.0
 */
public class JiraStreamsKeyProvider implements StreamsKeyProvider
{
    private final ProjectKeys projectKeys;

    public JiraStreamsKeyProvider(ProjectKeys projectKeys)
    {
        this.projectKeys = checkNotNull(projectKeys, "projectKeys");
    }

    public Iterable<StreamsKey> getKeys()
    {
        return projectKeys.get();
    }
}
