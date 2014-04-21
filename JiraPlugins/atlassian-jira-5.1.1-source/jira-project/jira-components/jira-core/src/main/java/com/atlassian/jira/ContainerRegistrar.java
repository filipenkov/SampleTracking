package com.atlassian.jira;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.cache.CacheProvider;
import com.atlassian.cache.memory.MemoryCacheProvider;
import com.atlassian.configurable.ObjectConfigurationFactory;
import com.atlassian.configurable.XMLObjectConfigurationFactory;
import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.directory.DirectoryCacheFactoryImpl;
import com.atlassian.crowd.directory.InternalDirectoryUtils;
import com.atlassian.crowd.directory.InternalDirectoryUtilsImpl;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.directory.ldap.cache.DirectoryCacheFactory;
import com.atlassian.crowd.directory.ldap.util.LDAPPropertiesHelper;
import com.atlassian.crowd.directory.ldap.util.LDAPPropertiesHelperImpl;
import com.atlassian.crowd.directory.loader.DelegatedAuthenticationDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.DelegatedAuthenticationDirectoryInstanceLoaderImpl;
import com.atlassian.crowd.directory.loader.DelegatingDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.InternalDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.InternalDirectoryInstanceLoaderImpl;
import com.atlassian.crowd.directory.loader.InternalHybridDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.LDAPDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.LDAPDirectoryInstanceLoaderImpl;
import com.atlassian.crowd.directory.loader.RemoteCrowdDirectoryInstanceLoader;
import com.atlassian.crowd.directory.loader.RemoteCrowdDirectoryInstanceLoaderImpl;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.UnfilteredCrowdService;
import com.atlassian.crowd.embedded.core.CrowdDirectoryServiceImpl;
import com.atlassian.crowd.embedded.core.CrowdEmbeddedApplicationFactory;
import com.atlassian.crowd.embedded.core.CrowdServiceImpl;
import com.atlassian.crowd.embedded.core.FilteredCrowdServiceImpl;
import com.atlassian.crowd.embedded.core.FilteredGroupsProvider;
import com.atlassian.crowd.embedded.core.XmlFilteredGroupsProvider;
import com.atlassian.crowd.embedded.core.util.CrowdServiceFactory;
import com.atlassian.crowd.embedded.core.util.StaticCrowdServiceFactory;
import com.atlassian.crowd.embedded.ofbiz.InternalMembershipDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizApplicationDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizCacheFlushingManager;
import com.atlassian.crowd.embedded.ofbiz.OfBizDelegatingMembershipDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizDirectoryDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizGroupDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizInternalMembershipDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizUserDao;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.event.EventStore;
import com.atlassian.crowd.event.EventStoreGeneric;
import com.atlassian.crowd.event.StoringEventListener;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationManagerGeneric;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.manager.application.ApplicationServiceGeneric;
import com.atlassian.crowd.manager.authentication.TokenAuthenticationManager;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryManagerGeneric;
import com.atlassian.crowd.manager.directory.DirectorySynchronisationInformationStore;
import com.atlassian.crowd.manager.directory.DirectorySynchronisationInformationStoreImpl;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.DirectorySynchroniserHelper;
import com.atlassian.crowd.manager.directory.DirectorySynchroniserHelperImpl;
import com.atlassian.crowd.manager.directory.DirectorySynchroniserImpl;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManagerImpl;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorManager;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorManagerImpl;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.crowd.manager.directory.monitor.poller.QuartzDirectoryPollerManager;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.crowd.manager.lock.LockFactory;
import com.atlassian.crowd.manager.lock.ReentrantLockFactory;
import com.atlassian.crowd.manager.login.ForgottenLoginManager;
import com.atlassian.crowd.manager.permission.PermissionManagerImpl;
import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.proxy.TrustedProxyManager;
import com.atlassian.crowd.manager.validation.ClientValidationManager;
import com.atlassian.crowd.manager.validation.ClientValidationManagerImpl;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactoryImpl;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslaterImpl;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.crowd.util.InetAddressCacheUtil;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.crowd.util.PasswordHelper;
import com.atlassian.crowd.util.PasswordHelperImpl;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.EventThreadPoolConfiguration;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.EventExecutorFactoryImpl;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.internal.EventThreadPoolConfigurationImpl;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.EventExecutorFactory;
import com.atlassian.extras.api.LicenseManager;
import com.atlassian.extras.core.LicenseManagerFactory;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.GadgetStateFactory;
import com.atlassian.gadgets.directory.spi.DirectoryPermissionService;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecStore;
import com.atlassian.gadgets.opensocial.spi.Whitelist;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;
import com.atlassian.instrumentation.DefaultInstrumentRegistry;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.instrumentation.RegistryConfiguration;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.ThreadLocalOpTimerFactory;
import com.atlassian.jira.action.admin.export.DefaultSaxEntitiesExporter;
import com.atlassian.jira.action.admin.export.EntitiesExporter;
import com.atlassian.jira.action.component.SelectComponentAssigneesUtil;
import com.atlassian.jira.action.component.SelectComponentAssigneesUtilImpl;
import com.atlassian.jira.action.screen.AddFieldToScreenUtilImpl;
import com.atlassian.jira.admin.AnnouncementBanner;
import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.admin.IntroductionPropertyImpl;
import com.atlassian.jira.admin.RenderablePropertyFactory;
import com.atlassian.jira.appconsistency.db.LockedDatabaseOfBizDelegator;
import com.atlassian.jira.appconsistency.integrity.IntegrityCheckManager;
import com.atlassian.jira.appconsistency.integrity.IntegrityCheckManagerImpl;
import com.atlassian.jira.appconsistency.integrity.IntegrityChecker;
import com.atlassian.jira.applinks.JiraAppLinksHostApplication;
import com.atlassian.jira.applinks.JiraApplicationLinkService;
import com.atlassian.jira.applinks.JiraEntityLinkService;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.NodeAssociationStoreImpl;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.association.UserAssociationStoreImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarManagerImpl;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarPickerHelperImpl;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.AvatarServiceImpl;
import com.atlassian.jira.avatar.AvatarStore;
import com.atlassian.jira.avatar.CachingAvatarStore;
import com.atlassian.jira.avatar.OfbizAvatarStore;
import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationPropertiesServiceImpl;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.config.DefaultConstantsService;
import com.atlassian.jira.bc.customfield.CustomFieldService;
import com.atlassian.jira.bc.customfield.DefaultCustomFieldService;
import com.atlassian.jira.bc.dataimport.DataImportProductionDependencies;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.dataimport.DefaultDataImportService;
import com.atlassian.jira.bc.dataimport.DefaultExportService;
import com.atlassian.jira.bc.dataimport.DefaultImportResultStore;
import com.atlassian.jira.bc.dataimport.ExportService;
import com.atlassian.jira.bc.dataimport.ImportResultStore;
import com.atlassian.jira.bc.favourites.DefaultFavouritesService;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.DefaultFilterSubscriptionService;
import com.atlassian.jira.bc.filter.DefaultSearchRequestAdminService;
import com.atlassian.jira.bc.filter.DefaultSearchRequestService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.group.DefaultGroupService;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.bc.group.search.GroupPickerSearchServiceImpl;
import com.atlassian.jira.bc.imports.project.DefaultProjectImportService;
import com.atlassian.jira.bc.imports.project.ProjectImportService;
import com.atlassian.jira.bc.issue.DefaultIssueService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.issue.attachment.DefaultAttachmentService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.comment.DefaultCommentService;
import com.atlassian.jira.bc.issue.label.DefaultLabelService;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.bc.issue.link.DefaultIssueLinkService;
import com.atlassian.jira.bc.issue.link.DefaultRemoteIssueLinkService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.bc.issue.search.DefaultIssuePickerSearchService;
import com.atlassian.jira.bc.issue.search.DefaultSearchService;
import com.atlassian.jira.bc.issue.search.HistoryIssuePickerSearchProvider;
import com.atlassian.jira.bc.issue.search.IssuePickerSearchService;
import com.atlassian.jira.bc.issue.search.LuceneCurrentSearchIssuePickerSearchProvider;
import com.atlassian.jira.bc.issue.search.QueryCache;
import com.atlassian.jira.bc.issue.search.QueryCacheImpl;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.util.DefaultVisibilityValidator;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.vote.DefaultVoteService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.AutoWatchService;
import com.atlassian.jira.bc.issue.watcher.DefaultWatcherService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.worklog.DefaultWorklogService;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.bc.license.DefaultJiraServerIdProvider;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseServiceImpl;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.bc.license.JiraServerIdProvider;
import com.atlassian.jira.bc.portal.DefaultPortalPageService;
import com.atlassian.jira.bc.portal.GadgetApplinkUpgradeUtil;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.DefaultProjectService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.CachingProjectComponentStore;
import com.atlassian.jira.bc.project.component.DefaultProjectComponentManager;
import com.atlassian.jira.bc.project.component.DefaultProjectComponentService;
import com.atlassian.jira.bc.project.component.OfBizProjectComponentStore;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.bc.project.component.ProjectComponentStore;
import com.atlassian.jira.bc.project.projectoperation.DefaultProjectOperationManager;
import com.atlassian.jira.bc.project.projectoperation.ProjectOperationManager;
import com.atlassian.jira.bc.project.version.DefaultVersionService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.bc.projectroles.DefaultProjectRoleService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.scheme.distiller.DefaultSchemeDistillerService;
import com.atlassian.jira.bc.scheme.distiller.SchemeDistillerService;
import com.atlassian.jira.bc.scheme.mapper.DefaultSchemeGroupsToRoleTransformerService;
import com.atlassian.jira.bc.scheme.mapper.SchemeGroupsToRoleTransformerService;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.bc.security.login.LoginServiceImpl;
import com.atlassian.jira.bc.subtask.conversion.DefaultIssueToSubTaskConversionService;
import com.atlassian.jira.bc.subtask.conversion.DefaultSubTaskToIssueConversionService;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.bc.user.DefaultUserService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.bc.user.search.DefaultAssigneeService;
import com.atlassian.jira.bc.user.search.DefaultUserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.whitelist.DefaultWhitelistManager;
import com.atlassian.jira.bc.whitelist.DefaultWhitelistService;
import com.atlassian.jira.bc.whitelist.WhitelistManager;
import com.atlassian.jira.bc.whitelist.WhitelistService;
import com.atlassian.jira.bc.workflow.DefaultWorkflowService;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.DefaultBulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperationImpl;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.cache.HashRegistryCache;
import com.atlassian.jira.cache.HashRegistryCacheImpl;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.DefaultChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.ChartUtilsImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.DefaultConstantsManager;
import com.atlassian.jira.config.DefaultFeatureManager;
import com.atlassian.jira.config.DefaultIssueTypeManager;
import com.atlassian.jira.config.DefaultLocaleManager;
import com.atlassian.jira.config.DefaultPriorityManager;
import com.atlassian.jira.config.DefaultReindexMessageManager;
import com.atlassian.jira.config.DefaultResolutionManager;
import com.atlassian.jira.config.DefaultStatusManager;
import com.atlassian.jira.config.DefaultSubTaskManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.ResolutionManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.component.AppPropertiesComponentAdaptor;
import com.atlassian.jira.config.component.InvocationSwitcher;
import com.atlassian.jira.config.component.SimpleSwitchingComponentAdaptor;
import com.atlassian.jira.config.component.SwitchingInvocationAdapter;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.DatabaseConfigurationManagerImpl;
import com.atlassian.jira.config.database.MultiTenantDatabaseConfigurationLoader;
import com.atlassian.jira.config.database.SystemTenantDatabaseConfigurationLoader;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesChecker;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.BackingPropertySetManager;
import com.atlassian.jira.config.properties.DbBackedPropertiesManager;
import com.atlassian.jira.config.properties.MemorySwitchToDatabaseBackedPropertiesManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.DefaultIndexPathService;
import com.atlassian.jira.config.util.DefaultJiraHome;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexPathService;
import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.config.util.ThumbnailConfiguration;
import com.atlassian.jira.config.webwork.WebworkConfigurator;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.crowd.embedded.CrowdDelegatingI18Helper;
import com.atlassian.jira.crowd.embedded.DefaultJaacsService;
import com.atlassian.jira.crowd.embedded.JaacsService;
import com.atlassian.jira.crowd.embedded.JiraInstanceFactory;
import com.atlassian.jira.crowd.embedded.NoopClientProperties;
import com.atlassian.jira.crowd.embedded.NoopForgottenLoginManager;
import com.atlassian.jira.crowd.embedded.NoopPropertyManager;
import com.atlassian.jira.crowd.embedded.NoopTokenAuthenticationManager;
import com.atlassian.jira.crowd.embedded.NoopTrustedProxyManager;
import com.atlassian.jira.dashboard.JiraDashboardStateStoreManager;
import com.atlassian.jira.dashboard.JiraDirectoryPermissionService;
import com.atlassian.jira.dashboard.JiraExternalGadgetSpecStore;
import com.atlassian.jira.dashboard.JiraGadgetStateFactory;
import com.atlassian.jira.dashboard.JiraWhitelist;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.dashboard.permission.JiraGadgetPermissionManager;
import com.atlassian.jira.dashboard.permission.JiraPermissionService;
import com.atlassian.jira.dashboard.permission.JiraPluginGadgetSpecProviderPermission;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryImpl;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.event.DefaultListenerManager;
import com.atlassian.jira.event.JiraListenerHandlerConfigurationImpl;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.event.SubvertedListenerManager;
import com.atlassian.jira.event.issue.DefaultIssueEventManager;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.listeners.ProjectKeyRegexChangeListener;
import com.atlassian.jira.event.listeners.cache.JiraExternalLibrariesCacheClearingListener;
import com.atlassian.jira.event.listeners.mention.MentionEventListener;
import com.atlassian.jira.event.listeners.reindex.ReindexMessageListener;
import com.atlassian.jira.event.type.DefaultEventTypeManager;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.external.ExternalUtils;
import com.atlassian.jira.favourites.CachingFavouritesStore;
import com.atlassian.jira.favourites.DefaultFavouritesManager;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.favourites.FavouritesStore;
import com.atlassian.jira.favourites.OfBizFavouritesStore;
import com.atlassian.jira.hints.DefaultHintManager;
import com.atlassian.jira.hints.HintManager;
import com.atlassian.jira.image.dropdown.DefaultDropDownCreatorService;
import com.atlassian.jira.image.dropdown.DropDownCreatorService;
import com.atlassian.jira.image.separator.DefaultHeaderSeparatorService;
import com.atlassian.jira.image.separator.HeaderSeparatorService;
import com.atlassian.jira.image.util.ImageUtils;
import com.atlassian.jira.image.util.ImageUtilsImpl;
import com.atlassian.jira.imports.project.DefaultProjectImportManager;
import com.atlassian.jira.imports.project.DefaultProjectImportPersister;
import com.atlassian.jira.imports.project.ProjectImportManager;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.mapper.AutomaticDataMapper;
import com.atlassian.jira.imports.project.mapper.AutomaticDataMapperImpl;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.imports.project.validation.CustomFieldMapperValidator;
import com.atlassian.jira.imports.project.validation.CustomFieldMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.CustomFieldOptionMapperValidator;
import com.atlassian.jira.imports.project.validation.CustomFieldOptionMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.GroupMapperValidator;
import com.atlassian.jira.imports.project.validation.IssueLinkTypeMapperValidator;
import com.atlassian.jira.imports.project.validation.IssueLinkTypeMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.IssueSecurityLevelValidator;
import com.atlassian.jira.imports.project.validation.IssueTypeMapperValidator;
import com.atlassian.jira.imports.project.validation.IssueTypeMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.PriorityMapperValidator;
import com.atlassian.jira.imports.project.validation.ProjectImportValidators;
import com.atlassian.jira.imports.project.validation.ProjectImportValidatorsImpl;
import com.atlassian.jira.imports.project.validation.ProjectRoleActorMapperValidator;
import com.atlassian.jira.imports.project.validation.ProjectRoleActorMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.ProjectRoleMapperValidator;
import com.atlassian.jira.imports.project.validation.ResolutionMapperValidator;
import com.atlassian.jira.imports.project.validation.StatusMapperValidator;
import com.atlassian.jira.imports.project.validation.StatusMapperValidatorImpl;
import com.atlassian.jira.imports.project.validation.UserMapperValidator;
import com.atlassian.jira.imports.project.validation.UserMapperValidatorImpl;
import com.atlassian.jira.imports.xml.BackupXmlParser;
import com.atlassian.jira.imports.xml.DefaultBackupXmlParser;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationConfiguration;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.DefaultIssueFactory;
import com.atlassian.jira.issue.DefaultTemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.AttachmentZipKit;
import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.JqlChangeItemMapping;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.DefaultCommentManager;
import com.atlassian.jira.issue.comments.DefaultCommentPermissionManager;
import com.atlassian.jira.issue.comments.DefaultRecentCommentManager;
import com.atlassian.jira.issue.comments.RecentCommentManager;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.context.persistence.CachingFieldConfigContextPersister;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.customfields.ProjectImportLabelFieldParser;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverter;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverterImpl;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverterImpl;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.converters.DoubleConverterImpl;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.converters.GroupConverterImpl;
import com.atlassian.jira.issue.customfields.converters.MultiGroupConverter;
import com.atlassian.jira.issue.customfields.converters.MultiGroupConverterImpl;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverterImpl;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.converters.ProjectConverterImpl;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverterImpl;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.converters.StringConverterImpl;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.converters.UserConverterImpl;
import com.atlassian.jira.issue.customfields.impl.ProjectImportLabelFieldParserImpl;
import com.atlassian.jira.issue.customfields.manager.CachedGenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.CachedOptionsManager;
import com.atlassian.jira.issue.customfields.manager.DefaultGenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.EagerLoadingOfBizCustomFieldPersister;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.DefaultCustomFieldInputHelper;
import com.atlassian.jira.issue.fields.Assignees;
import com.atlassian.jira.issue.fields.ColumnViewDateTimeHelper;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.DefaultFieldManager;
import com.atlassian.jira.issue.fields.DefaultProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.DefaultTextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.FieldContextGenerator;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.TextFieldLimitProvider;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigCleanup;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigCleanupImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManagerImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManagerImpl;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManagerImpl;
import com.atlassian.jira.issue.fields.config.persistence.CachedFieldConfigSchemePersister;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersisterImpl;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigSchemePersister;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.DefaultColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.DefaultFieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldDescriptionHelper;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.option.CachedOptionSetManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.OptionSetPersister;
import com.atlassian.jira.issue.fields.option.OptionSetPersisterImpl;
import com.atlassian.jira.issue.fields.renderer.DefaultHackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.renderer.wiki.JiraIconManager;
import com.atlassian.jira.issue.fields.renderer.wiki.JiraRendererConfiguration;
import com.atlassian.jira.issue.fields.renderer.wiki.WikiMacroManager;
import com.atlassian.jira.issue.fields.renderer.wiki.embedded.JiraEmbeddedResourceRenderer;
import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraLinkResolver;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactoryImpl;
import com.atlassian.jira.issue.fields.rest.IssueFinder;
import com.atlassian.jira.issue.fields.rest.IssueFinderImpl;
import com.atlassian.jira.issue.fields.rest.IssueLinkTypeFinder;
import com.atlassian.jira.issue.fields.rest.IssueLinkTypeFinderImpl;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactoryImpl;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrlsImpl;
import com.atlassian.jira.issue.fields.screen.CachingFieldScreenStore;
import com.atlassian.jira.issue.fields.screen.DefaultFieldScreenManager;
import com.atlassian.jira.issue.fields.screen.DefaultFieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.DefaultFieldScreenStore;
import com.atlassian.jira.issue.fields.screen.DefaultProjectFieldScreenHelper;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactoryImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenStore;
import com.atlassian.jira.issue.fields.screen.ProjectFieldScreenHelper;
import com.atlassian.jira.issue.fields.screen.issuetype.DefaultIssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.DefaultProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelperImpl;
import com.atlassian.jira.issue.index.BulkOnlyIndexManager;
import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.jira.issue.index.DefaultChangeHistoryRetriever;
import com.atlassian.jira.issue.index.DefaultCommentRetriever;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.DefaultIndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.index.DefaultIssueIndexer;
import com.atlassian.jira.issue.index.IndexDirectoryFactory;
import com.atlassian.jira.issue.index.IndexedChangeHistoryFieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.index.managers.FieldIndexerManagerImpl;
import com.atlassian.jira.issue.label.AlphabeticalLabelRenderer;
import com.atlassian.jira.issue.label.CachingLabelStore;
import com.atlassian.jira.issue.label.DefaultAlphabeticalLabelRenderer;
import com.atlassian.jira.issue.label.DefaultLabelManager;
import com.atlassian.jira.issue.label.DefaultLabelUtil;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelStore;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.label.OfBizLabelStore;
import com.atlassian.jira.issue.link.DefaultIssueLinkCreator;
import com.atlassian.jira.issue.link.DefaultIssueLinkManager;
import com.atlassian.jira.issue.link.DefaultIssueLinkTypeManager;
import com.atlassian.jira.issue.link.DefaultRemoteIssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkCreator;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyer;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyerImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLinkStore;
import com.atlassian.jira.issue.link.RemoteIssueLinkStoreImpl;
import com.atlassian.jira.issue.managers.DefaultAttachmentManager;
import com.atlassian.jira.issue.managers.DefaultCustomFieldManager;
import com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper;
import com.atlassian.jira.issue.managers.DefaultIssueManager;
import com.atlassian.jira.issue.managers.DefaultRendererManager;
import com.atlassian.jira.issue.managers.IssueDeleteHelper;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.issue.search.CachingSearchRequestStore;
import com.atlassian.jira.issue.search.DefaultReaderCache;
import com.atlassian.jira.issue.search.DefaultSearchRequestAdminManager;
import com.atlassian.jira.issue.search.DefaultSearchRequestFactory;
import com.atlassian.jira.issue.search.DefaultSearchRequestManager;
import com.atlassian.jira.issue.search.DefaultSystemClauseHandlerFactory;
import com.atlassian.jira.issue.search.OfBizSearchRequestStore;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchContextFactory;
import com.atlassian.jira.issue.search.SearchContextFactoryImpl;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchProviderFactoryImpl;
import com.atlassian.jira.issue.search.SearchRequestAdminManager;
import com.atlassian.jira.issue.search.SearchRequestFactory;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchRequestStore;
import com.atlassian.jira.issue.search.SystemClauseHandlerFactory;
import com.atlassian.jira.issue.search.handlers.AffectedVersionSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.AssigneeSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.CommentSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.ComponentSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.CreatedDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.DescriptionSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.DueDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.EnvironmentSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.FixForVersionSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.IssueTypeSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.LabelsSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.PrioritySearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.ProjectSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.ReporterSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.ResolutionDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.ResolutionSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.StatusSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.SummarySearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.UpdatedDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.WorkRatioSearchHandlerFactory;
import com.atlassian.jira.issue.search.managers.DefaultIssueSearcherManager;
import com.atlassian.jira.issue.search.managers.DefaultSearchHandlerManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.parameters.lucene.DefaultPermissionQueryFactory;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionQueryFactory;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGeneratorImpl;
import com.atlassian.jira.issue.search.providers.LuceneSearchProvider;
import com.atlassian.jira.issue.search.searchers.impl.AssigneeSearcher;
import com.atlassian.jira.issue.search.searchers.impl.PrioritySearcher;
import com.atlassian.jira.issue.search.searchers.impl.ReporterSearcher;
import com.atlassian.jira.issue.search.searchers.impl.ResolutionSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.DefaultFieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.util.DefaultLuceneQueryModifier;
import com.atlassian.jira.issue.search.util.DefaultQueryCreator;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.issue.search.util.QueryCreator;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.search.util.SearchSortUtilImpl;
import com.atlassian.jira.issue.security.DefaultProjectIssueSecuritySchemeHelper;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.issue.security.IssueSecurityHelperImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManagerImpl;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.issue.security.IssueSecurityTypeManager;
import com.atlassian.jira.issue.security.ProjectIssueSecuritySchemeHelper;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.issue.statistics.ComponentStatisticsMapper;
import com.atlassian.jira.issue.statistics.FixForVersionStatisticsMapper;
import com.atlassian.jira.issue.statistics.IssueKeyStatisticsMapper;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.statistics.PriorityStatisticsMapper;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.RaisedInVersionStatisticsMapper;
import com.atlassian.jira.issue.statistics.ReporterStatisticsMapper;
import com.atlassian.jira.issue.statistics.ResolutionStatisticsMapper;
import com.atlassian.jira.issue.statistics.SecurityLevelStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatusStatisticsMapper;
import com.atlassian.jira.issue.statistics.SubTaskStatisticsMapper;
import com.atlassian.jira.issue.subscription.DefaultSubscriptionManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.thumbnail.DefaultThumbnailManager;
import com.atlassian.jira.issue.thumbnail.DisabledThumbNailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactoryImpl;
import com.atlassian.jira.issue.util.DefaultIssueUpdater;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.views.util.DefaultIssueViewUtil;
import com.atlassian.jira.issue.views.util.DefaultSearchRequestHeader;
import com.atlassian.jira.issue.views.util.DefaultSearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.DefaultSearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.SearchRequestHeader;
import com.atlassian.jira.issue.views.util.SearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.vote.DefaultIssueVoterAccessor;
import com.atlassian.jira.issue.vote.DefaultVoteManager;
import com.atlassian.jira.issue.vote.DefaultVotedIssuesAccessor;
import com.atlassian.jira.issue.vote.IssueVoterAccessor;
import com.atlassian.jira.issue.vote.OfbizVoteHistoryStore;
import com.atlassian.jira.issue.vote.VoteHistoryStore;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.vote.VotedIssuesAccessor;
import com.atlassian.jira.issue.watchers.DefaultIssueWatcherAccessor;
import com.atlassian.jira.issue.watchers.DefaultWatchedIssuesAccessor;
import com.atlassian.jira.issue.watchers.DefaultWatcherManager;
import com.atlassian.jira.issue.watchers.IssueWatcherAccessor;
import com.atlassian.jira.issue.watchers.WatchedIssuesAccessor;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.issue.worklog.DefaultTimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.DefaultWorklogManager;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.issue.worklog.WorklogStore;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.context.AllTextClauseContextFactory;
import com.atlassian.jira.jql.context.ComponentClauseContextFactory;
import com.atlassian.jira.jql.context.ContextSetUtil;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.context.IssueIdClauseContextFactory;
import com.atlassian.jira.jql.context.IssueParentClauseContextFactory;
import com.atlassian.jira.jql.context.IssueSecurityLevelClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.ProjectCategoryClauseContextFactory;
import com.atlassian.jira.jql.context.QueryContextVisitor;
import com.atlassian.jira.jql.context.SavedFilterClauseContextFactory;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.context.StatusClauseContextFactory;
import com.atlassian.jira.jql.context.VersionClauseContextFactory;
import com.atlassian.jira.jql.operand.DefaultJqlOperandResolver;
import com.atlassian.jira.jql.operand.DefaultPredicateOperandResolver;
import com.atlassian.jira.jql.operand.EmptyWasClauseOperandHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.PredicateOperandHandlerRegistry;
import com.atlassian.jira.jql.operand.PredicateOperandResolver;
import com.atlassian.jira.jql.operand.registry.DefaultPredicateRegistry;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.operand.registry.PluginsAwareJqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.operand.registry.PredicateRegistry;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.permission.CustomFieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.query.AffectedVersionClauseQueryFactory;
import com.atlassian.jira.jql.query.AllTextClauseQueryFactory;
import com.atlassian.jira.jql.query.AssigneeClauseQueryFactory;
import com.atlassian.jira.jql.query.ChangedClauseQueryFactory;
import com.atlassian.jira.jql.query.CommentClauseQueryFactory;
import com.atlassian.jira.jql.query.ComponentClauseQueryFactory;
import com.atlassian.jira.jql.query.CreatedDateClauseQueryFactory;
import com.atlassian.jira.jql.query.CurrentEstimateClauseQueryFactory;
import com.atlassian.jira.jql.query.DefaultLuceneQueryBuilder;
import com.atlassian.jira.jql.query.DefaultQueryRegistry;
import com.atlassian.jira.jql.query.DescriptionClauseQueryFactory;
import com.atlassian.jira.jql.query.DueDateClauseQueryFactory;
import com.atlassian.jira.jql.query.EnvironmentClauseQueryFactory;
import com.atlassian.jira.jql.query.FixForVersionClauseQueryFactory;
import com.atlassian.jira.jql.query.HistoryPredicateQueryFactory;
import com.atlassian.jira.jql.query.IssueIdClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueParentClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueSecurityLevelClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueTypeClauseQueryFactory;
import com.atlassian.jira.jql.query.LuceneQueryBuilder;
import com.atlassian.jira.jql.query.OriginalEstimateClauseQueryFactory;
import com.atlassian.jira.jql.query.PriorityClauseQueryFactory;
import com.atlassian.jira.jql.query.ProjectCategoryClauseQueryFactory;
import com.atlassian.jira.jql.query.ProjectClauseQueryFactory;
import com.atlassian.jira.jql.query.QueryRegistry;
import com.atlassian.jira.jql.query.ReporterClauseQueryFactory;
import com.atlassian.jira.jql.query.ResolutionClauseQueryFactory;
import com.atlassian.jira.jql.query.ResolutionDateClauseQueryFactory;
import com.atlassian.jira.jql.query.SavedFilterClauseQueryFactory;
import com.atlassian.jira.jql.query.StatusClauseQueryFactory;
import com.atlassian.jira.jql.query.SummaryClauseQueryFactory;
import com.atlassian.jira.jql.query.TimeSpentClauseQueryFactory;
import com.atlassian.jira.jql.query.UpdatedDateClauseQueryFactory;
import com.atlassian.jira.jql.query.VoterClauseQueryFactory;
import com.atlassian.jira.jql.query.VotesClauseQueryFactory;
import com.atlassian.jira.jql.query.WasClauseQueryFactory;
import com.atlassian.jira.jql.query.WatcherClauseQueryFactory;
import com.atlassian.jira.jql.query.WatchesClauseQueryFactory;
import com.atlassian.jira.jql.query.WorkRatioClauseQueryFactory;
import com.atlassian.jira.jql.resolver.ChangeHistoryFieldIdResolver;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.jql.resolver.CustomFieldOptionResolver;
import com.atlassian.jira.jql.resolver.IssueSecurityLevelResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.resolver.ResolutionResolver;
import com.atlassian.jira.jql.resolver.ResolverManager;
import com.atlassian.jira.jql.resolver.ResolverManagerImpl;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.resolver.UserResolverImpl;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueKeySupportImpl;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.jql.util.JqlIssueSupportImpl;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.jql.util.JqlLocalDateSupportImpl;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupportImpl;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.jql.util.WatchesIndexValueConverter;
import com.atlassian.jira.jql.validator.AffectedVersionValidator;
import com.atlassian.jira.jql.validator.AllTextValidator;
import com.atlassian.jira.jql.validator.AssigneeValidator;
import com.atlassian.jira.jql.validator.ChangedClauseValidator;
import com.atlassian.jira.jql.validator.CommentValidator;
import com.atlassian.jira.jql.validator.ComponentValidator;
import com.atlassian.jira.jql.validator.CreatedDateValidator;
import com.atlassian.jira.jql.validator.CurrentEstimateValidator;
import com.atlassian.jira.jql.validator.DefaultOperatorUsageValidator;
import com.atlassian.jira.jql.validator.DefaultOrderByValidator;
import com.atlassian.jira.jql.validator.DefaultValidatorRegistry;
import com.atlassian.jira.jql.validator.DescriptionValidator;
import com.atlassian.jira.jql.validator.DueDateValidator;
import com.atlassian.jira.jql.validator.EnvironmentValidator;
import com.atlassian.jira.jql.validator.FixForVersionValidator;
import com.atlassian.jira.jql.validator.HistoryFieldValueValidator;
import com.atlassian.jira.jql.validator.IssueIdValidator;
import com.atlassian.jira.jql.validator.IssueParentValidator;
import com.atlassian.jira.jql.validator.IssueSecurityLevelClauseValidator;
import com.atlassian.jira.jql.validator.IssueTypeValidator;
import com.atlassian.jira.jql.validator.LabelsValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.validator.OrderByValidator;
import com.atlassian.jira.jql.validator.OriginalEstimateValidator;
import com.atlassian.jira.jql.validator.PriorityValidator;
import com.atlassian.jira.jql.validator.ProjectCategoryValidator;
import com.atlassian.jira.jql.validator.ProjectValidator;
import com.atlassian.jira.jql.validator.ReporterValidator;
import com.atlassian.jira.jql.validator.ResolutionDateValidator;
import com.atlassian.jira.jql.validator.ResolutionValidator;
import com.atlassian.jira.jql.validator.SavedFilterClauseValidator;
import com.atlassian.jira.jql.validator.SavedFilterCycleDetector;
import com.atlassian.jira.jql.validator.StatusValidator;
import com.atlassian.jira.jql.validator.SummaryValidator;
import com.atlassian.jira.jql.validator.TimeSpentValidator;
import com.atlassian.jira.jql.validator.UpdatedDateValidator;
import com.atlassian.jira.jql.validator.UserCustomFieldValidator;
import com.atlassian.jira.jql.validator.ValidatorRegistry;
import com.atlassian.jira.jql.validator.ValidatorVisitor;
import com.atlassian.jira.jql.validator.VotesValidator;
import com.atlassian.jira.jql.validator.WasClauseValidator;
import com.atlassian.jira.jql.validator.WatchesValidator;
import com.atlassian.jira.jql.validator.WorkRatioValidator;
import com.atlassian.jira.jql.values.CustomFieldOptionsClauseValuesGenerator;
import com.atlassian.jira.jql.values.GroupValuesGenerator;
import com.atlassian.jira.jql.values.LabelsClauseValuesGenerator;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.JiraLicenseManagerImpl;
import com.atlassian.jira.license.JiraLicenseStore;
import com.atlassian.jira.license.JiraLicenseStoreImpl;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.license.LicenseJohnsonEventRaiserImpl;
import com.atlassian.jira.license.LicenseStringFactory;
import com.atlassian.jira.license.LicenseStringFactoryImpl;
import com.atlassian.jira.mail.DefaultIssueMailQueueItemFactory;
import com.atlassian.jira.mail.DefaultMailLoggingManager;
import com.atlassian.jira.mail.DefaultSubscriptionMailQueueItemFactory;
import com.atlassian.jira.mail.DefaultTemplateContextFactory;
import com.atlassian.jira.mail.DefaultTemplateIssueFactory;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.mail.MailServiceImpl;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.mail.MailThreadManagerImpl;
import com.atlassian.jira.mail.MailingListCompiler;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.mail.TemplateContextFactory;
import com.atlassian.jira.mail.TemplateIssueFactory;
import com.atlassian.jira.mention.MentionFinder;
import com.atlassian.jira.mention.MentionFinderImpl;
import com.atlassian.jira.mention.MentionService;
import com.atlassian.jira.mention.MentionServiceImpl;
import com.atlassian.jira.mention.commands.EmailMentionedUsers;
import com.atlassian.jira.movesubtask.DefaultMoveSubTaskOperationManager;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskParentOperation;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskTypeOperation;
import com.atlassian.jira.multitenant.EventPublisherDestroyer;
import com.atlassian.jira.multitenant.MultiTenantHostComponentProvider;
import com.atlassian.jira.multitenant.MultiTenantHostComponentProxier;
import com.atlassian.jira.multitenant.PluginsEventPublisher;
import com.atlassian.jira.notification.DefaultNotificationSchemeManager;
import com.atlassian.jira.notification.DefaultProjectNotificationsSchemeHelper;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.notification.ProjectNotificationsSchemeHelper;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.DefaultPermissionContextFactory;
import com.atlassian.jira.permission.DefaultProjectPermissionSchemeHelper;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermissionSchemeHelper;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.permission.WorkflowBasedPermissionSchemeManager;
import com.atlassian.jira.permission.WorkflowPermissionFactory;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.DefaultComponentClassManager;
import com.atlassian.jira.plugin.DefaultPackageScannerConfiguration;
import com.atlassian.jira.plugin.DefaultPluginLoaderFactory;
import com.atlassian.jira.plugin.JiraCacheResetter;
import com.atlassian.jira.plugin.JiraContentTypeResolver;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.jira.plugin.JiraModuleFactory;
import com.atlassian.jira.plugin.JiraOsgiContainerManager;
import com.atlassian.jira.plugin.JiraPluginManager;
import com.atlassian.jira.plugin.JiraPluginPersistentStateStore;
import com.atlassian.jira.plugin.JiraPluginResourceDownload;
import com.atlassian.jira.plugin.JiraServletContextFactory;
import com.atlassian.jira.plugin.OfBizPluginVersionStore;
import com.atlassian.jira.plugin.PluginLoaderFactory;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.plugin.PluginVersionStore;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.plugin.assignee.impl.DefaultAssigneeResolver;
import com.atlassian.jira.plugin.componentpanel.fragment.impl.ComponentDescriptionFragment;
import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelInvoker;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelInvokerImpl;
import com.atlassian.jira.plugin.issueview.DefaultIssueViewURLHandler;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParamsHelper;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParamsHelperImpl;
import com.atlassian.jira.plugin.issueview.IssueViewURLHandler;
import com.atlassian.jira.plugin.keyboardshortcut.CachingKeyboardShortcutManager;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinker;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinkerImpl;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomePreference;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomePreferenceOsgiDelegator;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ActivityStreamFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.CreatedVsResolvedFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.DueVersionsFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.FiltersMenuVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ProjectAdminMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ProjectDescriptionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.RecentIssuesFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ReleaseNotesMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.ReportsMenuFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.StatusSummaryFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByAssigneeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByComponentFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByFixVersionFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByIssueTypeFragment;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.UnresolvedIssuesByPriorityFragment;
import com.atlassian.jira.plugin.projectpanel.impl.VersionDrillDownRenderer;
import com.atlassian.jira.plugin.searchrequestview.DefaultSearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.plugin.searchrequestview.auth.Authorizer;
import com.atlassian.jira.plugin.searchrequestview.auth.AuthorizerImpl;
import com.atlassian.jira.plugin.studio.StudioHooks;
import com.atlassian.jira.plugin.userformat.DefaultUserFormatManager;
import com.atlassian.jira.plugin.userformat.DefaultUserFormats;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.plugin.userformat.configuration.PluginsAwareUserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.DefaultUserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.DefaultUserFormatTypes;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatTypes;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.plugin.util.PluginModuleTrackerFactory;
import com.atlassian.jira.plugin.util.orderings.DefaultModuleDescriptorOrderingsFactory;
import com.atlassian.jira.plugin.versionpanel.fragment.impl.VersionDescriptionFragment;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.ConditionDescriptorFactoryImpl;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkFactoryModuleDescriptors;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.JiraWebFragmentHelper;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactoryModuleDescriptors;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webresource.JiraWebResourceBatchingConfiguration;
import com.atlassian.jira.plugin.webresource.JiraWebResourceIntegration;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManagerImpl;
import com.atlassian.jira.plugin.webwork.AutowireCapableWebworkActionRegistry;
import com.atlassian.jira.plugin.webwork.DefaultAutowireCapableWebworkActionRegistry;
import com.atlassian.jira.plugin.webwork.WebworkPluginSecurityServiceHelper;
import com.atlassian.jira.portal.CachingPortalPageStore;
import com.atlassian.jira.portal.CachingPortletConfigurationStore;
import com.atlassian.jira.portal.DefaultPortalPageManager;
import com.atlassian.jira.portal.OfBizPortalPageStore;
import com.atlassian.jira.portal.OfbizPortletConfigurationStore;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.portal.PortletConfigurationManagerImpl;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.portal.gadgets.CachingExternalGadgetStore;
import com.atlassian.jira.portal.gadgets.OfbizExternalGadgetStore;
import com.atlassian.jira.project.CachingProjectManager;
import com.atlassian.jira.project.DefaultProjectFactory;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectCategoryStore;
import com.atlassian.jira.project.ProjectCategoryStoreImpl;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.project.renderer.ProjectDescriptionRendererImpl;
import com.atlassian.jira.project.util.ReleaseNoteManager;
import com.atlassian.jira.project.version.CachingVersionStore;
import com.atlassian.jira.project.version.DefaultVersionManager;
import com.atlassian.jira.project.version.OfBizVersionStore;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.project.version.VersionStore;
import com.atlassian.jira.propertyset.DefaultJiraPropertySetFactory;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.render.SwitchingEncoder;
import com.atlassian.jira.scheduler.JiraSchedulerFactory;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.DefaultSchemeManagerFactory;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.SchemeDistiller;
import com.atlassian.jira.scheme.distiller.SchemeDistillerImpl;
import com.atlassian.jira.scheme.mapper.SchemeGroupsToRolesTransformer;
import com.atlassian.jira.scheme.mapper.SchemeGroupsToRolesTransformerImpl;
import com.atlassian.jira.security.DefaultGlobalPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.SubvertedPermissionManager;
import com.atlassian.jira.security.ThreadLocalCachingPermissionManager;
import com.atlassian.jira.security.WorkflowBasedPermissionManager;
import com.atlassian.jira.security.auth.rememberme.JiraRememberMeConfiguration;
import com.atlassian.jira.security.auth.rememberme.JiraRememberMeService;
import com.atlassian.jira.security.auth.rememberme.JiraRememberMeTokenDao;
import com.atlassian.jira.security.auth.trustedapps.CachingTrustedApplicationManager;
import com.atlassian.jira.security.auth.trustedapps.CachingTrustedApplicationStore;
import com.atlassian.jira.security.auth.trustedapps.CurrentApplicationFactory;
import com.atlassian.jira.security.auth.trustedapps.CurrentApplicationStore;
import com.atlassian.jira.security.auth.trustedapps.DefaultCurrentApplicationFactory;
import com.atlassian.jira.security.auth.trustedapps.DefaultCurrentApplicationStore;
import com.atlassian.jira.security.auth.trustedapps.DefaultTrustedApplicationManager;
import com.atlassian.jira.security.auth.trustedapps.DefaultTrustedApplicationService;
import com.atlassian.jira.security.auth.trustedapps.DefaultTrustedApplicationStore;
import com.atlassian.jira.security.auth.trustedapps.DefaultTrustedApplicationValidator;
import com.atlassian.jira.security.auth.trustedapps.SeraphTrustedApplicationsManager;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationManager;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationStore;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationValidator;
import com.atlassian.jira.security.groups.DefaultGroupManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.login.LoginManagerImpl;
import com.atlassian.jira.security.login.LoginStore;
import com.atlassian.jira.security.login.LoginStoreImpl;
import com.atlassian.jira.security.roles.CachingProjectRoleAndActorStore;
import com.atlassian.jira.security.roles.DefaultProjectRoleManager;
import com.atlassian.jira.security.roles.OfBizProjectRoleAndActorStore;
import com.atlassian.jira.security.roles.PluginDelegatingRoleActorFactory;
import com.atlassian.jira.security.roles.ProjectRoleAndActorStore;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManagerImpl;
import com.atlassian.jira.security.xsrf.DefaultXsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.SimpleXsrfTokenGenerator;
import com.atlassian.jira.security.xsrf.XsrfDefaults;
import com.atlassian.jira.security.xsrf.XsrfDefaultsImpl;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.service.DefaultInBuiltServiceTypes;
import com.atlassian.jira.service.DefaultServiceManager;
import com.atlassian.jira.service.DefaultServiceTypes;
import com.atlassian.jira.service.InBuiltServiceTypes;
import com.atlassian.jira.service.OfBizServiceConfigStore;
import com.atlassian.jira.service.ServiceConfigStore;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.ServiceTypes;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.service.util.handler.MessageUserProcessorImpl;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.servlet.JiraCaptchaServiceImpl;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.sharing.CachingSharePermissionStore;
import com.atlassian.jira.sharing.DefaultShareManager;
import com.atlassian.jira.sharing.DefaultSharePermissionReindexer;
import com.atlassian.jira.sharing.DefaultShareTypeValidatorUtils;
import com.atlassian.jira.sharing.DefaultSharedEntityAccessorFactory;
import com.atlassian.jira.sharing.OfBizSharePermissionStore;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.sharing.SharePermissionReindexer;
import com.atlassian.jira.sharing.SharePermissionStore;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.index.DefaultQueryFactory;
import com.atlassian.jira.sharing.index.DefaultSharedEntityIndexManager;
import com.atlassian.jira.sharing.index.DefaultSharedEntityIndexer;
import com.atlassian.jira.sharing.index.DirectoryFactory;
import com.atlassian.jira.sharing.index.IndexPathDirectoryFactory;
import com.atlassian.jira.sharing.index.IsSharedQueryFactory;
import com.atlassian.jira.sharing.index.QueryFactory;
import com.atlassian.jira.sharing.index.SharedEntityIndexManager;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.index.SharedEntitySearchContextToQueryFactoryMap;
import com.atlassian.jira.sharing.type.DefaultShareTypeFactory;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GlobalShareTypeRenderer;
import com.atlassian.jira.sharing.type.GlobalShareTypeValidator;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.GroupShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.GroupShareTypeRenderer;
import com.atlassian.jira.sharing.type.GroupShareTypeValidator;
import com.atlassian.jira.sharing.type.ProjectSharePermissionComparator;
import com.atlassian.jira.sharing.type.ProjectShareQueryFactory;
import com.atlassian.jira.sharing.type.ProjectShareType;
import com.atlassian.jira.sharing.type.ProjectShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ProjectShareTypeRenderer;
import com.atlassian.jira.sharing.type.ProjectShareTypeValidator;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.soap.axis.SoapAttachmentHelper;
import com.atlassian.jira.soap.axis.SoapAttachmentHelperImpl;
import com.atlassian.jira.startup.JiraStartupPluginSystemListener;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.startup.PluginInfoProviderImpl;
import com.atlassian.jira.studio.PluginStudioHooks;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskManagerImpl;
import com.atlassian.jira.template.DefaultTemplateManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.template.soy.SoyTemplateRendererProviderImpl;
import com.atlassian.jira.template.velocity.DefaultVelocityTemplatingEngine;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.template.velocity.VelocityTemplateCache;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.timezone.TimeZoneManagerImpl;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.timezone.TimeZoneServiceCachingDecorator;
import com.atlassian.jira.timezone.TimeZoneServiceImpl;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.transaction.TransactionSupportImpl;
import com.atlassian.jira.upgrade.BuildVersionRegistry;
import com.atlassian.jira.upgrade.DefaultBuildVersionRegistry;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.upgrade.UpgradeManagerImpl;
import com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandlerRegistry;
import com.atlassian.jira.upgrade.tasks.jql.DefaultClauseXmlHandlerRegistry;
import com.atlassian.jira.upgrade.tasks.jql.DefaultOrderByXmlHandler;
import com.atlassian.jira.upgrade.tasks.jql.OrderByXmlHandler;
import com.atlassian.jira.upgrade.tasks.util.Sequences;
import com.atlassian.jira.upgrade.util.DefaultLegacyPortletUpgradeTaskFactory;
import com.atlassian.jira.upgrade.util.LegacyPortletUpgradeTaskFactory;
import com.atlassian.jira.upgrade.util.UpgradeUtils;
import com.atlassian.jira.user.AutoGroupAdder;
import com.atlassian.jira.user.AutoGroupAdderImpl;
import com.atlassian.jira.user.CachingExternalEntityStore;
import com.atlassian.jira.user.CachingUserHistoryStore;
import com.atlassian.jira.user.DefaultSecureUserTokenManager;
import com.atlassian.jira.user.DefaultUserAdminHistoryManager;
import com.atlassian.jira.user.DefaultUserHistoryManager;
import com.atlassian.jira.user.DefaultUserIssueHistoryManager;
import com.atlassian.jira.user.DefaultUserProjectHistoryManager;
import com.atlassian.jira.user.DefaultUserPropertyManager;
import com.atlassian.jira.user.DefaultUserQueryHistoryManager;
import com.atlassian.jira.user.ExternalEntityStore;
import com.atlassian.jira.user.OfBizUserHistoryStore;
import com.atlassian.jira.user.OfbizExternalEntityStore;
import com.atlassian.jira.user.SecureUserTokenManager;
import com.atlassian.jira.user.SessionBasedAnonymousUserHistoryStore;
import com.atlassian.jira.user.UserAdminHistoryManager;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserHistoryStore;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.UserQueryHistoryManager;
import com.atlassian.jira.user.directory.loader.JiraDbCachingRemoteDirectoryInstanceLoader;
import com.atlassian.jira.user.preferences.DefaultUserPreferencesManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.DefaultUserManager;
import com.atlassian.jira.user.util.GlobalUserPreferencesUtil;
import com.atlassian.jira.user.util.GlobalUserPreferencesUtilImpl;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserSharingPreferencesUtil;
import com.atlassian.jira.user.util.UserSharingPreferencesUtilImpl;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.user.util.UserUtilImpl;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.DateFieldFormatImpl;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.EmailFormatterImpl;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.FileSystemFileFactory;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.GroupPermissionCheckerImpl;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraComponentFactory;
import com.atlassian.jira.util.JiraComponentLocator;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.JiraContactHelperImpl;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraKeyUtilsBean;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.JiraUtilsBean;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.LuceneDirectoryUtilsImpl;
import com.atlassian.jira.util.UnsupportedBrowserManager;
import com.atlassian.jira.util.UriValidator;
import com.atlassian.jira.util.UriValidatorFactory;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.util.VelocityParamFactoryImpl;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.i18n.I18nTranslationModeImpl;
import com.atlassian.jira.util.index.CompositeIndexLifecycleManager;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.util.log.JiraLogLocator;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.util.system.JiraSystemRestarterImpl;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.util.system.SystemInfoUtilsImpl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.vcs.DefaultRepositoryManager;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtilImpl;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.FieldVisibilityManagerImpl;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.SafeRedirectChecker;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldContextConfigHelper;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldContextConfigHelperImpl;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidatorImpl;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelperImpl;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.action.admin.translation.TranslationManagerImpl;
import com.atlassian.jira.web.action.issue.BugAssociatorPrefs;
import com.atlassian.jira.web.action.issue.DefaultBugAssociatorPrefs;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBeanImpl;
import com.atlassian.jira.web.action.issue.IssueSearchLimits;
import com.atlassian.jira.web.action.issue.IssueSearchLimitsImpl;
import com.atlassian.jira.web.action.issue.UpdateFieldsHelperBean;
import com.atlassian.jira.web.action.issue.UpdateFieldsHelperBeanImpl;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapper;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapperImpl;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarLanguageUtilImpl;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.jira.web.action.util.DefaultImportResultHandler;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.jira.web.action.util.FieldsResourceIncluder;
import com.atlassian.jira.web.action.util.FieldsResourceIncluderImpl;
import com.atlassian.jira.web.action.util.ImportResultHandler;
import com.atlassian.jira.web.action.util.PopularIssueTypesUtil;
import com.atlassian.jira.web.action.util.PopularIssueTypesUtilImpl;
import com.atlassian.jira.web.action.util.SearchRequestDisplayBean;
import com.atlassian.jira.web.bean.ApplicationPropertiesBackedNonZipExpandableExtensions;
import com.atlassian.jira.web.bean.BulkEditBeanFactory;
import com.atlassian.jira.web.bean.BulkEditBeanFactoryImpl;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.NonZipExpandableExtensions;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactory;
import com.atlassian.jira.web.bean.TimeTrackingGraphBeanFactoryImpl;
import com.atlassian.jira.web.component.ModuleWebComponent;
import com.atlassian.jira.web.component.ModuleWebComponentImpl;
import com.atlassian.jira.web.component.TableLayoutFactory;
import com.atlassian.jira.web.component.TableLayoutUtils;
import com.atlassian.jira.web.component.TableLayoutUtilsImpl;
import com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator;
import com.atlassian.jira.web.component.jql.DefaultAutoCompleteJsonGenerator;
import com.atlassian.jira.web.component.subtask.ColumnLayoutItemFactory;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import com.atlassian.jira.web.dispatcher.PluginsAwareViewMapping;
import com.atlassian.jira.web.servlet.MimeSniffingKit;
import com.atlassian.jira.web.servlet.rpc.AxisServletProvider;
import com.atlassian.jira.web.servlet.rpc.PluggableAxisServletProvider;
import com.atlassian.jira.web.servlet.rpc.PluggableXmlRpcRequestProcessor;
import com.atlassian.jira.web.servlet.rpc.XmlRpcRequestProcessor;
import com.atlassian.jira.web.session.DefaultSessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.currentusers.JiraUserSessionTracker;
import com.atlassian.jira.web.util.AuthorizationSupport;
import com.atlassian.jira.web.util.DefaultAuthorizationSupport;
import com.atlassian.jira.web.util.DefaultWebAttachmentManager;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.FileIconUtil;
import com.atlassian.jira.web.util.FileIconUtilImpl;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.web.util.OutlookDateManagerImpl;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.jira.workflow.CachingDraftWorkflowStore;
import com.atlassian.jira.workflow.CachingWorkflowDescriptorStore;
import com.atlassian.jira.workflow.DefaultOSWorkflowConfigurator;
import com.atlassian.jira.workflow.DefaultProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.DefaultWorkflowSchemeManager;
import com.atlassian.jira.workflow.DefaultWorkflowsRepository;
import com.atlassian.jira.workflow.DraftWorkflowStore;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.IssueWorkflowManagerImpl;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.jira.workflow.OSWorkflowManager;
import com.atlassian.jira.workflow.OfBizDraftWorkflowStore;
import com.atlassian.jira.workflow.OfBizWorkflowDescriptorStore;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.jira.workflow.WorkflowDescriptorStore;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtilFactory;
import com.atlassian.jira.workflow.WorkflowTransitionUtilFactoryImpl;
import com.atlassian.jira.workflow.WorkflowsRepository;
import com.atlassian.jira.workflow.configuration.ConfigurationFactory;
import com.atlassian.jira.workflow.migration.DefaultMigrationHelperFactory;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.jira.workflow.names.DefaultWorkflowCopyNameFactory;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;
import com.atlassian.license.DefaultSIDManager;
import com.atlassian.license.SIDManager;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueImpl;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.multitenant.MultiTenantComponentFactory;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.MultiTenantManager;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;
import com.atlassian.multitenant.event.DefaultPeeringEventPublisherManager;
import com.atlassian.multitenant.event.PeeringEventPublisher;
import com.atlassian.multitenant.event.PeeringEventPublisherManager;
import com.atlassian.multitenant.plugins.MultiTenantModuleDescriptorFactory;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import com.atlassian.plugin.metadata.DefaultPluginMetadataManager;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.atlassian.plugin.servlet.ContentTypeResolver;
import com.atlassian.plugin.servlet.DefaultServletModuleManager;
import com.atlassian.plugin.servlet.DownloadStrategy;
import com.atlassian.plugin.servlet.ServletContextFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.web.DefaultWebInterfaceManager;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.PluginResourceLocatorImpl;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.plugin.webresource.WebResourceUrlProviderImpl;
import com.atlassian.renderer.IconManager;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2LinkRenderer;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.macro.code.CodeMacro;
import com.atlassian.renderer.v2.macro.code.SourceCodeFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.ActionScriptFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.JavaFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.JavaScriptFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.NoneFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.SqlFormatter;
import com.atlassian.renderer.v2.macro.code.formatter.XmlFormatter;
import com.atlassian.sal.spi.HostContextAccessor;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.seraph.auth.AuthenticationContext;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import com.atlassian.seraph.service.rememberme.DefaultRememberMeTokenGenerator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.atlassian.seraph.service.rememberme.RememberMeTokenGenerator;
import com.atlassian.seraph.spi.rememberme.RememberMeConfiguration;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;
import com.atlassian.velocity.JiraVelocityManager;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.velocity.htmlsafe.event.referenceinsertion.EnableHtmlEscapingDirectiveHandler;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.ConstantParameter;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.ArrayList;

