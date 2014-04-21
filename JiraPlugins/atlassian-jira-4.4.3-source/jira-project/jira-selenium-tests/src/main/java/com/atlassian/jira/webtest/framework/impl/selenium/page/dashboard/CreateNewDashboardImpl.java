package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumChildPage;
import com.atlassian.jira.webtest.framework.page.dashboard.CreateNewDashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Default implementation of {@link com.atlassian.jira.webtest.framework.page.dashboard.CreateNewDashboard}.
 *
 * @since v4.3
 */
public class CreateNewDashboardImpl extends AbstractSeleniumChildPage<Dashboard> implements CreateNewDashboard
{
    private final Locator nameInputLocator;
    private final Locator descriptionInputLocator;
    private final Locator submitAddLocator;
    private final Locator cancelLocator;

    public CreateNewDashboardImpl(Dashboard parent, SeleniumContext ctx)
    {
        super(parent, ctx);
        this.nameInputLocator = name("portalPageName");
        this.descriptionInputLocator = name("portalPageDescription");
        this.submitAddLocator = id("add_submit");
        this.cancelLocator = id("cancelButton");
    }

    @Override
    protected Locator detector()
    {
        return css("a[title=Get online help about Configuring Multiple Dashboards]");
    }

    @Override
    protected Locator backLocator()
    {
        return cancelLocator;
    }

    @Override
    public CreateNewDashboard name(KeySequence name)
    {
        nameInputLocator.element().type(name);
        return this;
    }

    @Override
    public CreateNewDashboard description(KeySequence description)
    {
        descriptionInputLocator.element().type(description);
        return this;
    }

    @Override
    public Dashboard submitAdd()
    {
        submitAddLocator.element().click();
        waitFor().pageLoad();
        return parentPage;
    }

}

