package com.atlassian.jira;

import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.configurable.XMLObjectConfigurationFactory;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.sharing.index.DirectoryFactory;
import com.atlassian.jira.sharing.index.MemoryDirectoryFactory;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import com.atlassian.jira.web.servlet.rpc.AxisServletProvider;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.quartz.Scheduler;
import webwork.util.ValueStack;

/**
 * Provides static methods for obtaining 'Manager' classes, though which much of JIRA's functionality is exposed.
 *
 * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor} instead. Since v4.4.
 */
@SuppressWarnings ( { "JavaDoc" })
public class ManagerFactory
{
    private static final Logger log = Logger.getLogger(ManagerFactory.class);
    private static volatile ManagerFactory instance = new ManagerFactory();

    /**
     * This method will refresh all the caches in JIRA (hopefully! :)) <strong>This method should not be called by
     * anyone</strong>
     *
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    public static synchronized void quickRefresh()
    {
        quickRefresh(null);
    }

    /**
     * This method will refresh all the caches in JIRA (hopefully! :)) <strong>This method should not be called by
     * anyone</strong>
     *
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static synchronized void quickRefresh(IssueIndexManager indexManager)
    {
        log.debug("ManagerFactory.quickRefresh");

        if (MultiTenantContext.getManager().isSystemTenant())
        {
            final IndexLifecycleManager oldIndexManager = getIndexLifecycleManager();
            if (oldIndexManager != null)
            {
                // the tests null this guy because they put in the MemoryIndexManager, should fix the tests but this is a BIG job
                oldIndexManager.shutdown();
            }
            final TaskManager taskManager = ComponentManager.getInstance().getTaskManager();
            if (taskManager != null)
            {
                taskManager.shutdownAndWait(0);
            }

            /*
             * first we shutdown, THEN we unregister listener. why? because some listeners are listening for
             * shutdown-related events.
             */
            ComponentManager.getInstance().shutdown();
            ComponentManager.getComponentInstanceOfType(EventPublisher.class).unregisterAll();
            ComponentManager.getInstance().initialise();

            // Need to let tests put the CrowdReadWrite Service in before we start all our components
            // This needs be be before ProjectManager, else we will be too late and CrowdService will have been instantiated.
            if (StaticCrowdServiceFactory.getCrowdService() != null)
            {
                ManagerFactory.addService(CrowdService.class, StaticCrowdServiceFactory.getCrowdService());
            }

            //because we often use projectmanager and we don't want it cached, then we should set it here (for tests only)
            addService(ProjectManager.class, new DefaultProjectManager());

            // we want the shared entities to use a memory based DirectoryFactory
            addService(DirectoryFactory.class, new MemoryDirectoryFactory());

            addService(OsgiServiceProxyFactory.class, new OsgiServiceProxyFactory((OsgiServiceProxyFactory.ServiceTrackerFactory) null));

            // Need to let tests put the memory index manager in before we start all of our components
            if (indexManager != null)
            {
                ManagerFactory.addService(IssueIndexManager.class, indexManager);
            }

            // Need to bootstrap all the components that need initialising
            ComponentManager.getInstance().quickStart();
            CoreFactory.globalRefresh();

