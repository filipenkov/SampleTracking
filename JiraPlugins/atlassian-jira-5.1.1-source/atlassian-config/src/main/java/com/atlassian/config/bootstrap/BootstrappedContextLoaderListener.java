package com.atlassian.config.bootstrap;

import com.atlassian.config.spring.BootstrappedContainerContext;
import com.atlassian.config.spring.BootstrappedContextLoader;
import com.atlassian.config.util.BootstrapUtils;
import com.atlassian.spring.container.ContainerContextLoaderListener;
import com.atlassian.spring.container.SpringContainerContext;
import com.atlassian.config.db.DatabaseHelper;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import java.util.Properties;

/**
 * If bootstrapping has been successful, and the application is fully configured, bring up the full Spring application
 * context including Hibernate.
 */
public class BootstrappedContextLoaderListener extends ContainerContextLoaderListener implements ServletContextListener
{
    private static final Logger log = Logger.getLogger(BootstrappedContextLoaderListener.class);

    public boolean canInitialiseContainer()
    {
        AtlassianBootstrapManager bootstrapManager = BootstrapUtils.getBootstrapManager();

        if (bootstrapManager == null)
            return false;

        if (!bootstrapManager.isBootstrapped())
            return false;

        boolean hibernateSetup = bootstrapManager.getHibernateConfig().isHibernateSetup();

        if (!hibernateSetup && BootstrapUtils.getBootstrapManager().isSetupComplete())
        {
            log.error("Hibernate not yet setup, but setup complete - can't initalise container - corrupt project.cfg.xml?");
        }

        return hibernateSetup;
    }

    protected SpringContainerContext getNewSpringContainerContext()
    {
        return new BootstrappedContainerContext();
    }

    /**
     * Make sure the bootstrap context is loaded as this app's parent context
     */
    public ContextLoader createContextLoader()
    {
        return new BootstrappedContextLoader();
    }
}
