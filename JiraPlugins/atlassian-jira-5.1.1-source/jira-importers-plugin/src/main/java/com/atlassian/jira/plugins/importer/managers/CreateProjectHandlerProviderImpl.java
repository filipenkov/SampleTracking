package com.atlassian.jira.plugins.importer.managers;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class CreateProjectHandlerProviderImpl implements CreateProjectHandlerProvider {
    
    private List<CreateProjectHandler> createProjectHandlers;

    public CreateProjectHandlerProviderImpl(List<CreateProjectHandler> createProjectHandlers) {
        this.createProjectHandlers = createProjectHandlers;
    }

    @Override
    public CreateProjectHandler getHandler() {
        final Iterator<CreateProjectHandler> iterator = createProjectHandlers.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    public void setHandler(CreateProjectHandler handler) {
        this.createProjectHandlers = Lists.newArrayList(handler);
    }
}
