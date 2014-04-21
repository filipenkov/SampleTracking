package com.atlassian.jira;

import com.atlassian.cache.CacheProvider;
import com.atlassian.cache.memory.MemoryCacheProvider;
import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.password.factory.PasswordEncoderFactoryImpl;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.EventThreadPoolConfiguration;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.EventExecutorFactoryImpl;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.internal.EventThreadPoolConfigurationImpl;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.EventExecutorFactory;
import com.atlassian.instrumentation.DefaultInstrumentRegistry;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.instrumentation.RegistryConfiguration;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.ThreadLocalOpTimerFactory;
import com.atlassian.jira.appconsistency.db.LockedDatabaseOfBizDelegator;
import com.atlassian.jira.bc.license.BootstrapJiraServerIdProvider;
import com.atlassian.jira.bc.license.JiraServerIdProvider;
import com.atlassian.jira.config.BootstrapFeatureManager;
import com.atlassian.jira.config.DefaultLocaleManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.component.SimpleSwitchingComponentAdaptor;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.DatabaseConfigurationManagerImpl;
import com.atlassian.jira.config.database.SystemTenantDatabaseConfigurationLoader;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.BackingPropertySetManager;
import com.atlassian.jira.config.properties.DbBackedPropertiesManager;
import com.atlassian.jira.config.properties.MemorySwitchToDatabaseBackedPropertiesManager;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.DefaultJiraHome;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.config.webwork.WebworkConfigurator;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryImpl;
import com.atlassian.jira.event.JiraListenerHandlerConfigurationImpl;
import com.atlassian.jira.i18n.JiraI18nResolver;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationConfiguration;
import com.atlassian.jira.multitenant.EventPublisherDestroyer;
import com.atlassian.jira.multitenant.MultiTenantHostComponentProvider;
import com.atlassian.jira.multitenant.MultiTenantHostComponentProxier;
import com.atlassian.jira.multitenant.PluginsEventPublisher;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.BootstrapPluginLoaderFactory;
import com.atlassian.jira.plugin.BootstrapPluginVersionStore;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.DefaultComponentClassManager;
import com.atlassian.jira.plugin.DefaultPackageScannerConfiguration;
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
import com.atlassian.jira.plugin.PluginLoaderFactory;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.plugin.PluginVersionStore;
import com.atlassian.jira.plugin.util.PluginModuleTrackerFactory;
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
import com.atlassian.jira.propertyset.DefaultJiraPropertySetFactory;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.sal.JiraApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManagerImpl;
import com.atlassian.jira.security.xsrf.DefaultXsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.SimpleXsrfTokenGenerator;
import com.atlassian.jira.security.xsrf.XsrfDefaults;
import com.atlassian.jira.security.xsrf.XsrfDefaultsImpl;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.setting.GzipCompression;
import com.atlassian.jira.startup.JiraStartupPluginSystemListener;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.velocity.DefaultVelocityTemplatingEngine;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.timezone.NoDatabaseTimeZoneResolver;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.FileSystemFileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraComponentFactory;
import com.atlassian.jira.util.JiraComponentLocator;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.UnsupportedBrowserManager;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.i18n.I18nTranslationModeImpl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapper;
import com.atlassian.jira.web.action.setup.IndexLanguageToLocaleMapperImpl;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarLanguageUtilImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.dispatcher.PluginsAwareViewMapping;
import com.atlassian.jira.web.session.currentusers.JiraUserSessionTracker;
import com.atlassian.jira.web.util.JiraLocaleUtils;
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
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.spi.HostContextAccessor;
import com.atlassian.seraph.auth.AuthenticationContext;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import com.atlassian.velocity.JiraVelocityManager;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.velocity.htmlsafe.event.referenceinsertion.EnableHtmlEscapingDirectiveHandler;
import org.picocontainer.defaults.ConstantParameter;

import static com.atlassian.jira.ComponentContainer.Scope.INTERNAL;
import static com.atlassian.jira.ComponentContainer.Scope.PROVIDED;

/**
 * Register the components in the {@link com.atlassian.jira.ComponentContainer} that allow JIRA to be bootstrapped
 * without needing to be backed by an underlying database.
 *
 * The components in here at meant to be the MINIMUM set of components that JIRA needs.  Don't add components here unless
 * you are positive that that pre-database code in JIRA needs it.
 *
 */
