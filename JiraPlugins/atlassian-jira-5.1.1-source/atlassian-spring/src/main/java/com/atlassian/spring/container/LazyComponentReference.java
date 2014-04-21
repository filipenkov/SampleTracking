package com.atlassian.spring.container;

import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.StringUtils;

/**
 * Lazily loaded reference to a component that is not constructed until required.
 *
 * This class is not an alternative to correctly using dependency injection but is a replacement for the necessary
 * evil of loading components in places such as servlets where all sense of best practices breaks down.
 *
 * @param <T>
 */
public class LazyComponentReference<T> extends LazyReference<T>
{
    private final String key;

    /**
     * @param key of the component
     */
    public LazyComponentReference(String key)
    {
        if (StringUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("Argument 'key' cannot be empty or null");
        }
        this.key = key;
    }

    /**
     * Calls {@link com.atlassian.spring.container.ContainerManager#getComponent(String)}
     *
     * @return component
     * @throws IllegalStateException if container is not yet setup
     */
    @Override
    protected T create() throws Exception
    {
        if (!ContainerManager.isContainerSetup())
        {
            throw new IllegalStateException("Container is not setup");
        }
        return (T)ContainerManager.getComponent(key);
    }
}
