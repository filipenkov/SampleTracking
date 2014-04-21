package com.atlassian.jira.plugins.monitor;

import com.atlassian.jira.plugin.webfragment.conditions.IsOnDemandCondition;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsSysAdminCondition;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * @since v5.1
 */
public class DisplayMonitoringCondition implements Condition
{
    private final UserIsSysAdminCondition isSysAdmin;
    private final IsOnDemandCondition isOnDemand;
    private final MonitoringFeature monitoring;

    public DisplayMonitoringCondition(MonitoringFeature monitoring)
    {
        this.monitoring = monitoring;
        isSysAdmin = JiraUtils.loadComponent(UserIsSysAdminCondition.class);
        isOnDemand = JiraUtils.loadComponent(IsOnDemandCondition.class);
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        isSysAdmin.init(params);
        isOnDemand.init(params);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return monitoring.enabled() && isSysAdmin.shouldDisplay(context);
    }
}
