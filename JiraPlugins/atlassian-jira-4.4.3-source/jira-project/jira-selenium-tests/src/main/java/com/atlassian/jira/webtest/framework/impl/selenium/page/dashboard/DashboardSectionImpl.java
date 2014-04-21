package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.page.dashboard.Dashboard;
import com.atlassian.jira.webtest.framework.page.dashboard.DashboardToolsMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link com.atlassian.jira.webtest.framework.component.AjsDropdown.Section}.
 *
 * @since v4.3
 */
public class DashboardSectionImpl extends AbstractSeleniumComponent<AjsDropdown<Dashboard>> implements AjsDropdown.Section<Dashboard>
{
    private final List<AjsDropdown.Item<Dashboard>> toolItems;

    protected DashboardSectionImpl(DashboardToolsMenu parent, SeleniumContext context)
    {
        super(parent, context);
        this.toolItems = initToolItems();
    }

    private List<AjsDropdown.Item<Dashboard>> initToolItems()
    {
        List<AjsDropdown.Item<Dashboard>> answer = new ArrayList<AjsDropdown.Item<Dashboard>>();
        for (DashboardToolsMenu.ToolItems toolItem : DashboardToolsMenu.ToolItems.values())
        {
            answer.add(new DashboardItemImpl(toolItem, this, context));
        }
        return answer;
    }


    @Override
    public String id()
    {
        return "";
    }

    @Override
    public String header()
    {
        throw new IllegalStateException("no header");
    }

    @Override
    public boolean hasHeader()
    {
        return false;
    }

    @Override
    public TimedQuery<List<AjsDropdown.Item<Dashboard>>> items()
    {
        return queries().forStaticValue(toolItems);
    }

    @Override
    public Locator locator()
    {
        return parent().locator();
    }

    @Override
    public TimedCondition isReady()
    {
        return parent().isReady();
    }

    @Override
    protected Locator detector()
    {
        return locator();
    }

}
