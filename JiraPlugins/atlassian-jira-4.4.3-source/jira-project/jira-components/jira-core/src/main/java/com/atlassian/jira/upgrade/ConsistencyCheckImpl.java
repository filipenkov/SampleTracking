package com.atlassian.jira.upgrade;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.startup.JiraStartupLogger;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.CompositeShutdown;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.system.check.PluginVersionCheck;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.web.ContextKeys;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionSupport;
import webwork.dispatcher.ActionResult;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The consistency checker runs every time the web app is reloaded, and checks JIRA's consistency (duh ;))
 * <p/>
 * At the moment it just looks to check that certain Listeners are loaded, but in the future it can check the
 * consistency of data etc.
 */
public class ConsistencyCheckImpl implements ConsistencyChecker
{
    private static final Logger log = Logger.getLogger(ConsistencyCheckImpl.class);
    private final JiraStartupLogger startupLog = new JiraStartupLogger();

    static final String INIT_KEY = ConsistencyCheckImpl.class.getName() + ":initialized";

    public void destroy(final ServletContext context)
    {
        final Boolean initialized = (Boolean) context.getAttribute(INIT_KEY);
        if (initialized == null || !initialized)
        {
            return;
        }

        // make sure all services clean up after themselves
        final Collection<JiraServiceContainer> services = getServices();

        for (final Object element : services)
        {
            final JiraServiceContainer service = (JiraServiceContainer) element;
            try
            {
                service.destroy();
            }
            catch (final RuntimeException e)
            {
                log.error(
                    "Failed to destroy service '" + ((service != null) && (service.getName() != null) ? service.getName() : "Unknown") + "' " + e.getMessage(),
                    e);
            }
        }
        try
        {
            getShutdown().shutdown();
        }
        catch (final RuntimeException e)
        {
            log.error("Failed to run shutdown hooks.", e);
        }
    }

    private Shutdown getShutdown()
    {
        return new CompositeShutdown(getIndexManagerShutdown(), getComponentManager(), new Shutdown()
                {
                    public void shutdown()
                    {
                        getTaskManager().shutdownAndWait(0);
                    }
                });
    }

    /*
     * overridden in tests
     */
    Shutdown getComponentManager()
    {
        return ComponentManager.getInstance();
    }

    /*
     * overridden in tests
     */
    IndexLifecycleManager getIndexManager()
    {
        return ComponentManager.getComponent(IndexLifecycleManager.class);
    }

    /**
     * The shutdown instance used to shutdown the index manager. Needed to handle very rare exceptions.
     *
     * @return a shutdownable
     */
    private Shutdown getIndexManagerShutdown()
    {
        try
        {
            return getIndexManager();
        }
        catch (final RuntimeException e)
        {
            log.error("Failed to get IndexManager, cannot shut it down cleanly...", e);
            return new Shutdown()
            {
                public void shutdown()
                {}

                @Override
                public String toString()
                {
                    return "NullShutdownForIndexManager";
                }
            };
        }
    }

    /*
     * overridden in tests
     */
    TaskManager getTaskManager()
    {
        return ComponentManager.getComponent(TaskManager.class);
    }

    /**
     * Gets all the currently registered services with JIRA.
     *
     * @return Unmodifiable collection of {@link com.atlassian.jira.service.JiraServiceContainer}
     * @see com.atlassian.jira.service.ServiceManager#getServices()
     */
    protected Collection<JiraServiceContainer> getServices()
    {
        return ComponentManager.getComponent(ServiceManager.class).getServices();
    }

    public void initialise(final ServletContext servletContext)
    {
        setStartupTime(servletContext);

        checkConsistency(servletContext);
        checkAndInitLucene(servletContext);
        new PluginVersionCheck(ComponentManager.getComponentInstanceOfType(PluginAccessor.class), ComponentManager.getComponentInstanceOfType(BuildUtilsInfo.class)).check(servletContext);

        printJIRAStartupMessage(servletContext);

        ServletContextProvider.getServletContext().setAttribute(INIT_KEY, Boolean.TRUE);
    }

