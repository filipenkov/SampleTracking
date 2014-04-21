package com.atlassian.crowd.embedded;

import org.hsqldb.jdbcDriver;
import org.ofbiz.core.entity.GenericTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import static org.ofbiz.core.entity.TransactionUtil.beginLocalTransaction;
import static org.ofbiz.core.entity.TransactionUtil.rollbackLocalTransaction;

public class OfBizPlatformTransactionManager implements PlatformTransactionManager
{
    static
    {
        new org.postgresql.Driver();
        new jdbcDriver(); // Will only work if in a static block
    }

    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException
    {
        try
        {
            beginLocalTransaction("defaultDS", -1);
        }
        catch (GenericTransactionException e)
        {
            e.printStackTrace();
        }
        return new OfBizTransactionStatus();
    }

    public void commit(TransactionStatus status) throws TransactionException
    {
        // do nothing?
    }

    public void rollback(TransactionStatus status) throws TransactionException
    {
        try
        {
            rollbackLocalTransaction(true);
        }
        catch (GenericTransactionException e)
        {
            e.printStackTrace();
        }
    }

    private static class OfBizTransactionStatus implements TransactionStatus
    {

        public boolean isNewTransaction()
        {
            return true;
        }

        public boolean hasSavepoint()
        {
            return false;
        }

        public void setRollbackOnly()
        {
            // do nothing
        }

        public boolean isRollbackOnly()
        {
            return false;
        }

        public boolean isCompleted()
        {
            return false;
        }

        public Object createSavepoint() throws TransactionException
        {
            return null;
        }

        public void rollbackToSavepoint(Object savepoint) throws TransactionException
        {
            try
            {
                rollbackLocalTransaction(true);
            }
            catch (GenericTransactionException e)
            {
                e.printStackTrace();
            }
        }

        public void releaseSavepoint(Object savepoint) throws TransactionException
        {
            // do nothing?
        }
    }
}
