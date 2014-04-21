package com.atlassian.sal.jira.upgrade;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import com.atlassian.sal.core.upgrade.DefaultPluginUpgradeManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// IMPORTANT: JRA-25571: JiraPluginUpgradeManager must implement InitializingBean for now because of ClassLoader issues. Better fix coming in v5.0
public class JiraPluginUpgradeManager extends DefaultPluginUpgradeManager implements LifecycleAware, InitializingBean, DisposableBean
{
    static final String SAL_PLUGIN_KEY = "com.atlassian.sal.jira";

    public JiraPluginUpgradeManager(List<PluginUpgradeTask> upgradeTasks, TransactionTemplate transactionTemplate,
        PluginAccessor pluginAccessor, PluginSettingsFactory pluginSettingsFactory)
    {
        super(upgradeTasks, transactionTemplate, pluginAccessor, pluginSettingsFactory, ComponentAccessor.getPluginEventManager());
    }

    protected Map<String, List<PluginUpgradeTask>> getUpgradeTasks()
    {
        Map<String, List<PluginUpgradeTask>> upgradeTasks = super.getUpgradeTasks();
        List<PluginUpgradeTask> salTasks = upgradeTasks.get(SAL_PLUGIN_KEY);
        if (salTasks == null)
        {
            return upgradeTasks;
        }
        else
        {
            Map<String, List<PluginUpgradeTask>> sortedUpgradeTasks = new LinkedHashMap<String, List<PluginUpgradeTask>>();
            sortedUpgradeTasks.put(SAL_PLUGIN_KEY, salTasks);
            // According to the LinkedHashMap contract, when the sal tasks are reinserted into the map, they will still
            // stay in first position, so the method below is safe.
            sortedUpgradeTasks.putAll(upgradeTasks);
            return sortedUpgradeTasks;
        }
    }
}
