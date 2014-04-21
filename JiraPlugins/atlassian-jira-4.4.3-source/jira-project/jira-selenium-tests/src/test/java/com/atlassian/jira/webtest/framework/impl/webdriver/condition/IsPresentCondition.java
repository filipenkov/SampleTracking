package com.atlassian.jira.webtest.framework.impl.webdriver.condition;

import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import org.openqa.selenium.By;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Condition that an element represented by a WebDriver {@link org.openqa.selenium.By} query exists on the page
 * within the current test context.
 *
 * @since v4.3
 */
public class IsPresentCondition extends AbstractWebDriverBasedCondition
{
    private final By byQuery;

    public IsPresentCondition(By byQuery, WebDriverContext context)
    {
        super(context);
        this.byQuery = notNull("query", byQuery);
    }

    public boolean now()
    {
        return !engine.findElements(byQuery).isEmpty();
    }
}
