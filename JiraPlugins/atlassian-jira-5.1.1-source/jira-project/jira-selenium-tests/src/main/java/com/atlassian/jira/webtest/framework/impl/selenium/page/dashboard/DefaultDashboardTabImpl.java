package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.core.condition.Conditions;
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
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * <i>Non</i>-default implementation of {@link com.atlassian.jira.webtest.framework.page.dashboard.DashboardTab},
 * for case where the tab <i>is</i> default:).
 *
 * @since v4.3
 */
public class DefaultDashboardTabImpl extends AbstractNamedTab<DashboardTab> implements DashboardTab
{
    private static final String ACTIVE_TAB_LOCATOR_TEMPLATE = "ul.vertical.tabs li.active span[title=%s]";
    private static final String NON_ACTIVE_TAB_LOCATOR_TEMPLATE = "ul.vertical.tabs li span[title=%s]";
    private static final String DEFAULT_DASHBOARD_TAB_NAME = "default";

    private final Locator container;

    private final Dashboard dashboard;

    public DefaultDashboardTabImpl(Dashboard dashboard, SeleniumContext context)
    {
        super(DEFAULT_DASHBOARD_TAB_NAME, context);
        this.dashboard = notNull("dashboard", dashboard);
        this.container = id("dashboard-content");
    }

    @Override
    protected Locator detector()
    {
        return container;
    }

    @Override
    public Locator locator()
    {
        return container;
    }

    @Override
    public TimedCondition isOpen()
    {
        return and(dashboard.isAt(), dashboard.hasDefaultTab());
    }

    @Override
    public TimedCondition isClosed()
    {
        return not(isOpen());
    }

    @Override
    public TimedCondition isOpenable()
    {
        return Conditions.falseCondition();
    }

    @Override
    public DashboardTab open()
    {
        throw new UnsupportedOperationException("You can't :P");
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
