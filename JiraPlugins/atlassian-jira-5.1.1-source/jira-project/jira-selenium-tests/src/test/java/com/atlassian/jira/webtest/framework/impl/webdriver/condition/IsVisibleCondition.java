package com.atlassian.jira.webtest.framework.impl.webdriver.condition;

import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Condition that an element represented by a WebDriver {@link org.openqa.selenium.By} query is visible on the page
 * within the current test context.
 *
 * @since v4.3
 */
public class IsVisibleCondition extends AbstractWebDriverBasedCondition
{
    private static final Logger log = Logger.getLogger(IsVisibleCondition.class);

    private final By byQuery;

    public IsVisibleCondition(By byQuery, WebDriverContext context)
    {
        super(context);
        this.byQuery = notNull("query", byQuery);
    }

    public boolean now()
    {
        List<WebElement> elements = engine.findElements(byQuery);
        if (elements.isEmpty())
        {
            return false;
        }
        WebElement first = getFirst(elements);
        if (!isRendered(first))
        {
            // TODO might need to throw exception?
            log.warn("WebElement does not implement RenderedWebElement: <" + first + ">");
        }
        return asRendered(first).isDisplayed();
    }

    /**
     * As a default behaviour (by API), we only consider the first found element.
     *
     * @param elements list of elements that we know is of size > 0
     * @return first element
     */
    private WebElement getFirst(List<WebElement> elements)
    {
        return elements.get(0);
    }

    private boolean isRendered(final WebElement first)
    {
        return first instanceof RenderedWebElement;
    }

    private RenderedWebElement asRendered(WebElement element)
    {
        return (RenderedWebElement) element;
    }

}
