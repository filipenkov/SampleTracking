package com.atlassian.jira.webtest.selenium.framework.core;

import com.atlassian.jira.webtest.selenium.framework.model.CancelType;

/**
 * A page object with ability to be cancelled (closed).
 *
 * @param <T> type of this page object returned by cancel action
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.core.Cancelable} instead
 */
@Deprecated
public interface Cancelable<T extends PageObject> extends PageObject
{
    /**
     * Cancel this element.
     *
     * @param cancelType type of cancel action to perform
     * @return this page object
     */
    T cancel(CancelType cancelType);
}
