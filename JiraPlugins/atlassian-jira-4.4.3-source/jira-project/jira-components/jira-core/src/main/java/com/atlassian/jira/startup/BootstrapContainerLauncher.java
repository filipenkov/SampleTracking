package com.atlassian.jira.startup;


import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.webwork.WebworkConfigurator;
import org.apache.log4j.Logger;

/**
 * The BootstrapContainerLauncher will bootstrap enough of JIRA during run level 0.  It has the ability to check the
 * state of the database connection and configure it if need be.  If the database is operational then it will move to
 * the next level and start JIRA completely.
 *
 * @see ComponentContainerLauncher
 */
public class BootstrapContainerLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(BootstrapContainerLauncher.class);

    public void start()
    {
        try
        {
            bootstrapJIRA();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during BootstrapContainerLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during BootstrapContainerLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    private void bootstrapJIRA()
    {
        try
        {
            //
            // builds enough of the PICO world to allow JIRA to be setup without a database
            ComponentManager.getInstance().buildBootstrapPICOContainer();
            if (JiraStartupChecklist.startupOK())
            {

                //
                // if the database is not setup then we need the plugins system.  However if it is already setup then we can skip this step
                // as save ourselves some time by skipping the bootstrap plugins system
                //
                DatabaseConfigurationManager dbcm = ComponentManager.getComponent(DatabaseConfigurationManager.class);
                if (!dbcm.isDatabaseSetup())
                {
                    ComponentManager.getInstance().bootstrapPluginsSystem();

                    // we need to have configuration setup for webwork here
                    WebworkConfigurator.setupConfiguration();
                }
            }
        }
        catch (Exception ex)
        {
            log.fatal("A fatal error occurred during bootstrapping. JIRA has been locked.", ex);
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Component Manager", message));
        }
    }

    public void stop()
    {
        // nothing to stop
    }
}
