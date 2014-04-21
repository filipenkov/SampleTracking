package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginModuleComponent;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumManagePluginModuleComponent extends AbstractSeleniumPluginModuleComponent<ManagePluginModuleComponent> implements ManagePluginModuleComponent
{
    private final Locator disableButtonLocator;
    private final Locator enableButtonLocator;
    private final Locator actionsFieldDetector;



    public SeleniumManagePluginModuleComponent(SeleniumContext context,Locator listLocator, final String pluginKey, final String moduleKey)
    {
        super(context, listLocator, pluginKey, moduleKey, ManagePluginModuleComponent.class);
        disableButtonLocator = locator().combine(css("upm-module-disable"));
        enableButtonLocator = locator().combine(css("upm-module-enable"));
        actionsFieldDetector = locator().combine(css(".upm-module-actions"));
    }


    @Override
    public TimedCondition isEnabled()
    {
        return Conditions.not(isDisabled());
    }

    @Override
    public TimedCondition isDisabled()
    {
        return conditions().hasClassBuilder(detector(),"upm-module-disabled").defaultTimeout(Timeouts.AJAX_ACTION).build();
    }

    @Override
    public TimedCondition canBeDisabled()
    {
        return Conditions.not(cannotBeDisabled());
    }

    @Override
    public TimedCondition cannotBeDisabled()
    {
        return conditions().hasClassBuilder((SeleniumLocator) locator().combine(css(".upm-module-actions")),"upm-module-cannot-disable").defaultTimeout(Timeouts.AJAX_ACTION).build();
    }

    @Override
    public ManagePluginModuleComponent enable()
    {
            if (enableButtonLocator.element().isVisible().byDefaultTimeout())
            {
                enableButtonLocator.element().click();
                return this;
            }
            else
            {
                throw new IllegalStateException(moduleKey + "module in plugin "+ pluginKey+ " is missing the enable button");
            }
    }

    @Override
    public ManagePluginModuleComponent disable()
    {
            if (disableButtonLocator.element().isVisible().byDefaultTimeout())
            {
                disableButtonLocator.element().click();
                return this;
            }
            else
            {
                throw new IllegalStateException(moduleKey + "module in plugin "+ pluginKey+  "  is missing the disable button");
            }
    }
}