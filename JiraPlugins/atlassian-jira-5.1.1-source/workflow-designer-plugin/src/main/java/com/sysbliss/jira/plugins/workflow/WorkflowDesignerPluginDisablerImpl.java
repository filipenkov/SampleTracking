package com.sysbliss.jira.plugins.workflow;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

/**
 * Author: jdoklovic
 */
public class WorkflowDesignerPluginDisablerImpl implements WorkflowDesignerPluginDisabler, LifecycleAware {

    public static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WorkflowDesignerPluginDisablerImpl.class);
    private final PluginAccessor pluginAccessor;

    public WorkflowDesignerPluginDisablerImpl(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    public void onStart() {
        if (pluginAccessor.isPluginEnabled("com.sysbliss.jira.plugins.jira-workflow-designer")) {
            log.info("Removing old JIRA Workflow Designer...");
            Plugin oldJWD = pluginAccessor.getEnabledPlugin("com.sysbliss.jira.plugins.jira-workflow-designer");
            oldJWD.disable();
            oldJWD.uninstall();
        }
    }
}
