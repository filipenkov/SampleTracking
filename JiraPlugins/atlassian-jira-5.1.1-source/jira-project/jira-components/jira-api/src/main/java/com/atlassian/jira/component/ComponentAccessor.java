package com.atlassian.jira.component;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldAccessor;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.velocity.VelocityManager;
import org.quartz.Scheduler;

/**
 * Provides static methods for accessing JIRA's managed components (ie the components in the PicoContainer).
 * <p>
 * Normally developers should get the dependencies injected into the constructor of the calling class via Pico, however
 * this utility provides access for when that is impossible (eg to escape from a circular dependency).
 *
 * @since v4.3
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@PublicApi
public class ComponentAccessor
{
    private volatile static Worker _worker;

    /**
     * Returns the core component which is stored in JIRA's Dependency Injection container under the key that is the given class.
     * <p>
     * In practise, this is the same as {@link #getComponentOfType(Class)} except it will fail faster if the
     * given Class is not a known component key (it also has a shorter and more meaningful name).
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Container.
     * That is, it retrieves core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     *
     * @param componentClass class to find a component instance by
     * @return the dependency injection component
     * @see #getComponentOfType(Class)
     */
    public static <T> T getComponent(Class<T> componentClass)
    {
        return getWorker().getComponent(componentClass);
    }

    /**
     * Returns the core component which is stored in JIRA's Dependency Injection container of the given Type (Class or Interface).
     * <p>
     * First it tries to find the component using the given Class as a key (like {@link #getComponent(Class)}),
     * however, if this fails then it will try to find a <em>unique</em> component that implements/extends the given Class.
     * This seems unlikely to be useful, but is included for now, for completeness and backward compatibility.
     * <p>
     * Please note that this method only gets components from JIRA's core Pico Container.
     * That is, it retrieves core components and components declared in Plugins1 plugins, but not components declared in Plugins2 plugins.
     *
     * @param componentClass class to find a component instance by
     * @return the dependency injection component
     * @see #getComponent(Class)
     */
    public static <T> T getComponentOfType(Class<T> componentClass)
    {
        return getWorker().getComponentOfType(componentClass);
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
     * @param componentClass class to find an OSGi component instance for
     * @return found component
     * @see #getComponentOfType(Class)
     */
    public static <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return getWorker().getOSGiComponentInstanceOfType(componentClass);
    }

    public static ProjectManager getProjectManager()
    {
        return getComponent(ProjectManager.class);
    }

    public static ApplicationProperties getApplicationProperties()
    {
        return getComponent(ApplicationProperties.class);
    }

    public static JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return getComponent(JiraAuthenticationContext.class);
    }

    public static ConstantsManager getConstantsManager()
    {
        return getComponent(ConstantsManager.class);
    }

    public static VelocityManager getVelocityManager()
    {
        return getComponent(VelocityManager.class);
    }       

    public static VelocityParamFactory getVelocityParamFactory()
    {
        return getComponent(VelocityParamFactory.class);
    }

    public static I18nHelper.BeanFactory getI18nHelperFactory()
    {
        return getComponent(I18nHelper.BeanFactory.class);
    }

    public static FieldManager getFieldManager()
    {
        return getComponent(FieldManager.class);
    }

    /**
     * Get FieldAccessor component.
     *
     * @deprecated Use {@link #getFieldManager()} instead. Since v5.0.
     */
    public static FieldAccessor getFieldAccessor()
    {
        // special case for now, we get the FieldManager and cast it.
        return getWorker().getFieldAccessor();
    }

    public static IssueManager getIssueManager()
    {
        return getComponent(IssueManager.class);
    }

    public static AttachmentManager getAttachmentManager()
    {
        return getComponent(AttachmentManager.class);
    }

    public static UserManager getUserManager()
    {
        return getComponent(UserManager.class);
    }

    public static PermissionManager getPermissionManager()
    {
        return getComponent(PermissionManager.class);
    }

    public static PermissionContextFactory getPermissionContextFactory()
    {
        return getComponent(PermissionContextFactory.class);
    }

    public static CustomFieldManager getCustomFieldManager()
    {
        return getComponent(CustomFieldManager.class);
    }

    public static UserUtil getUserUtil()
    {
        return getComponent(UserUtil.class);
    }

    public static GroupManager getGroupManager()
    {
        return getComponent(GroupManager.class);
    }

    public static EventTypeManager getEventTypeManager()
    {
        return getComponent(EventTypeManager.class);
    }

    public static IssueEventManager getIssueEventManager()
    {
        return getComponent(IssueEventManager.class);
    }

    public static WorkflowManager getWorkflowManager()
    {
        return getComponent(WorkflowManager.class);
    }

    public static IssueFactory getIssueFactory()
    {
        return getComponent(IssueFactory.class);
    }

    public static VersionManager getVersionManager()
    {
        return getComponent(VersionManager.class);
    }

    public static CommentManager getCommentManager()
    {
        return getComponent(CommentManager.class);
    }

    public static MailThreadManager getMailThreadManager()
    {
        return getComponent(MailThreadManager.class);
    }    

    /**
     * Retrieves and returns the web resource manager instance
     *
     * @return web resource manager
     */
    public static WebResourceManager getWebResourceManager()
    {
        return getComponent(WebResourceManager.class);
    }

    /**
     * Retrieves and returns the web resource URL provider instance
     *
     * @return web resource URL provider
     */
    public static WebResourceUrlProvider getWebResourceUrlProvider()
    {
        return getComponent(WebResourceUrlProvider.class);
    }

    /**
     * Retrieves and return the bulk operation manager instance
     *
     * @return bulk operation manager
     */
    public static BulkOperationManager getBulkOperationManager()
    {
        return getComponent(BulkOperationManager.class);
    }

    /**
     * Retrieves and returns the move subtask operation manager instance
     *
     * @return move subtask operation manager
     */
    public static MoveSubTaskOperationManager getMoveSubTaskOperationManager()
    {
        return getComponent(MoveSubTaskOperationManager.class);
    }

    /**
     * Retrieves and returns the worklog manager instance
     *
     * @return worklog manager
     */
    public static WorklogManager getWorklogManager()
    {
        return getComponent(WorklogManager.class);
    }

    /**
     * Retrieves and returns the project factory instance
     *
     * @return project factory
     */
    public static ProjectFactory getProjectFactory()
    {
        return getComponent(ProjectFactory.class);
    }

    /**
     * Retrieves and returns the issue type scheme manager instance
     *
     * @return issue type scheme manager
     */
    public static IssueTypeSchemeManager getIssueTypeSchemeManager()
    {
        return getComponent(IssueTypeSchemeManager.class);
    }

    /**
     * Retrieves and returns the issue type screen scheme manager instance
     *
     * @return issue type screen scheme manager
     */
    public static IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return getComponent(IssueTypeScreenSchemeManager.class);
    }

    /**
     * Retrieves and returns the subtask manager instance
     *
     * @return subtask manager
     */
    public static SubTaskManager getSubTaskManager()
    {
        return getComponent(SubTaskManager.class);
    }

    /**
     * Returns the IssueLinkManager component.
     *
     * @return the IssueLinkManager component.
     */
    public static IssueLinkManager getIssueLinkManager()
    {
        return getComponent(IssueLinkManager.class);
    }

    public static CrowdService getCrowdService()
    {
        return getComponent(CrowdService.class);
    }

    /**
     * Retrieves and returns the field layout manager
     *
     * @return field layout manager
     */
    public static FieldLayoutManager getFieldLayoutManager()
    {
        return getComponent(FieldLayoutManager.class);
    }

    /**
     * Retrieves and returns the column layout manager instance
     *
     * @return column layout manager
     */
    public static ColumnLayoutManager getColumnLayoutManager()
    {
        return getComponent(ColumnLayoutManager.class);
    }

    /**
     * Retrieves and returns the vote manager instance
     *
     * @return vote manager
     */
    public static VoteManager getVoteManager()
    {
        return getComponent(VoteManager.class);
    }

    public static PluginAccessor getPluginAccessor()
    {
        return getComponent(PluginAccessor.class);
    }

    public static PluginEventManager getPluginEventManager()
    {
        return getComponent(PluginEventManager.class);
    }

    public static ComponentClassManager getComponentClassManager()
    {
        return getComponent(ComponentClassManager.class);
    }

    public static PluginController getPluginController()
    {
        return getComponent(PluginController.class);
    }

    /**
     * Retrieves the RendererManager component.
     *
     * @return the RendererManager component.
     */
    public static RendererManager getRendererManager()
    {
        return getComponent(RendererManager.class);
    }

    /**
     * Retrieves and returns the field screen renderer factory instance
     *
     * @return field screen renderer factory
     */
    public static FieldScreenRendererFactory getFieldScreenRendererFactory()
    {
        return getComponent(FieldScreenRendererFactory.class);
    }

    /**
     * Retrieves and returns the workflow scheme manager instance
     *
     * @return workflow scheme manager
     */
    public static WorkflowSchemeManager getWorkflowSchemeManager()
    {
        return getComponent(WorkflowSchemeManager.class);
    }

    /**
     * Returns the IssueIndexManager component.
     *
     * @return the IssueIndexManager component.
     */
    public static IssueIndexManager getIssueIndexManager()
    {
        return getComponent(IssueIndexManager.class);
    }

    /**
     * Retrieves and returns the issue service instance
     *
     * @return issue service
     */
    public static IssueService getIssueService()
    {
        return getComponent(IssueService.class);
    }

    /**
     * Retrieves and returns the index path manager instance
     *
     * @return index path manager
     */
    public static IndexPathManager getIndexPathManager()
    {
        return getComponent(IndexPathManager.class);
    }

    /**
     * Retrieves and returns the attachment path instance
     *
     * @return attachment path manager
     */
    public static AttachmentPathManager getAttachmentPathManager()
    {
        return getComponent(AttachmentPathManager.class);
    }

    /**
     * Retrieves and returns the translation manager instance
     *
     * @return translation manager
     */
    public static TranslationManager getTranslationManager()
    {
        return getComponent(TranslationManager.class);
    }

    /**
     * Retrieves and returns the watcher manager instance
     *
     * @return watcher manager
     */
    public static WatcherManager getWatcherManager()
    {
        return getComponent(WatcherManager.class);
    }

