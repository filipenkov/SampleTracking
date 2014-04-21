package com.atlassian.streams.jira;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.spi.StreamsKeyProvider.StreamsKey;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.transform;

public class ProjectKeys
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    public ProjectKeys(final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
    }

    public Iterable<StreamsKey> get()
    {
        return ImmutableList.<StreamsKey>builder().addAll(projectKeys()).build();
    }
    
    private Iterable<StreamsKey> projectKeys()
    {
        return transform(permissionManager.getProjectObjects(Permissions.BROWSE, authenticationContext.getLoggedInUser()), ToStreamsKey.INSTANCE);
    }
    
    enum ToStreamsKey implements Function<Project, StreamsKey>
    {
        INSTANCE;

        public StreamsKey apply(Project p)
        {
            return new StreamsKey(p.getKey(), p.getName());
        }
    }

}