/**
 * Register our components in the {@link ComponentContainer}
 */
@SuppressWarnings ("deprecation")
class ContainerRegistrar
{
    private static final Logger log = Logger.getLogger(ContainerRegistrar.class);

    private static final String MIME_TYPES_INPUTSTREAM_KEY = "mime.types-inputstream";
    private static final String FILE_ICONS = "file.icons";

    private final ComponentContainer.Scope PROVIDED = ComponentContainer.Scope.PROVIDED;
    private final ComponentContainer.Scope INTERNAL = ComponentContainer.Scope.INTERNAL;

    public void registerComponents(final ComponentContainer register, final boolean startupOK)
    {
        // components that have been registered with a particular key (because they can't be distinguished on the basis of class alone
        register.instance(INTERNAL, MIME_TYPES_INPUTSTREAM_KEY, ClassLoaderUtils.getResourceAsStream("mime.types", getClass()));
        register.instance(INTERNAL, FILE_ICONS, FileIconBean.DEFAULT_FILE_ICONS);

        // components that have to be wired together, because they need another component that is registered with a particular key
        register.implementation(INTERNAL, MimeManager.class, MimeManager.class, MIME_TYPES_INPUTSTREAM_KEY);

        register.implementation(INTERNAL, FileIconBean.class, FileIconBean.class, FILE_ICONS, MimeManager.class);
        register.implementation(PROVIDED, FileIconUtil.class, FileIconUtilImpl.class);

        register.implementation(INTERNAL, MimeSniffingKit.class);

        // components that are already constructed
        register.instance(PROVIDED, ActionDispatcher.class, CoreFactory.getActionDispatcher());
        register.instance(INTERNAL, DelegatorInterface.class, CoreFactory.getGenericDelegator());
        register.implementation(INTERNAL, UserAssociationStore.class, UserAssociationStoreImpl.class);
        register.implementation(INTERNAL, NodeAssociationStore.class, NodeAssociationStoreImpl.class);
        register.instance(PROVIDED, AssociationManager.class, CoreFactory.getAssociationManager());
        register.instance(PROVIDED, MailServerManager.class, MailFactory.getServerManager());
        register.implementation(PROVIDED, MailLoggingManager.class, DefaultMailLoggingManager.class);
        register.implementation(PROVIDED, MessageUserProcessor.class, MessageUserProcessorImpl.class);

        // Multitenant components
        register.instance(PROVIDED, TenantReference.class, MultiTenantContext.getTenantReference());
        register.instance(PROVIDED, MultiTenantComponentFactory.class, MultiTenantContext.getFactory());
        register.instance(PROVIDED, MultiTenantManager.class, MultiTenantContext.getManager());

        // Crowd Embedded registration
        register.implementation(PROVIDED, UnfilteredCrowdService.class, CrowdServiceImpl.class);
        register.implementation(INTERNAL, FilteredGroupsProvider.class, XmlFilteredGroupsProvider.class);
        register.implementation(PROVIDED, CrowdService.class, FilteredCrowdServiceImpl.class, UnfilteredCrowdService.class, FilteredGroupsProvider.class);
        register.implementation(PROVIDED, CrowdDirectoryService.class, CrowdDirectoryServiceImpl.class);

        register.implementation(INTERNAL, ApplicationFactory.class, CrowdEmbeddedApplicationFactory.class);
        // Set max events to 10000 based on advice in https://studio.atlassian.com/wiki/display/EMBCWD/API+Upgrade+Guides
        register.instance(INTERNAL, EventStore.class, new EventStoreGeneric(10000));
        register.implementation(PROVIDED, ApplicationService.class, ApplicationServiceGeneric.class);
        register.implementation(PROVIDED, ApplicationManager.class, ApplicationManagerGeneric.class);

        // Crowd listener registration
        register.implementation(INTERNAL, AutoGroupAdder.class, AutoGroupAdderImpl.class);

        // The "Daddy" DirectoryInstanceLoader that Delegates to the correct implementation
        register.implementation(PROVIDED, DirectoryInstanceLoader.class, DelegatingDirectoryInstanceLoader.class);
        // Bind all the specific DirectoryInstanceLoader implementations under their marker interface.
        register.implementation(INTERNAL, InternalDirectoryInstanceLoader.class, InternalDirectoryInstanceLoaderImpl.class);
        register.implementation(INTERNAL, RemoteCrowdDirectoryInstanceLoader.class, RemoteCrowdDirectoryInstanceLoaderImpl.class);
        register.implementation(INTERNAL, CrowdClientFactory.class, RestCrowdClientFactory.class);
        register.implementation(INTERNAL, DelegatedAuthenticationDirectoryInstanceLoader.class, DelegatedAuthenticationDirectoryInstanceLoaderImpl.class);
        register.implementation(INTERNAL, InternalHybridDirectoryInstanceLoader.class, JiraDbCachingRemoteDirectoryInstanceLoader.class);
        register.implementation(INTERNAL, LDAPDirectoryInstanceLoader.class, LDAPDirectoryInstanceLoaderImpl.class);
        register.implementation(INTERNAL, DirectoryPollerManager.class, QuartzDirectoryPollerManager.class);
        register.implementation(INTERNAL, DirectoryMonitorManager.class, DirectoryMonitorManagerImpl.class);
        register.implementation(INTERNAL, DirectoryCacheFactory.class, DirectoryCacheFactoryImpl.class);

        // User Directory Synchronisation
        register.implementation(INTERNAL, LockFactory.class, ReentrantLockFactory.class);
        register.implementation(INTERNAL, DirectoryLockManager.class, DirectoryLockManager.class);
        register.implementation(INTERNAL, DirectorySynchroniserHelper.class, DirectorySynchroniserHelperImpl.class);
        register.implementation(INTERNAL, DirectorySynchroniser.class, DirectorySynchroniserImpl.class);
        register.implementation(INTERNAL, SynchronisationStatusManager.class, SynchronisationStatusManagerImpl.class);
        register.implementation(INTERNAL, DirectorySynchronisationInformationStore.class, DirectorySynchronisationInformationStoreImpl.class);

        register.implementation(PROVIDED, DirectoryManager.class, DirectoryManagerGeneric.class);
        register.implementation(INTERNAL, com.atlassian.crowd.manager.permission.PermissionManager.class, PermissionManagerImpl.class);

        register.implementation(INTERNAL, PasswordHelper.class, PasswordHelperImpl.class);

        register.implementation(INTERNAL, InternalDirectoryUtils.class, InternalDirectoryUtilsImpl.class);

        register.implementation(PROVIDED, PasswordEncoderFactory.class, PasswordEncoderFactoryImpl.class);

        register.implementation(PROVIDED, LDAPPropertiesMapper.class, LDAPPropertiesMapperImpl.class);
        register.implementation(INTERNAL, LDAPPropertiesHelper.class, LDAPPropertiesHelperImpl.class);
        register.implementation(INTERNAL, LDAPQueryTranslater.class, LDAPQueryTranslaterImpl.class);

        // The Directory Hooks
        register.implementation(INTERNAL, CrowdServiceFactory.class, StaticCrowdServiceFactory.class);
        register.implementation(INTERNAL, InstanceFactory.class, JiraInstanceFactory.class);

        // SPI implementations
        register.implementation(INTERNAL, DirectoryDao.class, OfBizDirectoryDao.class);
        register.implementation(INTERNAL, ApplicationDAO.class, OfBizApplicationDao.class);
        register.implementation(INTERNAL, UserDao.class, OfBizUserDao.class);
        register.implementation(INTERNAL, GroupDao.class, OfBizGroupDao.class);
        register.implementation(INTERNAL, MembershipDao.class, OfBizDelegatingMembershipDao.class);
        register.implementation(INTERNAL, InternalMembershipDao.class, OfBizInternalMembershipDao.class);
        register.implementation(INTERNAL, OfBizCacheFlushingManager.class);

        // Jira as a Crowd Server
        register.implementation(INTERNAL, JaacsService.class, DefaultJaacsService.class);
        register.implementation(PROVIDED, ClientValidationManager.class, ClientValidationManagerImpl.class);
        register.instance(INTERNAL, InetAddressCacheUtil.class, new InetAddressCacheUtil(null));
        register.implementation(PROVIDED, PropertyManager.class, NoopPropertyManager.class);
        register.implementation(PROVIDED, ForgottenLoginManager.class, NoopForgottenLoginManager.class);
        register.implementation(PROVIDED, TokenAuthenticationManager.class, NoopTokenAuthenticationManager.class);
        register.implementation(PROVIDED, TrustedProxyManager.class, NoopTrustedProxyManager.class);
        register.implementation(PROVIDED, com.atlassian.crowd.util.I18nHelper.class, CrowdDelegatingI18Helper.class);
        register.implementation(PROVIDED, ClientProperties.class, NoopClientProperties.class);
        register.implementation
                (
                        INTERNAL,
                        StoringEventListener.class, StoringEventListener.class,
                        new ComponentParameter(EventStore.class),
                        new ComponentParameter(EventPublisher.class),
                        new ConstantParameter(true)
                );

        register.implementation(PROVIDED, SearchProvider.class, LuceneSearchProvider.class);
        register.implementation(PROVIDED, SearchProviderFactory.class, SearchProviderFactoryImpl.class);
        register.implementation(PROVIDED, SearchService.class, DefaultSearchService.class);
        register.implementation(PROVIDED, SearchContextFactory.class, SearchContextFactoryImpl.class);

        register.implementation(PROVIDED, ReaderCache.class, DefaultReaderCache.class);

        register.implementation(PROVIDED, PermissionsFilterGenerator.class, PermissionsFilterGeneratorImpl.class);
        register.implementation(INTERNAL, PermissionQueryFactory.class, DefaultPermissionQueryFactory.class);

        try
        {
            register.instance(INTERNAL, Scheduler.class, new JiraSchedulerFactory().getScheduler());
        }
        catch (final SchedulerException e)
        {
            log.error("Exception registering component instance for Scheduler Factory", e);
        }

        // --------------------------------------------------------------//
        // Components that change depending on an application property //
        // --------------------------------------------------------------//
        // ThumbnailManagers
        register.component(PROVIDED, new AppPropertiesComponentAdaptor(ThumbnailManager.class, DefaultThumbnailManager.class,
                DisabledThumbNailManager.class, APKeys.JIRA_OPTION_ALLOWTHUMBNAILS));
        register.implementation(INTERNAL, DefaultThumbnailManager.class);
        register.implementation(INTERNAL, DisabledThumbNailManager.class);

        // IssueManager
        register.implementation(PROVIDED, IssueManager.class, DefaultIssueManager.class);
        register.implementation(INTERNAL, IssueUtilsBean.class);

        // Time Duration Formatting
        register.implementation(INTERNAL, JiraDurationUtils.class);

        register.implementation(PROVIDED, RepositoryManager.class, DefaultRepositoryManager.class);
        register.implementation(PROVIDED, SubTaskManager.class, DefaultSubTaskManager.class);
        register.implementation(PROVIDED, IssueSecuritySchemeManager.class, IssueSecuritySchemeManagerImpl.class);
        register.implementation(PROVIDED, IssueSecurityLevelManager.class, IssueSecurityLevelManagerImpl.class);
        register.implementation(PROVIDED, IssueTypeScreenSchemeManager.class, DefaultIssueTypeScreenSchemeManager.class);
        register.implementation(PROVIDED, FieldScreenSchemeManager.class, DefaultFieldScreenSchemeManager.class);
        register.implementation(PROVIDED, WorkflowManager.class, OSWorkflowManager.class);
        register.implementation(INTERNAL, WorkflowsRepository.class, DefaultWorkflowsRepository.class);
        register.instance(INTERNAL, com.opensymphony.workflow.config.Configuration.class, new ConfigurationFactory().get());
        register.implementation(INTERNAL, WorkflowCopyNameFactory.class, DefaultWorkflowCopyNameFactory.class);
        register.implementation(PROVIDED, MigrationHelperFactory.class, DefaultMigrationHelperFactory.class);
        register.implementation(PROVIDED, IssueWorkflowManager.class, IssueWorkflowManagerImpl.class);
        register.implementation(PROVIDED, ColumnLayoutManager.class, DefaultColumnLayoutManager.class);
        register.implementation(PROVIDED, FieldLayoutManager.class, DefaultFieldLayoutManager.class);
        register.implementation(INTERNAL, FieldDescriptionHelper.class);

        // --------------------------------------------------------------//
        // Components that change depending a thread local property //
        // --------------------------------------------------------------//

        // if we are importing - then we don't want to index each issue manually
        register.implementation(INTERNAL, DefaultIndexManager.class);
        register.implementation(INTERNAL, BulkOnlyIndexManager.class);

        register.component(PROVIDED, new SwitchingInvocationAdapter(IssueIndexManager.class, DefaultIndexManager.class, BulkOnlyIndexManager.class,
                new InvocationSwitcher()
                {
                    public boolean isEnabled()
                    {
                        return ImportUtils.isIndexIssues();
                    }
                }));

        register.implementation(PROVIDED, PermissionContextFactory.class, DefaultPermissionContextFactory.class);

        // internalContainer.registerComponentImplementation(DefaultPermissionManager.class);
        register.implementation(INTERNAL, WorkflowBasedPermissionManager.class);
        register.implementation(INTERNAL, WorkflowPermissionFactory.class);
        register.implementation(INTERNAL, SubvertedPermissionManager.class);
        register.implementation(INTERNAL, ThreadLocalCachingPermissionManager.class);

        // Register the PermissionManager with the SwitchingInvocationAdapter.
        // This adapter returns a proxy that allows dynamic determination of which class implementation to use based on
        // logic provided through an InvocationSwitcher object.
        register.component(PROVIDED, new SwitchingInvocationAdapter(PermissionManager.class, ThreadLocalCachingPermissionManager.class,
                SubvertedPermissionManager.class, new InvocationSwitcher()
                {

                    public boolean isEnabled()
                    {
                        return !ImportUtils.isSubvertSecurityScheme();
                    }
                }));

        register.implementation(INTERNAL, DefaultListenerManager.class);
        register.implementation(INTERNAL, SubvertedListenerManager.class);
        register.component(PROVIDED, new SimpleSwitchingComponentAdaptor(ListenerManager.class)
        {
            @Override
            public Class<? extends ListenerManager> getComponentImplementation()
            {
                return ImportUtils.isEnableNotifications() ? DefaultListenerManager.class : SubvertedListenerManager.class;
            }
        });

        // --------------------------------------------------------------//
        // Components that wrap themselves (and need specific init) //
        // --------------------------------------------------------------//
        // this one needs specific registration, so it doesn't try to get itself (as it implements the interface that it needs)
        register.implementation(PROVIDED, ProjectManager.class, CachingProjectManager.class, DefaultProjectManager.class,
                ProjectComponentManager.class, ProjectFactory.class, UserManager.class, ApplicationProperties.class);
        register.implementation(INTERNAL, DefaultProjectManager.class);
        register.implementation(INTERNAL, ProjectCategoryStore.class, ProjectCategoryStoreImpl.class);

        register.implementation(INTERNAL, VersionStore.class, CachingVersionStore.class, OfBizVersionStore.class);
        register.implementation(INTERNAL, OfBizVersionStore.class);

        register.implementation(PROVIDED, JiraHome.class, DefaultJiraHome.class);

        // --------------------------------------------------------------//
        // Components that can be autowired //
        // --------------------------------------------------------------//

        register.instance(INTERNAL, ComponentFactory.class, JiraComponentFactory.getInstance());
        register.implementation(INTERNAL, ComponentLocator.class, JiraComponentLocator.class);
        register.implementation(INTERNAL, IntegrityCheckManager.class, IntegrityCheckManagerImpl.class);
        register.implementation(INTERNAL, IntegrityChecker.class);
        register.implementation(PROVIDED, BugAssociatorPrefs.class, DefaultBugAssociatorPrefs.class);

        register.implementation(PROVIDED, IssueUpdater.class, DefaultIssueUpdater.class);
        register.implementation(INTERNAL, IssueEventDispatcher.class);
        register.implementation(INTERNAL, ChangeLogUtils.class);
        register.implementation(PROVIDED, SystemInfoUtils.class, SystemInfoUtilsImpl.class);
        register.implementation(PROVIDED, QueryCreator.class, DefaultQueryCreator.class);
        register.implementation(INTERNAL, ReleaseNoteManager.class);
        register.implementation(INTERNAL, MemorySwitchToDatabaseBackedPropertiesManager.class);
        register.implementation(INTERNAL, DbBackedPropertiesManager.class);
        register.component(INTERNAL, new SimpleSwitchingComponentAdaptor(BackingPropertySetManager.class)
        {
            @Override
            public Class getComponentImplementation()
            {
                return register.getComponentInstance(DatabaseConfigurationManager.class).isDatabaseSetup() ?
                        DbBackedPropertiesManager.class : MemorySwitchToDatabaseBackedPropertiesManager.class;
            }
        });

        register.implementation(INTERNAL, PropertiesManager.class);
        register.implementation(INTERNAL, ApplicationPropertiesStore.class);
        register.implementation(PROVIDED, ApplicationProperties.class, ApplicationPropertiesImpl.class);
        register.implementation(INTERNAL, TimeZoneServiceImpl.class);
        register.implementation(PROVIDED, TimeZoneService.class, TimeZoneServiceCachingDecorator.class, TimeZoneServiceImpl.class);
        register.implementation(PROVIDED, TimeZoneManager.class, TimeZoneManagerImpl.class);

        // Velocity components
        register.implementation(INTERNAL, VelocityEngineFactory.class, VelocityEngineFactory.Default.class);
        register.implementation(INTERNAL, EnableHtmlEscapingDirectiveHandler.class);
        register.implementation(PROVIDED, VelocityTemplatingEngine.class, DefaultVelocityTemplatingEngine.class);
        register.implementation(PROVIDED, VelocityManager.class, JiraVelocityManager.class);
        register.implementation(PROVIDED, ConstantsManager.class, DefaultConstantsManager.class);
        register.implementation(PROVIDED, ResolutionManager.class, DefaultResolutionManager.class);
        register.implementation(PROVIDED, PriorityManager.class, DefaultPriorityManager.class);
        register.implementation(PROVIDED, StatusManager.class, DefaultStatusManager.class);
        register.implementation(PROVIDED, IssueTypeManager.class, DefaultIssueTypeManager.class);

        register.implementation(PROVIDED, ConstantsService.class, DefaultConstantsService.class);
        register.implementation(PROVIDED, WatcherService.class, DefaultWatcherService.class);
        register.implementation(INTERNAL, AutoWatchService.class);
        register.implementation(PROVIDED, ProjectRoleManager.class, DefaultProjectRoleManager.class);
        register.implementation(PROVIDED, ProjectRoleService.class, DefaultProjectRoleService.class);
        register.implementation(PROVIDED, PermissionSchemeManager.class, WorkflowBasedPermissionSchemeManager.class);
        register.implementation(PROVIDED, WatcherManager.class, DefaultWatcherManager.class);
        register.implementation(PROVIDED, IssueWatcherAccessor.class, DefaultIssueWatcherAccessor.class);
        register.implementation(PROVIDED, WatchedIssuesAccessor.class, DefaultWatchedIssuesAccessor.class);
        register.implementation(PROVIDED, AttachmentManager.class, DefaultAttachmentManager.class);
        register.implementation(PROVIDED, AttachmentService.class, DefaultAttachmentService.class);
        register.implementation(INTERNAL, AttachmentZipKit.class, AttachmentZipKit.class);
        register.implementation(INTERNAL, NonZipExpandableExtensions.class, ApplicationPropertiesBackedNonZipExpandableExtensions.class);
        register.implementation(PROVIDED, ProjectService.class, DefaultProjectService.class);
        register.implementation(PROVIDED, FieldManager.class, DefaultFieldManager.class);
        register.implementation(PROVIDED, CustomFieldManager.class, DefaultCustomFieldManager.class);
        register.implementation(PROVIDED, CustomFieldService.class, DefaultCustomFieldService.class);
        register.implementation(PROVIDED, FieldScreenManager.class, DefaultFieldScreenManager.class);
        register.implementation(INTERNAL, FieldScreenStore.class, CachingFieldScreenStore.class, DefaultFieldScreenStore.class);
        register.implementation(INTERNAL, DefaultFieldScreenStore.class);
        register.implementation(PROVIDED, TextFieldCharacterLengthValidator.class, DefaultTextFieldCharacterLengthValidator.class);
        register.implementation(PROVIDED, MailThreadManager.class, MailThreadManagerImpl.class);
        register.implementation(PROVIDED, CvsRepositoryUtil.class, CvsRepositoryUtilImpl.class);
        register.implementation(PROVIDED, WebAttachmentManager.class, DefaultWebAttachmentManager.class);
        register.implementation(INTERNAL, I18nBean.CachingFactory.class);
        register.implementation(PROVIDED, I18nHelper.BeanFactory.class, I18nBean.AccessorFactory.class);
        register.implementation(PROVIDED, I18nTranslationMode.class, I18nTranslationModeImpl.class);

        register.implementation(INTERNAL, JiraLocaleUtils.class);
        register.implementation(PROVIDED, LocaleManager.class, DefaultLocaleManager.class);
        // Deprecated IssueLinkService
        register.implementation(PROVIDED, com.atlassian.jira.bc.issue.issuelink.IssueLinkService.class, com.atlassian.jira.bc.issue.issuelink.DefaultIssueLinkService.class);
        // new IssueLinkService
        register.implementation(PROVIDED, IssueLinkService.class, DefaultIssueLinkService.class);
        register.implementation(PROVIDED, IssueLinkManager.class, DefaultIssueLinkManager.class);
        register.implementation(PROVIDED, IssueLinkTypeManager.class, DefaultIssueLinkTypeManager.class);
        register.implementation(PROVIDED, IssueLinkTypeDestroyer.class, IssueLinkTypeDestroyerImpl.class);
        register.implementation(PROVIDED, RemoteIssueLinkService.class, DefaultRemoteIssueLinkService.class);
        register.implementation(PROVIDED, RemoteIssueLinkManager.class, DefaultRemoteIssueLinkManager.class);
        register.implementation(INTERNAL, RemoteIssueLinkStore.class, RemoteIssueLinkStoreImpl.class);
        register.implementation(PROVIDED, JiraBaseUrls.class, JiraBaseUrlsImpl.class);
        register.implementation(PROVIDED, BulkOperationManager.class, DefaultBulkOperationManager.class);
        register.implementation(PROVIDED, MoveSubTaskOperationManager.class, DefaultMoveSubTaskOperationManager.class);
        register.implementation(PROVIDED, VoteService.class, DefaultVoteService.class);
        register.implementation(PROVIDED, VoteManager.class, DefaultVoteManager.class);
        register.implementation(PROVIDED, VoteHistoryStore.class, OfbizVoteHistoryStore.class);
        register.implementation(PROVIDED, IssueVoterAccessor.class, DefaultIssueVoterAccessor.class);
        register.implementation(PROVIDED, VotedIssuesAccessor.class, DefaultVotedIssuesAccessor.class);
        register.implementation(PROVIDED, AuthenticationContext.class, AuthenticationContextImpl.class);
        register.implementation(PROVIDED, JiraAuthenticationContext.class, JiraAuthenticationContextImpl.class);
        register.implementation(INTERNAL, MyJiraHomePreference.class, MyJiraHomePreferenceOsgiDelegator.class);
        register.implementation(PROVIDED, MyJiraHomeLinker.class, MyJiraHomeLinkerImpl.class);

        //
        // Properties Adaptors
        //

        register.implementation(PROVIDED, EncodingConfiguration.class, EncodingConfiguration.PropertiesAdaptor.class);
        register.implementation(PROVIDED, IndexPathService.class, DefaultIndexPathService.class);
        register.implementation(PROVIDED, IndexPathManager.class, IndexPathManager.PropertiesAdaptor.class);
        register.implementation(PROVIDED, AttachmentPathManager.class, AttachmentPathManager.PropertiesAdaptor.class);
        register.implementation(PROVIDED, ThumbnailConfiguration.class, ThumbnailConfiguration.PropertiesAdaptor.class);
        register.implementation(PROVIDED, IndexDirectoryFactory.class, IndexDirectoryFactory.IndexPathAdapter.class);
        register.implementation(PROVIDED, IndexingConfiguration.class, IndexingConfiguration.PropertiesAdapter.class);
        register.implementation(PROVIDED, IndexWriterConfiguration.class, IndexWriterConfiguration.PropertiesAdaptor.class);
        register.implementation(PROVIDED, DefaultIssueIndexer.CommentRetriever.class, DefaultCommentRetriever.class);
        register.implementation(PROVIDED, DefaultIssueIndexer.ChangeHistoryRetriever.class, DefaultChangeHistoryRetriever.class);
        register.implementation(PROVIDED, TimeTrackingConfiguration.class, TimeTrackingConfiguration.PropertiesAdaptor.class);

        //
        // managers
        //

        register.implementation(PROVIDED, CommentManager.class, DefaultCommentManager.class);
        register.implementation(PROVIDED, RecentCommentManager.class, DefaultRecentCommentManager.class);
        register.implementation(PROVIDED, CommentPermissionManager.class, DefaultCommentPermissionManager.class);
        register.implementation(PROVIDED, CommentService.class, DefaultCommentService.class);

        register.implementation(PROVIDED, WorklogManager.class, DefaultWorklogManager.class);
        register.implementation(PROVIDED, ChangeHistoryManager.class, DefaultChangeHistoryManager.class);
        register.implementation(PROVIDED, SecurityTypeManager.class, IssueSecurityTypeManager.class);
        register.implementation(PROVIDED, MailQueue.class, MailQueueImpl.class);
        register.implementation(PROVIDED, MailService.class, MailServiceImpl.class);
        register.implementation(PROVIDED, NotificationSchemeManager.class, DefaultNotificationSchemeManager.class);
        register.implementation(INTERNAL, SearchRequestStore.class, CachingSearchRequestStore.class, OfBizSearchRequestStore.class);
        register.implementation(INTERNAL, OfBizSearchRequestStore.class);
        register.implementation(PROVIDED, SearchRequestManager.class, DefaultSearchRequestManager.class);
        register.implementation(PROVIDED, SearchRequestService.class, DefaultSearchRequestService.class);
        register.implementation(PROVIDED, SearchRequestFactory.class, DefaultSearchRequestFactory.class);
        register.implementation(INTERNAL, SearchRequestAdminManager.class, DefaultSearchRequestAdminManager.class);
        register.implementation(INTERNAL, SearchRequestAdminService.class, DefaultSearchRequestAdminService.class);
        register.implementation(INTERNAL, SearchRequestDisplayBean.Factory.class);

        register.implementation(PROVIDED, SearchHandlerManager.class, DefaultSearchHandlerManager.class);
        register.implementation(PROVIDED, IssueSearcherManager.class, DefaultIssueSearcherManager.class);

        //This initialises the CustomFieldManager, which requires the CustomFieldManager to be up.
        //This is a problem, because the CustomFieldManager loads up the PluginManager, which introduces a
        //circular dependency.
        register.implementation(INTERNAL, FieldIndexerManager.class, FieldIndexerManagerImpl.class);
        register.implementation(INTERNAL, ChangeHistoryFieldConfigurationManager.class);
        register.implementation(INTERNAL, ChangeHistoryFieldConstants.class);
        register.implementation(PROVIDED, IndexedChangeHistoryFieldManager.class, DefaultIndexedChangeHistoryFieldManager.class);
        register.implementation(INTERNAL, JqlChangeItemMapping.class);
        register.implementation(INTERNAL, ObjectConfigurationFactory.class, XMLObjectConfigurationFactory.class);
        register.implementation(INTERNAL, PermissionTypeManager.class);
        register.implementation(INTERNAL, ServiceConfigStore.class, OfBizServiceConfigStore.class);
        register.implementation(PROVIDED, ServiceManager.class, DefaultServiceManager.class);
        register.implementation(INTERNAL, ServiceTypes.class, DefaultServiceTypes.class);
        register.implementation(INTERNAL, InBuiltServiceTypes.class, DefaultInBuiltServiceTypes.class);
        register.implementation(PROVIDED, SubscriptionManager.class, DefaultSubscriptionManager.class);
        register.implementation(PROVIDED, FilterSubscriptionService.class, DefaultFilterSubscriptionService.class);
        register.implementation(PROVIDED, GlobalPermissionManager.class, DefaultGlobalPermissionManager.class);
        register.implementation(INTERNAL, NotificationTypeManager.class);
        register.implementation(PROVIDED, WorkflowSchemeManager.class, DefaultWorkflowSchemeManager.class);
        register.implementation(PROVIDED, VersionManager.class, DefaultVersionManager.class);
        register.implementation(PROVIDED, VersionService.class, DefaultVersionService.class);
        register.implementation(INTERNAL, VersionHelperBean.class);
        register.implementation(PROVIDED, ProjectComponentManager.class, DefaultProjectComponentManager.class);
        register.implementation(INTERNAL, CollectionReorderer.class);
        register.implementation(PROVIDED, OutlookDateManager.class, OutlookDateManagerImpl.class);
        register.implementation(PROVIDED, AvatarManager.class, AvatarManagerImpl.class);
        register.implementation(PROVIDED, AvatarService.class, AvatarServiceImpl.class);
        register.implementation(PROVIDED, AvatarPickerHelper.class, AvatarPickerHelperImpl.class);

        register.implementation(PROVIDED, ImageUtils.class, ImageUtilsImpl.class);
        register.implementation(PROVIDED, DropDownCreatorService.class, DefaultDropDownCreatorService.class);
        register.implementation(PROVIDED, HeaderSeparatorService.class, DefaultHeaderSeparatorService.class);

        register.implementation(INTERNAL, DatabaseConfigurationManager.class, DatabaseConfigurationManagerImpl.class);
        if (MultiTenantContext.getManager().isSystemTenant())
        {
            register.implementation(INTERNAL, DatabaseConfigurationLoader.class, SystemTenantDatabaseConfigurationLoader.class);
        }
        else
        {
            register.implementation(INTERNAL, DatabaseConfigurationLoader.class, MultiTenantDatabaseConfigurationLoader.class);
        }

        if (startupOK)
        {
            register.implementation(PROVIDED, OfBizDelegator.class, DefaultOfBizDelegator.class);
        }
        else
        {
            register.implementation(PROVIDED, OfBizDelegator.class, LockedDatabaseOfBizDelegator.class);
        }
        register.implementation(INTERNAL, EntityEngine.class, EntityEngineImpl.class);
        register.implementation(PROVIDED, OptionsManager.class, CachedOptionsManager.class);
        register.implementation(PROVIDED, IssueLinkCreator.class, DefaultIssueLinkCreator.class);
        register.implementation(PROVIDED, SoapAttachmentHelper.class, SoapAttachmentHelperImpl.class);

        // Note that we can't register this as a component here, as it gets reset in ManagerFactory.globalReset()
        // internalContainer.registerComponentInstance(LicenseManager.class, LicenseManager.getInstance());
        register.implementation(INTERNAL, ExternalUtils.class);
        register.implementation(INTERNAL, SchemePermissions.class);
        register.implementation(INTERNAL, JiraKeyUtilsBean.class);
        register.implementation(INTERNAL, ProjectKeyRegexChangeListener.class);
        register.implementation(INTERNAL, JiraUtilsBean.class);
        register.implementation(PROVIDED, TranslationManager.class, TranslationManagerImpl.class);
        register.implementation(PROVIDED, IssueFactory.class, DefaultIssueFactory.class);
        register.implementation(PROVIDED, TemplateIssueFactory.class, DefaultTemplateIssueFactory.class);
        register.implementation(PROVIDED, TemplateContextFactory.class, DefaultTemplateContextFactory.class);
        register.implementation(PROVIDED, IssueMailQueueItemFactory.class, DefaultIssueMailQueueItemFactory.class);
        register.implementation(PROVIDED, FieldScreenRendererFactory.class, FieldScreenRendererFactoryImpl.class);
        register.implementation(PROVIDED, EventTypeManager.class, DefaultEventTypeManager.class);
        register.implementation(PROVIDED, TemplateManager.class, DefaultTemplateManager.class);
        register.implementation(PROVIDED, SubscriptionMailQueueItemFactory.class, DefaultSubscriptionMailQueueItemFactory.class);
        register.implementation(INTERNAL, MailingListCompiler.class);

        register.implementation(INTERNAL, EntitiesExporter.class, DefaultSaxEntitiesExporter.class);

        register.implementation(PROVIDED, IssueCreationHelperBean.class, IssueCreationHelperBeanImpl.class);
        register.implementation(PROVIDED, UpdateFieldsHelperBean.class, UpdateFieldsHelperBeanImpl.class);
        // this is used as a webwork bean, see viewissue.jsp, don't turn this into an interface without fixing all of those
        register.implementation(INTERNAL, FieldVisibilityBean.class);
        register.implementation(PROVIDED, FieldVisibilityManager.class, FieldVisibilityManagerImpl.class);

        register.implementation(INTERNAL, TableLayoutFactory.class);
        register.implementation(INTERNAL, ColumnLayoutItemFactory.class);
        register.implementation(PROVIDED, TableLayoutUtils.class, TableLayoutUtilsImpl.class);

        register.implementation(PROVIDED, UserPreferencesManager.class, DefaultUserPreferencesManager.class);
        register.implementation(PROVIDED, UserPropertyManager.class, DefaultUserPropertyManager.class);
        register.implementation(INTERNAL, ExternalEntityStore.class, CachingExternalEntityStore.class, OfbizExternalEntityStore.class);
        register.implementation(INTERNAL, OfbizExternalEntityStore.class);

        // Context Related
        register.implementation(INTERNAL, FieldConfigPersister.class, FieldConfigPersisterImpl.class);
        register.implementation(INTERNAL, FieldConfigContextPersister.class, CachingFieldConfigContextPersister.class);
        register.implementation(PROVIDED, FieldConfigCleanup.class, FieldConfigCleanupImpl.class);
        register.implementation(PROVIDED, FieldConfigManager.class, FieldConfigManagerImpl.class);
        register.implementation(PROVIDED, GenericConfigManager.class, CachedGenericConfigManager.class, DefaultGenericConfigManager.class);
        register.implementation(INTERNAL, DefaultGenericConfigManager.class);
        register.implementation(INTERNAL, JiraContextTreeManager.class);
        register.implementation(PROVIDED, FieldConfigSchemeManager.class, FieldConfigSchemeManagerImpl.class);
        register.implementation(INTERNAL, FieldConfigSchemePersister.class, CachedFieldConfigSchemePersister.class);

        register.implementation(PROVIDED, OptionSetManager.class, CachedOptionSetManager.class);
        register.implementation(INTERNAL, OptionSetPersister.class, OptionSetPersisterImpl.class);

        register.implementation(PROVIDED, IssueTypeSchemeManager.class, IssueTypeSchemeManagerImpl.class);
        register.implementation(INTERNAL, IssueTypeManageableOption.class);

        register.implementation(INTERNAL, BulkMoveOperation.class, BulkMoveOperationImpl.class);
        register.implementation(INTERNAL, BulkMigrateOperation.class);
        register.implementation(INTERNAL, BulkWorkflowTransitionOperation.class);
        register.implementation(PROVIDED, BulkEditBeanFactory.class, BulkEditBeanFactoryImpl.class);

        register.implementation(INTERNAL, MoveSubTaskTypeOperation.class);
        register.implementation(INTERNAL, MoveSubTaskParentOperation.class);

        register.implementation(PROVIDED , SoyTemplateRendererProvider.class, SoyTemplateRendererProviderImpl.class);

        // @TODO This is only required for the RPC plugin. Remove once refactored to use ExternalUtils
//        register.implementation(INTERNAL, IssueDeleteInterface.class, IssueDelete.class);

        // converters + persisters for custom fields
        register.implementation(PROVIDED, DatePickerConverter.class, DatePickerConverterImpl.class);
        register.implementation(PROVIDED, DateTimeConverter.class, DateTimeConverterImpl.class);
        register.implementation(PROVIDED, DoubleConverter.class, DoubleConverterImpl.class);
        register.implementation(PROVIDED, ProjectConverter.class, ProjectConverterImpl.class);
        register.implementation(PROVIDED, SelectConverter.class, SelectConverterImpl.class);
        register.implementation(PROVIDED, StringConverter.class, StringConverterImpl.class);
        register.implementation(PROVIDED, UserConverter.class, UserConverterImpl.class);
        register.implementation(PROVIDED, MultiUserConverter.class, MultiUserConverterImpl.class);
        register.implementation(PROVIDED, GroupConverter.class, GroupConverterImpl.class);
        register.implementation(PROVIDED, MultiGroupConverter.class, MultiGroupConverterImpl.class);
        register.implementation(PROVIDED, CustomFieldValuePersister.class, EagerLoadingOfBizCustomFieldPersister.class);
        register.implementation(PROVIDED, InternalWebSudoManager.class, InternalWebSudoManagerImpl.class);
        // CustomFieldValidator is INTERNAL because it is not used by Custom Fields, only by the CreateCustomField WebWork action.
        register.implementation(INTERNAL, CustomFieldValidator.class, CustomFieldValidatorImpl.class);

        register.implementation(PROVIDED, IssueIndexer.class, DefaultIssueIndexer.class);

        register.implementation(PROVIDED, UpgradeManager.class, UpgradeManagerImpl.class, JiraLicenseService.class, BuildUtilsInfo.class,
                I18nHelper.BeanFactory.class, ApplicationProperties.class, BuildVersionRegistry.class, EventPublisher.class,
                OfBizDelegator.class, IndexLifecycleManager.class, OutlookDateManager.class, FeatureManager.class);

        // plugin management
        // plugins 2.x
        if (MultiTenantContext.getManager().isSystemTenant())
        {
            registerSystemTenantPluginSystem(register);
        }
        else
        {
            registerNormalTenantPluginSystem(register);
        }

        register.implementation(INTERNAL, PluginModuleTrackerFactory.class);
        register.implementation(INTERNAL, ModuleDescriptors.Orderings.class, DefaultModuleDescriptorOrderingsFactory.class);
        register.implementation(INTERNAL, EventDispatcher.class, AsynchronousAbleEventDispatcher.class);
        register.implementation(INTERNAL, EventExecutorFactory.class, EventExecutorFactoryImpl.class);
        register.implementation(INTERNAL, EventThreadPoolConfiguration.class, EventThreadPoolConfigurationImpl.class);
        register.implementation(INTERNAL, ListenerHandlersConfiguration.class, JiraListenerHandlerConfigurationImpl.class);

        // This probably won't work for multitenancy... not sure
        register.implementation(INTERNAL, ReindexMessageListener.class);
        register.implementation(INTERNAL, JiraExternalLibrariesCacheClearingListener.class);

        // web fragments
        register.implementation(PROVIDED, WebInterfaceManager.class, DefaultWebInterfaceManager.class);
        register.implementation(PROVIDED, WebFragmentHelper.class, JiraWebFragmentHelper.class);
        register.implementation(PROVIDED, WebResourceManager.class, JiraWebResourceManagerImpl.class);
        register.implementation(PROVIDED, WebResourceIntegration.class, JiraWebResourceIntegration.class);
        register.implementation(PROVIDED, WebResourceUrlProvider.class, WebResourceUrlProviderImpl.class);
        register.implementation(PROVIDED, ResourceBatchingConfiguration.class, JiraWebResourceBatchingConfiguration.class);
        register.implementation(PROVIDED, SimpleLinkManager.class, DefaultSimpleLinkManager.class);
        register.implementation(PROVIDED, SimpleLinkFactoryModuleDescriptors.class, DefaultSimpleLinkFactoryModuleDescriptors.class);
        register.implementation(INTERNAL, ConditionDescriptorFactory.class, ConditionDescriptorFactoryImpl.class);
        register.implementation(INTERNAL, JiraWebInterfaceManager.class);
        register.implementation(INTERNAL, JiraContactHelper.class, JiraContactHelperImpl.class);

        // statistic mappers
        register.implementation(INTERNAL, StatusStatisticsMapper.class);
        register.implementation(INTERNAL, AssigneeStatisticsMapper.class);
        register.implementation(INTERNAL, ReporterStatisticsMapper.class);
        register.implementation(INTERNAL, ProjectStatisticsMapper.class);
        register.implementation(INTERNAL, ComponentStatisticsMapper.class);
        register.implementation(INTERNAL, IssueTypeStatisticsMapper.class);
        register.implementation(INTERNAL, ResolutionStatisticsMapper.class);
        register.implementation(INTERNAL, PriorityStatisticsMapper.class);
        register.implementation(INTERNAL, FixForVersionStatisticsMapper.class);
        register.implementation(INTERNAL, RaisedInVersionStatisticsMapper.class);
        register.implementation(INTERNAL, SecurityLevelStatisticsMapper.class);
        register.implementation(INTERNAL, IssueKeyStatisticsMapper.class);
        register.implementation(INTERNAL, SubTaskStatisticsMapper.class);

        // Utility Classes
        register.implementation(PROVIDED, EmailFormatter.class, EmailFormatterImpl.class);
        register.implementation(PROVIDED, GroupPermissionChecker.class, GroupPermissionCheckerImpl.class);
        register.implementation(PROVIDED, UserManager.class, DefaultUserManager.class);
        register.implementation(PROVIDED, UserUtil.class, UserUtilImpl.class);
        register.implementation(PROVIDED, UserService.class, DefaultUserService.class);
        register.implementation(PROVIDED, GlobalUserPreferencesUtil.class, GlobalUserPreferencesUtilImpl.class);
        register.implementation(PROVIDED, UserSharingPreferencesUtil.class, UserSharingPreferencesUtilImpl.class);
        register.implementation(INTERNAL, UpgradeUtils.class);
        register.implementation(PROVIDED, IndexLanguageToLocaleMapper.class, IndexLanguageToLocaleMapperImpl.class);
        register.implementation(PROVIDED, CalendarLanguageUtil.class, CalendarLanguageUtilImpl.class);
        register.implementation(INTERNAL, UnsupportedBrowserManager.class);

        // the Jira specific components of the wiki renderer
        register.implementation(PROVIDED, RendererManager.class, DefaultRendererManager.class);
        // wiki renderer components
        // Pre-register the code format macro so we can pass the known formatters in.
        final ArrayList<SourceCodeFormatter> codeFormatters = new ArrayList<SourceCodeFormatter>();
        codeFormatters.add(new SqlFormatter());
        codeFormatters.add(new JavaFormatter());
        codeFormatters.add(new JavaScriptFormatter());
        codeFormatters.add(new ActionScriptFormatter());
        codeFormatters.add(new XmlFormatter());
        codeFormatters.add(new NoneFormatter());
        register.instance(INTERNAL, "code.macro.formatters", codeFormatters);
        register.implementation(INTERNAL, CodeMacro.class, CodeMacro.class, V2SubRenderer.class, "code.macro.formatters");

        // Register all the other renderer components
        // initialize with an empty list, we will set this in initializeComponents later
        register.instance(INTERNAL, "code.macro.components", new ArrayList<RendererComponent>());
        register.implementation(PROVIDED, Renderer.class, V2Renderer.class, "code.macro.components");
        register.implementation(INTERNAL, V2SubRenderer.class);
        register.implementation(PROVIDED, IconManager.class, JiraIconManager.class);
        register.implementation(PROVIDED, RendererConfiguration.class, JiraRendererConfiguration.class);
        register.implementation(PROVIDED, LinkRenderer.class, V2LinkRenderer.class);
        register.implementation(PROVIDED, RendererAttachmentManager.class,
                com.atlassian.jira.issue.fields.renderer.wiki.embedded.RendererAttachmentManager.class);
        register.implementation(PROVIDED, EmbeddedResourceRenderer.class, JiraEmbeddedResourceRenderer.class);
        register.implementation(PROVIDED, LinkResolver.class, JiraLinkResolver.class);
        register.implementation(PROVIDED, MacroManager.class, WikiMacroManager.class);

        // Default Assignee Resolver
        register.implementation(PROVIDED, AssigneeResolver.class, DefaultAssigneeResolver.class);

        // used for views of search requests
        register.implementation(PROVIDED, SearchRequestURLHandler.class, DefaultSearchRequestURLHandler.class);
        register.implementation(PROVIDED, IssueViewURLHandler.class, DefaultIssueViewURLHandler.class);
        register.implementation(PROVIDED, SearchRequestHeader.class, DefaultSearchRequestHeader.class);
        register.implementation(PROVIDED, SearchRequestPreviousView.class, DefaultSearchRequestPreviousView.class);
        register.implementation(PROVIDED, SearchRequestViewBodyWriterUtil.class, DefaultSearchRequestViewBodyWriterUtil.class);
        register.implementation(PROVIDED, VelocityRequestContextFactory.class, DefaultVelocityRequestContextFactory.class);
        register.implementation(PROVIDED, IssueViewUtil.class, DefaultIssueViewUtil.class);
        register.implementation(PROVIDED, VelocityParamFactory.class, VelocityParamFactoryImpl.class);

        // web components
        register.implementation(INTERNAL, WebFragmentWebComponent.class);

        register.implementation(INTERNAL, GroupSelectorUtils.class);
        register.implementation(PROVIDED, ProjectFactory.class, DefaultProjectFactory.class);

        register.implementation(PROVIDED, RoleActorFactory.class, PluginDelegatingRoleActorFactory.class);
        register.implementation(INTERNAL, OfBizProjectRoleAndActorStore.class);
        register.implementation(INTERNAL, ProjectRoleAndActorStore.class, CachingProjectRoleAndActorStore.class,
                OfBizProjectRoleAndActorStore.class, RoleActorFactory.class);
        register.implementation(INTERNAL, ProjectComponentStore.class, CachingProjectComponentStore.class,
                OfBizProjectComponentStore.class);
        register.implementation(INTERNAL, OfBizProjectComponentStore.class);
        register.implementation(PROVIDED, ProjectComponentService.class, DefaultProjectComponentService.class);
        register.implementation(PROVIDED, SchemeFactory.class, DefaultSchemeFactory.class);
        register.implementation(PROVIDED, SchemeDistiller.class, SchemeDistillerImpl.class);
        register.implementation(PROVIDED, SchemeDistillerService.class, DefaultSchemeDistillerService.class);
        register.implementation(PROVIDED, SchemeManagerFactory.class, DefaultSchemeManagerFactory.class);
        register.implementation(PROVIDED, SchemeGroupsToRolesTransformer.class, SchemeGroupsToRolesTransformerImpl.class);
        register.implementation(PROVIDED, SchemeGroupsToRoleTransformerService.class, DefaultSchemeGroupsToRoleTransformerService.class);

        register.implementation(PROVIDED, IssueToSubTaskConversionService.class, DefaultIssueToSubTaskConversionService.class);
        register.implementation(PROVIDED, SubTaskToIssueConversionService.class, DefaultSubTaskToIssueConversionService.class);

        register.implementation(PROVIDED, IssuePickerSearchService.class, DefaultIssuePickerSearchService.class);
        register.implementation(INTERNAL, HistoryIssuePickerSearchProvider.class);
        register.implementation(INTERNAL, LuceneCurrentSearchIssuePickerSearchProvider.class);
        register.implementation(PROVIDED, UserPickerSearchService.class, DefaultUserPickerSearchService.class);
        register.implementation(PROVIDED, GroupPickerSearchService.class, GroupPickerSearchServiceImpl.class);
        register.implementation(PROVIDED, AssigneeService.class, DefaultAssigneeService.class);

        register.implementation(PROVIDED, Authorizer.class, AuthorizerImpl.class);

        register.implementation(PROVIDED, VisibilityValidator.class, DefaultVisibilityValidator.class);
        register.implementation(PROVIDED, WorklogService.class, DefaultWorklogService.class);

        register.implementation(INTERNAL, WorklogStore.class, OfBizWorklogStore.class);
        register.implementation(PROVIDED, TimeTrackingIssueUpdater.class, DefaultTimeTrackingIssueUpdater.class);
        register.implementation(PROVIDED, AggregateTimeTrackingCalculatorFactory.class, AggregateTimeTrackingCalculatorFactoryImpl.class);
        register.implementation(PROVIDED, TimeTrackingGraphBeanFactory.class, TimeTrackingGraphBeanFactoryImpl.class);
        register.implementation(PROVIDED, ModuleWebComponent.class, ModuleWebComponentImpl.class);

        register.implementation(INTERNAL, OfBizDraftWorkflowStore.class);
        register.implementation(INTERNAL, DraftWorkflowStore.class, CachingDraftWorkflowStore.class, OfBizDraftWorkflowStore.class);
        register.implementation(INTERNAL, OfBizWorkflowDescriptorStore.class);
        register.implementation(INTERNAL, WorkflowDescriptorStore.class, CachingWorkflowDescriptorStore.class,
                OfBizWorkflowDescriptorStore.class);
        register.implementation(PROVIDED, WorkflowService.class, DefaultWorkflowService.class);
        register.implementation(PROVIDED, WorkflowTransitionUtilFactory.class, WorkflowTransitionUtilFactoryImpl.class);

        register.implementation(PROVIDED, ProjectOperationManager.class, DefaultProjectOperationManager.class);

        register.implementation(INTERNAL, GlobalPermissionGroupAssociationUtil.class);

        register.implementation(PROVIDED, JiraPropertySetFactory.class, DefaultJiraPropertySetFactory.class);

        // GroupManager
        register.implementation(PROVIDED, GroupManager.class, DefaultGroupManager.class);
        // Group Service
        register.implementation(PROVIDED, GroupService.class, DefaultGroupService.class);

        // --------------------------------------------------------------------------
        // Trusted Applications
        register.implementation(INTERNAL, DefaultTrustedApplicationStore.class);
        register.implementation(INTERNAL, TrustedApplicationStore.class, CachingTrustedApplicationStore.class,
                DefaultTrustedApplicationStore.class);
        register.implementation(INTERNAL, DefaultTrustedApplicationManager.class);
        register.implementation(PROVIDED, TrustedApplicationManager.class, CachingTrustedApplicationManager.class,
                DefaultTrustedApplicationManager.class);
        register.implementation(PROVIDED, TrustedApplicationService.class, DefaultTrustedApplicationService.class);
        register.implementation(PROVIDED, TrustedApplicationValidator.class, DefaultTrustedApplicationValidator.class);

        // for the filter
        register.implementation(INTERNAL, CurrentApplicationStore.class, DefaultCurrentApplicationStore.class);
        register.implementation(PROVIDED, CurrentApplicationFactory.class, DefaultCurrentApplicationFactory.class);
        register.implementation(PROVIDED, TrustedApplicationsManager.class, SeraphTrustedApplicationsManager.class);
        register.implementation(PROVIDED, TrustedApplicationsConfigurationManager.class, SeraphTrustedApplicationsManager.class);

        // --------------------------------------------------------------------------
        // User Formatting
        register.implementation(INTERNAL, UserFormatModuleDescriptors.class, DefaultUserFormatModuleDescriptors.class);
        register.implementation(INTERNAL, UserFormatTypes.class, DefaultUserFormatTypes.class);
        register.implementation(INTERNAL, UserFormatTypeConfiguration.class, PluginsAwareUserFormatTypeConfiguration.class);
        register.implementation(PROVIDED, UserFormatManager.class, DefaultUserFormatManager.class);
        register.implementation(PROVIDED, UserFormats.class, DefaultUserFormats.class);

        // Favourites & Sharing
        register.implementation(PROVIDED, ShareManager.class, DefaultShareManager.class);
        register.implementation(INTERNAL, SharePermissionStore.class, CachingSharePermissionStore.class, OfBizSharePermissionStore.class);
        register.implementation(INTERNAL, OfBizSharePermissionStore.class);

        register.implementation(PROVIDED, ShareTypeFactory.class, DefaultShareTypeFactory.class);
        register.implementation(INTERNAL, SharePermissionDeleteUtils.class);
        register.implementation(PROVIDED, SharedEntityIndexer.class, DefaultSharedEntityIndexer.class);
        register.implementation(PROVIDED, SharedEntityIndexManager.class, DefaultSharedEntityIndexManager.class);
        register.implementation(PROVIDED, DirectoryFactory.class, IndexPathDirectoryFactory.class);

        register.implementation(INTERNAL, GlobalShareType.class);
        register.implementation(INTERNAL, GlobalShareTypeRenderer.class);
        register.implementation(INTERNAL, GlobalShareTypeValidator.class);

        register.implementation(INTERNAL, GroupShareType.class);
        register.implementation(INTERNAL, GroupShareTypeRenderer.class);
        register.implementation(INTERNAL, GroupShareTypeValidator.class);
        register.implementation(INTERNAL, GroupShareTypePermissionChecker.class);

        register.implementation(INTERNAL, ProjectShareType.class);
        register.implementation(INTERNAL, ProjectShareTypeRenderer.class);
        register.implementation(INTERNAL, ProjectShareTypeValidator.class);
        register.implementation(INTERNAL, ProjectShareTypePermissionChecker.class);
        register.implementation(INTERNAL, ProjectShareQueryFactory.class);
        register.implementation(INTERNAL, ProjectSharePermissionComparator.class);

        register.implementation(PROVIDED, QueryFactory.class, DefaultQueryFactory.class);
        register.implementation(INTERNAL, SharedEntitySearchContextToQueryFactoryMap.class);
        register.implementation(INTERNAL, com.atlassian.jira.sharing.index.PermissionQueryFactory.class);
        register.implementation(INTERNAL, IsSharedQueryFactory.class);

        register.implementation(PROVIDED, ShareTypeValidatorUtils.class, DefaultShareTypeValidatorUtils.class);

        register.implementation(PROVIDED, FavouritesService.class, DefaultFavouritesService.class);
        register.implementation(PROVIDED, FavouritesManager.class, DefaultFavouritesManager.class);
        register.implementation(PROVIDED, SharedEntityAccessor.Factory.class, DefaultSharedEntityAccessorFactory.class);
        register.implementation(INTERNAL, FavouritesStore.class, CachingFavouritesStore.class, OfBizFavouritesStore.class);
        register.implementation(INTERNAL, OfBizFavouritesStore.class);
        register.implementation(PROVIDED, SharePermissionReindexer.class, DefaultSharePermissionReindexer.class);

        // Dashboards / Portals / Portlets
        register.implementation(PROVIDED, PortalPageService.class, DefaultPortalPageService.class);

        register.implementation(PROVIDED, PortalPageManager.class, DefaultPortalPageManager.class);
        register.implementation(INTERNAL, PortalPageStore.class, CachingPortalPageStore.class, OfBizPortalPageStore.class);
        register.implementation(INTERNAL, OfBizPortalPageStore.class);

        register.implementation(INTERNAL, PortletConfigurationStore.class, CachingPortletConfigurationStore.class,
                OfbizPortletConfigurationStore.class);
        register.implementation(INTERNAL, OfbizPortletConfigurationStore.class);
        register.implementation(PROVIDED, PortletConfigurationManager.class, PortletConfigurationManagerImpl.class);

        // Project Tab Panels
        register.implementation(INTERNAL, ProjectDescriptionFragment.class);
        register.implementation(INTERNAL, DueIssuesFragment.class);
        register.implementation(INTERNAL, CreatedVsResolvedFragment.class);
        register.implementation(INTERNAL, ActivityStreamFragment.class);
        register.implementation(INTERNAL, RecentIssuesFragment.class);
        register.implementation(INTERNAL, DueVersionsFragment.class);
        register.implementation(INTERNAL, UnresolvedIssuesByPriorityFragment.class);
        register.implementation(INTERNAL, UnresolvedIssuesByAssigneeFragment.class);
        register.implementation(INTERNAL, UnresolvedIssuesByComponentFragment.class);
        register.implementation(INTERNAL, UnresolvedIssuesByFixVersionFragment.class);
        register.implementation(INTERNAL, UnresolvedIssuesByIssueTypeFragment.class);
        register.implementation(INTERNAL, StatusSummaryFragment.class);
        register.implementation(INTERNAL, ComponentDescriptionFragment.class);
        register.implementation(INTERNAL, VersionDescriptionFragment.class);
        register.implementation(INTERNAL, VersionDrillDownRenderer.class);

        //Menu Fragments for browse project
        register.implementation(INTERNAL, FiltersMenuFragment.class);
        register.implementation(INTERNAL, ReportsMenuFragment.class);
        register.implementation(INTERNAL, ProjectAdminMenuFragment.class);
        register.implementation(INTERNAL, FiltersMenuComponentFragment.class);
        register.implementation(INTERNAL, FiltersMenuVersionFragment.class);
        register.implementation(INTERNAL, ReleaseNotesMenuFragment.class);

        //Popular Issue Types util
        register.implementation(PROVIDED, PopularIssueTypesUtil.class, PopularIssueTypesUtilImpl.class);

        // Long Running Task Support
        register.implementation(PROVIDED, TaskManager.class, TaskManagerImpl.class);

        register.implementation(PROVIDED, IssueSecurityHelper.class, IssueSecurityHelperImpl.class);

        // Plugin Version Store
        register.implementation(INTERNAL, PluginVersionStore.class, OfBizPluginVersionStore.class);

        // ProjectImportStuff
        register.implementation(INTERNAL, ProjectImportService.class, DefaultProjectImportService.class);
        register.implementation(INTERNAL, ProjectImportManager.class, DefaultProjectImportManager.class);
        register.implementation(INTERNAL, BackupXmlParser.class, DefaultBackupXmlParser.class);
        register.implementation(INTERNAL, AutomaticDataMapper.class, AutomaticDataMapperImpl.class);
        register.implementation(INTERNAL, ProjectImportValidators.class, ProjectImportValidatorsImpl.class);
        register.implementation(INTERNAL, PriorityMapperValidator.class);
        register.implementation(INTERNAL, ResolutionMapperValidator.class);
        register.implementation(INTERNAL, ProjectRoleMapperValidator.class);
        register.implementation(INTERNAL, GroupMapperValidator.class);
        register.implementation(INTERNAL, UserMapperValidator.class, UserMapperValidatorImpl.class);
        register.implementation(INTERNAL, IssueTypeMapperValidator.class, IssueTypeMapperValidatorImpl.class);
        register.implementation(INTERNAL, IssueSecurityLevelValidator.class);
        register.implementation(INTERNAL, StatusMapperValidator.class, StatusMapperValidatorImpl.class);
        register.implementation(INTERNAL, IssueLinkTypeMapperValidator.class, IssueLinkTypeMapperValidatorImpl.class);
        register.implementation(INTERNAL, CustomFieldOptionMapperValidator.class, CustomFieldOptionMapperValidatorImpl.class);
        register.implementation(INTERNAL, CustomFieldMapperValidator.class, CustomFieldMapperValidatorImpl.class);
        register.implementation(INTERNAL, ProjectRoleActorMapperValidator.class, ProjectRoleActorMapperValidatorImpl.class);
        register.implementation(INTERNAL, IssueTypeImportHelper.class);
        register.implementation(INTERNAL, ProjectImportPersister.class, DefaultProjectImportPersister.class);

        register.implementation(PROVIDED, JiraApplicationContext.class, DefaultJiraApplicationContext.class);

        register.implementation(PROVIDED, IndexLifecycleManager.class, CompositeIndexLifecycleManager.class);

        //Start TimeTracking Searching
        register.implementation(INTERNAL, WorkRatioValidator.class);
        register.implementation(INTERNAL, WorkRatioClauseQueryFactory.class);
        register.implementation(INTERNAL, WorkRatioSearchHandlerFactory.class);

        register.implementation(INTERNAL, CurrentEstimateValidator.class);
        register.implementation(INTERNAL, CurrentEstimateClauseQueryFactory.class);

        register.implementation(INTERNAL, OriginalEstimateValidator.class);
        register.implementation(INTERNAL, OriginalEstimateClauseQueryFactory.class);

        register.implementation(INTERNAL, TimeSpentValidator.class);
        register.implementation(INTERNAL, TimeSpentClauseQueryFactory.class);
        //End TimeTracking Searching

        register.implementation(PROVIDED, ResolverManager.class, ResolverManagerImpl.class);

        //Start User Searching
        register.implementation(PROVIDED, UserResolver.class, UserResolverImpl.class);

        //Start Select Option Searching
        register.implementation(INTERNAL, CustomFieldOptionResolver.class);

        //Start Reporter Searching
        register.implementation(INTERNAL, ReporterClauseQueryFactory.class);
        register.implementation(INTERNAL, ReporterValidator.class);
        register.implementation(INTERNAL, ReporterSearchHandlerFactory.class);
        register.implementation(INTERNAL, ReporterSearcher.class);
        //End Reporter Searching

        //Start Assignee Searching
        register.implementation(INTERNAL, AssigneeClauseQueryFactory.class);
        register.implementation(INTERNAL, AssigneeValidator.class);
        register.implementation(INTERNAL, AssigneeSearchHandlerFactory.class);
        register.implementation(INTERNAL, AssigneeSearcher.class);
        //End Assignee Searching

        register.implementation(INTERNAL, UserCustomFieldValidator.class);
        //End User Searcher

        //start Component Searching
        register.implementation(INTERNAL, ComponentClauseQueryFactory.class);
        register.implementation(INTERNAL, ComponentValidator.class);
        register.implementation(INTERNAL, ComponentSearchHandlerFactory.class);
        register.implementation(INTERNAL, ComponentResolver.class);
        //end Component Searching

        //Start Project Searching
        register.implementation(INTERNAL, ProjectClauseQueryFactory.class);
        register.implementation(INTERNAL, ProjectValidator.class);
        register.implementation(INTERNAL, ProjectSearchHandlerFactory.class);
        register.implementation(INTERNAL, ProjectResolver.class);
        //End Project Searching.

        //Start Priority Searching
        register.implementation(INTERNAL, PriorityResolver.class);
        register.implementation(INTERNAL, PriorityClauseQueryFactory.class);
        register.implementation(INTERNAL, PriorityValidator.class);
        register.implementation(INTERNAL, PrioritySearchHandlerFactory.class);
        register.implementation(INTERNAL, PrioritySearcher.class);
        //End Priority Searching.

        //Start Priority Searching.
        register.implementation(INTERNAL, ResolutionResolver.class);
        register.implementation(INTERNAL, ResolutionClauseQueryFactory.class);
        register.implementation(INTERNAL, ResolutionValidator.class);
        register.implementation(INTERNAL, ResolutionSearcher.class);
        register.implementation(INTERNAL, ResolutionSearchHandlerFactory.class);
        //End Priority Searching.

        //Start IssueType searching.
        register.implementation(INTERNAL, IssueTypeResolver.class);
        register.implementation(INTERNAL, IssueTypeClauseQueryFactory.class);
        register.implementation(INTERNAL, IssueTypeValidator.class);
        register.implementation(INTERNAL, IssueTypeSearchHandlerFactory.class);
        //End IssueType searching.

        //Start summary searching.
        register.implementation(INTERNAL, SummaryValidator.class);
        register.implementation(INTERNAL, SummaryClauseQueryFactory.class);
        register.implementation(INTERNAL, SummarySearchHandlerFactory.class);
        //End summary searching.

        //Start labels searching.
        register.implementation(INTERNAL, LabelsValidator.class);
        register.implementation(INTERNAL, LabelsClauseValuesGenerator.class);
        register.implementation(INTERNAL, LabelsSearchHandlerFactory.class);
        //End labels searching.

        //Start description searching.
        register.implementation(INTERNAL, DescriptionClauseQueryFactory.class);
        register.implementation(INTERNAL, DescriptionValidator.class);
        register.implementation(INTERNAL, DescriptionSearchHandlerFactory.class);
        //End description searching.

        //Start description searching.
        register.implementation(INTERNAL, EnvironmentClauseQueryFactory.class);
        register.implementation(INTERNAL, EnvironmentValidator.class);
        register.implementation(INTERNAL, EnvironmentSearchHandlerFactory.class);
        //End description searching.

        //Start comment searching.
        register.implementation(INTERNAL, CommentClauseQueryFactory.class);
        register.implementation(INTERNAL, CommentValidator.class);
        register.implementation(INTERNAL, CommentSearchHandlerFactory.class);
        //End comment searching.

        //Start status searching.
        register.implementation(INTERNAL, StatusResolver.class);
        register.implementation(INTERNAL, StatusClauseQueryFactory.class);
        register.implementation(INTERNAL, StatusValidator.class);
        register.implementation(INTERNAL, StatusSearchHandlerFactory.class);
        register.implementation(INTERNAL, StatusClauseContextFactory.class);
        //End status searching.

        //Start affects version searching.
        register.implementation(INTERNAL, AffectedVersionClauseQueryFactory.class);
        register.implementation(INTERNAL, AffectedVersionValidator.class);
        register.implementation(INTERNAL, AffectedVersionSearchHandlerFactory.class);
        //End affects version searching.

        //Start fixfor version searching.
        register.implementation(INTERNAL, FixForVersionClauseQueryFactory.class);
        register.implementation(INTERNAL, FixForVersionValidator.class);
        register.implementation(INTERNAL, FixForVersionSearchHandlerFactory.class);
        //End fixfor version searching.

        //Start created date searching.
        register.implementation(INTERNAL, CreatedDateClauseQueryFactory.class);
        register.implementation(INTERNAL, CreatedDateValidator.class);
        register.implementation(INTERNAL, CreatedDateSearchHandlerFactory.class);
        //End created date searching.

        //Start updated date searching.
        register.implementation(INTERNAL, UpdatedDateClauseQueryFactory.class);
        register.implementation(INTERNAL, UpdatedDateValidator.class);
        register.implementation(INTERNAL, UpdatedDateSearchHandlerFactory.class);
        //End updated date searching.

        //Start due date searching.
        register.implementation(INTERNAL, DueDateClauseQueryFactory.class);
        register.implementation(INTERNAL, DueDateValidator.class);
        register.implementation(INTERNAL, DueDateSearchHandlerFactory.class);
        //End due date searching.

        //Start resolved date searching.
        register.implementation(INTERNAL, ResolutionDateClauseQueryFactory.class);
        register.implementation(INTERNAL, ResolutionDateValidator.class);
        register.implementation(INTERNAL, ResolutionDateSearchHandlerFactory.class);
        //End resolved date searching.

        //Start vote searching
        register.implementation(INTERNAL, VotesValidator.class);
        register.implementation(INTERNAL, VotesIndexValueConverter.class);
        register.implementation(INTERNAL, VotesClauseQueryFactory.class);
        //End vote searching

        //Start voter searching
        register.implementation(INTERNAL, VoterClauseQueryFactory.class);
        //End voter searching

        //Start vote searching
        register.implementation(INTERNAL, WatchesValidator.class);
        register.implementation(INTERNAL, WatchesIndexValueConverter.class);
        register.implementation(INTERNAL, WatchesClauseQueryFactory.class);
        //End vote searching

        //Start watcher searching
        register.implementation(INTERNAL, WatcherClauseQueryFactory.class);
        //End watcher searching

        //Start Issue Key/ID searching.
        register.implementation(INTERNAL, IssueIdClauseQueryFactory.class);
        register.implementation(INTERNAL, IssueIdClauseContextFactory.Factory.class);
        register.implementation(INTERNAL, IssueIdValidator.class);
        register.implementation(PROVIDED, JqlIssueKeySupport.class, JqlIssueKeySupportImpl.class);
        register.implementation(PROVIDED, JqlIssueSupport.class, JqlIssueSupportImpl.class);
        //End issue Key/ID searcher.

        //Start Issue Parent searching.
        register.implementation(INTERNAL, IssueParentClauseQueryFactory.class);
        register.implementation(INTERNAL, IssueParentValidator.class);
        register.implementation(INTERNAL, IssueParentClauseContextFactory.class);
        //End issue Parent searcher.

        //Start issue security level searching
        register.implementation(INTERNAL, IssueSecurityLevelResolver.class);
        register.implementation(INTERNAL, IssueSecurityLevelClauseValidator.class);
        register.implementation(INTERNAL, IssueSecurityLevelClauseQueryFactory.class);
        register.implementation(INTERNAL, IssueSecurityLevelClauseContextFactory.Creator.class);
        //End issue security level searching

        //Start project category searching
        register.implementation(INTERNAL, ProjectCategoryResolver.class);
        register.implementation(INTERNAL, ProjectCategoryValidator.class);
        register.implementation(INTERNAL, ProjectCategoryClauseQueryFactory.class);
        register.implementation(INTERNAL, ProjectCategoryClauseContextFactory.class);
        //End project category searching

        register.implementation(INTERNAL, AllTextClauseQueryFactory.class);
        register.implementation(INTERNAL, AllTextValidator.class);
        register.implementation(INTERNAL, AllTextClauseContextFactory.class);

        //Start change History searching
        register.implementation(INTERNAL, HistoryPredicateQueryFactory.class);
        register.implementation(INTERNAL, EmptyWasClauseOperandHandler.class);
        register.implementation(INTERNAL, WasClauseQueryFactory.class);
        register.implementation(INTERNAL, WasClauseValidator.class);
        register.implementation(INTERNAL, ChangedClauseQueryFactory.class);
        register.implementation(INTERNAL, ChangedClauseValidator.class);
        register.implementation(INTERNAL, ChangeHistoryFieldIdResolver.class);
        register.implementation(INTERNAL, HistoryFieldValueValidator.class);
        //End change History searching

        //Start JQL
        register.implementation(INTERNAL, LuceneQueryBuilder.class, DefaultLuceneQueryBuilder.class);
        register.implementation(INTERNAL, QueryCache.class, QueryCacheImpl.class);
        register.implementation(INTERNAL, OrderByXmlHandler.class, DefaultOrderByXmlHandler.class);
        register.implementation(INTERNAL, SimpleClauseContextFactory.class);
        register.implementation(INTERNAL, CustomFieldOptionsClauseValuesGenerator.class);
        register.implementation(INTERNAL, GroupValuesGenerator.class);
        register.implementation(PROVIDED, SearchSortUtil.class, SearchSortUtilImpl.class);
        register.implementation(INTERNAL, QueryContextConverter.class);
        register.implementation(INTERNAL, SavedFilterCycleDetector.class);
        register.implementation(INTERNAL, QueryRegistry.class, DefaultQueryRegistry.class);
        register.implementation(INTERNAL, QueryContextVisitor.QueryContextVisitorFactory.class);
        register.implementation(INTERNAL, VersionClauseContextFactory.class);
        register.implementation(INTERNAL, ComponentClauseContextFactory.class);
        register.implementation(INTERNAL, FieldConfigSchemeClauseContextUtil.class);
        register.implementation(INTERNAL, JqlSelectOptionsUtil.class);
        register.implementation(INTERNAL, JqlCascadingSelectLiteralUtil.class);
        register.implementation(INTERNAL, ValidatorVisitor.ValidatorVisitorFactory.class);
        register.implementation(INTERNAL, ValidatorRegistry.class, DefaultValidatorRegistry.class);
        register.implementation(INTERNAL, JqlFunctionHandlerRegistry.class, PluginsAwareJqlFunctionHandlerRegistry.class);
        register.implementation(INTERNAL, OperatorUsageValidator.class, DefaultOperatorUsageValidator.class);
        register.implementation(INTERNAL, ClauseXmlHandlerRegistry.class, DefaultClauseXmlHandlerRegistry.class);
        register.implementation(INTERNAL, VersionResolver.class);
        register.implementation(PROVIDED, JqlQueryParser.class, DefaultJqlQueryParser.class);
        register.implementation(INTERNAL, OrderByValidator.class, DefaultOrderByValidator.class);
        register.implementation(INTERNAL, JqlClauseBuilderFactory.class, JqlClauseBuilderFactoryImpl.class);
        register.implementation(PROVIDED, JqlOperandResolver.class, DefaultJqlOperandResolver.class);
        register.implementation(INTERNAL, PredicateOperandResolver.class, DefaultPredicateOperandResolver.class);
        register.implementation(INTERNAL, PredicateOperandHandlerRegistry.class);
        register.implementation(INTERNAL, FieldFlagOperandRegistry.class, DefaultFieldFlagOperandRegistry.class);
        register.implementation(INTERNAL, LuceneQueryModifier.class, DefaultLuceneQueryModifier.class);
        register.implementation(PROVIDED, JqlDateSupport.class, JqlDateSupportImpl.class);
        register.implementation(PROVIDED, JqlLocalDateSupport.class, JqlLocalDateSupportImpl.class);
        register.implementation(INTERNAL, JqlTimetrackingDurationSupport.class, JqlTimetrackingDurationSupportImpl.class);
        register.implementation(PROVIDED, JqlStringSupport.class, JqlStringSupportImpl.class);
        register.implementation(INTERNAL, DiffViewRenderer.class);
        register.implementation(INTERNAL, SavedFilterResolver.class);
        register.implementation(INTERNAL, SavedFilterClauseQueryFactory.class);
        register.implementation(INTERNAL, SavedFilterClauseValidator.class);
        register.implementation(INTERNAL, SavedFilterClauseContextFactory.class);
        register.implementation(INTERNAL, FieldContextGenerator.class);
        register.instance(INTERNAL, ContextSetUtil.getInstance());
        register.implementation(INTERNAL, SystemClauseHandlerFactory.class, DefaultSystemClauseHandlerFactory.class);
        register.implementation(PROVIDED, AutoCompleteJsonGenerator.class, DefaultAutoCompleteJsonGenerator.class);
        register.implementation(INTERNAL, FieldClausePermissionChecker.Factory.class, FieldClausePermissionChecker.DefaultFactory.class);
        register.implementation(INTERNAL, CustomFieldClausePermissionChecker.Factory.class, CustomFieldClausePermissionChecker.DefaultFactory.class);
        register.implementation(INTERNAL, MultiClauseDecoratorContextFactory.Factory.class);
        register.implementation(PROVIDED, PredicateRegistry.class, DefaultPredicateRegistry.class);
        register.implementation(PROVIDED, LuceneDirectoryUtils.class, LuceneDirectoryUtilsImpl.class);
        //End JQL

        register.implementation(PROVIDED, CustomFieldInputHelper.class, DefaultCustomFieldInputHelper.class);

        // User History
        register.implementation(INTERNAL, UserHistoryStore.class, SessionBasedAnonymousUserHistoryStore.class,
                CachingUserHistoryStore.class, ApplicationProperties.class, VelocityRequestContextFactory.class);
        register.implementation(INTERNAL, CachingUserHistoryStore.class);
        register.implementation(INTERNAL, OfBizUserHistoryStore.class);
        register.implementation(PROVIDED, UserHistoryManager.class, DefaultUserHistoryManager.class);
        register.implementation(PROVIDED, UserIssueHistoryManager.class, DefaultUserIssueHistoryManager.class);
        register.implementation(PROVIDED, UserProjectHistoryManager.class, DefaultUserProjectHistoryManager.class);
        register.implementation(PROVIDED, UserAdminHistoryManager.class, DefaultUserAdminHistoryManager.class);
        register.implementation(PROVIDED, UserQueryHistoryManager.class, DefaultUserQueryHistoryManager.class);

        register.implementation(INTERNAL, OSWorkflowConfigurator.class, DefaultOSWorkflowConfigurator.class);

        register.implementation(PROVIDED, HashRegistryCache.class, HashRegistryCacheImpl.class);

        register.implementation(INTERNAL, PagerManager.class);

        register.implementation(PROVIDED, ChartUtils.class, ChartUtilsImpl.class);
        register.implementation(PROVIDED, ChartFactory.class, DefaultChartFactory.class);

        register.implementation(PROVIDED, HostContextAccessor.class, DefaultHostContextAccessor.class);

        register.implementation(PROVIDED, IssueViewRequestParamsHelper.class, IssueViewRequestParamsHelperImpl.class);

        register.implementation(INTERNAL, AvatarStore.class, CachingAvatarStore.class, OfbizAvatarStore.class);

        register.implementation(PROVIDED, MentionFinder.class, MentionFinderImpl.class);
        register.implementation(PROVIDED, MentionService.class, MentionServiceImpl.class);
        register.implementation(INTERNAL, MentionEventListener.class);
        register.implementation(INTERNAL, EmailMentionedUsers.class);

        register.implementation(INTERNAL, OfbizAvatarStore.class);

        register.implementation(PROVIDED, ReindexMessageManager.class, DefaultReindexMessageManager.class);
        register.implementation(INTERNAL, CustomFieldContextConfigHelper.class, CustomFieldContextConfigHelperImpl.class);
        register.implementation(INTERNAL, FieldLayoutSchemeHelper.class, FieldLayoutSchemeHelperImpl.class);

        // Atlassian-cache
        register.implementation(INTERNAL, CacheProvider.class, MemoryCacheProvider.class);
        register.implementation(PROVIDED, com.atlassian.cache.CacheManager.class, com.atlassian.cache.DefaultCacheManager.class,
                CacheProvider.class);

        // IssueService classes
        register.implementation(PROVIDED, IssueService.class, DefaultIssueService.class);
        // TODO: IssueDeleteHelper should be internal?
        register.implementation(PROVIDED, IssueDeleteHelper.class, DefaultIssueDeleteHelper.class);

        //Dashboards
        register.implementation(PROVIDED, DashboardStateStore.class, JiraDashboardStateStoreManager.class);
        register.implementation(PROVIDED, ExternalGadgetSpecStore.class, JiraExternalGadgetSpecStore.class);
        register.implementation(PROVIDED, GadgetStateFactory.class, JiraGadgetStateFactory.class);
        register.implementation(PROVIDED, DirectoryPermissionService.class, JiraDirectoryPermissionService.class);
        register.implementation(PROVIDED, DashboardPermissionService.class, JiraPermissionService.class);
        register.implementation(PROVIDED, Whitelist.class, JiraWhitelist.class);
        register.implementation(INTERNAL, OfbizExternalGadgetStore.class);
        register.implementation(INTERNAL, com.atlassian.jira.portal.gadgets.ExternalGadgetStore.class, CachingExternalGadgetStore.class,
                OfbizExternalGadgetStore.class);
        register.implementation(PROVIDED, GadgetPermissionManager.class, JiraGadgetPermissionManager.class);
        register.implementation(PROVIDED, PluginGadgetSpecProviderPermission.class, JiraPluginGadgetSpecProviderPermission.class);

        //Whitelisting
        register.implementation(PROVIDED, WhitelistService.class, DefaultWhitelistService.class);
        register.implementation(PROVIDED, WhitelistManager.class, DefaultWhitelistManager.class);

        register.implementation(INTERNAL, AutowireCapableWebworkActionRegistry.class, DefaultAutowireCapableWebworkActionRegistry.class);
        register.implementation(INTERNAL, ActionConfiguration.class, ActionConfiguration.FromWebWorkConfiguration.class);
        register.implementation(INTERNAL, WebworkPluginSecurityServiceHelper.class);
        register.implementation(INTERNAL, LegacyPortletUpgradeTaskFactory.class, DefaultLegacyPortletUpgradeTaskFactory.class);

        register.implementation(PROVIDED, BuildUtilsInfo.class, BuildUtilsInfoImpl.class);
        register.instance(INTERNAL, ExternalLinkUtil.class, ExternalLinkUtilImpl.getInstance());

        register.implementation(INTERNAL, AuthorizationSupport.class, DefaultAuthorizationSupport.class);
        register.instance(INTERNAL, UriValidator.class,
                UriValidatorFactory.create(register.getComponentInstance(ApplicationProperties.class)));

        register.component(INTERNAL, new ConstructorInjectionComponentAdapter(AddFieldToScreenUtilImpl.class,
                AddFieldToScreenUtilImpl.class));
        register.component(INTERNAL, new ConstructorInjectionComponentAdapter(SelectComponentAssigneesUtil.class,
                SelectComponentAssigneesUtilImpl.class));

        // XSRF protection
        register.implementation(INTERNAL, XsrfDefaults.class, XsrfDefaultsImpl.class);
        register.implementation(PROVIDED, XsrfTokenGenerator.class, SimpleXsrfTokenGenerator.class);
        register.implementation(PROVIDED, XsrfInvocationChecker.class, DefaultXsrfInvocationChecker.class);

        // The Jira Session tracking support
        register.implementation(INTERNAL, JiraUserSessionTracker.class);

        // Seraph Remember Me SPI
        register.implementation(INTERNAL, RememberMeConfiguration.class, JiraRememberMeConfiguration.class);
        register.implementation(INTERNAL, RememberMeTokenDao.class, JiraRememberMeTokenDao.class);
        register.implementation(INTERNAL, RememberMeTokenGenerator.class, DefaultRememberMeTokenGenerator.class);
        register.implementation(INTERNAL, RememberMeService.class, JiraRememberMeService.class);

        // License 2.0 objects
        register.implementation(PROVIDED, JiraLicenseService.class, JiraLicenseServiceImpl.class);
        register.implementation(PROVIDED, JiraServerIdProvider.class, DefaultJiraServerIdProvider.class);
        register.component(INTERNAL, new DelegateComponentAdapter<JiraLicenseUpdaterService>(JiraLicenseUpdaterService.class,
                register.getComponentAdapter(JiraLicenseService.class)));
        // TODO: JiraLicenseManager: Provided for Login Gadget? Is it API?
        register.implementation(PROVIDED, JiraLicenseManager.class, JiraLicenseManagerImpl.class);
        register.implementation(INTERNAL, JiraLicenseStore.class, JiraLicenseStoreImpl.class);
        register.implementation(INTERNAL, LicenseJohnsonEventRaiser.class, LicenseJohnsonEventRaiserImpl.class);
        register.instance(INTERNAL, LicenseManager.class, LicenseManagerFactory.getLicenseManager());
        register.implementation(INTERNAL, SIDManager.class, DefaultSIDManager.class);
        register.implementation(INTERNAL, LicenseStringFactory.class, LicenseStringFactoryImpl.class);

        // TODO: LoginService: Provided for Login Gadget? Is it API?
        register.implementation(PROVIDED, LoginService.class, LoginServiceImpl.class);
        register.implementation(INTERNAL, LoginManager.class, LoginManagerImpl.class);
        register.implementation(INTERNAL, LoginStore.class, LoginStoreImpl.class);

        register.implementation(INTERNAL, JiraSystemRestarter.class, JiraSystemRestarterImpl.class);
        register.implementation(INTERNAL, JiraCaptchaService.class, JiraCaptchaServiceImpl.class);
        register.implementation(INTERNAL, JiraLogLocator.class);
        register.implementation(INTERNAL, BuildVersionRegistry.class, DefaultBuildVersionRegistry.class);

        register.implementation(PROVIDED, SessionSearchObjectManagerFactory.class, DefaultSessionSearchObjectManagerFactory.class);

        register.implementation(PROVIDED, KeyboardShortcutManager.class, CachingKeyboardShortcutManager.class);

        register.implementation(PROVIDED, LabelService.class, DefaultLabelService.class);
        register.implementation(PROVIDED, LabelManager.class, DefaultLabelManager.class);
        register.implementation(PROVIDED, LabelUtil.class, DefaultLabelUtil.class);
        register.implementation(PROVIDED, ProjectImportLabelFieldParser.class, ProjectImportLabelFieldParserImpl.class);

        register.implementation(INTERNAL, OfBizLabelStore.class);
        register.implementation(INTERNAL, LabelStore.class, CachingLabelStore.class, OfBizLabelStore.class);

        register.implementation(PROVIDED, HintManager.class, DefaultHintManager.class);
        register.implementation(PROVIDED, TemporaryAttachmentsMonitorLocator.class, DefaultTemporaryAttachmentsMonitorLocator.class);

        register.implementation(PROVIDED, FieldsResourceIncluder.class, FieldsResourceIncluderImpl.class);
        register.implementation(INTERNAL, CalendarResourceIncluder.class);

        register.implementation(INTERNAL, HackyFieldRendererRegistry.class, DefaultHackyFieldRendererRegistry.class);
        register.implementation(INTERNAL, FileFactory.class, FileSystemFileFactory.class);
        register.implementation(PROVIDED, IssueSearchLimits.class, IssueSearchLimitsImpl.class);

        register.implementation(PROVIDED, AlphabeticalLabelRenderer.class, DefaultAlphabeticalLabelRenderer.class);
        register.implementation(PROVIDED, PluginInfoProvider.class, PluginInfoProviderImpl.class);

        //UAL
        register.implementation(PROVIDED, InternalHostApplication.class, JiraAppLinksHostApplication.class);
        //The ApplicationLinkService and the EntityLinkService are provided by the applinks-plugin
        //we use the OsgiServiceProxyFactory to get a reference to them, so we can use them inside JIRA.
        register.implementation(INTERNAL, OsgiServiceProxyFactory.class);
        register.implementation(INTERNAL, ApplicationLinkService.class, JiraApplicationLinkService.class);
        register.implementation(INTERNAL, EntityLinkService.class, JiraEntityLinkService.class);
        register.implementation(INTERNAL, SafeRedirectChecker.class);

        register.implementation(INTERNAL, GadgetApplinkUpgradeUtil.class);

        register.implementation(INTERNAL, DataImportProductionDependencies.class);
        register.implementation(INTERNAL, DataImportService.class, DefaultDataImportService.class);
        register.implementation(INTERNAL, ImportResultHandler.class, DefaultImportResultHandler.class);

        register.implementation(INTERNAL, ExportService.class, DefaultExportService.class);

        register.implementation(PROVIDED, DateTimeFormatterFactory.class, DateTimeFormatterFactoryImpl.class);
        register.component(PROVIDED, new DateTimeFormatterComponentAdapter());

        register.implementation(PROVIDED, DateFieldFormat.class, DateFieldFormatImpl.class);
        register.implementation(PROVIDED, DateTimeFieldChangeLogHelper.class, DateTimeFieldChangeLogHelperImpl.class);
        register.implementation(INTERNAL, ColumnViewDateTimeHelper.class);

        register.implementation(PROVIDED, SecureUserTokenManager.class, DefaultSecureUserTokenManager.class);

        register.implementation(INTERNAL, XmlRpcRequestProcessor.class, PluggableXmlRpcRequestProcessor.class);
        register.implementation(INTERNAL, AxisServletProvider.class, PluggableAxisServletProvider.class);

        register.implementation(PROVIDED, ApplicationPropertiesService.class, ApplicationPropertiesServiceImpl.class);

        // These are only provided because we need it in the project config plugin and satellite admin pages to show
        // shared projects, among other things. It is really an internal class.
        register.implementation(PROVIDED, ProjectWorkflowSchemeHelper.class, DefaultProjectWorkflowSchemeHelper.class);
        register.implementation(PROVIDED, ProjectFieldLayoutSchemeHelper.class, DefaultProjectFieldLayoutSchemeHelper.class);
        register.implementation(PROVIDED, ProjectIssueTypeScreenSchemeHelper.class, DefaultProjectIssueTypeScreenSchemeHelper.class);
        register.implementation(PROVIDED, ProjectPermissionSchemeHelper.class, DefaultProjectPermissionSchemeHelper.class);
        register.implementation(PROVIDED, ProjectNotificationsSchemeHelper.class, DefaultProjectNotificationsSchemeHelper.class);
        register.implementation(PROVIDED, ProjectIssueSecuritySchemeHelper.class, DefaultProjectIssueSecuritySchemeHelper.class);
        register.implementation(PROVIDED, ProjectFieldScreenHelper.class, DefaultProjectFieldScreenHelper.class);

        register.implementation(PROVIDED, FeatureManager.class, DefaultFeatureManager.class);
        register.implementation(PROVIDED, IssueLinksBeanBuilderFactory.class, IssueLinksBeanBuilderFactoryImpl.class);
        register.implementation(PROVIDED, IssueLinkTypeFinder.class, IssueLinkTypeFinderImpl.class);
        register.implementation(PROVIDED, IssueFinder.class, IssueFinderImpl.class);
        register.implementation(PROVIDED, FieldHtmlFactory.class, FieldHtmlFactoryImpl.class);

        register.implementation(PROVIDED, IssueTabPanelInvoker.class, IssueTabPanelInvokerImpl.class);

        register.implementation(INTERNAL, ApplicationPropertiesChecker.class);
        register.implementation(PROVIDED, Encoder.class, SwitchingEncoder.class);
        register.implementation(INTERNAL, AnnouncementBanner.class);
        register.implementation(PROVIDED, IntroductionProperty.class, IntroductionPropertyImpl.class);
        register.implementation(INTERNAL, CustomFieldDefaultVelocityParams.class);
        register.implementation(PROVIDED, ProjectDescriptionRenderer.class, ProjectDescriptionRendererImpl.class);

        register.implementation(INTERNAL, TextFieldLimitProvider.class);
        register.implementation(INTERNAL, VelocityTemplateCache.class);
        register.implementation(INTERNAL, GzipCompression.class);
        register.implementation(INTERNAL, Assignees.class);
        register.implementation(INTERNAL, RenderablePropertyFactory.class);
        register.implementation(INTERNAL, CustomFieldDescription.class);
    }