//    /**
//     * Retrieves and returns the search provider instance
//     *
//     * @return search provider
//     */
//    public static SearchService getSearchService()
//    {
//        return (SearchService) getComponent(SearchService.class);
//    }

//    /**
//     * Retrieves and returns the search provider instance
//     *
//     * @return search provider
//     */
//    public static SearchProvider getSearchProvider()
//    {
//        return (SearchProvider) getComponent(SearchProvider.class);
//    }

//    /**
//     * Retrieves the search request service
//     *
//     * @return search request service
//     * @since v3.13
//     */
//    public static SearchRequestService getSearchRequestService()
//    {
//        return (SearchRequestService) getComponent(SearchRequestService.class);
//    }

//    /**
//     * Retrieves the search request admin service
//     *
//     * @return search request service
//     * @since v3.13
//     */
//    public static SearchRequestAdminService getSearchRequestAdminService()
//    {
//        return (SearchRequestAdminService) getComponent(SearchRequestAdminService.class);
//    }
//
//    /**
//     * Retrieves a {@link SearchRequestFactory}
//     *
//     * @return search request factory
//     * @since v3.13
//     */
//    public static SearchRequestFactory getSearchRequestFactory()
//    {
//        return (SearchRequestFactory) getComponent(SearchRequestFactory.class);
//    }

    /**
     * Retrieves and returns the field screen manager instance
     *
     * @return field screen manager
     */
    public static FieldScreenManager getFieldScreenManager()
    {
        return getComponent(FieldScreenManager.class);
    }

