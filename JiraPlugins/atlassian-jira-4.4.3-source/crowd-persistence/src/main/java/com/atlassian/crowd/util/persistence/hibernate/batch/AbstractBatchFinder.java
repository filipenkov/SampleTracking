package com.atlassian.crowd.util.persistence.hibernate.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract implementation of the batch finder, sub classes should extend this implementation with an implementation
 * of {@link #processBatchFind(long, java.util.Collection, Class)} which performs the hibernate version specific
 * search.
 * <p/>
 * Callback methods {@link #beforeFind()} and {@link #afterFind()} can be used to wrap the search in a session
 * and/or transaction.
 */
public abstract class AbstractBatchFinder implements BatchFinder
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractBatchFinder.class);

    private int batchSize = 20;

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    /**
     * Returns a collection of entities that match the
     * names provided. Any names that cannot be matched
     * to persistent entities are not present in the resultant
     * collection.
     * <p/>
     * Internally, this performs a:
     * <code>SELECT * FROM entityTable WHERE entityName IN (...)</code>
     * <p/>
     * This is batched such that the size of the <code>IN</code>
     * clause is at most the <code>batchSize</code>.
     *
     * @param directoryID     directory ID of the entities to return.
     * @param names           collection of entity names. This, along with the directoryID
     *                        should form the primary key of the entity.
     * @param persistentClass the persistent class to lookup. This must
     *                        be a Hibernate-mapped DirectoryEntity.
     * @return a collection of the DirectoryEntities that exist
     *         matching any of the supplied names.
     */
    public <E extends Serializable> Collection<E> find(long directoryID, Collection<String> names, Class<E> persistentClass)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Attempting to find " + names.size() + " objects of class " + persistentClass.getName());
        }

        Collection<E> results = new ArrayList<E>();
        Collection<String> nameBatch = new ArrayList<String>(batchSize);

        beforeFind();
        try
        {
            for (String name : names)
            {
                nameBatch.add(name);

                if (nameBatch.size() == batchSize)
                {
                    results.addAll(processBatchFind(directoryID, nameBatch, persistentClass));
                    nameBatch.clear();
                }
            }

            if (!nameBatch.isEmpty())
            {
                results.addAll(processBatchFind(directoryID, nameBatch, persistentClass));
                nameBatch.clear();
            }
        }
        finally
        {
            afterFind();
        }

        return results;
    }

    protected void beforeFind()
    {
    }

    protected void afterFind()
    {
    }

    protected abstract <E> Collection<E> processBatchFind(long directoryID, Collection<String> names, Class<E> persistentClass);

}
