package com.atlassian.streams.common;

import com.atlassian.streams.spi.SessionManager;

import com.google.common.base.Supplier;

/**
 * A stub {@link SessionManager} that simply calls the underlying {@link Supplier}.
 * This is used by our {@link SwitchingSessionManager} instance as a fallback if there
 * is no other {@link SessionManager} (see {@code spi.xml}).
 */
public final class PassThruSessionManager implements SessionManager
{
    public <T> T withSession(Supplier<T> s)
    {
        return s.get();
    }
}
