package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityExpander;

/**
 * <p>A resolver to find the expander for object or types.</p>
 */
public interface EntityExpanderResolver
{
    /**
     * Tells whether this resolver can get an expander for the given instance.
     *
     * @param type the type to resolve the expander for.
     * @return {@code true} if an expander can be found for this object instance, {@code false} otherwise.
     */
    boolean hasExpander(Class<?> type);

    /**
     * Gets an {@link EntityExpander} for the given type.
     *
     * @param type the type of object to look up the expander for.
     * @param <T> the type of object to retrieve the expander for.
     * @return the EntityExpander, {@code null} if none could be found. This method will never return {@code null} if
     *         {@link #hasExpander(Class)} returns {@code true} for the same instance.
     */
    <T> EntityExpander<T> getExpander(Class<? extends T> type);
}
