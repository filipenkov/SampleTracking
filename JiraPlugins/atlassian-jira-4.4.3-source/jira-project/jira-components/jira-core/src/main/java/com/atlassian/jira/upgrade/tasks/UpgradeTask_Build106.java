package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericValue;

/**
 * Fix for JRA-7996, clean out references to default resolutions that no longer exist in the system.
 */
public class UpgradeTask_Build106 extends AbstractUpgradeTask
{
    private final PropertiesManager propertiesManager;

    public UpgradeTask_Build106(PropertiesManager propertiesManager)
    {
        this.propertiesManager = propertiesManager;
    }

    public String getBuildNumber()
    {
        return "106";
    }

    public String getShortDescription()
    {
        return "If there is an entry for a default resolution that does not exist then remove it.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        if (propertiesManager.getPropertySet().exists(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION))
        {
            // Get the default resolution
            String defaultResolution = propertiesManager.getPropertySet().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);

            // Try to resolve the resolution for the default value
            GenericValue resolution = ManagerFactory.getConstantsManager().getResolution(defaultResolution);
            if(resolution == null)
            {
                // remove the property
                propertiesManager.getPropertySet().remove(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
            }
        }
    }
}
