package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.component.Component;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.core.component.Component}. 
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumComponent<P extends PageObject> extends AbstractLocatorBasedPageObject implements Component<P>
{
    private final P parent;

    protected AbstractSeleniumComponent(P parent, SeleniumContext context)
    {
        super(context);
        this.parent = notNull("parent", parent);
    }

    @Override
    public final P parent()
    {
        return parent;
    }
}
