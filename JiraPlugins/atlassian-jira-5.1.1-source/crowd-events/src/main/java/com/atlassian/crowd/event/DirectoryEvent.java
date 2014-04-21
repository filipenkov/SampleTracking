package com.atlassian.crowd.event;

import com.atlassian.crowd.embedded.api.Directory;

import com.google.common.base.Preconditions;

/**
 * An Event that represents any operation on a {@link com.atlassian.crowd.embedded.api.Directory}
 * going via the {@link com.atlassian.crowd.manager.directory.DirectoryManager}.
 * <p/>
 * These events correspond to operations performed via Crowd (eg. Crowd Console, Crowd connected applications).
 */
public abstract class DirectoryEvent extends Event
{
    private final Directory directory;

    public DirectoryEvent(Object source, Directory directory)
    {
        super(source);
        Preconditions.checkNotNull(directory, "directory may not be null");
        this.directory = directory;
    }

    public Directory getDirectory()
    {
        return directory;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DirectoryEvent))
        {
            return false;
        }

        DirectoryEvent that = (DirectoryEvent) o;

        if (!directory.equals(that.directory))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return directory != null ? directory.hashCode() : 0;
    }
}
