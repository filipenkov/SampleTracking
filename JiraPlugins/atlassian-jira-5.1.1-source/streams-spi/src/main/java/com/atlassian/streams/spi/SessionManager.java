package com.atlassian.streams.spi;

import com.google.common.base.Supplier;

/**
 * An optional service that should be implemented by applications that need to have new sessions or database
 * connections opened when threads are spun off from the main request thread.
 */
public interface SessionManager
{
    /**
     * Responsible for opening sessions or acquiring database connections if there isn't one already available, running
     * the {@code Supplier} and then closing the session or releasing the database connection.
     *
     * @return value returned by the supplier
     */
    <T> T withSession(Supplier<T> s);
}
