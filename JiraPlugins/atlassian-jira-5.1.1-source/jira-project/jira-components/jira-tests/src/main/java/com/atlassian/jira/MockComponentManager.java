package com.atlassian.jira;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.mock.pico.MockPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * Probably the fastest component manager in the world.
 *
 * @since v4.3
 */
public class MockComponentManager extends ComponentManager
{
    final MutablePicoContainer mockContainer = new MockPicoContainer();

    public MockComponentManager()
    {
    }

    @Override
    public PicoContainer getContainer()
    {
        return getMutablePicoContainer();
    }

    @Override
    public MutablePicoContainer getMutablePicoContainer()
    {
        return mockContainer;
    }
}
