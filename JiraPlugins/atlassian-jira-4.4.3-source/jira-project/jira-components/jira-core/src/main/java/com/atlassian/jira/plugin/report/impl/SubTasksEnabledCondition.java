package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;
import com.atlassian.jira.ComponentManager;

/**
 * EnabledCondition that checks whether SubTasks are enabled or not.
 *
 * @since v3.11
 */
public class SubTasksEnabledCondition implements EnabledCondition
{
    public boolean isEnabled()
    {
        return ComponentManager.getInstance().getSubTaskManager().isSubTasksEnabled();
    }
}
