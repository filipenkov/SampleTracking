package com.atlassian.jira.startup;

import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformationFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.scheduler.JiraSchedulerLauncher;
import com.atlassian.jira.upgrade.ConsistencyCheckImpl;
import com.atlassian.jira.upgrade.PluginSystemLauncher;
import com.atlassian.jira.upgrade.PluginUpgradeLauncher;
import com.atlassian.jira.upgrade.UpgradeLauncher;
import com.atlassian.jira.util.devspeed.JiraDevSpeedTimer;
import com.atlassian.jira.web.ServletContextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This implementation of JiraLauncher contains all of the smarts of what to start in which order to ensure that JIRA
 * starts properly. These used to be servlet context-listeners but in a multitenant world JIRA "startup" is no longer
 * tied to context creation. So the logic needs to be managed differently. Which is where the DefaultJiraLauncher steps
 * in.
 *
 * @since v4.3
 */
public class DefaultJiraLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(DefaultJiraLauncher.class);

    private final ChecklistLauncher startupChecklist;
    private final BootstrapContainerLauncher bootstrapContainerLauncher;
    private final ComponentContainerLauncher componentContainerLauncher;
    private final UpgradeLauncher upgradeLauncher;
    private final JiraSchedulerLauncher schedulerLauncher;
    private final DatabaseLauncher databaseLauncher;
    private final PluginSystemLauncher pluginSystemLauncher;
    private final ConsistencyCheckImpl consistencyChecker;
    private final SystemInfoLauncher systemInfoLauncher;
    private final PluginUpgradeLauncher pluginUpgradeLauncher;

    public DefaultJiraLauncher()
    {
        this.startupChecklist = new ChecklistLauncher();
        this.bootstrapContainerLauncher = new BootstrapContainerLauncher();
        this.componentContainerLauncher = new ComponentContainerLauncher();
        this.upgradeLauncher = new UpgradeLauncher();
        this.schedulerLauncher = new JiraSchedulerLauncher();
        this.databaseLauncher = new DatabaseLauncher();
        this.pluginSystemLauncher = new PluginSystemLauncher();
        this.consistencyChecker = new ConsistencyCheckImpl();
        this.systemInfoLauncher = new SystemInfoLauncher();
        this.pluginUpgradeLauncher = new PluginUpgradeLauncher();
    }

    @Override
    public void start()
    {
        JiraDevSpeedTimer.run(getStartupName(), new Runnable()
        {
            public void run()
            {
                preDbLaunch();
                postDbLaunch();
            }
        });
    }

    /**
     * Things that can or must be done before the database is configured.
     */
    private void preDbLaunch()
    {
        systemInfoLauncher.start();
        startupChecklist.start();
        bootstrapContainerLauncher.start();
    }

    /**
     * Those things that need the database to have been configured.
     */
    private void postDbLaunch()
    {
        if (JiraStartupChecklist.startupOK())
        {
            final DatabaseConfigurationManager dbcm = ComponentAccessor.getComponentOfType(DatabaseConfigurationManager.class);

            dbcm.doNowOrWhenDatabaseConfigured(new Runnable()
            {
                @Override
                public void run()
                {
                    new DatabaseChecklistLauncher(dbcm, ServletContextProvider.getServletContext()).start();
                }
            }, "Database Checklist Launcher");

            dbcm.doNowOrWhenDatabaseActivated(new Runnable()
            {
                @Override
                public void run()
                {
                    componentContainerLauncher.start();
                    databaseLauncher.start();
                    pluginSystemLauncher.start();
                    consistencyChecker.initialise(ServletContextProvider.getServletContext());
                    upgradeLauncher.start();
                    pluginUpgradeLauncher.start();
                    schedulerLauncher.start();
                }

            }, "Post database-configuration launchers");
        }
    }

    @Override
    public void stop()
    {
        log.info("Stopping launchers");
        schedulerLauncher.stop();
        pluginUpgradeLauncher.stop();
        upgradeLauncher.stop();
        consistencyChecker.destroy(ServletContextProvider.getServletContext());
        pluginSystemLauncher.stop();
        databaseLauncher.stop();
        componentContainerLauncher.stop();
        bootstrapContainerLauncher.stop();
        startupChecklist.stop();
        systemInfoLauncher.stop();

    }

    private String getStartupName()
    {
        final String jvmInputArguments = StringUtils.defaultString(RuntimeInformationFactory.getRuntimeInformation().getJvmInputArguments());
        final boolean rebel = jvmInputArguments.contains("jrebel.jar");
        final boolean debugMode = jvmInputArguments.contains("-Xdebug");

        return new StringBuilder("jira.startup")
                .append(debugMode ? ".debug" : ".run")
                .append(rebel ? ".jrebel" : "").toString();
    }
}
