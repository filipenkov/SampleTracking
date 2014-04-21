package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.GlobalPage;
import com.atlassian.jira.webtest.framework.page.Page;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract representation of the {@link com.atlassian.jira.webtest.selenium.framework.pages.GlobalPage}
 * interface, defined in terms of {@link #linkLocator()} provided by subclasses.
 *
 * @since v4.2
 */
public abstract class SeleniumAbstractGlobalPage<T extends GlobalPage<T>> extends AbstractSeleniumPage
        implements Page, GlobalPage<T>
{
    private final Class<T> targetType;

    protected SeleniumAbstractGlobalPage(Class<T> targetType, SeleniumContext ctx)
    {
        super(ctx);
        this.targetType = notNull("targetType", targetType);
    }

    public final T goTo()
    {
        if (!isAt().now())
        {
            linkLocator().element().click();
            client.waitForPageToLoad();
        }
        return asTargetType();
    }

    /**
     * Locator of a globally accessible link leading to this page.
     *
     * @return global link locator
     */
    protected abstract Locator linkLocator();


    protected final T asTargetType()
    {
        return targetType.cast(this);
    }
}
