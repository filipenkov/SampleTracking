package com.atlassian.crowd.util.persistence.hibernate.batch;

public interface HibernateOperation
{
    /**
     * Interface to logic that performs a single hibernate operation on a target object for
     * use within a batch.
     *
     * @param object perform the operation on this object
     */
    void performOperation(Object object);
}
