package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.PageObject;

/**
 * Represents a component of another, composite page object
 *
 * @param <P> type of component parent
 * @since v4.3
 */
public interface Component<P extends PageObject> extends PageObject
{

    /**
     * A reference to this compoent's parent.
     *
     * @return component's parent
     */
    P parent();
}
