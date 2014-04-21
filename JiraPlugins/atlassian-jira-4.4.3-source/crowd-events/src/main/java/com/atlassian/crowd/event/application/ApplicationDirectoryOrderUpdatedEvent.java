package com.atlassian.crowd.event.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.application.Application;

/**
 * Event which occurs when a directory is moved to another position in
 * Application's list of included directories.
 */
public class ApplicationDirectoryOrderUpdatedEvent
{
    private final Application application;
    private final Directory directory;

    public ApplicationDirectoryOrderUpdatedEvent(final Application application, final Directory directory)
    {
        this.application = application;
        this.directory = directory;
    }

    public Application getApplication()
    {
        return application;
    }

    public Directory getDirectory()
    {
        return directory;
    }
}
