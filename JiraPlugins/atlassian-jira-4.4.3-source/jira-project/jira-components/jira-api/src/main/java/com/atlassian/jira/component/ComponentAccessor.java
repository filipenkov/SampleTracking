package com.atlassian.jira.component;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldAccessor;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.trackback.TrackbackManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.event.PluginEventManager;
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
public class ComponentAccessor
{
    private static Worker _worker;

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

    public static ProjectManager getProjectManager()
    {
        return getComponentOfType(ProjectManager.class);
    }

    public static ApplicationProperties getApplicationProperties()
    {
        return getComponentOfType(ApplicationProperties.class);
    }

    public static JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return getComponentOfType(JiraAuthenticationContext.class);
    }

    public static ConstantsManager getConstantsManager()
    {
        return getComponentOfType(ConstantsManager.class);
    }

    public static VelocityManager getVelocityManager()
    {
        return getComponentOfType(VelocityManager.class);
    }       

    public static I18nHelper.BeanFactory getI18nHelperFactory()
    {
        return getComponentOfType(I18nHelper.BeanFactory.class);
    }

    public static FieldAccessor getFieldAccessor()
    {
        // special case for now, we get the FieldManager and cast it.
        return getWorker().getFieldAccessor();
    }

    public static IssueManager getIssueManager()
    {
        return getComponentOfType(IssueManager.class);
    }

    public static AttachmentManager getAttachmentManager()
    {
        return getComponent(AttachmentManager.class);
    }

    public static UserManager getUserManager()
    {
        return getComponentOfType(UserManager.class);
    }

    public static PermissionManager getPermissionManager()
    {
        return getComponentOfType(PermissionManager.class);
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
        return getComponentOfType(GroupManager.class);
    }

    public static EventTypeManager getEventTypeManager()
    {
        return getComponent(EventTypeManager.class);
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

//    /**
//     * Retrieves and returns the web resource manager instance
//     *
//     * @return web resource manager
//     */
//    public static WebResourceManager getWebResourceManager()
//    {
//        return getComponent(WebResourceManager.class);
//    }

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

//    /**
//     * Retrieves and returns the issue type screen scheme manager instance
//     *
//     * @return issue type screen scheme manager
//     */
//    public static IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
//    {
//        return (IssueTypeScreenSchemeManager) getComponent(IssueTypeScreenSchemeManager.class);
//    }

    /**
     * Retrieves and returns the subtask manager instance
     *
     * @return subtask manager
     */
    public static SubTaskManager getSubTaskManager()
    {
        return getComponent(SubTaskManager.class);
    }

//    /**
//     * Retrieves and returns the issuel link manager instance NOTE: Needed especially for custom workflow conditions
//     * that check an issue's links for progression.
//     *
//     * @return issuel link manager
//     */
//    public static IssueLinkManager getIssueLinkManager()
//    {
//        return (IssueLinkManager) getComponent(IssueLinkManager.class);
//    }

    /**
     * Retrieves and returns the trackback manager instance. Used in OfBizTrackbackStore, which is constructed from
     * within the trackback module.
     *
     * @return trackback manager
     */
    public static TrackbackManager getTrackbackManager()
    {
        return getComponent(TrackbackManager.class);
    }

    public static CrowdService getCrowdService()
    {
        return getComponent(CrowdService.class);
    }

//    /**
//     * Retrieves and returns the permission type manager instance
//     *
//     * @return permission type manager
//     */
//    public static PermissionTypeManager getPermissionTypeManager()
//    {
//        return (PermissionTypeManager) getComponent(PermissionTypeManager.class);
//    }

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

//    public static ComponentClassManager getComponentClassManager()
//    {
//        return (ComponentClassManager) getComponent(ComponentClassManager.class);
//    }

    public static PluginController getPluginController()
    {
        return getComponent(PluginController.class);
    }

//    /**
//     * Retrieves and returns the upgrade manager instance
//     *
//     * @return upgrade manager
//     */
//    public static UpgradeManager getUpgradeManager()
//    {
//        return (UpgradeManager) getComponent(UpgradeManager.class);
//    }

//    /**
//     * Retrieves and returns the renderer manager instance
//     *
//     * @return renderer manager
//     */
//    public static RendererManager getRendererManager()
//    {
//        return (RendererManager) getComponent(RendererManager.class);
//    }

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

//    /**
//     * Retrieves and returns the index lifecycle manager instance
//     *
//     * @return index lifecycle manager
//     */
//    public static IndexLifecycleManager getIndexLifecycleManager()
//    {
//        return (IndexLifecycleManager) getComponent(IndexLifecycleManager.class);
//    }
//
//    /**
//     * Retrieves and returns the issue index manager instance
//     *
//     * @return index manager
//     */
//    public static IssueIndexManager getIndexManager()
//    {
//        return (IssueIndexManager) getComponent(IssueIndexManager.class);
//    }

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

//    public static ListenerManager getListenerManager()
//    {
//        return (ListenerManager) getComponent(ListenerManager.class);
//    }

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

//    public static GlobalPermissionManager getGlobalPermissionManager()
//    {
//        return (GlobalPermissionManager) getComponent(GlobalPermissionManager.class);
//    }

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

//    public static CustomFieldValidator getCustomFieldValidator()
//    {
//        return (CustomFieldValidator) getComponent(CustomFieldValidator.class);
//    }

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

        FieldAccessor getFieldAccessor();
    }
}
