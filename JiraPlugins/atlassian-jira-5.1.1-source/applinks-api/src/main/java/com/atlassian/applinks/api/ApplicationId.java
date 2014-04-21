package com.atlassian.applinks.api;

import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;

import java.util.UUID;

/**
 * The unique ID of an {@link ApplicationLink}.
 *
 * Note that this ID may change under certain circumstances. If you intend 
 * to store it for future look-ups, you should listen for the {@link ApplicationLinksIDChangedEvent} to keep your
 * stored ID current.
 *
 * @since 3.0
 */
public class ApplicationId
{
    private final String id;

    /**
     * Creates a new {@link ApplicationId}. The supplied id must be in the format described by {@link UUID#toString()}
     *
     * @param id the application id String
     */
    public ApplicationId(final String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("id must not be null");
        }

        try
        {
            UUID.fromString(id); //test to see if the supplied id String is a valid UUID string
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("id must be a valid java.util.UUID string: " + id, e);
        }

        this.id = id;
    }

    public String get()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return id;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ApplicationId that = (ApplicationId) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
