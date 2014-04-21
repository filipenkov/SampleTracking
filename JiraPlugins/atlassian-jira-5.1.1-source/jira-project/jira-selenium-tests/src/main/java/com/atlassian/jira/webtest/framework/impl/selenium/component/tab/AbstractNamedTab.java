package com.atlassian.jira.webtest.framework.impl.selenium.component.tab;

import com.atlassian.jira.webtest.framework.component.tab.NamedTab;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract implementation of {@link com.atlassian.jira.webtest.framework.component.tab.NamedTab}.
 *
 * @since v4.3
 */
public abstract class AbstractNamedTab<T extends NamedTab<T>> extends AbstractLocatorBasedPageObject implements NamedTab<T>
{
    private final String tabName;


    protected AbstractNamedTab(String tabName, SeleniumContext context)
    {
        super(context);
        this.tabName = notNull("tabName", tabName);
    }

    @Override
    public String name()
    {
        return tabName;
    }
}
