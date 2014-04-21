package com.atlassian.gadgets.util;

import java.util.concurrent.Callable;

import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import static com.google.common.base.Preconditions.checkNotNull;

public class TransactionRunner
{
    private final TransactionTemplate transactionTemplate;
    
    public TransactionRunner(TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = checkNotNull(transactionTemplate, "transactionTemplate");
    }
    
    @SuppressWarnings("unchecked")
    public <T> T execute(Callable<T> c)
    {
        return (T) transactionTemplate.execute(newCallback(checkNotNull(c)));
    }
    
    public void execute(Runnable r)
    {
        transactionTemplate.execute(newCallback(checkNotNull(r)));
    }
    
    private TransactionCallback newCallback(Callable<?> c)
    {
        return new CallableCallback(c);
    }
    
    private TransactionCallback newCallback(Runnable r)
    {
        return new RunnableCallback(r);
    }

    private final class CallableCallback implements TransactionCallback
    {
        private final Callable<?> c;

        public CallableCallback(Callable<?> c)
        {
            this.c = c;
        }

        public Object doInTransaction()
        {
            try
            {
                return c.call();
            }
            catch (Exception e)
            {
                throw new TransactionException(e);
            }
        }
    }
    
    private final class RunnableCallback implements TransactionCallback
    {
        private final Runnable r;
        
        public RunnableCallback(Runnable r)
        {
            this.r = r;
        }
        
        public Object doInTransaction()
        {
            r.run();
            return null;
        }
    }
    
    public static final class TransactionException extends RuntimeException
    {
        TransactionException(Throwable e)
        {
            super(e);
        }
    }
}