            getConstantsManager().refresh();
        }
        else
        {
            MultiTenantContext.getController().refreshTenant(MultiTenantContext.getTenantReference().get());
        }
    }

    /**
     * This should *never* be called, except in tests, or if you are importing or seting up for the first time. The
     * reason this is called is to ensure that all the managers are reinitialised after the license has changed.
     * <p/>
     * Note: Make sure the scheduler is shutdown
     */
    public static synchronized void globalRefresh()
    {
        log.debug("ManagerFactory.globalRefresh");

        // todo Make JIRA system tenant refresh using the tenant refresh method
        // Also, when the system tenant is refreshed, all the remaining tenants will probably have to be refreshed too
        if (MultiTenantContext.getManager().isSystemTenant())
        {
            // Shutdown Index manager as it is about to be refreshed.
            getIndexLifecycleManager().shutdown();

            // shutdown task manager
            final TaskManager taskManager = ComponentManager.getInstance().getTaskManager();
            taskManager.shutdownAndWait(0);

            ComponentManager.getInstance().shutdown();

            // Rebuild all the registered objects
            ComponentManager.getInstance().initialise();

            // clear the method cache for JRA-16750
            ValueStack.clearMethods();

            // Need to bootstrap all the components that need initialising
            ComponentManager.getInstance().start();
            CoreFactory.globalRefresh();
            ComponentManager.getComponentInstanceOfType(AxisServletProvider.class).reset();

            getConstantsManager().refresh();
        }
        else
        {
            try
            {
                MultiTenantContext.getController().refreshTenant(MultiTenantContext.getTenantReference().get());
            }
            catch (RuntimeException re)
            {
                log.error("Error restarting tenant", re);
                // rethrow here because changes are the exception will get lost by some dumb finally block throwing
                // an exception because JIRA is bad, so we'll miss the exception, however we don't JIRA to think it's
                // been successful.
                throw re;
            }
        }
    }

    // registry stuff - see Registry pattern in Martin Fowler's Enterprise Patterns book
    private static ManagerFactory getInstance()
    {
        return instance;
    }

    private ManagerFactory()
    {
    }

    private <T> void setService(final Class<T> clazz, final T instance)
    {
        addService(clazz, instance);
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static <T> ComponentAdapter addService(final Class<T> clazz, final T instance)
    {
        final PicoContainer container = ComponentManager.getInstance().getContainer();
        final MutablePicoContainer mutableContainer = (MutablePicoContainer) container;
        //unregister current implemenation (if one exists) - prevent duplicate registration exception
        final ComponentAdapter result = mutableContainer.unregisterComponent(clazz);
        if (instance != null)
        {
            mutableContainer.registerComponentInstance(clazz, instance);
        }
        return result;
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static void removeService(final Class<?> clazz)
    {
        addService(clazz, null);
    }

    /**
     * This method resets the registry.
     */
    public static void initialise()
    {
        instance = new ManagerFactory();
    }

    /**
     * @deprecated This shit is only being used in "unit" tests that extend from LegacyJiraMockTestCase, don't use this
     *             anymore. Please write your unit tests using mocks.
     */
    @Deprecated
    public static void refreshIssueManager()
    {
        getInstance().setService(IssueManager.class, ComponentManager.getInstance().getIssueManager());
    }

    /**
     * Create a new UpgradeManager.  This may be needed if more upgrade tasks are added, or if the license has been
     * changed.
     *
     * @deprecated Use {@link ComponentManager#refreshUpgradeManager()} instead. That method is as nasty as this one.
     */
    @Deprecated
    public static void refreshUpgradeManager()
    {
        //this is very ugly.  We should find a way to get Pico to reload its classes.
        final PicoContainer container = ComponentManager.getInstance().getContainer();
        final MutablePicoContainer mutableContainer = (MutablePicoContainer) container;
        mutableContainer.unregisterComponent(UpgradeManager.class);
        mutableContainer.registerComponentImplementation(UpgradeManager.class, UpgradeManagerImpl.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getApplicationProperties()} instead. Since v5.0.
     */
    public static ApplicationProperties getApplicationProperties()
    {
        return (ApplicationProperties) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ApplicationProperties.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getAttachmentManager()} instead. Since v5.0.
     */
    public static AttachmentManager getAttachmentManager()
    {
        return ComponentManager.getInstance().getAttachmentManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getConstantsManager()} instead. Since v5.0.
     */
    public static ConstantsManager getConstantsManager()
    {
        return (ConstantsManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ConstantsManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getCustomFieldManager()} instead. Since v5.0.
     */
    public static CustomFieldManager getCustomFieldManager()
    {
        return (CustomFieldManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(CustomFieldManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getFieldManager()} instead. Since v5.0.
     */
    public static FieldManager getFieldManager()
    {
        return (FieldManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(FieldManager.class);
    }

    @Deprecated
    public static IndexLifecycleManager getIndexLifecycleManager()
    {
        return ComponentManager.getInstance().getIndexLifecycleManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getIssueIndexManager()} instead. Since v5.0.
     */
    public static IssueIndexManager getIndexManager()
    {
        return ComponentManager.getInstance().getIndexManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getIssueManager()} instead. Since v4.4.
     */
    public static IssueManager getIssueManager()
    {
        return (IssueManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(IssueManager.class);
    }

    @Deprecated
    public static IssueSecuritySchemeManager getIssueSecuritySchemeManager()
    {
        return (IssueSecuritySchemeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(IssueSecuritySchemeManager.class);
    }

    @Deprecated
    public static SecurityTypeManager getIssueSecurityTypeManager()
    {
        return (SecurityTypeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(SecurityTypeManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getListenerManager()} instead. Since v5.0.
     */
    public static ListenerManager getListenerManager()
    {
        return (ListenerManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ListenerManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getMailQueue()} instead. Since v5.0.
     */
    public static MailQueue getMailQueue()
    {
        return (MailQueue) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(MailQueue.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getNotificationSchemeManager()} instead. Since v5.0.
     */
    public static NotificationSchemeManager getNotificationSchemeManager()
    {
        return (NotificationSchemeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(NotificationSchemeManager.class);
    }

    @Deprecated
    public static NotificationTypeManager getNotificationTypeManager()
    {
        return (NotificationTypeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(NotificationTypeManager.class);
    }

    @Deprecated
    public static XMLObjectConfigurationFactory getObjectConfigurationFactory()
    {
        return (XMLObjectConfigurationFactory) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(
                XMLObjectConfigurationFactory.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getPermissionManager()} instead. Since v5.0.
     */
    public static PermissionManager getPermissionManager()
    {
        return ComponentManager.getInstance().getPermissionManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getPermissionSchemeManager()} instead. Since v5.0.
     */
    public static PermissionSchemeManager getPermissionSchemeManager()
    {
        return (PermissionSchemeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(PermissionSchemeManager.class);
    }

    @Deprecated
    public static PermissionTypeManager getPermissionTypeManager()
    {
        return (PermissionTypeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(PermissionTypeManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getProjectManager()} instead. Since v5.0.
     */
    public static ProjectManager getProjectManager()
    {
        return (ProjectManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ProjectManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getScheduler()} instead. Since v5.0.
     */
    public static Scheduler getScheduler()
    {
        return (Scheduler) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(Scheduler.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getIssueSecurityLevelManager()} instead. Since v5.0.
     */
    public static IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return (IssueSecurityLevelManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(IssueSecurityLevelManager.class);
    }

    @Deprecated
    public static SearchRequestManager getSearchRequestManager()
    {
        return (SearchRequestManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(SearchRequestManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getServiceManager()} instead. Since v5.0.
     */
    public static ServiceManager getServiceManager()
    {
        return (ServiceManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ServiceManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getSubscriptionManager()} instead. Since v5.0.
     */
    public static SubscriptionManager getSubscriptionManager()
    {
        return (SubscriptionManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(SubscriptionManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.ComponentManager#getUpgradeManager()} instead. Since v5.0.
     */
    public static UpgradeManager getUpgradeManager()
    {
        return (UpgradeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(UpgradeManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getGlobalPermissionManager()} instead. Since v5.0.
     */
    public static GlobalPermissionManager getGlobalPermissionManager()
    {
        return (GlobalPermissionManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(GlobalPermissionManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getVelocityManager()} instead. Since v5.0.
     */
    public static VelocityManager getVelocityManager()
    {
        return (VelocityManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(VelocityManager.class);
    }

    @Deprecated
    public static OutlookDateManager getOutlookDateManager()
    {
        return (OutlookDateManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(OutlookDateManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getWorkflowManager()} instead. Since v5.0.
     */
    public static WorkflowManager getWorkflowManager()
    {
        return ComponentManager.getInstance().getWorkflowManager();
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getLocaleManager()} instead. Since v5.0.
     */
    public static LocaleManager getLocaleManager()
    {
        return (LocaleManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(LocaleManager.class);
    }

    @Deprecated
    public static JiraLocaleUtils getJiraLocaleUtils()
    {
        return (JiraLocaleUtils) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(JiraLocaleUtils.class);
    }

    /**
     * @return MailThreadManager
     *
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getMailThreadManager()} instead. Since v4.4.
     */
    @Deprecated
    public static MailThreadManager getMailThreadManager()
    {
        return (MailThreadManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(MailThreadManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getWorkflowSchemeManager()} instead. Since v5.0.
     */
    public static WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return (WorkflowSchemeManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(WorkflowSchemeManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getOptionsManager()} instead. Since v5.0.
     */
    public static OptionsManager getOptionsManager()
    {
        return (OptionsManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(OptionsManager.class);
    }

    @Deprecated
    public static CustomFieldValidator getCustomFieldValidator()
    {
        return (CustomFieldValidator) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(CustomFieldValidator.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getUserManager()} instead. Since v5.0.
     */
    public static UserManager getUserManager()
    {
        return (UserManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(UserManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getGroupManager()} instead. Since v5.0.
     */
    public static GroupManager getGroupManager()
    {
        return (GroupManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(GroupManager.class);
    }

    @Deprecated
    /**
     * @deprecated Use {@link com.atlassian.jira.component.ComponentAccessor#getUserPropertyManager()} instead. Since v5.0.
     */
    public static UserPropertyManager getUserPropertyManager()
    {
        return (UserPropertyManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(UserPropertyManager.class);
    }
}
