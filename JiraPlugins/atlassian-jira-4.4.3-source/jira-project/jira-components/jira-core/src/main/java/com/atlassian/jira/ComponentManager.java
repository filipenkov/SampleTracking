package com.atlassian.jira;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.extension.ContainerProvider;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.util.TextAnalyzer;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.MailingListCompiler;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.multitenant.EventPublisherDestroyer;
import com.atlassian.jira.multitenant.MultiTenantHostComponentProxier;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.plugin.component.ComponentModuleDescriptor;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.trackback.TrackbackManager;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.Shutdown;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.NotificationException;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.velocity.VelocityManager;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.util.tracker.ServiceTracker;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This component manager uses PicoContainer to resolve all the dependencies between components.
 * <p/>
 * <p/> It is responsible for initialising a large number of components in JIRA. Any components defined here may be
 * injected via a constructor.
 * <p/>
 * <p/> The ComponentManager also has various static accessor methods for non-Pico-managed objects, eg.
 * <code>ComponentManager.getInstance().getProjectManager()</code>. These should only be used if constructor based
 * injection is not feasible.
 * <p/>
 * <p/> More information can be found at the <a href="http://www.picocontainer.org">picocontainer website</a>.
 */
public class ComponentManager implements Shutdown
{

    public static final String EXTENSION_PROVIDER_PROPERTY = "jira.extension.container.provider";

    private static final Logger log = Logger.getLogger(ComponentManager.class);

    // This is a lazy reference, otherwise the ComponentManager class can't be loaded unless the multi tenant system
    // has been initialised. This results in NoClassDefFoundError's in unit tests, which don't say what the actual cause
    // is (a NullPointerException)
    private static final LazyReference<MultiTenantComponentMap<ComponentManager>> COMPONENT_MANAGER_MAP =
            new LazyReference<MultiTenantComponentMap<ComponentManager>>()
            {
                @Override
                protected MultiTenantComponentMap<ComponentManager> create() throws Exception
                {
                    if (MultiTenantContext.getFactory() == null)
                    {
                        throw new IllegalStateException("MultiTenantContext not initialised! If this is a unit test, you need to call MultiTenantContextTestUtils.setupMultiTenantSystem() in the set up code (this class is in the jira-tests Maven module)");
                    }

                    // We do not want the MultiTenantManager to handle the destruction of the ComponentManager
                    // so we do not register the listener. Instead it is handled explicitly by the JiraLauncher.
                    return MultiTenantContext.getFactory().createComponentMapBuilder(new MultiTenantCreator<ComponentManager>()
                    {
                        @Override
                        public ComponentManager create(Tenant tenant)
                        {
                            return new ComponentManager();
                        }
                    }).registerListener(MultiTenantComponentMap.Registration.NO).construct();
                }
            };

    //
    // instance fields
    //

    private volatile ComponentContainer internalContainer;
    private volatile MutablePicoContainer container;

    private final PluginSystem pluginSystem = new PluginSystem();
    private static final CopyOnWriteMap<String, ServiceTracker> serviceTrackerCache = CopyOnWriteMap.newHashMap();

    private volatile State state = StateImpl.NOT_STARTED;

    /**
     * Constructor made private, singleton.
     */
    ComponentManager()
    {
    }

    /**
     * Initialization registers components for the bootstrap loading of JIRA.  This gets enough of PICO registered to
     * allow JIRA to bootstrap without a database.
     */
    public void buildBootstrapPICOContainer()
    {
        internalContainer = new ComponentContainer();
        new BootstrapContainerRegistrar().registerComponents(internalContainer);
        container = internalContainer.getPicoContainer();
    }

    /**
     * Bootstrap the cut down JIRA plugin system.  This starts enough plugins to allow JIRA to bootstrap without a
     * database.
     */
    public void bootstrapPluginsSystem()
    {
        startPluginSystem();
        eagerlyInstantiate();
    }


    /**
     * Initialization registers components and then registers extensions.
     */
    public void initialise()
    {
        registerComponents();
        registerExtensions();
    }

    /**
     * Adds license configuration in license manager, then calls {@link ComponentManager#quickStart()} and {@link
     * ComponentManager#eagerlyInstantiate()}.
     */
    public synchronized void start()
    {
        quickStart();
        // Need to ensure that the components are eagerly instantiated after the "component" plugins had a chance to register.
        // As otherwise the default components are used to instantiate other default components. See JRA-4950
        eagerlyInstantiate();
    }

    /**
     * This is here (outside of the initialise method) as the getComponentInstancesOfType method starts instantiating
     * components and calls on the LicenseComponentAdpater which tries to get reference to this object using the {@link
     * ComponentManager#getInstance()} method. That method returns null as the reference to this object does not exist
     * until the initialise method completes. So this method should be invoked after the initialise method completes
     * execution.
     */
    public void quickStart()
    {
        startPluginSystem();
    }