    private void registerSystemTenantPluginSystem(ComponentContainer register)
    {
        /*
            Components that are registered here are components necessary for a single plugins system.
         */

        register.implementation(INTERNAL, JiraStartupPluginSystemListener.class);
        register.implementation(INTERNAL, HostContainer.class, JiraHostContainer.class);
        register.implementation(INTERNAL, JiraModuleDescriptorFactory.class);
        register.implementation(PROVIDED, ModuleDescriptorFactory.class,MultiTenantModuleDescriptorFactory.class,
                HostContainer.class,
                JiraModuleDescriptorFactory.class,
                PluginsEventPublisher.class,
                MultiTenantComponentFactory.class,
                MultiTenantManager.class);
        register.implementation(PROVIDED, PluginLoaderFactory.class, DefaultPluginLoaderFactory.class);
        register.implementation(PROVIDED, ModuleFactory.class, JiraModuleFactory.class);
        register.implementation(PROVIDED, ServletModuleManager.class, DefaultServletModuleManager.class,
                new ConstantParameter(ServletContextProvider.getServletContext()),
                PluginEventManager.class);
        register.implementation(PROVIDED, PluginMetadataManager.class, DefaultPluginMetadataManager.class);

        DelegateComponentAdapter.Builder.builderFor(JiraPluginManager.class).implementing(PluginSystemLifecycle.class).implementing(
                PluginAccessor.class).implementing(PluginController.class).registerWith(PROVIDED, register);

        // Event publisher for each tenant
        MultiTenantComponentFactory factory = MultiTenantContext.getFactory();
        // This uses the system no tenant strategy because the Spring DM publishes many events that end up going to
        // this event publisher, in threads that have no context.  Here they get directed to the system tenant.
        MultiTenantComponentMap<EventPublisher> eventPublisherMap = factory.createComponentMapBuilder(new MultiTenantCreator<EventPublisher>()
        {
            @Override
            public EventPublisher create(Tenant tenant)
            {
                return JiraUtils.loadComponent(EventPublisherImpl.class);
            }
        }).setNoTenantStrategy(MultiTenantComponentMap.NoTenantStrategy.SYSTEM).construct();
        EventPublisher eventPublisher = factory.createComponent(eventPublisherMap, EventPublisher.class);
        register.implementation(INTERNAL, PeeringEventPublisherManager.class, DefaultPeeringEventPublisherManager.class);
        register.implementation(INTERNAL, EventPublisher.class, PeeringEventPublisher.class,
                PeeringEventPublisherManager.class,
                new ConstantParameter(eventPublisher));
        register.instance(INTERNAL, new EventPublisherDestroyer(eventPublisherMap));

        // Event publisher for plugins, this is internal because it has to be explicitly provided
        register.implementation(INTERNAL, PluginsEventPublisher.class);
        register.implementation(INTERNAL, JiraCacheResetter.class);
        register.implementation(PROVIDED, IssueEventManager.class, DefaultIssueEventManager.class);

        register.implementation(PROVIDED, PackageScannerConfiguration.class, DefaultPackageScannerConfiguration.class);
        register.implementation(PROVIDED, DownloadStrategy.class, JiraPluginResourceDownload.class);
        register.implementation(PROVIDED, ContentTypeResolver.class, JiraContentTypeResolver.class);
        register.implementation(PROVIDED, PluginEventManager.class, DefaultPluginEventManager.class, PluginsEventPublisher.class);
        register.implementation(PROVIDED, PluginPath.class, PluginPath.JiraHomeAdapter.class);
        register.implementation(INTERNAL, OsgiContainerManager.class, JiraOsgiContainerManager.class);
        register.implementation(INTERNAL, HostComponentProvider.class, MultiTenantHostComponentProvider.class,
                new ConstantParameter(register.getHostComponentProvider()),
                new ComponentParameter(MultiTenantHostComponentProxier.class));
        register.implementation(INTERNAL, MultiTenantHostComponentProxier.class);
        register.implementation(PROVIDED, PluginResourceLocator.class, PluginResourceLocatorImpl.class);
        register.implementation(PROVIDED, ServletContextFactory.class, JiraServletContextFactory.class);
        register.implementation(INTERNAL, ComponentClassManager.class, DefaultComponentClassManager.class);

        register.implementation(INTERNAL, PluginPersistentStateStore.class, JiraPluginPersistentStateStore.class);

        register.implementation(INTERNAL, TransactionSupport.class, TransactionSupportImpl.class);

        register.implementation(INTERNAL, RegistryConfiguration.class, InstrumentationConfiguration.class);
        register.implementation(INTERNAL, OpTimerFactory.class, ThreadLocalOpTimerFactory.class);
        register.implementation(PROVIDED, InstrumentRegistry.class, DefaultInstrumentRegistry.class);
        register.implementation(INTERNAL, Instrumentation.class);

        // Webwork should only get configured once
        register.implementation(INTERNAL, WebworkConfigurator.class);
        register.implementation(INTERNAL, PluginsAwareViewMapping.Component.class);

        // Upgrade Tasks Components
        register.implementation(INTERNAL, Sequences.class);

        // Some hacks needed for studio.
        register.implementation(PROVIDED, StudioHooks.class, PluginStudioHooks.class);
        register.implementation(PROVIDED, ImportResultStore.class, DefaultImportResultStore.class);
    }

