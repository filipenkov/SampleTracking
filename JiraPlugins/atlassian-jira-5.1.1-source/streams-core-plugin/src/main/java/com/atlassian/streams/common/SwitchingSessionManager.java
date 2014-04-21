package com.atlassian.streams.common;

import com.atlassian.streams.spi.DelegatingSessionManager;
import com.atlassian.streams.spi.OptionalService;
import com.atlassian.streams.spi.SessionManager;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link SessionManager} that delegates to whatever {@link SessionManager} has been provided
 * by another module, or, if there isn't any, to a default implementation.
 * <p>
 * This module exports a single instance of this class, which can be imported by referring to
 * the {@link DelegatingSessionManager} interface.
 */
public class SwitchingSessionManager extends OptionalService<SessionManager> implements DelegatingSessionManager
{
    private final SessionManager defaultSessionManager;

    public SwitchingSessionManager(SessionManager defaultSessionManager)
    {
        super(SessionManager.class);
        this.defaultSessionManager = checkNotNull(defaultSessionManager, "defaultSessionManager");
    }

    public <T> T withSession(Supplier<T> s)
    {
        return getService().getOrElse(defaultSessionManager).withSession(s);
    }
}
