package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.TimedAssertions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;
import static com.atlassian.webtest.ui.keys.Sequences.keys;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.AjsDropdown.Item} for dashboard tools
 * menu,
 *
 * @since v4.3
 */
public class DashboardItemImpl extends AbstractSeleniumComponent<AjsDropdown.Section<Dashboard>> implements AjsDropdown.Item<Dashboard>
{
    private static final String LOCATOR_TEMPLATE = "li.dropdown-item a#%s";
    private static final String ACTIVE_LOCATOR_TEMPLATE = "li.dropdown-item.active a#%s";
    private final DashboardToolsMenu.ToolItems item;
    private final DashboardToolsMenu parentMenu;
    private final Locator main;
    private final Locator active;


    protected DashboardItemImpl(DashboardToolsMenu.ToolItems item, AjsDropdown.Section<Dashboard> parent, SeleniumContext context)
    {
        super(parent, context);
        this.item = notNull("item", item);
        this.parentMenu = (DashboardToolsMenu) parent.parent();
        this.main = parentMenu.locator().combine(css(String.format(LOCATOR_TEMPLATE, item.id())));
        this.active = parentMenu.locator().combine(css(String.format(ACTIVE_LOCATOR_TEMPLATE, item.id())));

    }

    DashboardToolsMenu.ToolItems toolItem()
    {
        return item;
    }

    @Override
    public Locator locator()
    {
        return main;
    }

    @Override
    protected Locator detector()
    {
        return main;
    }

    @Override
    public AjsDropdown<Dashboard> dropDown()
    {
        return parentMenu;
    }

    @Override
    public String name()
    {
        return main.element().text().now();
    }

    @Override
    public TimedCondition isSelected()
    {
        return active.element().isPresent();
    }

    @Override
    public TimedCondition isNotSelected()
    {
        return not(isSelected());
    }

    @Override
    public AjsDropdown.Item<Dashboard> select()
    {
        if (isSelected().now())
        {
            return this;
        }
        int positionCount = dropDown().itemCount().byDefaultTimeout();
        for (int i=0; i<=positionCount; i++)
        {
            dropDown().locator().element().type(keys(SpecialKeys.ARROW_DOWN));
            if (isSelected().byDefaultTimeout())
            {
                return this;
            }
        }
        throw new IllegalStateException("Unable to select");
    }

    @Override
    public AjsDropdown.Item<Dashboard> down()
    {
        TimedAssertions.assertThat(isSelected(), byDefaultTimeout());
        dropDown().locator().element().type(SpecialKeys.ARROW_DOWN);
        TimedAssertions.assertThat(isNotSelected(), byDefaultTimeout());
        return dropDown().selectedItem().byDefaultTimeout();
    }

    @Override
    public AjsDropdown.Item<Dashboard> up()
    {
        TimedAssertions.assertThat(isSelected(), byDefaultTimeout());
        dropDown().locator().element().type(SpecialKeys.ARROW_UP);
        TimedAssertions.assertThat(isNotSelected(), byDefaultTimeout());
        return dropDown().selectedItem().byDefaultTimeout();
    }
}
