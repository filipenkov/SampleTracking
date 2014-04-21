package com.atlassian.jira.util.concurrent;

import com.atlassian.jira.util.RuntimeInterruptedException;

import java.util.concurrent.CountDownLatch;

public class Latch extends CountDownLatch
{
    public Latch(final int permits)
    {
        super(permits);
    }

    @Override
    public void await()
    {
        try
        {
            super.await();
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
    }
}
