package com.atlassian.crowd.event.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.application.Application;

/**
 * Event which occurs when a directory is added to an Application's list of
 * included directories.
 */
public class ApplicationDirectoryAddedEvent
{
    private final Application application;
    private final Directory directory;

    public ApplicationDirectoryAddedEvent(final Application application, final Directory directory)
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
