package com.atlassian.crowd.event.remote;

import com.atlassian.crowd.event.Event;

/**
 * An event that models an event occuring on a Remote Directory.
 *
 * This events generally represent mutations at the physical
 * directory level.
 *
 * All classes in this package are subclasses of RemoteDirectoryEvent.
 */
public abstract class RemoteDirectoryEvent extends Event
{
    private final long directoryId;

    protected RemoteDirectoryEvent(Object source, long directoryId)
    {
        super(source);
        this.directoryId = directoryId;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }
}
