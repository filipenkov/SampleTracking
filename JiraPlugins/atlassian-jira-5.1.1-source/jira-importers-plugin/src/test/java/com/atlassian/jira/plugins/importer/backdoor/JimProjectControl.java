package com.atlassian.jira.plugins.importer.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;

import javax.annotation.Nonnull;
import java.util.List;

public class JimProjectControl extends JimBackdoorControl<JimProjectControl> {
    public JimProjectControl(JIRAEnvironmentData environmentData) {
        super(environmentData);
    }

    public Iterable<String> getUserRoles(@Nonnull String projectKey, @Nonnull String username) {
        return get(createResource().path("project").path(projectKey).path("roles").path(username), new GenericType<List<String>>() {});
    }
}
