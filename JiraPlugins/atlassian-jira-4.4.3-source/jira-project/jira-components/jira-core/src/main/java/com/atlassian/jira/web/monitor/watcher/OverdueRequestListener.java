package com.atlassian.jira.web.monitor.watcher;

import com.atlassian.jira.web.monitor.Request;

import java.util.List;

/**
 * Interface for listeners that need to be notified when there are overdue requests.
 *
 * @since v4.3
 */
public interface OverdueRequestListener
{
    /**
     * This callback is invoked when there are overdue requests. Note that this method will be called in a different
     * thread than the thread that registers the listener, so any implementations must ensure they are thread-safe.
     *
     * @param requests a List of the Request objects that are overdue
     * @param overdueThreshold the number of ms after which a request is deemed to be overdue
     */
    void requestsOverdue(List<Request> requests, long overdueThreshold);
}
