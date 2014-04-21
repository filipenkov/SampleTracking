package com.atlassian.crowd.manager.lock;

import java.util.concurrent.locks.Lock;

/**
 * A lock factory returns a lock given a key. Some implementations may return
 * a new instance every time, others may return cached or static instances.
 */
public interface LockFactory
{
    /**
     * Get the lock for the specified key. Getting the lock does not imply that it has been acquired, you
     * still need to acquire and unlock the lock as normal.
     * <p/>
     * <pre><code>
     *     Lock l = lockFactory.getLock("mylock");
     *     l.lock();
     *     try {
     *         // access the resource protected by this lock
     *     } finally {
     *         l.unlock();
     *     }
     * </code></pre>
     *
     * @param key the key for this lock
     * @return the lock for the key
     */
    // even though the key parameter isn't used in Crowd, it is used in implementations in the products -- do not remove!
    Lock getLock(String key);
}