    private void startPluginSystem()
    {
        if (MultiTenantContext.getManager().isSystemTenant())
        {
            pluginSystem.start();
        }
        else
        {
            DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
            internalContainer.getHostComponentProvider().provide(registrar);
            getComponent(MultiTenantHostComponentProxier.class).addToRegistry(registrar.getRegistry(), null);
        }

        state = StateImpl.PLUGINSYSTEM_STARTED;
        // now register component plugins before starting anything
        final PluginAccessor pluginAccessor = getPluginAccessor();
        final List<ComponentModuleDescriptor> funNewComponents = pluginAccessor.getEnabledModuleDescriptorsByClass(ComponentModuleDescriptor.class);

        if (!funNewComponents.isEmpty())
        {
            for (final ComponentModuleDescriptor componentModuleDescriptor : funNewComponents)
            {
                componentModuleDescriptor.registerComponents(container);
            }
        }
        container.registerComponentInstance(pluginAccessor.getClassLoader());

        state = StateImpl.COMPONENTS_REGISTERED;

        // now start all registered components
        @SuppressWarnings ("unchecked")
        final List<Startable> startableComponents = ((DefaultPicoContainer) getContainer()).getComponentInstancesOfType(Startable.class);
        if (startableComponents != null)
        {
            for (final Startable startable : startableComponents)
            {
                try
                {
                    if (!(startable instanceof PluginSystemLifecycle))// don't start the plugin manager twice!
                    {
                        startable.start();
                    }
                }
                catch (final Exception e)
                {
                    log.error("Error occurred while starting component '" + startable.getClass().getName() + "'.", e);
                    throw new InfrastructureException("Error occurred while starting component '" + startable.getClass().getName() + "'.", e);
                }
            }
        }
        state = StateImpl.STARTED;
    }

    public void shutdown()
    {
        if (MultiTenantContext.getManager().isSystemTenant())
        {
            pluginSystem.shutdown();
        }
        state = StateImpl.NOT_STARTED;
    }

    /**
     * What {@link State} is the {@link ComponentManager} in.
     *
     * @return the current state.
     */
    public State getState()
    {
        return state;
    }

    /**
     * Eagerly instantiates the container by making a call to {@link PicoContainer#getComponentInstances()} method on
     * the container that is returned by {@link ComponentManager#getContainer()} method.
     */
    @GuardedBy ("this")
    private void eagerlyInstantiate()
    {
        // this is to work around synchronisation problems with Pico (PICO-199)
        // http://jira.codehaus.org/browse/PICO-199
        // Pico has problems if it is instantiating A+B from different threads, and both depend on C
        // and they are using synchronised component adapters. You then get a deadlock.
        // This only happens when C is not registered, or C is not registered by its interface,
        // in which case PICO does a full tree walk (from within a synchronised method!).
        // This really needs to get fixed, but one work around is to full instantiate the tree first,
        // in which case, the need for a full tree walk is decreased.
        getContainer().getComponentInstances();
    }

    private void registerExtensions()
    {
        final ApplicationProperties applicationProperties = internalContainer.getComponentInstance(ApplicationProperties.class);
        final String extensionClassName = applicationProperties.getDefaultBackedString(EXTENSION_PROVIDER_PROPERTY);
        try
        {
            if (!StringUtils.isBlank(extensionClassName))
            {
                container = ((ContainerProvider) ClassLoaderUtils.loadClass(extensionClassName, getClass()).newInstance()).getContainer(internalContainer.getPicoContainer());
            }
            else
            {
                container = internalContainer.getPicoContainer();
            }
        }
        catch (final Throwable e)
        {
            log.error("Error loading extension class with name '" + extensionClassName + "'", e);
            container = internalContainer.getPicoContainer();
        }
    }

    /**
     * Returns container
     *
     * @return container
     */
    public PicoContainer getContainer()
    {
        return container;
    }

    /**
     * Returns container
     *
     * @return container
     */
    public MutablePicoContainer getMutablePicoContainer()
    {
        return container;
    }

    /**
     * This method registers all components with the internal pico-container.
     */
    private void registerComponents()
    {
        internalContainer = new ComponentContainer();
        new ContainerRegistrar().registerComponents(internalContainer, JiraStartupChecklist.startupOK());
    }

    /**
     * Retrieves and returns the web resource manager instance
     *
     * @return web resource manager
     */
    public WebResourceManager getWebResourceManager()
    {
        return (WebResourceManager) getContainer().getComponentInstanceOfType(WebResourceManager.class);
    }

    /**
     * Retrieves and returns the repository manager instance
     *
     * @return repository manager
     */
    public RepositoryManager getRepositoryManager()
    {
        return (RepositoryManager) getContainer().getComponentInstance(RepositoryManager.class);
    }

    /**
     * Retrieves and returns the CVS repository util compoment
     *
     * @return CVS repository util
     * @deprecated
     */
    @Deprecated
    public CvsRepositoryUtil getCvsRepositoryUtil()
    {
        return (CvsRepositoryUtil) getContainer().getComponentInstance(CvsRepositoryUtil.class);
    }

    /**
     * Retrieves and returns the attachment manager instance
     *
     * @return attachment manager
     */
    public AttachmentManager getAttachmentManager()
    {
        return (AttachmentManager) getContainer().getComponentInstance(AttachmentManager.class);
    }