    private void printJIRAStartupMessage(final ServletContext context)
    {
        // Print successful start up message if there are no events in the context.
        final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
        if ((eventCont == null) || !eventCont.hasEvents())
        {
            // Finished loading JIRA
            startupLog.printStartedMessage();
        }

        // We do this after JIRA has started up so that these warnings come last (and are thus still visible to the
        // admin. If we printed it before printStartedMessage() then they would get scrolled way off the screen
        // making it unlikely most people would ever see them.
        checkSystemEnvironment();
    }

    public void checkConsistency(final ServletContext context)
    {
        // check consistency
        try
        {
            log.debug("Checking JIRA consistency");

            final boolean connection = checkConnection(context);

            if (connection)
            {
                checkDataConsistency();
            }
        }
        catch (final Throwable e)
        {
            log.error("Exception during consistency check: " + e, e);
        }
    }

    public void checkDataConsistency() throws Exception
    {
        checkMailListenerAndService();
        checkIssueAssignHistoryListener();
        checkAttachmentPath();
        checkIndexingSetup();
        checkLanguageExists();
        checkAndInitSID();
    }

    private void checkSystemEnvironment()
    {
        final List<String> messages = SystemEnvironmentChecklist.getEnglishWarningMessages();

        for (String message : messages)
        {
            startupLog.printMessage(message, Level.WARN);
        }
    }

    /**
     * Looks for files that could be Lucene locks left after an unclean shutdown. Registers a Johnson Event in this
     * scenario.
     */
    private void checkAndInitLucene(final ServletContext context)
    {
        final ApplicationProperties ap = ManagerFactory.getApplicationProperties();

        if (ap.getOption(APKeys.JIRA_OPTION_INDEXING))
        {
            // Get a path for each index directory
            final IndexLifecycleManager indexManager = getIndexManager();

            // A collection to which we will add all found lock files (if any)
            final Collection<String> existingLockFilepaths = LuceneUtils.getStaleLockPaths(indexManager.getAllIndexPaths());

            // If there were any lock files found, then place an event into the context. Otherwise we are OK and
            // can proceed.
            if ((existingLockFilepaths != null) && !existingLockFilepaths.isEmpty())
            {
                final StringBuffer sb = new StringBuffer();
                for (final String filePath : existingLockFilepaths)
                {
                    if (filePath != null)
                    {
                        sb.append(filePath).append(' ');
                    }
                }

                if (sb.length() > 1)
                {
                    // Delete last " "
                    sb.deleteCharAt(sb.length() - 1);
                }

                // Log error message
                final Collection<String> messages = CollectionBuilder.newBuilder(
                        "Index lock file(s) found. This occurs either because JIRA was not cleanly shutdown",
                        "or because there is another instance of this JIRA installation currently running.",
                        "Please ensure that no other instance of this JIRA installation is running",
                        "and then remove the following lock file(s) and restart JIRA:", "", sb.toString(), "",
                        "Once restarted you will need to reindex your data to ensure that indexes are up to date.", "",
                        "Do NOT delete the lock file(s) if there is another JIRA running with the same index directory",
                        "instead cleanly shutdown the other instance.").asList();
                startupLog.printMessage(messages, Level.ERROR);

                final Event event = new Event(EventType.get("index-lock-already-exists"), "An existing index lock was found.",
                        EventLevel.get(EventLevel.ERROR));
                event.addAttribute("lockfiles", sb.toString());
                final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
                if (eventCont != null)
                {
                    eventCont.addEvent(event);
                }
            }
        }

        // Set max clauses even if indexing is disabled so that it takes affect when indexing is enabled again.
        // As max clauses is a static variable in Lucene, this will work.
        int maxClausesCount = 65000;

        try
        {
            maxClausesCount = Integer.parseInt(ap.getDefaultBackedString(APKeys.JIRA_SEARCH_MAXCLAUSES));
        }
        catch (final NumberFormatException e)
        {
            log.warn("Could not read the property '" + APKeys.JIRA_SEARCH_MAXCLAUSES + "' for the number of maximum search clauses. Using default " + maxClausesCount);
        }

        BooleanQuery.setMaxClauseCount(maxClausesCount); // Fixes JRA-3127 (JT)
    }

