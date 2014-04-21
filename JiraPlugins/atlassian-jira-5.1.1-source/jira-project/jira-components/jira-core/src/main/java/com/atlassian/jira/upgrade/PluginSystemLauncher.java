package com.atlassian.jira.upgrade;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.startup.FailedStartupCheck;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import org.apache.log4j.Logger;

/**
 * Starts the JIRA plugins system with ALL available plugins
 *
 * @since v4.4
 */
public class PluginSystemLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(PluginSystemLauncher.class);

    @Override
    public void start()
    {
        try
        {
            ComponentManager.getInstance().start();
        }
        catch (Exception ex)
        {
            log.fatal("A fatal error occured during initialisation. JIRA has been locked.", ex);
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Component Manager", message));
        }
    }

    @Override
    public void stop()
    {
        // nothing to stop
    }
}