    /**
     * Retrieves and returns the version manager instance
     *
     * @return version manager
     */
    public VersionManager getVersionManager()
    {
        return (VersionManager) getContainer().getComponentInstance(VersionManager.class);
    }

    /**
     * Retrieves and return the bulk operation manager instance
     *
     * @return bulk operation manager
     */
    public BulkOperationManager getBulkOperationManager()
    {
        return (BulkOperationManager) getContainer().getComponentInstance(BulkOperationManager.class);
    }

    /**
     * Retrieves and returns the move subtask operation manager instance
     *
     * @return move subtask operation manager
     */
    public MoveSubTaskOperationManager getMoveSubTaskOperationManager()
    {
        return (MoveSubTaskOperationManager) getContainer().getComponentInstance(MoveSubTaskOperationManager.class);
    }

    /**
     * Retuns a singleton instance of this class.
     *
     * @return a singleton instance of this class
     */
    public static ComponentManager getInstance()
    {
        return COMPONENT_MANAGER_MAP.get().get();
    }

    /**
     * This should only be called from LauncherContextListener when we are manipulating the lifecycle of the
     * ComponentManager during multitenancy.
     *
     * @param tenant that you are stopping
     */
    public static void stopTenant(final Tenant tenant)
    {
        try
        {
            MultiTenantContext.getTenantReference().set(tenant, true);

            final MultiTenantComponentMap<ComponentManager> tenantComponentMap = COMPONENT_MANAGER_MAP.get();
            tenantComponentMap.onTenantStop(tenant);
        }
        finally
        {
            MultiTenantContext.getTenantReference().remove();
        }
    }

    /**
     * Retrieves and returns a component which is an instance of given class.
     * <p>
     * In practise, this is the same as {@link #getComponent(Class)} except it will try to find a unique component that
     * implements/extends the given Class even if the Class is not an actual component key.
     * <p> Please note that this method only gets components from JIRA's core Pico Containter. That is, it retrieves
     * core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     * Plugins2 components can be retrieved via the {@link #getOSGiComponentInstanceOfType(Class)} method, but only if
     * they are public.
     *
     * @param clazz class to find a component instance by
     * @return found component
     * @see #getOSGiComponentInstanceOfType(Class)
     * @see PicoContainer#getComponentInstanceOfType(Class)
     */
    public static <T> T getComponentInstanceOfType(final Class<T> clazz)
    {
        // Try fast approach
        T component = getComponent(clazz);
        if (component != null)
        {
            return component;
        }
        // Look the slow way
        component = clazz.cast(getInstance().getContainer().getComponentInstanceOfType(clazz));
        if (component != null && log.isDebugEnabled())
        {
            // Lets log this so we know there is a naughty component
            // we also want a stacktrace at least temporarily to find the caller
            log.debug("Unable to find component with key '" + clazz + "' - eventually found '" + component + "' the slow way.", new IllegalArgumentException());
        }
        return component;
    }

    /**
     * Retrieves and returns a component which is an instance of given class.
     * <p>
     * In practise, this is the same as {@link #getComponentInstanceOfType(Class)} except it will fail faster if the
     * given Class is not a known component key (it also has a shorter and more meaningful name).
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Containter. That is, it retrieves
     * core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     * Plugins2 components can be retrieved via the {@link #getOSGiComponentInstanceOfType(Class)} method, but only if
     * they are public.
     *
     * @param clazz class to find a component instance by
     * @return found component, or null if not found
     * @see #getOSGiComponentInstanceOfType(Class)
     * @see PicoContainer#getComponentInstance(Object)
     */
    public static <T> T getComponent(final Class<T> clazz)
    {
        return clazz.cast(getInstance().getContainer().getComponentInstance(clazz));
    }

    /**
     * Retrieves and returns a public component from OSGi land via its class name.  This method can be used to retrieve
     * a component provided via a plugins2 OSGi bundle.  Please note that components returned via this method should
     * *NEVER* be cached (e.g. in a static field) as they may be refreshed at any time as a plugin is enabled/disabled
     * or the componentManager is reinitialised (after an XML import).
     * <p/>
     * <p> It is important to note that this only works for public components. That is components with {@code
     * public="true"} declared in their XML configuration. This means that they are available for other plugins to
     * import.
     * <p/>
     * <p> A use case for this is when for example for the dashboards plugin.  In several areas in JIRA we may want to
     * render gadgets via the {@link com.atlassian.gadgets.view.GadgetViewFactory}.  Whilst the interface for this
     * component is available in JIRA core, the implementation is provided by the dashboards OSGi bundle.  This method
     * will allow us to access it.
     *
     * @param clazz class to find an OSGi component instance for
     * @return found component
     * @see #getComponentInstanceOfType(Class)
     */
    public static <T> T getOSGiComponentInstanceOfType(final Class<T> clazz)
    {
        Assertions.notNull("class", clazz);

        final OsgiContainerManager osgiContainerManager = getComponentInstanceOfType(OsgiContainerManager.class);
        if (osgiContainerManager != null && osgiContainerManager.isRunning())
        {
            final ServiceTracker serviceTracker = getServiceTracker(clazz.getName(), osgiContainerManager);
            if (serviceTracker != null)
            {
                return clazz.cast(serviceTracker.getService());
            }
        }
        return null;
    }