    private void registerNormalTenantPluginSystem(ComponentContainer register)
    {
        /*
            This takes all the components needed in PICO that are shared out of the system tenant container
            and registers them in this container.
         */
        ComponentManager systemComponentManager;
        // We need the system tenant component manager
        MultiTenantContext.getTenantReference().set(MultiTenantContext.getSystemTenant(), true);
        try
        {
            systemComponentManager = ComponentManager.getInstance();
        }
        finally
        {
            MultiTenantContext.getTenantReference().remove();
        }
        // TODO: Pretty much all these seem INTERNAL?
        register.transfer(systemComponentManager, PROVIDED, HostContainer.class);
        register.transfer(systemComponentManager, PROVIDED, ModuleDescriptorFactory.class);
        // TODO: This looks like it should be internal
        register.transfer(systemComponentManager, PROVIDED, PluginLoaderFactory.class);
        register.transfer(systemComponentManager, PROVIDED, ModuleFactory.class);
        register.transfer(systemComponentManager, PROVIDED, ServletModuleManager.class);
        register.transfer(systemComponentManager, PROVIDED, PluginMetadataManager.class);

        // Plugin manager classes
        register.transfer(systemComponentManager, PROVIDED, PluginSystemLifecycle.class);
        register.transfer(systemComponentManager, PROVIDED, PluginAccessor.class);
        register.transfer(systemComponentManager, PROVIDED, PluginController.class);

        register.transfer(systemComponentManager, INTERNAL, EventPublisher.class);

        register.transfer(systemComponentManager, PROVIDED, PackageScannerConfiguration.class);
        register.transfer(systemComponentManager, PROVIDED, DownloadStrategy.class);
        register.transfer(systemComponentManager, PROVIDED, ContentTypeResolver.class);
        register.transfer(systemComponentManager, PROVIDED, PluginEventManager.class);
        // TODO: This looks like it should be internal
        register.transfer(systemComponentManager, PROVIDED, PluginPath.class);
        register.transfer(systemComponentManager, INTERNAL, OsgiContainerManager.class);
        register.transfer(systemComponentManager, PROVIDED, PluginResourceLocator.class);
        register.transfer(systemComponentManager, PROVIDED, ServletContextFactory.class);
        register.transfer(systemComponentManager, INTERNAL, ComponentClassManager.class);

        register.transfer(systemComponentManager, INTERNAL, MultiTenantHostComponentProxier.class);
        register.transfer(systemComponentManager, INTERNAL, PluginPersistentStateStore.class);
    }

}
