package com.atlassian.crowd.manager.lock;

import java.util.concurrent.locks.Lock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DirectoryLockManagerTest
{
    private DirectoryLockManager lockManager;

    @Before
    public void setUp()
    {
        lockManager = new DirectoryLockManager();
    }

    @Test
    public void lockManagerReturnsNonNullLocks()
    {
        assertNotNull(lockManager.getLock(0));
    }

    @Test
    public void lockManagerReturnsSameLockForSameId()
    {
        assertSame(lockManager.getLock(0), lockManager.getLock(0));
    }

    @Test
    public void lockManagerReturnsDifferentLocksForDifferentId()
    {
        assertNotSame(lockManager.getLock(0), lockManager.getLock(1));
    }

    @Test
    public void locksAreReentrant()
    {
        Lock l = lockManager.getLock(0);
        assertTrue(l.tryLock());
        assertTrue(l.tryLock());

        l.unlock();
        l.unlock();
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void locksAreInitiallyUnlocked()
    {
        Lock l = lockManager.getLock(0);
        l.unlock();
    }
}
