package com.atlassian.jira.webtest.framework.core;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;

/**
 * Abstract generic interface of every Selenium page object.  
 *
 * @since v4.2
 */
public interface PageObject
{
    /**
     * Returns condition representing a query about the state of this page object: if this evaluates to
     * <code>true</code>, this page object is present within the current test context and may be
     * queried/manipulated by the test driver.
     *
     * @return condition representing availability of this page object in the current text context
     */
    TimedCondition isReady();

    /**
     * Current test context.
     *
     * @return test context
     */
    WebTestContext context();
}
