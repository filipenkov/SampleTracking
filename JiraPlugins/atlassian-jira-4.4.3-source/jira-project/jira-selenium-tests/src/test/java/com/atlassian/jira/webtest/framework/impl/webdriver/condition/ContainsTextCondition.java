package com.atlassian.jira.webtest.framework.impl.webdriver.condition;

import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Condition that an element represented by a WebDriver {@link org.openqa.selenium.By} query contains a given text.
 *
 * @since v4.3
 */
public class ContainsTextCondition extends AbstractWebDriverBasedCondition
{
    private final By byQuery;
    private final String text;

    public ContainsTextCondition(By byQuery, WebDriverContext context, final String text)
    {
        super(context);
        this.byQuery = notNull("query", byQuery);
        this.text = notNull("text", text);
    }

    public boolean now()
    {
        List<WebElement> elements = engine.findElements(byQuery);
        return !elements.isEmpty() && getFirst(elements).getText().contains(text);
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

}