//    /**
//     * Retrieves and returns the field screen scheme manager instance
//     *
//     * @return field screen scheme manager
//     */
//    public static FieldScreenSchemeManager getFieldScreenSchemeManager()
//    {
//        return (FieldScreenSchemeManager) getComponent(FieldScreenSchemeManager.class);
//    }

//    /**
//     * Retrieves and returns the scheme permissions instance
//     *
//     * @return scheme permissions
//     */
//    public static SchemePermissions getSchemePermissions()
//    {
//        return (SchemePermissions) getComponent(SchemePermissions.class);
//    }

//    /**
//     * Retrieves and returns the text analyzer instance
//     *
//     * @return text analyzer
//     */
//    public static TextAnalyzer getTextAnalyzer()
//    {
//        return (TextAnalyzer) getComponent(TextAnalyzer.class);
//    }

    /**
     * Retrieves and returns the mail server manager instance
     *
     * @return mail server manager
     */
    public static MailServerManager getMailServerManager()
    {
        return getComponent(MailServerManager.class);
    }

//    /**
//     * Retrieves and returns the template manager instance
//     *
//     * @return template manager
//     */
//    public static TemplateManager getTemplateManager()
//    {
//        return (TemplateManager) getComponent(TemplateManager.class);
//    }

//    /**
//     * Retrieves and returns the assignee resolver instance
//     *
//     * @return assignee resolver
//     */
//    public static AssigneeResolver getAssigneeResolver()
//    {
//        return (AssigneeResolver) getComponent(AssigneeResolver.class);
//    }
//
//    /**
//     * Retrieves and returns the mailing list compiler instance
//     *
//     * @return mailing list compiler
//     */
//    public static MailingListCompiler getMailingListCompiler()
//    {
//        return (MailingListCompiler) getComponent(MailingListCompiler.class);
//    }
//
//    /**
//     * Retrieves and returns the subscription mail queue item factory instance
//     *
//     * @return subscription mail queue item factory
//     */
//    public static SubscriptionMailQueueItemFactory getSubscriptionMailQueueItemFactory()
//    {
//        return (SubscriptionMailQueueItemFactory) getComponent(SubscriptionMailQueueItemFactory.class);
//    }

    /**
     * Retrieves and returns the project component manager instance
     *
     * @return project component manager
     */
    public static ProjectComponentManager getProjectComponentManager()
    {
        return getComponent(ProjectComponentManager.class);
    }

    /**
     * Retrieves and returns the {@link com.atlassian.jira.issue.changehistory.ChangeHistoryManager} manager instance
     *
     * @return ChangeHistoryManager
     */
    public static ChangeHistoryManager getChangeHistoryManager()
    {
        return getComponent(ChangeHistoryManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public static UserPreferencesManager getUserPreferencesManager()
    {
        return getComponent(UserPreferencesManager.class);
    }

    /**
     * Retrieves and returns the user preferences manager instance
     *
     * @return user preferences manager
     */
    public static UserPropertyManager getUserPropertyManager()
    {
        return getComponent(UserPropertyManager.class);
    }

//    /**
//     * Retrieves and returns the JIRA duration utils instance
//     *
//     * @return JIRA duration utils
//     */
//    public static JiraDurationUtils getJiraDurationUtils()
//    {
//        return (JiraDurationUtils) getComponent(JiraDurationUtils.class);
//    }

//    /**
//     * Returns the {@link com.atlassian.jira.task.TaskManager}
//     *
//     * @return the {@link com.atlassian.jira.task.TaskManager}
//     */
//    public static TaskManager getTaskManager()
//    {
//        return (TaskManager) getComponent(TaskManager.class);
//    }

//    public static TrustedApplicationsManager getTrustedApplicationsManager()
//    {
//        return (TrustedApplicationsManager) getComponent(TrustedApplicationsManager.class);
//    }

//    /**
//     * @return the {@link com.atlassian.jira.bc.portal.PortalPageService}
//     */
//    public static PortalPageService getPortalPageService()
//    {
//        return (PortalPageService) getComponent(PortalPageService.class);
//    }
//
//    /**
//     * @return the {@link com.atlassian.jira.portal.PortalPageManager}
//     */
//    public static PortalPageManager getPortalPageManager()
//    {
//        return (PortalPageManager) getComponent(PortalPageManager.class);
//    }

    public static AvatarService getAvatarService()
    {
        return getComponent(AvatarService.class);
    }

    public static AvatarManager getAvatarManager()
    {
        return getComponent(AvatarManager.class);
    }    

//    public static IssueSecuritySchemeManager getIssueSecuritySchemeManager()
//    {
//        return (IssueSecuritySchemeManager) getComponent(IssueSecuritySchemeManager.class);
//    }

//    public static SecurityTypeManager getIssueSecurityTypeManager()
//    {
//        return (SecurityTypeManager) getComponent(SecurityTypeManager.class);
//    }

    public static ListenerManager getListenerManager()
    {
        return getComponent(ListenerManager.class);
    }

    public static MailQueue getMailQueue()
    {
        return getComponent(MailQueue.class);
    }

    public static NotificationSchemeManager getNotificationSchemeManager()
    {
        return getComponent(NotificationSchemeManager.class);
    }

//    public static NotificationTypeManager getNotificationTypeManager()
//    {
//        return (NotificationTypeManager) getComponent(NotificationTypeManager.class);
//    }

//    public static XMLObjectConfigurationFactory getObjectConfigurationFactory()
//    {
//        return (XMLObjectConfigurationFactory) getComponent(XMLObjectConfigurationFactory.class);
//    }

    public static PermissionSchemeManager getPermissionSchemeManager()
    {
        return getComponent(PermissionSchemeManager.class);
    }

    public static Scheduler getScheduler()
    {
        return getComponent(Scheduler.class);
    }

    public static IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return getComponent(IssueSecurityLevelManager.class);
    }

//    public static SearchRequestManager getSearchRequestManager()
//    {
//        return (SearchRequestManager) getComponent(SearchRequestManager.class);
//    }

    public static ServiceManager getServiceManager()
    {
        return getComponent(ServiceManager.class);
    }

    public static SubscriptionManager getSubscriptionManager()
    {
        return getComponent(SubscriptionManager.class);
    }

    public static GlobalPermissionManager getGlobalPermissionManager()
    {
        return getComponent(GlobalPermissionManager.class);
    }

//    public static SimpleBeanFactory getSimpleBeanFactory()
//    {
//        return (SimpleBeanFactory) getComponent(SimpleBeanFactory.class);
//    }

    public static LocaleManager getLocaleManager()
    {
        return getComponent(LocaleManager.class);
    }

    public static OptionsManager getOptionsManager()
    {
        return getComponent(OptionsManager.class);
    }

    public static OfBizDelegator getOfBizDelegator()
    {
        return getComponent(OfBizDelegator.class);
    }

    private static Worker getWorker()
    {
        if (_worker == null) throw new IllegalStateException("ComponentAccessor has not been initialised.");
        return _worker;
    }

    public static Worker initialiseWorker(Worker componentAccessorWorker)
    {
        Worker currentWorker = _worker;
        _worker = componentAccessorWorker;
        return currentWorker;
    }

    public static interface Worker
    {
        <T> T getComponent(Class<T> componentClass);

        <T> T getComponentOfType(Class<T> componentClass);

        /**
         * Use {@link #getComponent(Class)} with {code}FieldManager.class{code} as parameter.
         *
         * @return field accessor instance
         */
        @Deprecated
        FieldAccessor getFieldAccessor();

        <T> T getOSGiComponentInstanceOfType(Class<T> componentClass);
    }
}
