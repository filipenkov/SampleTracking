package com.atlassian.config.util;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.HomeLocator;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletContext;

public class BootstrapUtils
{
    private static final Logger log = Logger.getLogger(BootstrapUtils.class);

    private static ApplicationContext bootstrapContext;
    private static AtlassianBootstrapManager bootstrapManager;

    /**
     * Initialise the bootstrap manager.
     *
     * @param bootstrapContext the Spring bootstrap context
     * @param servletContext the servlet context of the web application
     */
    public static void init(ApplicationContext bootstrapContext, ServletContext servletContext) throws BootstrapException
    {
        // the HomeLocator needs the servletContext in case the home is defined inside a web.xml property (CONF-4054)
        ((HomeLocator)bootstrapContext.getBean("homeLocator")).lookupServletHomeProperty(servletContext);

        setBootstrapContext(bootstrapContext);
        AtlassianBootstrapManager bootstrapManager = getBootstrapManager();
        if (bootstrapManager == null)
            throw new BootstrapException("Could not initialise boostrap manager");

        bootstrapManager.init();

        if (!bootstrapManager.isBootstrapped())
            throw new BootstrapException("Unable to bootstrap application: " + bootstrapManager.getBootstrapFailureReason());
    }

    public static ApplicationContext getBootstrapContext()
    {
        return bootstrapContext;
    }

    public static void setBootstrapContext(ApplicationContext bootstrapContext)
    {
        BootstrapUtils.bootstrapContext = bootstrapContext;
    }

    public static AtlassianBootstrapManager getBootstrapManager()
    {
        if (bootstrapManager == null && bootstrapContext != null)
            bootstrapManager = (AtlassianBootstrapManager) bootstrapContext.getBean("bootstrapManager");

        if (bootstrapManager == null)
        {
            Exception e = new Exception();
            String context = e.getStackTrace().length > 1 ? e.getStackTrace()[1].toString() : "Unknown caller";
            log.warn("Attempting to retrieve bootstrap manager before it is set up: " + context);
        }

        return bootstrapManager;
    }

    public static void setBootstrapManager(AtlassianBootstrapManager bootstrapManager)
    {
        BootstrapUtils.bootstrapManager = bootstrapManager;
    }

    public static void closeContext()
    {
        if (bootstrapContext != null && bootstrapContext instanceof ConfigurableApplicationContext)
        {
            ((ConfigurableApplicationContext) bootstrapContext).close();
        }

        bootstrapContext = null;
        bootstrapManager = null;
    }


}
