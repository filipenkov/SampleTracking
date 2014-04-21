package com.atlassian.jira.startup;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.multitenant.MultiTenantContext;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.atlassian.jira.config.properties.SystemPropertyKeys.JELLY_SYSTEM_PROPERTY;
import static com.atlassian.jira.config.properties.SystemPropertyKeys.JIRA_I18N_RELOADBUNDLES;

/**
 * Listens to Web application startup and shutdown events to check that JIRA is valid to startup, and do whatever clean
 * up may be required on shutdown. <p> When JIRA is not valid to start, then the <code>JiraStartupChecklistFilter</code>
 * will disallow access to JIRA. </p>
 *
 * @see JiraStartupChecklist
 * @see JiraStartupChecklistFilter
 * @since v4.0
 */
public class ChecklistLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(ChecklistLauncher.class);

    public void start()
    {
        try
        {
            setJiraDevMode();
            runStartupChecks(ServletContextProvider.getServletContext());
            initialiseJiraApi();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during ChecklistLauncher initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during ChecklistLauncher initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    public void stop()
    {
        JiraStartupChecklist.stop();

        removeAnyJohnsonEvents();

        //cleanup any temporary attachments that for some reason did not get cleaned up when real attachments
        //got created or when session timeouts happened unbinding the {@link com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor}
        deleteTemporaryAttachmentsDir();

        //on shutdown remove the JUL-to-SLF4JBridgeHandler, to make sure we wont get NPE exceptions when
        //Tomcat nulls out static Logger instances on shutdown and SLF4J is trying to pass JUL logging along
        //to log4j.
        SLF4JBridgeHandler.uninstall();

        // We need to manually manage the lifecycle of the JiraStartupChecklist during Multitenancy so we have to
        // explicitly cleanup here after we have finished stopping.
        JiraStartupChecklist.destroyTenant(MultiTenantContext.getTenantReference().get());
    }

    private void removeAnyJohnsonEvents()
    {
        // this works around a problem in Johnson where there is in fact a static variable of events
        // that can resurrect itself
        JohnsonEventContainer container = JohnsonEventContainer.get(ServletContextProvider.getServletContext());
        @SuppressWarnings ({ "unchecked" })
        Collection<Event> events = container.getEvents();
        for (Event event : events)
        {
            container.removeEvent(event);
        }
    }

    private void runStartupChecks(final ServletContext servletContext)
    {
        JiraStartupLogger.log().info("Running JIRA startup checks.");
        if (JiraStartupChecklist.startupOK())
        {
            JiraStartupLogger.log().info("JIRA pre-database startup checks completed successfully.");
        }
        else
        {
            JiraStartupLogger.log().fatal("Startup check failed. JIRA will be locked.");
            // Lock the DB for ANY of the checks failing.
            GenericDelegator.lock();
            // TODO: Do we really need to raise a Johnson Event? We use our own filter to stop HTTP access.
            // This code was copied from the original "DatabaseCompatibilityEnforcer" Listener
            // Add a Johnson event to stop any other ServletContextListener's from trying to do anything.
            Event event = new Event(
                    EventType.get(JiraStartupChecklist.getFailedStartupCheck().getName()),
                    JiraStartupChecklist.getFailedStartupCheck().getFaultDescription(),
                    EventLevel.get(EventLevel.ERROR));
            JohnsonEventContainer.get(servletContext).addEvent(event);
        }
    }

    private void initialiseJiraApi()
    {
        // Initialise the ComponentAccessor with a Worker - this insulates the API from the implementation classes.
        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());
    }

    private void setJiraDevMode()
    {
        if (JiraSystemProperties.isDevMode())
        {
            // turn off minification of web resources, available from plugins 2.3 onwards
            setDefault("atlassian.webresource.disable.minification", "true");

            // disable mail
            setDefault("atlassian.mail.senddisabled", "true");
            setDefault("jira.trackback.senddisabled", "true");
            setDefault("atlassian.mail.fetchdisabled", "true", "atlassian.mail.popdisabled");

            // disable caches
            setDefault("com.atlassian.gadgets.dashboard.ignoreCache", "true");
            setDefault("atlassian.disable.caches", "true");

            // turn on jelly
            setDefault(JELLY_SYSTEM_PROPERTY, "true");

            //jira dev mode should also set atlassian.dev.mode to true if it isn't already set!
            setDefault(SystemPropertyKeys.ATLASSIAN_DEV_MODE, "true");

            setDefault(SystemPropertyKeys.WEBSUDO_IS_DISABLED, "true");

            setDefault(SystemPropertyKeys.SHOW_PERF_MONITOR, "true");

            // turn on i18n reload
            setDefault(JIRA_I18N_RELOADBUNDLES, "true");
        }
    }

    private void setDefault(String key, String value, String... relatedKeys)
    {
        if (System.getProperty(key) != null)
        {
            log.debug("Trying to set already defined system property '" + key + "' to '" + value + "' because development mode is on. Leaving as current value '" + System.getProperty(key) + "'.");
            return;
        }
        else if (relatedKeys != null)
        {
            for (String relatedKey : relatedKeys)
            {
                final String sysVal = System.getProperty(relatedKey);
                if (sysVal != null)
                {
                    final String mesg = "Trying to set system property '" + key + "' to '" + value + "' because development mode is on. But related property '" + relatedKey + "' is already set to '" + sysVal + "'. So not setting.";
                    if (sysVal.equals(value))
                    {
                        log.debug(mesg);
                    }
                    else
                    {
                        log.warn(mesg);
                    }
                    return;
                }
            }
        }
        log.info("Setting system property '" + key + "' to '" + value + "' for development mode.");
        System.setProperty(key, value);
    }

    /**
     * Deletes the temporary attachments directory.
     */
    private void deleteTemporaryAttachmentsDir()
    {
        try
        {
            File attachmentDirectory = AttachmentUtils.getTemporaryAttachmentDirectory();
            try
            {
                FileUtils.deleteDirectory(attachmentDirectory);
            }
            catch (IOException e)
            {
                log.error("Error (" + e.getMessage() + ") deleting temporary attachments directory '" + attachmentDirectory + "' on shutdown. Ignoring since this is not required.");
            }
        }
        catch (Throwable t)
        {
            log.warn("Couldn't delete temporary attachments directory", t);
        }
    }
}
