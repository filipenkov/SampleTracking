package com.atlassian.jira.webtest.framework.core;

/**
 * Defines default close modes. 
 *
 * @param <T> close operation target
 * @since v4.3
 */
public interface DefaultCloseMode<T extends PageObject>
{

    /**
     * Close by ESC key.
     *
     * @return target page object instance
     */
    T byEscape();
    
}
