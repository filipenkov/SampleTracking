package com.atlassian.jira.webtest.selenium.framework.pages;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract representation of the {@link com.atlassian.jira.webtest.selenium.framework.pages.GlobalPage}
 * interface, defined in terms of {@link #linkLocator()} provided by subclasses.
 *
 * @since v4.2
 */
public abstract class AbstractGlobalPage<T extends AbstractGlobalPage> extends AbstractPage implements Page, GlobalPage<T>
{
    private final Class<T> targetType;

    protected AbstractGlobalPage(Class<T> targetType, SeleniumContext ctx)
    {
        super(ctx);
        this.targetType = notNull("targetType", targetType);
    }

    public final T goTo()
    {
        if (!isAt())
        {
            client.click(linkLocator(), true);
        }
        return targetType.cast(this);
    }

    /**
     * Locator of a globally accessible link leading to this page.
     *
     * @return global link locator
     */
    protected abstract String linkLocator();


}
