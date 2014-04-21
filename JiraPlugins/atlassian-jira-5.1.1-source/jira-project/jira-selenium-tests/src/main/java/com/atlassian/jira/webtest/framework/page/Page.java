package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;


/**
 * Common interface of all objects representing Selenium JIRA pages.
 *
 * @since v4.2
 */
public interface Page extends PageObject
{
    /**
     * Check if the test driver is currently at this page
     *
     * @return <code>true</code>, if the test driver is currently at this page, <code>false<code> otherwise
     */
    TimedCondition isAt();
}
