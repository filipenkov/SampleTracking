package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.TimedAssertions;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.query.AbstractSeleniumTimedQuery;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu}.
 *
 * @since v4.3
 */
public class SeleniumDashboardToolsMenu extends AbstractSeleniumComponent<Dashboard> implements DashboardToolsMenu
{
    private final SeleniumLocator dropDownContainerLocator;
    private final Locator dropDownLinkLocator;
    private final Section<Dashboard> section;
    // TODO other mappings
    private final Map<ToolItems,Boolean> itemPostPageLoadMappings = ImmutableMap.<ToolItems,Boolean>builder()
            .put(ToolItems.COPY_DASHBOARD, true)
            .build();

    public SeleniumDashboardToolsMenu(Dashboard parent, SeleniumContext ctx)
    {
        super(parent, ctx);
        this.dropDownContainerLocator = id("dashboard-tools-dropdown");
        this.dropDownLinkLocator = dropDownContainerLocator.combine(forClass("aui-dd-link"));
        this.section = new DashboardSectionImpl(this, context);
    }

    @Override
    protected SeleniumLocator detector()
    {
        return dropDownContainerLocator;
    }

    @Override
    public Locator locator()
    {
        return dropDownContainerLocator;
    }

    @Override
    public TimedCondition isOpen()
    {
        return conditions().hasClassBuilder(dropDownContainerLocator, "active").defaultTimeout(Timeouts.COMPONENT_LOAD).build();
    }

    @Override
    public TimedCondition isClosed()
    {
        return not(isOpen());
    }

    @Override
    public TimedCondition isOpenable()
    {
        return and(parent().isAt(), isClosed());
    }

    @Override
    public AjsDropdown<Dashboard> open()
    {
        TimedAssertions.assertThat("Must be openable", isOpenable(), byDefaultTimeout());
        dropDownLinkLocator.element().click();
        return this;
    }

    @Override
    public CloseMode close()
    {
        return new CloseMode()
        {
            @Override
            public Dashboard byEnter()
            {
                DashboardItemImpl selected = selectedItemNow();
                selected.locator().element().type(Keys.ENTER);
                waitForPageLoadIfNecessary(selected);
                return parent();
            }

            @Override
            public Dashboard byEscape()
            {
                context.ui().pressInBody(Keys.ESCAPE);
                return parent();
            }

            @Override
            public Dashboard byClickIn(Item<Dashboard> item)
            {
                item.locator().element().click();
                waitForPageLoadIfNecessary(item);
                return parent();
            }

            @Override
            public Dashboard byClickIn(ToolItems item)
            {
                toolItem(item).locator().element().click();
                waitForPageLoadIfNecessary(item);
                return parent();
            }
        };
    }

    private void waitForPageLoadIfNecessary(Item<Dashboard> item)
    {
        waitForPageLoadIfNecessary(((DashboardItemImpl)item).toolItem());
    }

    private void waitForPageLoadIfNecessary(ToolItems item)
    {
        Boolean needsPageLoad = itemPostPageLoadMappings.get(item);
        if (Boolean.TRUE.equals(needsPageLoad))
        {
            waitFor().pageLoad();
        }
    }

    @Override
    public TimedCondition hasItem(String itemText)
    {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public DashboardItemImpl toolItem(ToolItems item)
    {
        for (Item<Dashboard> itemObj : section.items().now())
        {
            DashboardItemImpl dashboardItem = (DashboardItemImpl) itemObj;
            if (dashboardItem.toolItem() == item)
            {
                return dashboardItem;
            }
        }
        throw new IllegalStateException("Could not find item instance for: " + item);
    }

    @Override
    public TimedCondition hasSection(String id)
    {
        return Conditions.falseCondition();
    }

    @Override
    public TimedQuery<Integer> itemCount()
    {
        return queries().forStaticValue(ToolItems.values().length);
    }

    @Override
    public TimedQuery<Section<Dashboard>> section(String id)
    {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public TimedQuery<List<Section<Dashboard>>> allSections()
    {
        return queries().forStaticValue(Collections.<Section<Dashboard>>singletonList(section));
    }

    @Override
    public TimedQuery<Item<Dashboard>> item(String text)
    {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public TimedQuery<Item<Dashboard>> selectedItem()
    {
        return new AbstractSeleniumTimedQuery<Item<Dashboard>>(context, ExpirationHandler.RETURN_NULL, Timeouts.COMPONENT_LOAD)
        {
            @Override
            protected boolean shouldReturn(Item<Dashboard> currentEval)
            {
                return currentEval != null;
            }

            @Override
            protected Item<Dashboard> currentValue()
            {
                return selectedItemNow();
            }
        };
    }

    private DashboardItemImpl selectedItemNow()
    {
        for (Item<Dashboard> itemObj : section.items().now())
        {
            if (itemObj.isSelected().now())
            {
                return (DashboardItemImpl) itemObj;
            }
        }
        return null;
    }
}
