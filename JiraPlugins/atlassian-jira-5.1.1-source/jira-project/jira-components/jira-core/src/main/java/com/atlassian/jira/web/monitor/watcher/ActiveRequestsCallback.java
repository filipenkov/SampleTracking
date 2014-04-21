package com.atlassian.jira.web.monitor.watcher;

import com.atlassian.jira.web.monitor.Request;

import java.util.Collection;

/**
 * Callback used by the {@link RequestWatcher} to get the active requests.
 *
 * @see RequestWatcher
 * @since v4.3
 */
public interface ActiveRequestsCallback
{
    /**
     * Returns a collection containing the Request objects that correspond to the currently active requests. Note that
     * this method will get called in a different thread than the one that registers it, so implementations must take
     * care to be thread safe.
     *
     * @return a Collection of Request
     */
    Collection<Request> get();
}
