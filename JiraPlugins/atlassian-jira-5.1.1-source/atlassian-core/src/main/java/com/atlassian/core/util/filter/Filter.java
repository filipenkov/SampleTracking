package com.atlassian.core.util.filter;

/**
 * Defines what objects should be filtered out of a list.
 */
public interface Filter
{
    /**
     * Should an object be allowed through the filter?
     *
     * @return true if the object can pass through the filter, false
     *         if the object is rejected by the filter.
     */
    boolean isIncluded(Object o);
}
