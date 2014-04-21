package com.atlassian.jira.webtest.framework.page.admin;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.page.Page;

/**
 * Represents JIRA administration page. 
 *
 * @since v4.2
 */
public interface AdminPage extends Page
{

    /**
     * Locator for the link to the page on the Administration page.
     *
     * @return this page link locator
     */
    Locator adminLinkLocator();
}
