package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginModuleComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginModulesList;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class SeleniumManagePluginComponent extends AbstractSeleniumPluginComponent<ManagePluginComponent> implements ManagePluginComponent
{
    private final Locator disableButtonLocator;
    private final Locator enableButtonLocator;

    private final Locator toggleModuleLocator;
    private final Locator moduleListDetector;

    private final PluginModulesList<ManagePluginModuleComponent> moduleList;


    public SeleniumManagePluginComponent(SeleniumContext context, String pluginKey)
    {
        super(context, pluginKey, "manage", ManagePluginComponent.class);
        disableButtonLocator = id("upm-disable-"+pluginKey).withDefaultTimeout(Timeouts.AJAX_ACTION);
        enableButtonLocator = id("upm-enable-"+pluginKey).withDefaultTimeout(Timeouts.AJAX_ACTION);
        toggleModuleLocator = ((SeleniumLocator) this.locator().combine(css(".upm-module-toggle"))).withDefaultTimeout(Timeouts.AJAX_ACTION);
        moduleListDetector = ((SeleniumLocator) this.locator().combine(css(".upm-plugin-modules"))).withDefaultTimeout(Timeouts.AJAX_ACTION);
        moduleList = new SeleniumPluginModulesList<ManagePluginModuleComponent>(context,this.locator(),new ManagePluginModulesComponentFactory(this.locator().combine(css(".upm-plugin-modules")),pluginKey));
    }


    @Override
    public TimedCondition isEnabled()
    {
        return Conditions.not(isDisabled());
    }

    @Override
    public TimedCondition isDisabled()
    {
        return conditions().hasClassBuilder(detector(),"disabled").defaultTimeout(Timeouts.AJAX_ACTION).build();
    }

    @Override
    public ManagePluginComponent enable()
    {
        if (isExpanded().byDefaultTimeout())
        {
            if (enableButtonLocator.element().isPresent().byDefaultTimeout())
            {
                enableButtonLocator.element().click();
                return this;
            }
            else
            {
                throw new IllegalStateException(pluginKey + " plugin is missing the enable button");
            }
        }
        else
        {
             throw new IllegalStateException(pluginKey + " plugin enable button clicked when collapsed");
        }
    }

    @Override
    public ManagePluginComponent disable()
    {
        if (isExpanded().byDefaultTimeout())
        {
            if (disableButtonLocator.element().isPresent().byDefaultTimeout())
            {
                disableButtonLocator.element().click();
                return this;
            }
            else
            {
                throw new IllegalStateException(pluginKey + " plugin is missing the disable button");
            }
        }
        else
        {
             throw new IllegalStateException(pluginKey + " plugin disable button clicked when collapsed");
        }
    }

    @Override
    public TimedCondition isModuleListExpanded()
    {
        return and(moduleListDetector.element().isPresent(),conditions().hasClassBuilder((SeleniumLocator) moduleListDetector,"expanded").defaultTimeout(Timeouts.AJAX_ACTION).build());
    }

    @Override
    public TimedCondition isModuleListCollapsed()
    {
        return and(moduleListDetector.element().isPresent(),Conditions.not(conditions().hasClassBuilder((SeleniumLocator) moduleListDetector,"expanded").defaultTimeout(Timeouts.AJAX_ACTION).build()));
    }

    @Override
    public ManagePluginComponent toggleExpandModulesList()
    {
        this.toggleModuleLocator.element().click();
        return this;
    }

    @Override
    public PluginModulesList<ManagePluginModuleComponent> moduleList()
    {
        return moduleList;
    }

    private class ManagePluginModulesComponentFactory implements PluginModuleComponentFactory<ManagePluginModuleComponent>
    {
        private final String pluginKey;
        private final Locator listLocator;

        ManagePluginModulesComponentFactory(Locator listLocator, final String pluginKey)
        {
            this.pluginKey = pluginKey;
            this.listLocator = listLocator;
        }

        @Override
        public ManagePluginModuleComponent create(final String moduleKey)
        {
            return new SeleniumManagePluginModuleComponent(context,listLocator,pluginKey,moduleKey);
        }
    }

}
