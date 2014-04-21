package com.atlassian.plugins.rest.common.expand;

/**
 * Represents a JAXB rest data object that is expandable and is capable of
 * expanding itself. It does not need an accompanying
 * {@link com.atlassian.plugins.rest.common.expand.EntityExpander} but instead
 * can be expanded by the generic {@link SelfExpandingExpander}.
 *
 * @see SelfExpandingExpander
 * @since   v1.0.7
 * @author  Erik van Zijst
 */
public interface SelfExpanding
{
    /**
     * Instructs the self-expanding rest data entity to expand itself.
     */
    void expand();
}
