package com.atlassian.gadgets.util;

import java.util.concurrent.Callable;

import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionRunnerTest
{
    final TransactionTemplate template = new TransactionTemplate()
    {
        public Object execute(TransactionCallback callback)
        {
            return callback.doInTransaction();
        }
    };
    final TransactionRunner runner = new TransactionRunner(template);
    
    @Test
    public void assertThatExecuteWithCallableReturnsReturnValueFromCallable() throws Exception
    {
        Object rval = new Object(){};
        Callable<Object> c = mock(Callable.class);
        when(c.call()).thenReturn(rval);

        assertThat(runner.execute(c), is(sameInstance(rval)));
    }
    
    @Test
    public void verifyThatExecuteWithRunnableCallsRun()
    {
        Runnable r = mock(Runnable.class);
        runner.execute(r);
        verify(r).run();
    }
}
