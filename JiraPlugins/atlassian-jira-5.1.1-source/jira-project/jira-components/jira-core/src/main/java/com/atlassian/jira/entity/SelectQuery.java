package com.atlassian.jira.entity;

import com.atlassian.jira.ofbiz.OfBizDelegator;

import java.util.List;

/**
 * @since v5.2
 */
public interface SelectQuery<E>
{
    ExecutionContext<E> runWith(OfBizDelegator ofBizDelegator);

    ExecutionContext<E> runWith(EntityEngine entityEngine);

    interface ExecutionContext<E>
    {
        List<E> asList();

        /**
         * Returns the single Entity that is the result of this query.
         * <p>
         * Will return null if no rows were returned by the DB query.
         * Throws {@link IllegalStateException} if more than one row is found.
         *
         * @return the Entity found or null of none found.
         *
         * @throws IllegalStateException if more than one row is found.
         */
        E singleValue() throws IllegalStateException;

        <R> R consumeWith(EntityListConsumer<E, R> consumer);
    }
}
