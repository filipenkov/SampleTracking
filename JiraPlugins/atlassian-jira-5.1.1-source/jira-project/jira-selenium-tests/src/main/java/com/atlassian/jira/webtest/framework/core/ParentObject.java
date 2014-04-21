package com.atlassian.jira.webtest.framework.core;

/**
 * An interface implemented by all framework components that are parent to a significant number of child page objects.
 * It defines a factory-style generic method to retrieve any child component that is necessary. 
 *
 *
 * @since v4.3
 */
public interface ParentObject
{
    /**
     * Get child of this component for given <tt>childType</tt>.
     *
     * @param childType class of the child
     * @param <T> type of the child
     * @return child component instance
     * @throws IllegalArgumentException if <tt>childType</tt> is not supported by this parent component.
     */
    <T extends PageObject> T getChild(Class<T> childType);
}
