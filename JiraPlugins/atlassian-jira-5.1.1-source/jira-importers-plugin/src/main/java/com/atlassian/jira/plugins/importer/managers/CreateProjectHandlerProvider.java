package com.atlassian.jira.plugins.importer.managers;

import javax.annotation.Nullable;

public interface CreateProjectHandlerProvider {
    @Nullable
    CreateProjectHandler getHandler();

    void setHandler(CreateProjectHandler handler);
}
