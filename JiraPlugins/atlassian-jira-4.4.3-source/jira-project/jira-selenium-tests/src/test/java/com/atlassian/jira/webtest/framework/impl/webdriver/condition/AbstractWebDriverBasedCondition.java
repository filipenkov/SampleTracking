package com.atlassian.jira.webtest.framework.impl.webdriver.condition;

import com.atlassian.jira.webtest.framework.core.condition.AbstractTimedCondition;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import org.openqa.selenium.WebDriver;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition} in the WebDriver world.
 *
 * @since v4.2
 */
public abstract class AbstractWebDriverBasedCondition extends AbstractTimedCondition
{
    // TODO should introduce a proper way of passing this from subclasses
    private static final long DEFAULT_TIMEOUT = 500L;
    
    protected final WebDriverContext context;
    protected final WebDriver engine;

    public AbstractWebDriverBasedCondition(WebDriverContext context)
    {
        super(DEFAULT_TIMEOUT, notNull("context", context).conditionInterval());
        this.context = context;
        this.engine = notNull("engine", context.engine());
    }
    
}
