package com.atlassian.jira;

import com.atlassian.event.api.EventPublisher;

/**
* @since v5.0
*/
public class MockEventPublisher implements EventPublisher
{
    @Override
    public void publish(Object event)
    {
    }

    @Override
    public void register(Object listener)
    {
    }

    @Override
    public void unregister(Object listener)
    {
    }

    @Override
    public void unregisterAll()
    {
    }
}
