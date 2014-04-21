package com.atlassian.crowd.event.directory;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.event.Event;

/**
 * This event is designed to be thrown by a {@link com.atlassian.crowd.directory.SynchronisableDirectory}
 * after it has completed synchronising its cache.
 */
public class RemoteDirectorySynchronisedEvent extends Event
{
    private final RemoteDirectory remoteDirectory;

    public RemoteDirectorySynchronisedEvent(Object source, RemoteDirectory remoteDirectory)
    {
        super(source);
        this.remoteDirectory= remoteDirectory;
    }

    public RemoteDirectory getRemoteDirectory()
    {
        return remoteDirectory;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RemoteDirectorySynchronisedEvent))
        {
            return false;
        }

        RemoteDirectorySynchronisedEvent that = (RemoteDirectorySynchronisedEvent) o;

        if (!remoteDirectory.equals(that.remoteDirectory))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return remoteDirectory.hashCode();
    }

}