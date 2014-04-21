package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;

/**
 * The context for expansions.
 * @param <T> The type of the entity being expanded.
 */
public interface ExpandContext<T>
{
    /**
     * @return the expandable annotation on the field that is the entity being expanded.
     */
    Expandable getExpandable();

    /**
     * @return the entity being expanded
     */
    T getEntity();

    /**
     * @return the expand parameter used to match the entity being expanded.
     */
    ExpandParameter getEntityExpandParameter();
}
