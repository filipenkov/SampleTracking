package com.atlassian.crowd.event.migration;

import com.atlassian.crowd.event.Event;

public abstract class XMLRestoreEvent extends Event
{
    public XMLRestoreEvent(Object source)
    {
        super(source);
    }
}
