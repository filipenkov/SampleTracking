package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

public class UpgradeTask_Build105 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build105.class);
    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build105(PropertiesManager propertiesManager)
    {
        this.propertiesManager = propertiesManager;
    }

    public String getBuildNumber()
    {
        return "105";
    }

    public String getShortDescription()
    {
        return "Increase max size of announcement banner";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        if (propertiesManager.getPropertySet().exists(APKeys.JIRA_ALERT_HEADER))
        {
            int headerType = propertiesManager.getPropertySet().getType(APKeys.JIRA_ALERT_HEADER);
            if (PropertySet.STRING == headerType)
            {
                String header = propertiesManager.getPropertySet().getString(APKeys.JIRA_ALERT_HEADER);
                propertiesManager.getPropertySet().remove(APKeys.JIRA_ALERT_HEADER);
                propertiesManager.getPropertySet().setText(APKeys.JIRA_ALERT_HEADER, header);
            }
            else
            {
                log.warn("Alert header is of unexpected type " + headerType + "; ignoring");
            }
        }

    }

}
