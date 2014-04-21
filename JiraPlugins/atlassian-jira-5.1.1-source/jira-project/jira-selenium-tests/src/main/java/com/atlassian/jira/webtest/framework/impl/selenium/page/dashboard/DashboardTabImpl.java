package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.gadget.Gadget;
import com.atlassian.jira.webtest.framework.gadget.ReferenceGadget;
import com.atlassian.jira.webtest.framework.impl.selenium.component.tab.AbstractNamedTab;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.gadget.ReferenceGadgetImpl;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardTab;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Default implementation of {@link com.atlassian.jira.webtest.framework.page.dashboard.DashboardTab}.
 *
 * @since v4.3
 */
public class DashboardTabImpl extends AbstractNamedTab<DashboardTab> implements DashboardTab
{
    private static final String ACTIVE_TAB_LOCATOR_TEMPLATE = "ul.vertical.tabs li.active span[title=%s]";
    private static final String NON_ACTIVE_TAB_LOCATOR_TEMPLATE = "ul.vertical.tabs li span[title=%s]";

    private final Locator activeTabLocator;
    private final Locator tabLinkLocator;
    private final Locator container;

    private final Dashboard dashboard;

    public DashboardTabImpl(Dashboard dashboard, String tabName, SeleniumContext context)
    {
        super(tabName, context);
        this.dashboard = notNull("dashboard", dashboard);
        this.activeTabLocator = css(String.format(ACTIVE_TAB_LOCATOR_TEMPLATE, tabName));
        this.tabLinkLocator = css(String.format(NON_ACTIVE_TAB_LOCATOR_TEMPLATE, tabName));
        this.container = id("dashboard-content");
    }

    @Override
    protected Locator detector()
    {
        return activeTabLocator;
    }

    @Override
    public Locator locator()
    {
        return container;
    }

    @Override
    public TimedCondition isOpen()
    {
        return activeTabLocator.element().isPresent();
    }

    @Override
    public TimedCondition isClosed()
    {
        return not(isOpen());
    }

    @Override
    public TimedCondition isOpenable()
    {
        return and(dashboard.isAt(), isClosed());
    }

    @Override
    public DashboardTab open()
    {
        assertThat(isOpenable(), byDefaultTimeout());
        tabLinkLocator.element().click();
        return this;
    }

    @Override
    public TimedCondition hasGadget(Class<? extends Gadget> gadgetType)
    {
        return conditions().containsText(css("h3.dashboard-item-title"), GadgetInfo.gadgetName(gadgetType));
    }

    @Override
    public TimedCondition hasGadget(Class<? extends Gadget> gadgetType, int gadgetId)
    {
        return conditions().containsText(id("gadget-" + gadgetId + "-title"), GadgetInfo.gadgetName(gadgetType));
    }

    @Override
    public <T extends Gadget> T gadget(Class<T> gadgetType)
    {
        if (gadgetType.equals(ReferenceGadget.class))
        {
            return (T) new ReferenceGadgetImpl(dashboard, context);
        }
        else
        {
            throw new IllegalStateException("This sophisticated DI mechanism has not been able to figure out what you're"
                    + "talking about: <" + gadgetType + "> A.K.A. we desperately _need_ DI here (atlassian-selenium2 to the rescue)");
        }
    }

    @Override
    public <T extends Gadget> T gadget(Class<T> gadgetType, int gadgetId)
    {
        if (gadgetType.equals(ReferenceGadget.class))
        {
            return (T) new ReferenceGadgetImpl(gadgetId, dashboard, context);
        }
        else
        {
            throw new IllegalStateException("This sophisticated DI mechanism has not been able to figure out what you're"
                    + "talking about: <" + gadgetType + "> A.K.A. we desperately _need_ DI here (atlassian-selenium2 to the rescue)");
        }
    }

}
