package com.atlassian.event.internal;

import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.ListenerInvoker;

/**
 * A stub event dispatcher that simply calls the invoker.
 */
final class StubEventDispatcher implements EventDispatcher
{
    public void dispatch(ListenerInvoker invoker, Object event)
    {
        invoker.invoke(event);
    }
}
