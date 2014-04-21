package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginComponent;
import com.atlassian.upm.pageobjects.Ids;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPluginComponent<T extends PluginComponent<T>> extends AbstractLocatorBasedPageObject implements PluginComponent<T>
{
    protected final String pluginKey;
    protected final String tabId;
    protected final Locator mainLocator;
    protected final Locator headerRowLocator;

    private final Class<T> targetType;


    protected AbstractSeleniumPluginComponent(SeleniumContext context, String pluginKey, String tabId, Class<T> targetType)
    {
        super(context);
        this.pluginKey = pluginKey;
        this.tabId = tabId;
        mainLocator = id("upm-plugin-"+createHash()).withDefaultTimeout(Timeouts.COMPONENT_LOAD);
        headerRowLocator = mainLocator.combine(forClass("upm-plugin-row"));
        this.targetType = targetType;
    }

    @Override
    public Locator locator()
    {
        return mainLocator;
    }


    @Override
    protected SeleniumLocator detector()
    {
        return (SeleniumLocator) mainLocator;
    }

    @Override
    public String getPluginKey()
    {
        return this.pluginKey;
    }

    @Override
    public TimedCondition isExpanded()
    {
        return and(isReady(), conditions().hasClassBuilder(detector(),"expanded").defaultTimeout(Timeouts.AJAX_ACTION).build());
    }

    @Override
    public TimedCondition isCollapsed()
    {
        return Conditions.not(isExpanded());
    }

    @Override
    public T expand()
    {
        if (isCollapsed().byDefaultTimeout())
        {
            headerRowLocator.element().click();
            return targetType.cast(this);
        }
        else
        {
            throw new IllegalStateException(this.pluginKey + " plugin component already expanded");
        }
    }

    @Override
    public T collapse()
    {
        if (isExpanded().byDefaultTimeout())
        {
            headerRowLocator.element().click();
            return targetType.cast(this);
        }
        else
        {
            throw new IllegalStateException(this.pluginKey + " plugin component already collapsed");
        }
    }

    private long createHash()
    {
        return Ids.createHash(pluginKey, tabId);
    }
}