@SuppressWarnings ("deprecation")
class BootstrapContainerRegistrar
{
    public void registerComponents(final ComponentContainer register)
    {
        // MT bootstrap stuff
        register.instance(PROVIDED, TenantReference.class, MultiTenantContext.getTenantReference());
        register.instance(PROVIDED, MultiTenantComponentFactory.class, MultiTenantContext.getFactory());
        register.instance(PROVIDED, MultiTenantManager.class, MultiTenantContext.getManager());

        register.implementation(INTERNAL, JiraStartupPluginSystemListener.class);
        register.implementation(INTERNAL, HostContainer.class, JiraHostContainer.class);
        register.implementation(INTERNAL, JiraModuleDescriptorFactory.class);

        register.implementation(PROVIDED, ModuleDescriptorFactory.class, MultiTenantModuleDescriptorFactory.class,
                HostContainer.class,
                JiraModuleDescriptorFactory.class,
                PluginsEventPublisher.class,
                MultiTenantComponentFactory.class,
                MultiTenantManager.class);

        register.implementation(PROVIDED, PluginLoaderFactory.class, BootstrapPluginLoaderFactory.class);
        register.implementation(PROVIDED, ModuleFactory.class, JiraModuleFactory.class);

        register.implementation(PROVIDED, ServletModuleManager.class, DefaultServletModuleManager.class,
                new ConstantParameter(ServletContextProvider.getServletContext()),
                PluginEventManager.class);

        register.implementation(PROVIDED, PluginMetadataManager.class, DefaultPluginMetadataManager.class);

        DelegateComponentAdapter.Builder.builderFor(JiraPluginManager.class)
                .implementing(PluginSystemLifecycle.class)
                .implementing(PluginAccessor.class)
                .implementing(PluginController.class)
                .registerWith(PROVIDED, register);

        register.implementation(INTERNAL, JiraCacheResetter.class);

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

        register.implementation(PROVIDED, PluginEventManager.class, DefaultPluginEventManager.class,
                PluginsEventPublisher.class);

        register.implementation(PROVIDED, PluginPath.class, PluginPath.JiraHomeAdapter.class);
        register.implementation(INTERNAL, OsgiContainerManager.class, JiraOsgiContainerManager.class);

        register.implementation(INTERNAL, HostComponentProvider.class, MultiTenantHostComponentProvider.class,
                new ConstantParameter(register.getHostComponentProvider()),
                MultiTenantHostComponentProxier.class);

        register.implementation(INTERNAL, MultiTenantHostComponentProxier.class);

        register.implementation(PROVIDED, PackageScannerConfiguration.class, DefaultPackageScannerConfiguration.class);

        register.implementation(PROVIDED, DownloadStrategy.class, JiraPluginResourceDownload.class);
        register.implementation(PROVIDED, ContentTypeResolver.class, JiraContentTypeResolver.class);

        register.implementation(PROVIDED, PluginResourceLocator.class, PluginResourceLocatorImpl.class);
        register.implementation(PROVIDED, ServletContextFactory.class, JiraServletContextFactory.class);
        register.implementation(INTERNAL, ComponentClassManager.class, DefaultComponentClassManager.class);

        register.implementation(PROVIDED, HostContextAccessor.class, DefaultHostContextAccessor.class);

        register.implementation(INTERNAL, PluginPersistentStateStore.class, JiraPluginPersistentStateStore.class);
        register.implementation(INTERNAL, PluginVersionStore.class, BootstrapPluginVersionStore.class);
        register.implementation(INTERNAL, PluginModuleTrackerFactory.class);

        register.implementation(INTERNAL, EventDispatcher.class, AsynchronousAbleEventDispatcher.class);
        register.implementation(INTERNAL, EventExecutorFactory.class, EventExecutorFactoryImpl.class);
        register.implementation(INTERNAL, EventThreadPoolConfiguration.class, EventThreadPoolConfigurationImpl.class);
        register.implementation(INTERNAL, ListenerHandlersConfiguration.class, JiraListenerHandlerConfigurationImpl.class);

        register.implementation(PROVIDED, WebInterfaceManager.class, DefaultWebInterfaceManager.class);
        register.implementation(PROVIDED, WebFragmentHelper.class, JiraWebFragmentHelper.class);
        register.implementation(PROVIDED, WebResourceManager.class, JiraWebResourceManagerImpl.class);
        register.implementation(PROVIDED, WebResourceIntegration.class, JiraWebResourceIntegration.class);
        register.implementation(PROVIDED, WebResourceUrlProvider.class, WebResourceUrlProviderImpl.class);
        register.implementation(PROVIDED, ResourceBatchingConfiguration.class, JiraWebResourceBatchingConfiguration.class);
        register.implementation(PROVIDED, SimpleLinkManager.class, DefaultSimpleLinkManager.class);
        register.implementation(PROVIDED, SimpleLinkFactoryModuleDescriptors.class, DefaultSimpleLinkFactoryModuleDescriptors.class);
        register.implementation(INTERNAL, JiraWebInterfaceManager.class);
        register.implementation(PROVIDED, EncodingConfiguration.class, EncodingConfiguration.PropertiesAdaptor.class);

        // Velocity components
        register.implementation(INTERNAL, VelocityEngineFactory.class, VelocityEngineFactory.Default.class);
        register.implementation(INTERNAL, EnableHtmlEscapingDirectiveHandler.class);
        register.implementation(PROVIDED, VelocityTemplatingEngine.class, DefaultVelocityTemplatingEngine.class);
        register.implementation(PROVIDED, VelocityManager.class, JiraVelocityManager.class);
        register.implementation(PROVIDED, VelocityRequestContextFactory.class, DefaultVelocityRequestContextFactory.class);

        // this will prevent ANY database access via OfBiz.  This is belts and braces on top of the bootstrapping
        register.implementation(PROVIDED, OfBizDelegator.class, LockedDatabaseOfBizDelegator.class);

        register.instance(PROVIDED, ActionDispatcher.class, CoreFactory.getActionDispatcher());
        register.implementation(INTERNAL, WebworkConfigurator.class);
        register.implementation(INTERNAL, PluginsAwareViewMapping.Component.class);
        register.implementation(INTERNAL, AutowireCapableWebworkActionRegistry.class, DefaultAutowireCapableWebworkActionRegistry.class);
        register.implementation(INTERNAL, ActionConfiguration.class, ActionConfiguration.FromWebWorkConfiguration.class);

        // this allows us to determine if the database is setup or not
        register.implementation(INTERNAL, DatabaseConfigurationManager.class, DatabaseConfigurationManagerImpl.class);
        register.implementation(INTERNAL, DatabaseConfigurationLoader.class, SystemTenantDatabaseConfigurationLoader.class);
        register.implementation(INTERNAL, ComponentLocator.class, JiraComponentLocator.class);

        register.implementation(INTERNAL, WebworkPluginSecurityServiceHelper.class);
        register.implementation(PROVIDED, AuthenticationContext.class, AuthenticationContextImpl.class);
        register.implementation(PROVIDED, JiraAuthenticationContext.class, JiraAuthenticationContextImpl.class);
        register.implementation(INTERNAL, JiraUserSessionTracker.class);

        register.implementation(INTERNAL, CacheProvider.class, MemoryCacheProvider.class);
        register.implementation(PROVIDED, com.atlassian.cache.CacheManager.class, com.atlassian.cache.DefaultCacheManager.class,
                CacheProvider.class);

        register.implementation(INTERNAL, I18nBean.CachingFactory.class);
        register.implementation(PROVIDED, I18nHelper.BeanFactory.class, I18nBean.AccessorFactory.class);
        register.implementation(INTERNAL, JiraLocaleUtils.class);
        register.implementation(PROVIDED, I18nTranslationMode.class, I18nTranslationModeImpl.class);
        // By rights, the I18nResolver and ApplcationResolver should be defined in jira-sal-plugin, but it is need by AUI during bootstrap
        // So instead of creating a separate jira-sal-plugin-micro-kernel plugin, we just whack it in the BootstrapContainer.
        register.implementation(PROVIDED, I18nResolver.class, JiraI18nResolver.class);
        register.implementation(PROVIDED, com.atlassian.sal.api.ApplicationProperties.class, JiraApplicationProperties.class);

        register.implementation(PROVIDED, LocaleManager.class, DefaultLocaleManager.class);
        register.implementation(PROVIDED, IndexLanguageToLocaleMapper.class, IndexLanguageToLocaleMapperImpl.class);
        register.implementation(PROVIDED, CalendarLanguageUtil.class, CalendarLanguageUtilImpl.class);
        register.implementation(INTERNAL, UnsupportedBrowserManager.class);

        register.implementation(PROVIDED, JiraPropertySetFactory.class, DefaultJiraPropertySetFactory.class);
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
        register.implementation(PROVIDED, JiraHome.class, DefaultJiraHome.class);
        register.implementation(INTERNAL, ApplicationPropertiesStore.class);

        register.implementation(PROVIDED, ApplicationProperties.class, ApplicationPropertiesImpl.class);
        register.implementation(PROVIDED, BuildUtilsInfo.class, BuildUtilsInfoImpl.class);
        register.implementation(INTERNAL, FileFactory.class, FileSystemFileFactory.class);

        register.implementation(PROVIDED, PasswordEncoderFactory.class, PasswordEncoderFactoryImpl.class);
        register.instance(INTERNAL, ComponentFactory.class, JiraComponentFactory.getInstance());

        register.implementation(INTERNAL, XsrfDefaults.class, XsrfDefaultsImpl.class);
        register.implementation(PROVIDED, XsrfTokenGenerator.class, SimpleXsrfTokenGenerator.class);
        register.implementation(PROVIDED, XsrfInvocationChecker.class, DefaultXsrfInvocationChecker.class);
        register.implementation(PROVIDED, InternalWebSudoManager.class, InternalWebSudoManagerImpl.class);
        register.implementation(PROVIDED, JiraServerIdProvider.class, BootstrapJiraServerIdProvider.class);

        register.implementation(INTERNAL, RegistryConfiguration.class, InstrumentationConfiguration.class);
        register.implementation(INTERNAL, OpTimerFactory.class, ThreadLocalOpTimerFactory.class);
        register.implementation(PROVIDED, InstrumentRegistry.class, DefaultInstrumentRegistry.class);
        register.implementation(INTERNAL, Instrumentation.class);

        register.implementation(ComponentContainer.Scope.INTERNAL, NoDatabaseTimeZoneResolver.class);
        register.implementation(PROVIDED, DateTimeFormatterFactory.class, DateTimeFormatterFactoryImpl.class);
        register.component(PROVIDED, new DateTimeFormatterComponentAdapter());

        register.implementation(PROVIDED, FeatureManager.class, BootstrapFeatureManager.class);
        register.implementation(INTERNAL, GzipCompression.class);
    }
}
