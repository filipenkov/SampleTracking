package com.atlassian.plugins.rest.common.transaction;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TransactionInterceptorTest
{
    private TransactionTemplate transactionTemplate;
    private TransactionInterceptor transactionInterceptor;
    private MethodInvocation methodInvocation;

    @Before
    public void setUp()
    {
        transactionTemplate = new TransactionTemplate()
        {

            public Object execute(TransactionCallback action)
            {
                return action.doInTransaction();
            }
        };
        transactionInterceptor = new TransactionInterceptor(transactionTemplate);
        methodInvocation = mock(MethodInvocation.class);
    }



    @Test
    public void testExecute() throws IllegalAccessException, InvocationTargetException
    {
        transactionInterceptor.intercept(methodInvocation);
        verify(methodInvocation).invoke();
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteThrowRuntimeException() throws IllegalAccessException, InvocationTargetException
    {
        doThrow(new RuntimeException()).when(methodInvocation).invoke();
        transactionInterceptor.intercept(methodInvocation);
    }
    @Test(expected = IllegalAccessException.class)
    public void testExecuteThrowIllegalAccessException() throws IllegalAccessException, InvocationTargetException
    {
        doThrow(new IllegalAccessException()).when(methodInvocation).invoke();
        transactionInterceptor.intercept(methodInvocation);
    }

    @Test(expected = InvocationTargetException.class)
    public void testExecuteThrowInvocationTargetException() throws IllegalAccessException, InvocationTargetException
    {
        doThrow(new InvocationTargetException(new RuntimeException())).when(methodInvocation).invoke();
        transactionInterceptor.intercept(methodInvocation);
    }
}
