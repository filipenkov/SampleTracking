package com.atlassian.plugins.rest.common.transaction;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import java.lang.reflect.InvocationTargetException;

/**
 * Wraps the resource method call in a transaction via SAL's {@link TransactionTemplate}.
 *
 * @since 2.0
 */
public class TransactionInterceptor implements ResourceInterceptor
{
    private final TransactionTemplate transactionTemplate;

    public TransactionInterceptor(TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = transactionTemplate;
    }

    public void intercept(final MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            transactionTemplate.execute(new TransactionCallback()
            {
                public Object doInTransaction()
                {
                    try
                    {
                        invocation.invoke();
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new TransactionException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new TransactionException(e);
                    }
                    return null;
                }
            });
        }
        catch (TransactionException ex)
        {
            Throwable t = ex.getCause();
            if (t instanceof IllegalAccessException)
            {
                throw (IllegalAccessException)t;
            }
            else if (t instanceof InvocationTargetException)
            {
                throw (InvocationTargetException)t;
            }
            else
            {
                throw new RuntimeException("This should not be possible");
            }
        }
    }

    private static final class TransactionException extends RuntimeException
    {
        private TransactionException(Throwable throwable)
        {
            super(throwable);
        }
    }
}
