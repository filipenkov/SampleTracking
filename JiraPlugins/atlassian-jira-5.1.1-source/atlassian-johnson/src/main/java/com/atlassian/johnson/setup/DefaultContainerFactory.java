package com.atlassian.johnson.setup;

import com.atlassian.johnson.JohnsonEventContainer;

import java.util.Map;

/**
 * This provides the old, non-multitenant functionality. This way existing apps don't need to worry about the
 * container-factory xml element and all that stuff
 * @since v1.1
 */
public class DefaultContainerFactory implements ContainerFactory
{
    public JohnsonEventContainer create()
    {
        return new JohnsonEventContainer();
    }

    public void init(final Map params)
    {
    }
}