    private static ServiceTracker getServiceTracker(final String className, final OsgiContainerManager osgiContainerManager)
    {
        ServiceTracker serviceTracker = serviceTrackerCache.get(className);
        if ((serviceTracker != null) && (serviceTracker.getService() == null))
        {
            // The Service Tracker is no longer valid - clear the whole cache.
            // (This happens on data import because we restart the plugin system.)
            serviceTrackerCache.clear();
            serviceTracker = null;
        }

        if (serviceTracker == null)
        {
            // JRA-18766. OsgiContainerManager.getServiceTracker() will actually create a new Service and store it in a List that lives for ever.
            // The main reason we are caching this is to avoid a memory leak.
            serviceTracker = osgiContainerManager.getServiceTracker(className);
            final ServiceTracker result = serviceTrackerCache.putIfAbsent(className, serviceTracker);
            return (result == null) ? serviceTracker : result;
        }
        return serviceTracker;
    }

    /**
     * Returns all the components currently inside of Pico which are instances of the given class.
     *
     * @param clazz the class to search for.
     * @return a list containing all the instances of the passed class registered in JIRA's pico container.
     */
    public static <T> List<T> getComponentsOfType(final Class<T> clazz)
    {
        final PicoContainer pico = getInstance().getContainer();
        @SuppressWarnings ("unchecked")
        final List<ComponentAdapter> adapters = pico.getComponentAdaptersOfType(clazz);
        if (adapters.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            final List<T> returnList = new ArrayList<T>(adapters.size());
            for (final ComponentAdapter adapter : adapters)
            {
                // remove cast when we go to a Java5 pico
                returnList.add(clazz.cast(adapter.getComponentInstance()));
            }
            return Collections.unmodifiableList(returnList);
        }
    }

    /**
     * Returns all the components currently inside Pico which are instances of the given class, mapping them to their
     * component key.
     *
     * @param iface The class to search for
     * @return a map, mapping the component key, to the instances of the clas registered in JIRA's pico container.
     */
    public static <T> Map<String, T> getComponentsOfTypeMap(final Class<T> iface)
    {
        final PicoContainer picoContainer = getInstance().getContainer();
        @SuppressWarnings ("unchecked")
        final List<ComponentAdapter> componentAdaptersOfType = picoContainer.getComponentAdaptersOfType(iface);

        final Map<String, T> implementations = new HashMap<String, T>();
        for (final ComponentAdapter componentAdapter : componentAdaptersOfType)
        {
            final T componentInstance = iface.cast(componentAdapter.getComponentInstance());
            implementations.put(String.valueOf(componentAdapter.getComponentKey()), componentInstance);
        }
        return Collections.unmodifiableMap(implementations);
    }

    private static class PluginSystem
    {
        enum State
        {
            NOT_STARTED,
            STARTED
        }

        volatile State state = State.NOT_STARTED;

        /**
         * Retrieves and returns the plugin system's lifecycle instance
         *
         * @return plugin lifecycle
         */
        public PluginSystemLifecycle getPluginSystemLifecycle()
        {
            return getComponentInstanceOfType(PluginSystemLifecycle.class);
        }

        void start()
        {
            if (state != State.NOT_STARTED)
            {
                return;
            }
            // start plugin manager first manually so that the component plugins can be startable themselves.
            final PluginSystemLifecycle pluginSystemLifecycle = getPluginSystemLifecycle();
            if (pluginSystemLifecycle instanceof Startable)
            {
                final Startable startablePluginManager = (Startable) pluginSystemLifecycle;
                try
                {
                    startablePluginManager.start();
                }
                catch (final NotificationException ex)
                {
                    // This is just a wrapper from the Plugin Events system - lets get the underlying cause.
                    final Throwable cause = ex.getCause();
                    throw new InfrastructureException("Error occurred while starting Plugin Manager. " + cause.getMessage(), cause);
                }
                catch (final Exception e)
                {
                    throw new InfrastructureException("Error occurred while starting Plugin Manager. " + e.getMessage(), e);
                }
            }
            else
            {
                throw new InfrastructureException("PluginManager does not implement startable anymore?!");
            }
            state = State.STARTED;
        }

        public void shutdown()
        {
            if (state != State.STARTED)
            {
                return;
            }
            try
            {
                getPluginSystemLifecycle().shutdown();
            }
            catch (final IllegalStateException ignore)
            {
                // if the plugin system hasn't been started for some reason or has been closed down it will throw an IllegalState
                // we don't care as long as it gets into the not started state
            }
            // Need to throw out the EventPublisher, this is just here temporarily to fix func tests
            getComponent(EventPublisherDestroyer.class).destroyEventPublisher();
            state = State.NOT_STARTED;
        }
    }

    /**
     * The state of the {@link ComponentManager}.
     *
     * @since 4.0
     */
    public interface State
    {
        /**
         * Have the components registered been with PICO including plugin components.
         *
         * @return true if the plugin system has started.
         */
        boolean isPluginSystemStarted();

        /**
         * Have the components registered been with PICO including plugin components.
         *
         * @return true if components have been registered.
         */
        boolean isComponentsRegistered();