    /**
     * Rather than create an upgrade task, just ensure that the language has been set, or else set it to English.
     */
    private void checkLanguageExists()
    {
        final ApplicationProperties ap = ManagerFactory.getApplicationProperties();

        if (ap.getString(APKeys.JIRA_I18N_LANGUAGE_INPUT) == null)
        {
            log.info("Input Language has not been set.  Setting to 'English'");
            ap.setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, APKeys.Languages.ENGLISH);
        }
    }

    private boolean checkConnection(final ServletContext context)
    {
        boolean worked = true;

        try
        {
            final GenericDelegator delegator = CoreFactory.getGenericDelegator();

            if (delegator == null)
            {
                log.error("Could not get GenericDelegator");
                worked = false;
            }
            else
            {
                try
                {
                    delegator.findAll("Project");
                }
                catch (final GenericEntityException e)
                {
                    log.error("Could not connect to database. Check your entityengine.xml settings: " + e, e);
                    worked = false;

                    //Add an error that you could not connect to the database
                    final Event event = new Event(EventType.get("database"), "Could not connect to database", e.getMessage(),
                            EventLevel.get(EventLevel.ERROR));
                    final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
                    if (eventCont != null)
                    {
                        eventCont.addEvent(event);
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            log.error("Could not check database connection. Check your entityengine.xml settings: " + t, t);
            worked = false;

            //Add an error that you could not connect to the database
            final Event event = new Event(EventType.get("database"), "Could not connect to database.", t.getMessage(),
                    EventLevel.get(EventLevel.ERROR));
            final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
            if (eventCont != null)
            {
                eventCont.addEvent(event);
            }
        }

        return worked;
    }

    private void checkMailListenerAndService() throws Exception
    {
        ensureSingleListener("com.atlassian.jira.event.listeners.mail.MailListener", "Mail Listener");
        ensureSingleService("com.atlassian.jira.service.services.mail.MailQueueService", "Mail Queue Service");
    }

    private void checkIssueAssignHistoryListener() throws Exception
    {
        ensureSingleListener("com.atlassian.jira.event.listeners.history.IssueAssignHistoryListener", "Issue Assignment Listener");
    }

    private void checkAttachmentPath() throws Exception
    {
        final ApplicationProperties ap = ManagerFactory.getApplicationProperties();

        if (ap.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS))
        {
            final String attachmentPath = ComponentAccessor.getAttachmentPathManager().getAttachmentPath();

            if ((attachmentPath == null) || !directoryExists(attachmentPath))
            {
                log.error("Attachments are turned on, but attachment path [" + attachmentPath + "] invalid - disabling attachments");
                ap.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, false);
            }
        }
    }

    /**
     * Checks that the index path exists, and the IndexingListener exists
     */
    private void checkIndexingSetup() throws Exception
    {
        final ApplicationProperties ap = ManagerFactory.getApplicationProperties();

        if (ap.getOption(APKeys.JIRA_OPTION_INDEXING))
        {
            final String indexPath = ComponentAccessor.getIndexPathManager().getIndexRootPath();

            if (!directoryExists(indexPath))
            {
                log.error("Indexing is turned on, but index path [" + indexPath + "] invalid - disabling indexing");
                removeListeners(IssueIndexListener.class.getName());

                //set option to false
                ap.setOption(APKeys.JIRA_OPTION_INDEXING, false);
                return;
            }
            // ensure the indexing listener is there
            ensureSingleListener(IssueIndexListener.class.getName(), "Issue Index Listener");
        }
    }

    /**
     * Returns true if a given path exists and is a directory
     */
    private boolean directoryExists(final String path)
    {
        if (path != null)
        {
            final File dir = new File(path);
            if (dir.exists() && dir.isDirectory())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks through all the listeners, and ensures that there is only one of the given listener.
     * <p/>
     * If there is more than one, the extras are deleted. If there aren't any, one is added with the given class and
     * name
     */
    private void ensureSingleListener(final String clazz, final String name) throws Exception
    {
        // check that there is one and only one cache listener
        @SuppressWarnings ("unchecked")
        final Collection<GenericValue> listenerConfigs = CoreFactory.getGenericDelegator().findAll("ListenerConfig");

        boolean foundOne = false;

        final List<GenericValue> toRemove = new ArrayList<GenericValue>();

        for (final Object element : listenerConfigs)
        {
            final GenericValue listenerConfig = (GenericValue) element;

            if (listenerConfig.getString("clazz").equals(clazz))
            {
                if (foundOne)
                {
                    // we already found one - delete this listener
                    toRemove.add(listenerConfig);
                }
                else
                {
                    foundOne = true;
                }
            }
        }

        if (!foundOne)
        {
            //if application setup - then send an error, else just @ info level
            if ("true".equals((ManagerFactory.getApplicationProperties()).getString(APKeys.JIRA_SETUP)))
            {
                log.error("Could not find " + name + ", adding.");
            }
            else
            {
                log.info("Could not find " + name + ", adding.");
            }

            // add new cache listener
            final ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.LISTENER_CREATE,
                    EasyMap.build("name", name, "clazz", clazz));

            try
            {
                ActionUtils.checkForErrors(aResult);
            }
            catch (final Exception e)
            {
                if (((ActionSupport) aResult.getFirstAction()).getHasErrorMessages())
                {
                    @SuppressWarnings ("unchecked")
                    final Collection<String> errorMessages = ((ActionSupport) aResult.getFirstAction()).getErrorMessages();
                    for (final String errorMsg : errorMessages)
                    {
                        log.error("Error adding listener: " + errorMsg);
                    }
                }
                else
                {
                    log.error("Error adding listener: " + e, e);
                }
            }
            ManagerFactory.getListenerManager().refresh();
        }
        else if (toRemove.size() > 0)
        {
            log.debug("Removing " + toRemove.size() + " extra listeners with class " + clazz);
            CoreFactory.getGenericDelegator().removeAll(toRemove);
            ManagerFactory.getListenerManager().refresh();
        }
    }

    /**
     * Looks through all the services, and ensures that there is only one of the given service.
     * <p/>
     * If there is more than one, the extras are deleted. If there aren't any, one is added with the given class and
     * name
     */
    private void ensureSingleService(final String clazz, final String name) throws Exception
    {
        final Collection<JiraServiceContainer> serviceConfigs = getServices();

        boolean foundOne = false;

        final List<JiraServiceContainer> toRemove = new ArrayList<JiraServiceContainer>();

        for (final JiraServiceContainer service : serviceConfigs)
        {
            if (service.getServiceClass().equals(clazz))
            {
                if (foundOne)
                {
                    // we already found one - delete this service
                    toRemove.add(service);
                }
                else
                {
                    foundOne = true;
                }
            }
        }

        if (!foundOne)
        {
            //if application setup - then send an error, else just @ info level
            if ("true".equals((ManagerFactory.getApplicationProperties()).getString(APKeys.JIRA_SETUP)))
            {
                log.error("Could not find " + name + ", adding.");
            }
            else
            {
                log.info("Could not find " + name + ", adding.");
            }

            // add new service
            try
            {
                ManagerFactory.getServiceManager().addService(name, clazz, DateUtils.MINUTE_MILLIS);
            }
            catch (final Exception e)
            {
                log.error("Error adding service: " + e, e);
            }

        }
        else if (!toRemove.isEmpty())
        {
            log.debug("Removing " + toRemove.size() + " extra services with class " + clazz);
            for (final Object element : toRemove)
            {
                final JiraServiceContainer serviceContainer = (JiraServiceContainer) element;
                ManagerFactory.getServiceManager().removeService(serviceContainer.getId());
            }
        }
    }

    /**
     * Stores the system startup time in the context so that system uptime can be computed and displayed on system info
     * page
     */
    private void setStartupTime(final ServletContext context)
    {
        if (context != null)
        {
            context.setAttribute(ContextKeys.STARTUP_TIME, System.currentTimeMillis());
        }
    }

    /**
     * Remove all the listeners of this class.
     *
     * @param clazz The classname of the listener to be removed
     */
    private void removeListeners(final String clazz) throws Exception
    {
        //remove the listener
        CoreFactory.getActionDispatcher().execute(ActionNames.LISTENER_DELETE, EasyMap.build("clazz", clazz));
    }

    /**
     * This methods just make sure to get the server ID which will generate one if necessary at JIRA's startup.
     */
    private void checkAndInitSID()
    {
        final String serverId = ComponentManager.getComponentInstanceOfType(JiraLicenseService.class).getServerId();
        if (log.isInfoEnabled())
        {
            log.info("The Server ID for this JIRA instance is: [" + serverId + "]");
        }
    }
}
