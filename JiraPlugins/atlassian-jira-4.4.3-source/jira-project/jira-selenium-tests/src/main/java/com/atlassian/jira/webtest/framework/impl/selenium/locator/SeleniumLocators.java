package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.core.locator.mapper.DefaultLocatorMapper;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.webtest.framework.core.locator.Locators.CLASS;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.CSS;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.ID;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.JQUERY;
import static com.atlassian.jira.webtest.framework.core.locator.Locators.XPATH;

/**
 * Factory of Selenium locators.
 *
 * @since v4.3
 */
public final class SeleniumLocators
{
    private SeleniumLocators()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static LocatorType NAME = new LocatorType()
    {
        @Override
        public String id()
        {
            return "name";
        }
    };

    /**
     * Locator mapper enhanced with Selenium-specific mappings.
     *
     */
    static DefaultLocatorMapper SELENIUM_MAPPER = new DefaultLocatorMapper().addMappings(NameMappings.ALL);

    /**
     * Create new ID Selenium locator.
     *
     * @param id id of the located element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator id(String id, SeleniumContext ctx)
    {
        return create(ID, id, ctx);
    }

    /**
     * Create new CSS class Selenium locator.
     *
     * @param cssClass class of the located element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator forClass(String cssClass, SeleniumContext ctx)
    {
        return create(CLASS, cssClass, ctx);
    }

    /**
     * Create new CSS Selenium locator.
     *
     * @param cssSelector CSS selector locating the element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator css(String cssSelector, SeleniumContext ctx)
    {
        return create(CSS, cssSelector, ctx);
    }

    /**
     * Create new jQuery Selenium locator.
     *
     * @param jQuerySelector jQuery selector locating the element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator jQuery(String jQuerySelector, SeleniumContext ctx)
    {
        return create(JQUERY, jQuerySelector, ctx);
    }

    /**
     * Create new XPath Selenium locator.
     *
     * @param xpath xpath query locating the element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator xpath(String xpath, SeleniumContext ctx)
    {
        return create(XPATH, xpath, ctx);
    }

    /**
     * Create new name Selenium locator.
     *
     * @param name HTML name of the located element
     * @param ctx Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator name(String name, SeleniumContext ctx)
    {
        return create(NAME, name, ctx);
    }

    /**
     * Create locator for given locator data and context.
     *
     * @param data locator data
     * @param ctx current Selenium context
     * @return new SeleniumLocator instance
     */
    public static SeleniumLocator create(LocatorData data, SeleniumContext ctx)
    {
        return create(data.type(), data.value(), ctx);
    }

    /**
     * Reverse-engineer Selenium-style <tt>fullLocator</tt> (the 'type=value' string) and create a {@link SeleniumLocator}
     * object representing it.
     *
     * @param fullLocator full Selenium locator
     * @param context Selenium context
     * @return new {@link SeleniumLocator} matching the inout
     * @see com.atlassian.jira.webtest.framework.impl.selenium.locator.StringLocators
     */
    public static SeleniumLocator create(String fullLocator, SeleniumContext context)
    {
        return create(StringLocators.fromLocator(fullLocator), StringLocators.removeLocatorPrefix(fullLocator), context);
    }

    /**
     * Create Selenium locator for given locator <tt>type</tt>, <tt>value</tt> and Selenium context. 
     *
     * @param type locator type
     * @param value locator value
     * @param ctx context
     * @return new {@link SeleniumLocator} instance
     */
    public static SeleniumLocator create(LocatorType type, String value, SeleniumContext ctx)
    {
        if (!type.equals(CLASS))
        {
            return createDefault(type, value, ctx);
        }
        else return new ClassLocator(ctx, value);
    }



    private static SeleniumLocator createDefault(LocatorType type, String value, SeleniumContext ctx)
    {
        return new Default(ctx, type, value);
    }

    private static class Default extends AbstractSeleniumLocator
    {
        private final String fullLocator;

        Default(SeleniumContext ctx, LocatorType locatorType, String value)
        {
            super(ctx, locatorType, value);
            this.fullLocator = StringLocators.create(locatorData);
        }
        Default(SeleniumContext ctx, LocatorType locatorType, String value, Timeouts defTimeout)
        {
            super(ctx, locatorType, value, defTimeout);
            this.fullLocator = StringLocators.create(locatorData);
        }


        public String fullLocator()
        {
            return fullLocator;
        }

        @Override
        public SeleniumLocator withDefaultTimeout(Timeouts defTimeout)
        {
            return new Default(context, locatorData.type(), locatorData.value(), defTimeout);
        }
    }

    private static class ClassLocator extends AbstractSeleniumLocator
    {
        private final String fullLocator;

        ClassLocator(SeleniumContext ctx, String value)
        {
            super(ctx, CLASS, value);
            // no 'class' locator in Selenium
            this.fullLocator = StringLocators.create(CSS, "." + value());
        }
        ClassLocator(SeleniumContext ctx, String value, Timeouts defTimeout)
        {
            super(ctx, CLASS, value, defTimeout);
            // no 'class' locator in Selenium
            this.fullLocator = StringLocators.create(CSS, "." + value());
        }

        public String fullLocator()
        {
            return fullLocator;
        }

        @Override
        public SeleniumLocator withDefaultTimeout(Timeouts defTimeout)
        {
            return new ClassLocator(context, value(), defTimeout);
        }
    }
}