        /**
         * Has the {@link ComponentManager} started
         *
         * @return true if the component manager has started.
         */
        boolean isStarted();
    }


    /**
     * An implementation of the above interface
     */
    private enum StateImpl implements State
    {
        /**
         * Not registered, plugins haven't started
         */
        NOT_STARTED
                {
                    public boolean isComponentsRegistered()
                    {
                        return false;
                    }

                    public boolean isPluginSystemStarted()
                    {
                        return false;
                    }

                    public boolean isStarted()
                    {
                        return false;
                    }
                },
        /**
         * Not registered, plugins haven't started
         */
        PLUGINSYSTEM_STARTED
                {
                    public boolean isComponentsRegistered()
                    {
                        return false;
                    }

                    public boolean isPluginSystemStarted()
                    {
                        return true;
                    }

                    public boolean isStarted()
                    {
                        return false;
                    }
                },
        /**
         * All components registered with PICO including plugin components and plugin system has started.
         */
        COMPONENTS_REGISTERED
                {
                    public boolean isComponentsRegistered()
                    {
                        return true;
                    }

                    public boolean isPluginSystemStarted()
                    {
                        return true;
                    }

                    public boolean isStarted()
                    {
                        return false;
                    }
                },
        /**
         * All components registered with PICO including plugin components and plugin system has started.
         */
        STARTED
                {
                    public boolean isComponentsRegistered()
                    {
                        return true;
                    }

                    public boolean isPluginSystemStarted()
                    {
                        return true;
                    }

                    public boolean isStarted()
                    {
                        return true;
                    }
                }
    }

    /**
     ===============================================================================
     Before we had generics it made sense to have type safe accessors for the various
     components but these days it is a code smell and I think we should deprecate these
     and the {@link ManagerFactory} - BB mar 2011
     ===============================================================================
     */

    /**
     * Retrieves and returns the issue updater instance NOTE: This method is only used for tests. The fact that it
     * exists means that tests need to be rewritten
     *
     * @return issue updater
     */
    public IssueUpdater getIssueUpdater()
    {
        return (IssueUpdater) getContainer().getComponentInstance(IssueUpdater.class);
    }

    /**
     * Retrieves and returns the Issue Creation Helper Bean instance.
     *
     * @return issue creation helper bean
     */
    public IssueCreationHelperBean getIssueCreationHelperBean()
    {
        return (IssueCreationHelperBean) getContainer().getComponentInstanceOfType(IssueCreationHelperBean.class);
    }

    /**
     * Retrieves and returns the file icon bean instance
     *
     * @return file icon bean
     */
    public FileIconBean getFileIconBean()
    {
        return (FileIconBean) getContainer().getComponentInstance(FileIconBean.class);
    }

    /**
     * Retrieves and returns the issue manager instance
     *
     * @return issue manager
     */
    public IssueManager getIssueManager()
    {
        return getComponentInstanceOfType(IssueManager.class);
    }

    /**
     * Retrieves and returns the workflow manager instance
     *
     * @return workflow manager
     */
    public WorkflowManager getWorkflowManager()
    {
        return (WorkflowManager) getContainer().getComponentInstanceOfType(WorkflowManager.class);
    }

    /**
     * Retrieves and returns the worklog manager instance
     *
     * @return worklog manager
     */
    public WorklogManager getWorklogManager()
    {
        return (WorklogManager) getContainer().getComponentInstanceOfType(WorklogManager.class);
    }

    /**
     * Get an IssueFactory instance, particularly useful for obtaining {@link Issue} from
     *
     * @return IssueFactory
     */
    @SuppressWarnings ( { "JavadocReference" })
    public IssueFactory getIssueFactory()
    {
        return (IssueFactory) getContainer().getComponentInstanceOfType(IssueFactory.class);
    }

    /**
     * Retrieves and returns the project factory instance
     *
     * @return project factory
     */
    public ProjectFactory getProjectFactory()
    {
        return (ProjectFactory) getContainer().getComponentInstanceOfType(ProjectFactory.class);
    }

    /**
     * Retrieves and returns the constants manager
     *
     * @return constants manager
     */
    public ConstantsManager getConstantsManager()
    {
        return (ConstantsManager) getContainer().getComponentInstanceOfType(ConstantsManager.class);
    }

    /**
     * Retrieves and returns the field manager instance
     *
     * @return field manager
     *
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getFieldAccessor()} instead. Since v4.4.
     */
    public FieldManager getFieldManager()
    {
        return (FieldManager) getContainer().getComponentInstanceOfType(FieldManager.class);
    }

    /**
     * Retrieves and returns the custom field manager
     *
     * @return custom field manager
     */
    public CustomFieldManager getCustomFieldManager()
    {
        return (CustomFieldManager) getContainer().getComponentInstanceOfType(CustomFieldManager.class);
    }

    /**
     * Retrieves and returns the issue type scheme manager instance
     *
     * @return issue type scheme manager
     */
    public IssueTypeSchemeManager getIssueTypeSchemeManager()
    {
        return (IssueTypeSchemeManager) getContainer().getComponentInstanceOfType(IssueTypeSchemeManager.class);
    }

