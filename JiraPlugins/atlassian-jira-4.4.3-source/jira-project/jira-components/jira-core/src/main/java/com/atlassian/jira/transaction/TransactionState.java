package com.atlassian.jira.transaction;

import org.ofbiz.core.entity.GenericTransactionException;

/**
 * This represents the state of a running transaction that can be comitted or rolled back
 *
 * @since v4.4.1
 */
public interface TransactionState
{
    /**
     * This will commit the transaction.
     *
     * @throws GenericTransactionException if the transaction cannot be commited
     */
    void commit() throws GenericTransactionException;

    /**
     * This will rollback the transaction.
     *
     * @throws GenericTransactionException if the transaction cannot be rollbacked
     */
    void rollback() throws GenericTransactionException;

    /**
     * @return if this represents a new transaction and hence whether calling {@link #commit()} or {@link #rollback()}
     *         will actually do anything
     */
    boolean isNewTransaction();
}
