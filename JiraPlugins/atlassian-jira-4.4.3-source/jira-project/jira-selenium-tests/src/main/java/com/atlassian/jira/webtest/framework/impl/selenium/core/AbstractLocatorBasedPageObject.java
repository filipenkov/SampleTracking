package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;

/**
 * <p>
 * Abstract Selenium page object based on a single locator. This locator is assumed to be unique for the given
 * object within the current test context and its presence/non presence decides whether this page object
 * is ready to be manipulated by the test.
 *
 * @since v4.3
 */
public abstract class AbstractLocatorBasedPageObject extends AbstractSeleniumPageObject implements PageObject
{

    protected AbstractLocatorBasedPageObject(SeleniumContext context)
    {
        super(context);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Generic implementation in terms of the underlying locator of this page object. Subclasses may extend
     * it to provide additional/alternative conditions of readiness.
     *
     * @return {@inheritDoc}
     * 
     * @see PageObject#isReady()
     * @see com.atlassian.jira.webtest.framework.core.condition.Conditions
     *
     */
    @Override
    public TimedCondition isReady()
    {
        return detector().element().isPresent();
    }

    /**
     * Main locator of this page object. Its presence means that this object is ready to be manipulated in the test.
     *
     * @return main locator of this page object
     */
    protected abstract Locator detector();

}