    /**
     * Retrieves and returns the issue type screen scheme manager instance
     *
     * @return issue type screen scheme manager
     */
    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return (IssueTypeScreenSchemeManager) getContainer().getComponentInstanceOfType(IssueTypeScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the subtask manager instance
     *
     * @return subtask manager
     */
    public SubTaskManager getSubTaskManager()
    {
        return (SubTaskManager) getContainer().getComponentInstanceOfType(SubTaskManager.class);
    }

    /**
     * Retrieves and returns the issuel link manager instance NOTE: Needed especially for custom workflow conditions
     * that check an issue's links for progression.
     *
     * @return issuel link manager
     */
    public IssueLinkManager getIssueLinkManager()
    {
        return (IssueLinkManager) getContainer().getComponentInstanceOfType(IssueLinkManager.class);
    }

    /**
     * Retrieves and returns the application properties.
     *
     * @return application properties
     */
    public ApplicationProperties getApplicationProperties()
    {
        return (ApplicationProperties) getContainer().getComponentInstanceOfType(ApplicationProperties.class);
    }

    /**
     * Retrieves and returns the trackback manager instance. Used in OfBizTrackbackStore, which is constructed from
     * within the trackback module.
     *
     * @return trackback manager
     */
    public TrackbackManager getTrackbackManager()
    {
        return (TrackbackManager) getContainer().getComponentInstance(TrackbackManager.class);
    }

    public CrowdService getCrowdService()
    {
        return (CrowdService) getContainer().getComponentInstance(CrowdService.class);
    }

    /**
     * Retrieves and returns the permission manager instance
     *
     * @return permission manager
     */
    public PermissionManager getPermissionManager()
    {
        return (PermissionManager) getContainer().getComponentInstance(PermissionManager.class);
    }

    /**
     * Retrieves and returns the permission type manager instance
     *
     * @return permission type manager
     */
    public PermissionTypeManager getPermissionTypeManager()
    {
        return (PermissionTypeManager) getContainer().getComponentInstance(PermissionTypeManager.class);
    }

    /**
     * Retrieves and returns the field layout manager
     *
     * @return field layout manager
     */
    public FieldLayoutManager getFieldLayoutManager()
    {
        return (FieldLayoutManager) getContainer().getComponentInstance(FieldLayoutManager.class);
    }

    /**
     * Retrieves and returns the column layout manager instance
     *
     * @return column layout manager
     */
    public ColumnLayoutManager getColumnLayoutManager()
    {
        return (ColumnLayoutManager) getContainer().getComponentInstance(ColumnLayoutManager.class);
    }

    /**
     * Retrieves and returns the project manager instance
     *
     * @return project manager
     */
    public ProjectManager getProjectManager()
    {
        return (ProjectManager) getContainer().getComponentInstance(ProjectManager.class);
    }

    /**
     * Retrieves and returns the vote manager instance
     *
     * @return vote manager
     */
    public VoteManager getVoteManager()
    {
        return (VoteManager) getContainer().getComponentInstance(VoteManager.class);
    }

    /**
     * Retrieves and returns the JIRA locale utils instance
     *
     * @return JIRA locale utils
     */
    public JiraLocaleUtils getJiraLocaleUtils()
    {
        return (JiraLocaleUtils) getContainer().getComponentInstance(JiraLocaleUtils.class);
    }

    /**
     * Retrieves and returns the plugin system's lifecycle instance
     *
     * @return plugin lifecycle
     */
    public PluginSystemLifecycle getPluginSystemLifecycle()
    {
        return pluginSystem.getPluginSystemLifecycle();
    }

    public PluginAccessor getPluginAccessor()
    {
        return (PluginAccessor) getContainer().getComponentInstance(PluginAccessor.class);
    }

    public PluginEventManager getPluginEventManager()
    {
        return (PluginEventManager) getContainer().getComponentInstance(PluginEventManager.class);
    }

    public ComponentClassManager getComponentClassManager()
    {
        return (ComponentClassManager) getContainer().getComponentInstance(ComponentClassManager.class);
    }

    public PluginController getPluginController()
    {
        return (PluginController) getContainer().getComponentInstance(PluginController.class);
    }

    /**
     * Retrieves and returns the upgrade manager instance
     *
     * @return upgrade manager
     */
    public UpgradeManager getUpgradeManager()
    {
        return (UpgradeManager) getContainer().getComponentInstance(UpgradeManager.class);
    }

    /**
     * Retrieves and returns the renderer manager instance
     *
     * @return renderer manager
     */
    public RendererManager getRendererManager()
    {
        return (RendererManager) getContainer().getComponentInstance(RendererManager.class);
    }

    /**
     * Retrieves and returns the field screen renderer factory instance
     *
     * @return field screen renderer factory
     */
    public FieldScreenRendererFactory getFieldScreenRendererFactory()
    {
        return (FieldScreenRendererFactory) getContainer().getComponentInstance(FieldScreenRendererFactory.class);
    }

    /**
     * Retrieves and returns the workflow scheme manager instance
     *
     * @return workflow scheme manager
     */
    public WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return (WorkflowSchemeManager) getContainer().getComponentInstance(WorkflowSchemeManager.class);
    }

