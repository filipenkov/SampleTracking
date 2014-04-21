package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;

/**
 * Represents selenium locator and its 'id=value' syntax.
 *
 * @since v4.2
 */
public interface SeleniumLocator extends Locator
{

    /**
     * Returns full Selenium compatible locator.
     *
     * @return full Selenium locator ('id=value')
     */
    String fullLocator();


    /**
     * Returns Selenium compatible locator, without the prefix (it is <b>NOT</b> always equal to {@link #value()}.
     *
     * @return Selenium locator value without prefix
     */
    String bareLocator();

    /**
     * A version of {@link com.atlassian.jira.webtest.framework.core.locator.Locator#combine(com.atlassian.jira.webtest.framework.core.locator.Locator)}
     * with covariant return type for easier usage in the Selenium implementation of the framework. 
     *
     * @param locator locator to nest
     * @return new locator resulting from nesting
     *
     * @see com.atlassian.jira.webtest.framework.core.locator.Locator#combine(com.atlassian.jira.webtest.framework.core.locator.Locator)
     */
    SeleniumLocator combine(Locator locator);

    /**
     * Create new locator with this locator's data and a custom default timeout for the timed conditions of this locator's
     * element.
     *
     * @param defTimeout default timeout for the new locator's timed conditions
     * @return new locator
     */
    SeleniumLocator withDefaultTimeout(Timeouts defTimeout);

    /**
     * Default timeout for the timed conditions of this locator's element.
     *
     * @return default timeout
     */
    Timeouts defaultTimeout();

}
