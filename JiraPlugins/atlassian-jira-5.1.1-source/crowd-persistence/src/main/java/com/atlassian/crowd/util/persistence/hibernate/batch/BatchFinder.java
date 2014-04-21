package com.atlassian.crowd.util.persistence.hibernate.batch;

import java.io.Serializable;
import java.util.Collection;

/**
 * Performs a named query against crowd data with the ability to split up the related IN clause.
 */
public interface BatchFinder
{
    /**
     * Find a set of entities by batching SQL disjunction queries.
     *
     * If some entities are not found in the database, they are
     * not present in the returned collection.
     *
     * @param directoryID directory ID of the entities to return.
     * @param names collection of entity names. This, along with the directoryID
     *        should form the primary key of the entity.
     * @param persistentClass the persistent class to lookup. This must
     *        be a Hibernate-mapped DirectoryEntity.
     * @return a collection of the DirectoryEntities that exist
     *         matching any of the supplied names.
     */
    <E extends Serializable> Collection<E> find(long directoryID, Collection<String> names, Class<E> persistentClass);
}
