package com.atlassian.crowd.manager.directory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents the results from an 'addAll' operation.
 */
public class BulkAddResult<T>
{
    private final Collection<T> failedEntities;
    private final Collection<T> existingEntities;
    private final long attemptedToAdd;
    private final boolean overwriteUsed;

    public BulkAddResult(long attemptingToAdd, boolean overwrite)
    {
        this.failedEntities = new ArrayList<T>();
        this.existingEntities = new ArrayList<T>();
        this.attemptedToAdd = attemptingToAdd;
        this.overwriteUsed = overwrite;
    }

    public void addFailedEntities(Collection<T> entities)
    {
        failedEntities.addAll(entities);
    }

    public void addFailedEntity(T entity)
    {
        failedEntities.add(entity);
    }

    public void addExistingEntity(T entity)
    {
        existingEntities.add(entity);
    }

    /**
     * Returns the entities which did failed to be
     * added during the bulk add process.
     *
     * @return failed entities.
     */
    public Collection<T> getFailedEntities()
    {
        return failedEntities;
    }

    /**
     * Returns the entities which did not get overwritten
     * during the bulk add process. This collection will
     * be empty if <code>overwrite</code> is <code>true</code>.
     *
     * @return existing entities.
     */
    public Collection<T> getExistingEntities()
    {
        return existingEntities;
    }

    /**
     * @return <code>true</code> iff the overwriting was requested
     * during the bulk add process.
     */
    public boolean isOverwriteUsed()
    {
        return overwriteUsed;
    }

    /**
     * @return the number of entities that was asked to be bulk added.
     */
    public long getAttemptedToAdd()
    {
        return attemptedToAdd;
    }

    /**
     * @return <code>attemptedToAdd - failedEntities - existingEntities</code>
     */
    public long getAddedSuccessfully()
    {
        return attemptedToAdd - failedEntities.size() - existingEntities.size();
    }
}
