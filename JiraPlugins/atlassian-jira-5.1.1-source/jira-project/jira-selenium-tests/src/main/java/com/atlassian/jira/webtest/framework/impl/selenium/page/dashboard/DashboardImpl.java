package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.dialog.AddGadgetDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.gadget.AddGadgetDialogImpl;
import com.atlassian.jira.webtest.framework.impl.selenium.page.SeleniumAbstractGlobalPage;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardTab;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;

/**
 * Default implementation of the {@link com.atlassian.jira.webtest.framework.page.dashboard.Dashboard} page.
 *
 * @since v4.3
 */
public class DashboardImpl extends SeleniumAbstractGlobalPage<Dashboard> implements Dashboard
{
    private final Locator detector;
    private final Locator linkLocator;
    private final Locator addGadgetLink;
    private final Locator dashboardTabContainerLocator;

    private final AddGadgetDialog addGadget;
    private final DashboardToolsMenu toolsMenu;
    private final DashboardTab defaultTab;

    public DashboardImpl(SeleniumContext ctx)
    {
        super(Dashboard.class, ctx);
        this.linkLocator = id("home_link");
        this.detector = css("li.selected").combine(linkLocator()).withDefaultTimeout(Timeouts.PAGE_LOAD);
        this.addGadgetLink = id("add-gadget");
        this.dashboardTabContainerLocator = css("#dashboard ul.vertical.tabs");
        this.addGadget = new AddGadgetDialogImpl(this, context);
        this.toolsMenu = new SeleniumDashboardToolsMenu(this, context);
        this.defaultTab = new DefaultDashboardTabImpl(this, context);
    }

    @Override
    protected Locator linkLocator()
    {
        return linkLocator;
    }

    @Override
    protected Locator detector()
    {
        return detector;
    }

    @Override
    public AddGadgetDialog openGadgetDialog()
    {
        assertThat(addGadget.isClosed(), byDefaultTimeout());
        addGadgetLink.element().click();
        return addGadget;
    }

    @Override
    public AddGadgetDialog gadgetDialog()
    {
        return addGadget;
    }

    @Override
    public DashboardToolsMenu toolsMenu()
    {
        return toolsMenu;
    }

    @Override
    public DashboardTab openTab(String tabName)
    {
        return tab(tabName).open();
    }

    @Override
    public DashboardTab tab(String name)
    {
        // TODO better
        return new DashboardTabImpl(this, name, context);
    }

    @Override
    public TimedCondition hasDefaultTab()
    {
        return dashboardTabContainerLocator.element().isNotPresent();
    }

    @Override
    public DashboardTab defaultTab()
    {
        if (hasDefaultTab().byDefaultTimeout())
        {
            return defaultTab;
        }
        else
        {
            throw new IllegalStateException("More than one tab on the Dashboard");
        }
    }
}
