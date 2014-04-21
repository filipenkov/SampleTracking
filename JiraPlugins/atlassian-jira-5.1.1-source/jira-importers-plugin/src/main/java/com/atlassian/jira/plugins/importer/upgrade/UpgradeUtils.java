package com.atlassian.jira.plugins.importer.upgrade;

import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
import org.apache.log4j.Logger;

public class UpgradeUtils {

    private static final Logger log = Logger.getLogger(UpgradeUtils.class);

    public static void logUpgradeTaskStart(PluginUpgradeTask upgradeTask, Logger log)
    {
        log.info("=========================================");
        log.info("Starting upgrade task (buildNumber=" + upgradeTask.getBuildNumber() + ") : " + upgradeTask.getShortDescription());
    }

    public static void logUpgradeTaskEnd(PluginUpgradeTask upgradeTask, Logger log)
    {
        log.info("Upgrade task finished (buildNumber=" + upgradeTask.getBuildNumber() + ") : " + upgradeTask.getShortDescription());
        log.info("=========================================");
    }

    public static void logUpgradeTaskStart(PluginUpgradeTask upgradeTask, org.slf4j.Logger log)
    {
        log.info("=========================================");
        log.info("Starting upgrade task (buildNumber=" + upgradeTask.getBuildNumber() + ") : " + upgradeTask.getShortDescription());
    }

    public static void logUpgradeTaskEnd(PluginUpgradeTask upgradeTask, org.slf4j.Logger log)
    {
        log.info("Upgrade task finished (buildNumber=" + upgradeTask.getBuildNumber() + ") : " + upgradeTask.getShortDescription());
        log.info("=========================================");
    }

}