    /**
     * Retrieves and returns the index lifecycle manager instance
     *
     * @return index lifecycle manager
     */
    public IndexLifecycleManager getIndexLifecycleManager()
    {
        return (IndexLifecycleManager) getContainer().getComponentInstance(IndexLifecycleManager.class);
    }

    /**
     * Retrieves and returns the issue index manager instance
     *
     * @return index manager
     */
    public IssueIndexManager getIndexManager()
    {
        return (IssueIndexManager) getContainer().getComponentInstance(IssueIndexManager.class);
    }

    /**
     * Retrieves and returns the issue service instance
     *
     * @return issue service
     */
    public IssueService getIssueService()
    {
        return getComponentInstanceOfType(IssueService.class);
    }

    /**
     * Retrieves and returns the index path manager instance
     *
     * @return index path manager
     */
    public IndexPathManager getIndexPathManager()
    {
        return getComponentInstanceOfType(IndexPathManager.class);
    }

    /**
     * Retrieves and returns the attachment path instance
     *
     * @return attachment path manager
     */
    public AttachmentPathManager getAttachmentPathManager()
    {
        return getComponentInstanceOfType(AttachmentPathManager.class);
    }

    /**
     * Retrieves and returns the translation manager instance
     *
     * @return translation manager
     */
    public TranslationManager getTranslationManager()
    {
        return (TranslationManager) getContainer().getComponentInstance(TranslationManager.class);
    }

