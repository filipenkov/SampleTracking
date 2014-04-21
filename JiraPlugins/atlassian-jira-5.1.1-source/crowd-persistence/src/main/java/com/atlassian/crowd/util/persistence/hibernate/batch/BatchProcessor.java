package com.atlassian.crowd.util.persistence.hibernate.batch;

import com.atlassian.crowd.util.BatchResult;

import java.io.Serializable;
import java.util.Collection;

/**
 * Threadsafe batch processor.
 *
 * This processor is essentially a heavyweight generic DAO for
 * processing operations over a collection of entities.
 *
 * For more information on the implementation details, please
 * see the javadoc for {@code AbstractBatchProcessor}.
 *
 */
public interface BatchProcessor
{
    <E extends Serializable> BatchResult<E> execute(HibernateOperation operation, Collection<E> objects);
}
