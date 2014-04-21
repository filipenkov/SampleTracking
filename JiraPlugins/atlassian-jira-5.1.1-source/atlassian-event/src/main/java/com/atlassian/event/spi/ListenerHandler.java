package com.atlassian.event.spi;

import java.util.List;

/**
 * Interface to find invokers for a given listener objects. A typical example might be listeners that implement a
 * specific interface or that have annotated listener methods.
 * @since 2.0
 */
public interface ListenerHandler
{
    /**
     * Retrieves the list of invokers for the given listener.
     * @param listener the listener object to get invokers for
     * @return a list of invokers linked to the listener object.
     */
    List<? extends ListenerInvoker> getInvokers(Object listener);
}