    /**
     * Retrieves and returns the JIRA authentication context instance
     *
     * @return JIRA authentication context
     */
    public JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return (JiraAuthenticationContext) getContainer().getComponentInstance(JiraAuthenticationContext.class);
    }

    /**
     * Retrieves and returns the watcher manager instance
     *
     * @return watcher manager
     */
    public WatcherManager getWatcherManager()
    {
        return (WatcherManager) getContainer().getComponentInstance(WatcherManager.class);
    }

    /**
     * Retrieves and returns the search provider instance
     *
     * @return search provider
     */
    public SearchService getSearchService()
    {
        return (SearchService) getContainer().getComponentInstance(SearchService.class);
    }

    /**
     * Retrieves and returns the search provider instance
     *
     * @return search provider
     */
    public SearchProvider getSearchProvider()
    {
        return (SearchProvider) getContainer().getComponentInstance(SearchProvider.class);
    }

    /**
     * Retrieves and returns the search request manager instance
     *
     * @return search request manager
     * @deprecated v3.13 please use {@link SearchRequestService}
     */
    @Deprecated
    public SearchRequestManager getSearchRequestManager()
    {
        return (SearchRequestManager) getContainer().getComponentInstance(SearchRequestManager.class);
    }

    /**
     * Retrieves the search request service
     *
     * @return search request service
     * @since v3.13
     */
    public SearchRequestService getSearchRequestService()
    {
        return (SearchRequestService) getContainer().getComponentInstance(SearchRequestService.class);
    }

    /**
     * Retrieves the search request admin service
     *
     * @return search request service
     * @since v3.13
     */
    public SearchRequestAdminService getSearchRequestAdminService()
    {
        return (SearchRequestAdminService) getContainer().getComponentInstance(SearchRequestAdminService.class);
    }

    /**
     * Retrieves a {@link SearchRequestFactory}
     *
     * @return search request factory
     * @since v3.13
     */
    public SearchRequestFactory getSearchRequestFactory()
    {
        return (SearchRequestFactory) getContainer().getComponentInstance(SearchRequestFactory.class);
    }

    /**
     * Retrieves and returns the field screen manager instance
     *
     * @return field screen manager
     */
    public FieldScreenManager getFieldScreenManager()
    {
        return (FieldScreenManager) getContainer().getComponentInstance(FieldScreenManager.class);
    }

    /**
     * Retrieves and returns the field screen scheme manager instance
     *
     * @return field screen scheme manager
     */
    public FieldScreenSchemeManager getFieldScreenSchemeManager()
    {
        return (FieldScreenSchemeManager) getContainer().getComponentInstance(FieldScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the scheme permissions instance
     *
     * @return scheme permissions
     */
    public SchemePermissions getSchemePermissions()
    {
        return (SchemePermissions) getContainer().getComponentInstanceOfType(SchemePermissions.class);
    }

    /**
     * Retrieves and returns the text analyzer instance
     *
     * @return text analyzer
     */
    public TextAnalyzer getTextAnalyzer()
    {
        return (TextAnalyzer) getContainer().getComponentInstanceOfType(TextAnalyzer.class);
    }

    /**
     * Retrieves and returns the mail server manager instance
     *
     * @return mail server manager
     */
    public MailServerManager getMailServerManager()
    {
        return (MailServerManager) getContainer().getComponentInstanceOfType(MailServerManager.class);
    }

    /**
     * Retrieves and returns teh event type manager instance
     *
     * @return event type manager
     */
    public EventTypeManager getEventTypeManager()
    {
        return (EventTypeManager) getContainer().getComponentInstanceOfType(EventTypeManager.class);
    }

    /**
     * Retrieves and returns the template manager instance
     *
     * @return template manager
     */
    public TemplateManager getTemplateManager()
    {
        return (TemplateManager) getContainer().getComponentInstanceOfType(TemplateManager.class);
    }

    /**
     * Retrieves and returns the user util instance
     *
     * @return user util
     */
    public UserUtil getUserUtil()
    {
        return (UserUtil) getContainer().getComponentInstanceOfType(UserUtil.class);
    }

    /**
     * Retrieves and returns the assignee resolver instance
     *
     * @return assignee resolver
     */
    public AssigneeResolver getAssigneeResolver()
    {
        return (AssigneeResolver) getContainer().getComponentInstanceOfType(AssigneeResolver.class);
    }

    /**
     * Retrieves and returns the mailing list compiler instance
     *
     * @return mailing list compiler
     */
    public MailingListCompiler getMailingListCompiler()
    {
        return (MailingListCompiler) getContainer().getComponentInstanceOfType(MailingListCompiler.class);
    }

    /**
     * Retrieves and returns the subscription mail queue item factory instance
     *
     * @return subscription mail queue item factory
     */
    public SubscriptionMailQueueItemFactory getSubscriptionMailQueueItemFactory()
    {
        return (SubscriptionMailQueueItemFactory) getContainer().getComponentInstanceOfType(SubscriptionMailQueueItemFactory.class);
    }

    /**
     * Retrieves and returns the velocity manager instance
     *
     * @return velocity manager
     */
    public VelocityManager getVelocityManager()
    {
        return (VelocityManager) getContainer().getComponentInstanceOfType(VelocityManager.class);
    }

    /**
     * Retrieves and returns the comment manager instance
     *
     * @return comment manager
     */
    public CommentManager getCommentManager()
    {
        return (CommentManager) getContainer().getComponentInstanceOfType(CommentManager.class);
    }

    /**
     * Create a new UpgradeManager. This may be needed if more upgrade tasks are added, or if the license has been
     * changed.
     */
    public void refreshUpgradeManager()
    {
        // this is very ugly. We should find a way to get Pico to reload its classes.
        final DefaultPicoContainer mutableContainer = (DefaultPicoContainer) container;
        mutableContainer.unregisterComponent(UpgradeManager.class);
        mutableContainer.registerComponentImplementation(UpgradeManager.class, UpgradeManagerImpl.class, Collections.EMPTY_LIST);
    }

    /**
     * Retrieves and returns the project component manager instance
     *
     * @return project component manager
     */
    public ProjectComponentManager getProjectComponentManager()
    {
        return (ProjectComponentManager) getContainer().getComponentInstance(ProjectComponentManager.class);
    }

    /**
     * Retrieves and returns the {@link ChangeHistoryManager} manager instance
     *
     * @return ChangeHistoryManager
     */
    public ChangeHistoryManager getChangeHistoryManager()
    {
        return (ChangeHistoryManager) getContainer().getComponentInstanceOfType(ChangeHistoryManager.class);
    }

    /**
     * Retrieves and returns the permission context factory instance
     *
     * @return permission context factory
     */
    public PermissionContextFactory getPermissionContextFactory()
    {
        return (PermissionContextFactory) getContainer().getComponentInstanceOfType(PermissionContextFactory.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public UserPreferencesManager getUserPreferencesManager()
    {
        return (UserPreferencesManager) getContainer().getComponentInstanceOfType(UserPreferencesManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public UserPropertyManager getUserPropertyManager()
    {
        return (UserPropertyManager) getContainer().getComponentInstanceOfType(UserPropertyManager.class);
    }

    /**
     * Retrieves and returns the JIRA duration utils instance
     *
     * @return JIRA duration utils
     */
    public JiraDurationUtils getJiraDurationUtils()
    {
        return (JiraDurationUtils) getContainer().getComponentInstanceOfType(JiraDurationUtils.class);
    }

    /**
     * Returns the {@link com.atlassian.jira.task.TaskManager}
     *
     * @return the {@link com.atlassian.jira.task.TaskManager}
     */
    public TaskManager getTaskManager()
    {
        return (TaskManager) getContainer().getComponentInstanceOfType(TaskManager.class);
    }

    public TrustedApplicationsManager getTrustedApplicationsManager()
    {
        return (TrustedApplicationsManager) getContainer().getComponentInstanceOfType(TrustedApplicationsManager.class);
    }

    public OutlookDateManager getOutlookDateManager()
    {
        return (OutlookDateManager) getContainer().getComponentInstanceOfType(OutlookDateManager.class);
    }

    /**
     * @return the {@link com.atlassian.jira.bc.portal.PortalPageService}
     */
    public PortalPageService getPortalPageService()
    {
        return (PortalPageService) getContainer().getComponentInstanceOfType(PortalPageService.class);
    }

    /**
     * @return the {@link com.atlassian.jira.portal.PortalPageManager}
     */
    public PortalPageManager getPortalPageManager()
    {
        return (PortalPageManager) getContainer().getComponentInstanceOfType(PortalPageManager.class);
    }

    public AvatarManager getAvatarManager()
    {
        return (AvatarManager) getContainer().getComponentInstanceOfType(AvatarManager.class);
    }
}
