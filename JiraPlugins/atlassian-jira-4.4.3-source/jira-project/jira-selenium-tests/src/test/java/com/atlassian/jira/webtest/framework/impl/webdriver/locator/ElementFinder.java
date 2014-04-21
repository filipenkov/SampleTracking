package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContextAware;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Utility to find elements for given WebDriver context and {@link org.openqa.selenium.By} query.
 *
 * @since v4.3
 */
public final class ElementFinder extends WebDriverContextAware
{
    private final By by;

    public ElementFinder(WebDriverContext context, By by)
    {
        super(context);
        this.by = notNull("by", by);
    }

    public boolean hasAny()
    {
        return !isEmpty();
    }

    public boolean isEmpty()
    {
        return findNow().isEmpty();
    }

    public WebElement getFirst()
    {
        List<WebElement> elems = findNow();
        if (elems.isEmpty())
        {
            throw new IllegalStateException("No elements located");
        }
        return elems.get(0);
    }

    private List<WebElement> findNow()
    {
        return engine.findElements(by);
    }


}
