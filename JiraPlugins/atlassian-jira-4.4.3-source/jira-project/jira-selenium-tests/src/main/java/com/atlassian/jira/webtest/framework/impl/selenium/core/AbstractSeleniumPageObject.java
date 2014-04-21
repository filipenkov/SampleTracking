package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;

/**
 * <p>
 * Utility base class that contains many DSL-style methods based on the {@link SeleniumContext},
 * accessible to subclasses.
 *
 * <p>
 * Your page objects will be so much <i>cooler</i> now!
 *
 * @since v4.2
 */
public abstract class AbstractSeleniumPageObject extends SeleniumContextAware implements PageObject
{
    private final SeleniumQueries queries;
    private final SeleniumConditions conditions;
    private final SeleniumWaits waits;
    private final SeleniumLocator body;

    protected AbstractSeleniumPageObject(SeleniumContext context)
    {
        super(context);
        this.queries = new SeleniumQueries(context);
        this.conditions = new SeleniumConditions(context);
        this.waits = new SeleniumWaits(context);
        this.body = css("body");
    }

    /* ------------------------------------------- LOCATOR FACTORY METHODS ------------------------------------------ */

    /**
     * Creates ID locator for given ID.
     *
     * @param id id of the element
     * @return ID locator
     * @see com.atlassian.jira.webtest.framework.core.locator.Locators#ID
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#id(String, SeleniumContext)
     */
    protected final SeleniumLocator id(String id)
    {
        return SeleniumLocators.id(id, context);
    }

    /**
     * Creates name locator for given HTML name.
     *
     * @param name name of the element
     * @return name locator
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#NAME
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#name(String, SeleniumContext)
     */
    protected final SeleniumLocator name(String name)
    {
        return SeleniumLocators.name(name, context);
    }

    /**
     * Creates class locator for given class name.
     *
     * @param className class name of the element
     * @return class name locator
     * @see com.atlassian.jira.webtest.framework.core.locator.Locators#CLASS
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#forClass(String, SeleniumContext)
     */
    protected final SeleniumLocator forClass(String className)
    {
        return SeleniumLocators.forClass(className, context);
    }

    /**
     * Creates CSS locator for given css selector.
     *
     * @param cssSelector CSS selector of the element
     * @return CSS locator
     * @see com.atlassian.jira.webtest.framework.core.locator.Locators#CSS
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#css(String, SeleniumContext)
     */
    protected final SeleniumLocator css(String cssSelector)
    {
        return SeleniumLocators.css(cssSelector, context);
    }

    /**
     * Creates jQuery locator for given jQuery selector.
     *
     * @param jquery jQuery selector of the element
     * @return jQuery locator
     * @see com.atlassian.jira.webtest.framework.core.locator.Locators#JQUERY
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#jQuery(String, SeleniumContext)
     */
    protected final SeleniumLocator jQuery(String jquery)
    {
        return SeleniumLocators.jQuery(jquery, context);
    }

    /**
     * Creates XPath locator for given XPath query.
     *
     * @param xpath XPath query
     * @return XPath locator
     * @see com.atlassian.jira.webtest.framework.core.locator.Locators#XPATH
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators#xpath(String, SeleniumContext)
     */
    protected final SeleniumLocator xpath(String xpath)
    {
        return SeleniumLocators.xpath(xpath, context);
    }

    /**
     * Creates locator of given type.
     *
     * @param locData locator data
     * @return locator
     */
    protected final SeleniumLocator locatorFor(LocatorData locData)
    {
        if (locData instanceof SeleniumLocator)
        {
            return (SeleniumLocator) locData;
        }
        return SeleniumLocators.create(locData, context);
    }

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    protected final SeleniumQueries queries()
    {
        return queries;
    }

    /* ----------------------------------------------- CONDITIONS --------------------------------------------------- */

    protected final SeleniumConditions conditions()
    {
        return conditions;
    }

    /* ------------------------------------------------ WAIT FOR ---------------------------------------------------- */

    protected final SeleniumWaits waitFor()
    {
        return waits;
    }

    /* ------------------------------------------------- UTILS ------------------------------------------------------ */

    protected final SeleniumLocator body()
    {
        return body;
    }

    
}
