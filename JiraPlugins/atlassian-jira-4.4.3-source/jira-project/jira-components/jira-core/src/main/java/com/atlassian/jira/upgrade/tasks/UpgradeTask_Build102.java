package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;

public class UpgradeTask_Build102 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build102.class);
    private final ConstantsManager constantsManager;

    public UpgradeTask_Build102(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public String getBuildNumber()
    {
        return "102";
    }

    public String getShortDescription()
    {
        return "Sets a default resolution if there isn't already one";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        String defaultResolution = getApplicationProperties().getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION);
        if (StringUtils.isBlank(defaultResolution))
        {
            Collection resolutionObjects = constantsManager.getResolutionObjects();
            if (resolutionObjects != null && !resolutionObjects.isEmpty())
            {
                Resolution resolution = (Resolution) resolutionObjects.iterator().next();
                getApplicationProperties().setString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION, resolution.getId());
            }
        }

    }

}
