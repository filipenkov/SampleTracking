package com.atlassian.crowd.event.remote;

import com.atlassian.crowd.model.Tombstone;

/**
 * Represents an "entity deleted" event from Active Directory.
 *
 * As it is not possible to recover the name of the a deleted entity,
 * the GUID is returned (along with the usnChanged).
 */
public abstract class ActiveDirectoryEntityDeletedEvent extends RemoteDirectoryEvent
{
    private final Tombstone tombstone;

    public ActiveDirectoryEntityDeletedEvent(Object source, long directoryID, Tombstone tombstone)
    {
        super(source, directoryID);
        this.tombstone = tombstone;
    }

    public Tombstone getTombstone()
    {
        return tombstone;
    }
}
