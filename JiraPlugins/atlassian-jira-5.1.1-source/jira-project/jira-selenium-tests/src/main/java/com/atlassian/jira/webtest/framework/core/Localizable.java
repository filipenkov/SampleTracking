package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.locator.Locator;

/**
 * A page object that can be unambiguously located.
 *
 * @since v4.3
 */
public interface Localizable extends PageObject
{

    /**
     * Locator unambiguously locating this page object.
     *
     * @return locator
     */
    Locator locator();
}
