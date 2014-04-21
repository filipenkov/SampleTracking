package com.atlassian.jira.webtest.framework.driver.admin.plugins;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.webtest.framework.core.condition.Conditions;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.page.GlobalPages;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.ManagePluginComponent;
import com.atlassian.jira.webtest.framework.page.admin.plugins.Plugins;
import com.atlassian.jira.webtest.framework.page.admin.plugins.PluginsTab;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.by;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.falseCondition;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * A helper class to perform high-level operations on the Plugins page object family.
 *
 * @since v4.3
 */
public class PluginsManagement
{
    private static final int PLUGIN_ENABLE_TIMEOUT = 4000;

    // TODO clean this up & segregate into management for each tab (ManageExisting)

    private final GlobalPages globalPages;
    private Plugins plugins;

    private final ExistingPluginsTabManagement manageExisting;

    public PluginsManagement(GlobalPages globalPages)
    {
        this.globalPages = globalPages;
        this.manageExisting = new ExistingPluginsTabManagement();
    }

    /**
     * Go to UPM admin page
     *
     * @return this helper instance
     */
    public PluginsManagement goToPlugins()
    {
        if (isNotAtPlugins().byDefaultTimeout())
        {
            plugins = globalPages.goToAdministration().goToPage(Plugins.class);
        }
        return this;
    }

    /**
     * Timed condition checking if the test is currently at the plugins page.
     *
     * @return timed condition checking if it's the UPM page
     */
    public TimedCondition isAtPlugins()
    {
        if (plugins == null)
        {
            return falseCondition();
        }
        else
        {
            return plugins.isAt();
        }
    }

    /**
     * Plugins page instance.
     *
     * @return plugins page instance
     */
    public Plugins plugins()
    {
        return plugins;
    }

    /**
     * Timed condition checking if the test is currently <b>not</b> at the plugins page.
     *
     * @return timed condition checking if it's not the UPM page
     */
    public TimedCondition isNotAtPlugins()
    {
        return not(isAtPlugins());
    }

    public PluginsManagement assertIsAtPlugins()
    {
        assertThat(isAtPlugins(), byDefaultTimeout());
        return this;
    }

    public PluginsManagement enableSystemPlugin(String pluginKey)
    {
        ManagePluginComponent pluginComponent = goToSystemPluginComponent(pluginKey);
        if (not(pluginComponent.isEnabled()).byDefaultTimeout())
        {
            pluginComponent.enable();
            assertThat(pluginComponent.isEnabled(), by(PLUGIN_ENABLE_TIMEOUT));
        }
        return this;
    }

    public void disableSystemPlugin(String pluginKey)
    {
        ManagePluginComponent pluginComponent = goToSystemPluginComponent(pluginKey);
        if (not(pluginComponent.isDisabled()).byDefaultTimeout())
        {
            pluginComponent.disable();
            assertThat(pluginComponent.isDisabled(), byDefaultTimeout());
            assertThat(pluginComponent.isDisabled(), byDefaultTimeout());
        }
    }

    public TimedCondition isSystemPluginEnabled(String pluginKey)
    {
        return goToSystemPluginComponent(pluginKey).isEnabled();
    }

    public TimedCondition isSystemPluginDisabled(String pluginKey)
    {
        return goToSystemPluginComponent(pluginKey).isDisabled();
    }

    private ManagePluginComponent goToSystemPluginComponent(String pluginKey)
    {
        goToPlugins();
        goToTab(ManageExistingPlugins.class);
        showSystemPlugins();
        ManagePluginComponent pluginComponent = findPluginComponentOnManageExistingTab(pluginKey);
        expandPlugin(pluginComponent);
        return pluginComponent;
    }

    public ExistingPluginsTabManagement manageExisting()
    {
        return manageExisting;
    }

    // TODO move this out to manage existing

    public <T extends PluginsTab<T>> PluginsManagement goToTab(Class<T> tabClass)
    {
        assertIsAtPlugins();
        if (isNotAtTab(tabClass).byDefaultTimeout())
        {
            T tab = plugins.pluginTab(tabClass).open();
            assertThat(tab.isOpen(), byDefaultTimeout());
        }
        return this;
    }

    public <T extends PluginsTab<T>> TimedCondition isAtTab(Class<T> tabClass)
    {
        if (isAtPlugins().byDefaultTimeout())
        {
            return plugins.pluginTab(tabClass).isOpen();
        }
        else
        {
            return Conditions.falseCondition();
        }
    }

    public <T extends PluginsTab<T>> TimedCondition isNotAtTab(Class<T> tabClass)
    {
        return not(isAtTab(tabClass));
    }

    public <T extends PluginsTab<T>> PluginsManagement assertIsAtTab(Class<T> tabClass)
    {
        assertThat(isAtTab(tabClass), byDefaultTimeout());
        return this;
    }

    public TimedCondition systemPluginExistsAndEnabled(final String pluginKey)
    {
        return Conditions.dependantCondition(manageExistingTab().systemPlugins().hasPluginComponent(pluginKey),
                new Supplier<TimedCondition>()
                {
                    @Override
                    public TimedCondition get()
                    {
                        return manageExistingTab().systemPlugins().findPluginComponent(pluginKey).now().isEnabled();
                    }
                });
    }

    public TimedCondition checkSystemPluginComponentExistsButDisabled(String pluginKey)
    {
        return Conditions.and(manageExistingTab().systemPlugins().hasPluginComponent(pluginKey),
                manageExistingTab().systemPlugins().findPluginComponent(pluginKey).byDefaultTimeout().isDisabled());
    }


    private void showSystemPlugins()
    {
        assertIsAtTab(ManageExistingPlugins.class);
        if (systemPluginsNotVisible().byDefaultTimeout())
        {
            manageExistingTab().showSystemPlugins();
        }
        assertSystemPluginsVisible();
    }

    private ManageExistingPlugins manageExistingTab()
    {
        return plugins.pluginTab(ManageExistingPlugins.class);
    }

    private TimedCondition systemPluginsVisible()
    {
        return plugins.pluginTab(ManageExistingPlugins.class).isSystemPluginsVisible();
    }

    private TimedCondition systemPluginsNotVisible()
    {
        return not(systemPluginsVisible());
    }

    private PluginsManagement assertSystemPluginsVisible()
    {
        assertThat(systemPluginsVisible(), byDefaultTimeout());
        return this;
    }

    private ManagePluginComponent findPluginComponentOnManageExistingTab(String pluginKey)
    {
        return manageExistingTab().systemPlugins().findPluginComponent(pluginKey).byDefaultTimeout();
    }

    private void expandPlugin(ManagePluginComponent component)
    {
        if (component.isCollapsed().byDefaultTimeout())
        {
            component.expand();
        }
        assertThat(component.isExpanded(), byDefaultTimeout());
    }

    public void disablePluginModule(String pluginKey, String moduleId)
    {
        ManagePluginComponent pluginComponent = goToSystemPluginComponent(pluginKey);
        pluginComponent.toggleExpandModulesList();
    }
}
