package com.atlassian.jira.startup;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.database.DatabaseConfigHandler;
import com.atlassian.jira.multitenant.JiraMultiTenantAuthorisationProvider;
import com.atlassian.jira.studio.startup.StudioStartupHooks;
import com.atlassian.jira.studio.startup.StudioStartupHooksLocator;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantComponentMapBuilder;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.MultiTenantDestroyer;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.quartz.SystemThreadPoolController;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.SchedulerConfigException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * We used to have a bunch of context-listeners defined in web.xml that were in charge of various part of the "startup"
 * of JIRA. Things like checking for database consistency, performing an upgrade, starting Quartz. In the multitenant
 * world you can longer do that kind of stuff per-context. Instead it needs to happen per-tenant: when a tenant starts
 * up you need to check *its* database.
 * <p/>
 * On startup we still need to perform initialization. Except now it also needs to be smart enough to handle per-tenant
 * initialization as well. All of those context-listeners have been collapsed into a single context-listener that can
 * handle this Brave New World.
 *
 * @since v4.3
 */
public class LauncherContextListener implements ServletContextListener
{
    private static final Logger log = Logger.getLogger(LauncherContextListener.class);
    private static final String LOG4J = "log4j.properties";

    /**
     * JRA-24138: This field is necessary so that we keep a hard reference to the launcher. If we don't it can be
     * garbage collected before the setup (and shutdown) is called which will lead to NPEs.
     */
    private volatile MultiTenantComponentMap<JiraLauncher> launcherMap;

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        try
        {
            log.debug("Launching JIRA");

            StudioStartupHooks startupHooks = StudioStartupHooksLocator.getStudioStartupHooks();
            configureLog4j(startupHooks);
            startupHooks.beforeJiraStart();
            initMultiTenantSystem();

            MultiTenantComponentMapBuilder<JiraLauncher> builder = MultiTenantContext.getFactory().createComponentMapBuilder(new MultiTenantCreator<JiraLauncher>()
            {
                @Override
                public JiraLauncher create(Tenant tenant)
                {
                    JiraLauncher launcher = new DefaultJiraLauncher();
                    launcher.start();
                    return launcher;
                }
            });
            builder.setDestroyer(new MultiTenantDestroyer<JiraLauncher>()
            {
                @Override
                public void destroy(Tenant tenant, JiraLauncher instance)
                {
                    try
                    {
                        MultiTenantContext.getTenantReference().set(tenant, true);
                        instance.stop();
                        ComponentManager.stopTenant(tenant);
                    }
                    finally
                    {
                        MultiTenantContext.getTenantReference().remove();
                    }
                }
            });
            builder.setLazyLoad(MultiTenantComponentMap.LazyLoadStrategy.EAGER_AFTER_STARTUP);
            //We need to assign the map to a hard reference as the .construct() only keeps a soft reference to the
            //returned map and we want it to exist for the life of JIRA.
            launcherMap = builder.construct();

            log.debug("Starting all tenants");
            // Start all tenants, this will start the system tenant first
            MultiTenantContext.getController().startAll();
            log.debug("Tenants started");

            startupHooks.afterJiraStart();
        }
        catch (RuntimeException e)
        {
            log.error("Unable to start JIRA.", e);
            throw e;
        }
        catch (Error e)
        {
            log.error("Unable to start JIRA.", e);
            throw e;
        }
    }

    private void initMultiTenantSystem()
    {
        log.debug("Initing multitenant system");
        Properties props = new Properties();
        // These could go into a multitenant.properties file, but here they are hidden so that people poking around
        // aren't tempted to play
        props.setProperty(MultiTenantContext.SYSTEM_TENANT_PROVIDER_KEY, SystemTenantProvider.class.getName());
        props.setProperty(MultiTenantContext.MULTI_TENANT_ENABLED_KEY, Boolean.TRUE.toString());
        props.setProperty(MultiTenantContext.SINGLE_TENANT_KEY, Boolean.TRUE.toString());
        props.setProperty(MultiTenantContext.MULTI_TENANT_AUTHORISATION_PROVIDER_KEY, JiraMultiTenantAuthorisationProvider.class.getName());
        props.setProperty("multitenant.handler.database", DatabaseConfigHandler.class.getName());

        // Now we get the MultiTenant subsystem to load the properties so that any existing multitenant.properties file
        // or system properties can override our configuration
        props.putAll(MultiTenantContext.loadProperties());

        // Now init the multitenant system
        MultiTenantContext.defaultInit(props);

        // This must be done prior to any tenants starting up, it creates the shared quartz thread pool
        try
        {
            // These magic numbers mean quartz gets 2 threads, and their priority is 4
            SystemThreadPoolController.getInstance().initialise(2, 4);
        }
        catch (SchedulerConfigException e)
        {
            throw new RuntimeException(e);
        }
        log.debug("Multitenant system initialised");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        //JRADEV-9155: The launcher needs to be destroyed first so that plugins are shutdown first. Normally this
        // map is destroyed last which is much too late. Plugins will have references to JIRA components which are
        // actually proxies around MultiTenantComponentMaps. If we let launcherMap get destroyed last then the JIRA
        // compontents that plugins are using will be destoryed before those plugins have been brought down.
        
        //NOTE: This code only works for the system tenant. When I did this multi-tenancy was completely broken.
        launcherMap.destroy();
        MultiTenantContext.getController().stopAll();
        SystemThreadPoolController.getInstance().shutdown();
    }

    private void configureLog4j(StudioStartupHooks startupHooks)
    {
        Properties properties = new Properties();
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream(LOG4J);
        if (resource != null)
        {
            try
            {
                properties.load(resource);
            }
            catch (IOException e)
            {
                log.warn("Unable read current log4j configuration. Assuming blank configuration.", e);
            }
            finally
            {
                IOUtils.closeQuietly(resource);
            }
        }
        else
        {
            log.warn("Unable to find '" + LOG4J + "' on class path.");
        }

        Properties newConfig = startupHooks.getLog4jConfiguration(properties);
        if (newConfig != null)
        {
            PropertyConfigurator.configure(newConfig);
        }
    }
}
