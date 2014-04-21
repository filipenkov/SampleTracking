package com.atlassian.jira.util;

/**
 * Consume the object a {@link Supplier} produces.
 */
public interface Consumer<T>
{
    /**
     * Consume the product.
     * 
     * @param element must not be null
     */
    void consume(@NotNull T element);
}