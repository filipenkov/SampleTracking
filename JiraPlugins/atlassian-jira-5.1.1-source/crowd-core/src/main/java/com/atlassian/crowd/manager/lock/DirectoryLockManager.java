package com.atlassian.crowd.manager.lock;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

/**
 * Manages locks for synchronisation purposes, one per SynchronisableDirectory. Applies a standard naming strategy
 * for a key and delegates to a {@link LockFactory} to construct the particular locks.
 *
 * @see com.atlassian.crowd.directory.SynchronisableDirectory
 */
public class DirectoryLockManager
{
    private static final String KEY_PREFIX = DirectoryLockManager.class.getName() + ".directory-";

    private final ConcurrentMap<String, Lock> locks;

    /**
     * Constructs an instance that delegates to a {@link ReentrantLockFactory} for lock construction.
     */
    public DirectoryLockManager()
    {
        this(new ReentrantLockFactory());
    }

    /**
     * Constructs an instance that delegates to the provided {@link LockFactory} for lock construction.
     *
     * @param lockFactory used to construct locks as required for each directory
     */
    public DirectoryLockManager(final LockFactory lockFactory)
    {
        locks = new MapMaker().softValues().makeComputingMap(new Function<String, Lock>() {
            public Lock apply(final String key)
            {
                return lockFactory.getLock(key);
            }
        });
    }

    /**
     * Returns the lock for the directory with the given ID. The lock has not been acquired when it is returned,
     * clients still need to do this as normal. For example:
     * <p/>
     * <pre><code>
     *     Lock lock = directoryLockManager.getLock(directory.getId());
     *     lock.lock();
     *     try {
     *         // access the resource protected by this lock
     *     } finally {
     *         lock.unlock();
     *     }
     * </code></pre>
     *
     * @param directoryId the ID of the directory to lock
     * @return the lock for the provided directory ID
     */
    public Lock getLock(long directoryId)
    {
        return locks.get(getLockKey(directoryId));
    }

    private String getLockKey(long directoryId)
    {
        return KEY_PREFIX + directoryId;
    }
}
