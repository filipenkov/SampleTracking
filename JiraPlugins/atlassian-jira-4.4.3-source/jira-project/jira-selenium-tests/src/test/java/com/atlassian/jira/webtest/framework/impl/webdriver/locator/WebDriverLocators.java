package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.core.locator.Locators;
import org.openqa.selenium.By;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Mapping between test framework locators and the WebDriver {@link org.openqa.selenium.By}.
 *
 * @see org.openqa.selenium.By
 * @since 4.3
 */
public enum WebDriverLocators
{
    ID(Locators.ID)
            {
                @Override
                public By create(String value)
                {
                    return By.id(value);
                }
            },
    CLASS(Locators.CLASS)
            {
                @Override
                public By create(String value)
                {
                    return By.className(value);
                }
            },
    CSS(Locators.CSS)
            {

                @Override
                public By create(String value)
                {
                    return By.cssSelector(value);
                }
            },
    XPATH(Locators.XPATH)
            {
                @Override
                public By create(String value)
                {
                    return By.xpath(value);
                }
            };

    private final LocatorType type;

    private WebDriverLocators(final LocatorType type)
    {
        this.type = type;
    }

    public static WebDriverLocators forType(LocatorType type)
    {
        notNull("type", type);
        for (WebDriverLocators loc : values())
        {
            if (loc.type.equals(type))
            {
                return loc;
            }
        }
        throw new IllegalArgumentException("No WebDriver locator for type <" + type + ">");
    }

    public LocatorType type()
    {
        return type;
    }

    /**
     * Create a WebDriver {@link org.openqa.selenium.By} locator corresponding to this locator type.
     *
     * @param value value of the locator
     * @return new WebDriver locator
     */
    public By create(String value)
    {
        throw new AbstractMethodError();
    }
}
