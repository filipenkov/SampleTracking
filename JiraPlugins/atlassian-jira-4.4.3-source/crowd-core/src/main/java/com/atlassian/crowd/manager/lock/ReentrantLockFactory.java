package com.atlassian.crowd.manager.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock factory that creates a new instance of {@link ReentrantLock} on each call to {@link #getLock(String)}.
 */
public class ReentrantLockFactory implements LockFactory
{
    /**
     * @inheritDoc
     */
    public Lock getLock(String key)
    {
        // even though the key parameter isn't used here, it is used in implementations in the products -- do not remove!
        return new ReentrantLock();
    }
}
