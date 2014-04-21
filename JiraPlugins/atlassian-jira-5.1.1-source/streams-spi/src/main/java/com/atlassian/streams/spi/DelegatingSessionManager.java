package com.atlassian.streams.spi;

/**
 * A {@link SessionManager} that delegates its {@link SessionManager#withSession(com.google.common.base.Supplier)}
 * method to another {@link SessionManager} if one exists, or if not, to a stub implementation
 * that simply calls the underlying {@link com.google.common.base.Supplier}.
 * <p>
 * There will always be a single {@link DelegatingSessionManager} instance provided by the
 * Activity Streams plugin.  All code that needs to make use of the {@link SessionManager}
 * (if any) provided by the host product plugin should import this component, since it will work
 * even if the host product does not provide a {@link SessionManager}.
 */
public interface DelegatingSessionManager extends SessionManager
{
}
