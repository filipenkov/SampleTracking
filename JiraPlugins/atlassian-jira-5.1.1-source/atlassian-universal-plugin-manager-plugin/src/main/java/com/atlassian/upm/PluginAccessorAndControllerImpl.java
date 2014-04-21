package com.atlassian.upm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.plugin.IllegalPluginStateException;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.impl.Versions;
import com.atlassian.upm.pac.PacAuditClient;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

import static com.atlassian.upm.Sys.getOverriddenUserInstalledPluginKeys;
import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.api.util.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.ArrayUtils.contains;

public final class PluginAccessorAndControllerImpl implements PluginAccessorAndController, BundleContextAware
{
    public static final String PLUGIN_INFO_USES_LICENSING_PARAM = "atlassian-licensing-enabled";
    public static final String PLUGIN_INFO_ICON_PARAM = "plugin-icon";
    public static final String PLUGIN_INFO_LOGO_PARAM = "plugin-logo";
    public static final String PLUGIN_INFO_BANNER_PARAM = "plugin-banner";
    public static final String PLUGIN_INFO_VENDOR_ICON_PARAM = "vendor-icon";
    public static final String PLUGIN_INFO_VENDOR_LOGO_PARAM = "vendor-logo";

    private static final String[] ATLASSIAN_LICENSED_PLUGINS = {
        "com.atlassian.bonfire.plugin",
        "com.pyxis.greenhopper.jira",
        "com.atlassian.confluence.extra.sharepoint",
        "com.atlassian.confluence.extra.team-calendars"};
    private static final String LICENSE_STORAGE_PLUGIN_KEY = "com.atlassian.upm.plugin-license-storage-plugin";
    
    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;
    private final PluginController pluginController;
    private final AuditLogService auditLogger;
    private final ConfigurationStore configurationStore;
    private final TransactionTemplate txTemplate;
    private final PluginFactory pluginFactory;
    private final PluginMetadataManager pluginMetadataManager;
    private final HostLicenseProvider hostLicenseProvider;
    private final PacAuditClient pacAuditClient;
    private String upmPluginKey;
    private Version upmVersion;
    
    public PluginAccessorAndControllerImpl(ApplicationProperties applicationProperties,
        PluginAccessor pluginAccessor, PluginController pluginController,
        AuditLogService auditLogger, ConfigurationStore configurationStore,
        TransactionTemplate txTemplate, PluginFactory pluginFactory,
        PluginMetadataManager pluginMetadataManager,
        HostLicenseProvider hostLicenseProvider,
        PacAuditClient pacAuditClient)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.pluginMetadataManager = checkNotNull(pluginMetadataManager, "pluginMetadataManager");
        this.pluginAccessor = checkNotNull(pluginAccessor, "pluginAccessor");
        this.pluginController = checkNotNull(pluginController, "pluginController");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
        this.configurationStore = checkNotNull(configurationStore, "configurationStore");
        this.txTemplate = checkNotNull(txTemplate, "txTemplate");
        this.pluginFactory = checkNotNull(pluginFactory, "pluginFactory");
        this.hostLicenseProvider = checkNotNull(hostLicenseProvider, "hostLicenseProvider");
        this.pacAuditClient = checkNotNull(pacAuditClient, "pacAuditClient");
    }
    
    public void setBundleContext(BundleContext bundleContext)
    {
        Dictionary<?, ?> headers = bundleContext.getBundle().getHeaders();
        upmPluginKey = headers.get("Atlassian-Plugin-Key").toString();
        upmVersion = Versions.fromString(headers.get("Bundle-Version").toString());
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#installPlugin(com.atlassian.plugin.PluginArtifact)
     */
    public String installPlugin(final PluginArtifact pluginArtifact)
    {
        return txTemplate.execute(new TransactionCallback<String>()
        {
            public String doInTransaction()
            {
                if (isSafeMode())
                {
                    throw new SafeModeException("Install plugin is not allowed when system is in safe mode");
                }

                String artifactName = pluginArtifact.getName();
                if (!(artifactName.endsWith(".jar") || artifactName.endsWith(".xml")))
                {
                    throw new UnknownPluginTypeException("Cannot install plugin with unsupported file extension: " + artifactName);
                }

                String pluginKey;
                Plugin plugin;
                try
                {
                    pluginKey = getOnlyElement(pluginController.installPlugins(pluginArtifact));
                    plugin = getPlugin(pluginKey);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", pluginArtifact.getName());
                    throw re;
                }

                if (pluginKey == null || plugin == null)
                {
                    throw new PluginException("Plugin failed to install: " + artifactName);
                }

                auditLogger.logI18nMessage("upm.auditLog.install.plugin.success", plugin.getName(), pluginKey, plugin.getVersion());
                return pluginKey;
            }
        });
    }

    public Set<String> installPlugins(final Iterable<PluginArtifact> pluginArtifacts)
    {
        return txTemplate.execute(new TransactionCallback<Set<String>>()
        {
            public Set<String> doInTransaction()
            {
                if (isSafeMode())
                {
                    throw new SafeModeException("Install plugin is not allowed when system is in safe mode");
                }
                try
                {
                    Set<String> pluginKeys = pluginController.installPlugins(toArray(pluginArtifacts, PluginArtifact.class));
                    Collection<String> pluginNamesAndKeys = Collections2.transform(pluginKeys, new Function<String, String>()
                    {
                        public String apply(String pluginKey)
                        {
                            return getPlugin(pluginKey).getName() + " (" + pluginKey + ")";
                        }
                    });
                    auditLogger.logI18nMessage("upm.auditLog.install.plugins.success", Joiner.on(",").join(pluginNamesAndKeys));
                    return pluginKeys;
                }
                catch (RuntimeException re)
                {
                    // It would be better to figure out which plugins were successfully installed, but in the failure mode
                    // that's difficult to ascertain
                    Iterable<String> pluginArtifactNames = transform(pluginArtifacts, GetPluginArtifactName.INSTANCE);
                    auditLogger.logI18nMessage("upm.auditLog.install.plugins.failure", Joiner.on(",").join(pluginArtifactNames));
                    throw re;
                }
            }
        });

    }

    private enum GetPluginArtifactName implements Function<PluginArtifact, String>
    {
        INSTANCE;

        public String apply(PluginArtifact from)
        {
            return from.getName();
        }
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#uninstallPlugin(com.atlassian.plugin.Plugin)
     */
    public void uninstallPlugin(final Plugin plugin)
    {
        final String pluginKey = plugin.getKey();
        final String pluginName = plugin.getName();
        final String pluginVersion = plugin.getVersion();
        boolean uninstalled = txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                if (isSafeMode())
                {
                    throw new SafeModeException("Uninstall plugin is not allowed when system is in safe mode");
                }

                try
                {
                    pluginController.uninstall(plugin.getPlugin());
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.uninstall.plugin.failure", pluginName, pluginKey, pluginVersion);
                    throw re;
                }
                // check if plugin still exists
                Plugin result = decorate(pluginAccessor.getPlugin(pluginKey));
                if (result == null || PluginState.UNINSTALLED.equals(result.getPluginState()))
                {
                    auditLogger.logI18nMessage("upm.auditLog.uninstall.plugin.success", pluginName, pluginKey, pluginVersion);
                    return true;
                }
                else if (PluginRestartState.REMOVE.equals(pluginAccessor.getPluginRestartState(pluginKey)))
                {
                    auditLogger.logI18nMessage("upm.auditLog.uninstall.plugin.requires.restart", pluginName, pluginKey, pluginVersion);
                    return true;
                }
                else
                {
                    auditLogger.logI18nMessage("upm.auditLog.uninstall.plugin.failure", pluginName, pluginKey, pluginVersion);
                    return false;
                }
            }
        });
        if (uninstalled)
        {
            pacAuditClient.logPluginUninstalled(pluginKey, pluginVersion);
        }
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#getPlugin(java.lang.String)
     */
    public Plugin getPlugin(String pluginKey)
    {
        return decorate(pluginAccessor.getPlugin(pluginKey));
    }

    /**
     * Decorates a {@code com.atlassian.plugin.Plugin} into a {@code com.atlassian.upm.Plugin}
     *
     * @param plugin com.atlassian.plugin.Plugin to decorate
     * @return the decorated com.atlassian.upm.spi.Plugin
     */
    private Plugin decorate(com.atlassian.plugin.Plugin plugin)
    {
        if (plugin == null)
        {
            return null;
        }

        return pluginFactory.createPlugin(plugin);
    }

    /**
     * Decorates a {@code com.atlassian.plugin.ModuleDescriptor} into a {@code com.atlassian.upm.Plugin.Module}
     *
     * @param module ModuleDescriptor to decorate
     * @return the decorated com.atlassian.upm.spi.Plugin.Module
     */
    private Module decorate(ModuleDescriptor<?> module)
    {
        if (module == null)
        {
            return null;
        }

        return pluginFactory.createModule(module);
    }


    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#getPluginModule(java.lang.String, java.lang.String)
     */
    public Module getPluginModule(String pluginKey, String moduleKey)
    {
        Plugin plugin = decorate(pluginAccessor.getPlugin(pluginKey));
        if (plugin == null)
        {
            return null;
        }

        return plugin.getModule(moduleKey);
    }

    private Module getPluginModuleByCompleteKey(String completeKey)
    {
        return decorate(pluginAccessor.getPluginModule(completeKey));
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#getPlugins()
     */
    public Iterable<Plugin> getPlugins()
    {
        //do not hit PAC to check for updates as most plugin contexts do not need this information.
        return pluginFactory.createPlugins(pluginAccessor.getPlugins());
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#getPlugins(java.lang.Iterable)
     */
    public Iterable<Plugin> getPlugins(Iterable<PluginVersion> availablePluginUpdates)
    {
        return pluginFactory.createPlugins(pluginAccessor.getPlugins(), availablePluginUpdates);
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#isPluginEnabled(java.lang.String)
     */
    public boolean isPluginEnabled(String key)
    {
        return pluginAccessor.isPluginEnabled(key);
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#isPluginModuleEnabled(java.lang.String)
     */
    public boolean isPluginModuleEnabled(String completeKey)
    {
        return pluginAccessor.isPluginModuleEnabled(completeKey);
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#enablePlugins(java.lang.String)
     */
    public boolean enablePlugin(final String pluginKey)
    {
        return txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                String pluginName = getPlugin(pluginKey).getName();
                try
                {
                    pluginController.enablePlugins(pluginKey);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.failure", pluginName, pluginKey);
                    throw re;
                }
                boolean enabled = pluginAccessor.isPluginEnabled(pluginKey);
                if (enabled)
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.success", pluginName, pluginKey);
                }
                else
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.failure", pluginName, pluginKey);
                }
                return enabled;
            }
        });
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#disablePlugin(java.lang.String)
     */
    public boolean disablePlugin(final String pluginKey)
    {
        Plugin plugin = getPlugin(pluginKey);
        final String pluginName = plugin.getName();
        final String pluginVersion = plugin.getVersion();
        boolean disabled = txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                try
                {
                    pluginController.disablePlugin(pluginKey);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.failure", pluginName, pluginKey);
                    throw re;
                }
                boolean disabled = !pluginAccessor.isPluginEnabled(pluginKey);
                if (disabled)
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.success", pluginName, pluginKey);
                }
                else
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.failure", pluginName, pluginKey);
                }
                return disabled;
            }
        });
        if (disabled)
        {
            pacAuditClient.logPluginDisabled(pluginKey, pluginVersion);
        }
        return disabled;
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#enablePluginModule(java.lang.String)
     */
    public boolean enablePluginModule(final String completeKey)
    {
        return txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                String pluginModuleName = getModuleNameOrKey(getPluginModuleByCompleteKey(completeKey));
                try
                {
                    pluginController.enablePluginModule(completeKey);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.module.failure", pluginModuleName, completeKey);
                    throw re;
                }
                boolean enabled = pluginAccessor.isPluginModuleEnabled(completeKey);
                if (enabled)
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.module.success", pluginModuleName, completeKey);
                }
                else
                {
                    auditLogger.logI18nMessage("upm.auditLog.enable.plugin.module.failure", pluginModuleName, completeKey);
                }
                return enabled;
            }
        });
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#disablePluginModule(java.lang.String)
     */
    public boolean disablePluginModule(final String completeKey)
    {
        return txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                String pluginModuleName = getModuleNameOrKey(getPluginModuleByCompleteKey(completeKey));
                try
                {
                    pluginController.disablePluginModule(completeKey);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.module.failure", pluginModuleName, completeKey);
                    throw re;
                }
                boolean disabled = !pluginAccessor.isPluginModuleEnabled(completeKey);
                if (disabled)
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.module.success", pluginModuleName, completeKey);
                }
                else
                {
                    auditLogger.logI18nMessage("upm.auditLog.disable.plugin.module.failure", pluginModuleName, completeKey);
                }
                return disabled;
            }
        });
    }

    private String getModuleNameOrKey(Module module)
    {
        String name = module.getName();
        return (name != null) ? name : module.getKey();
    }
    
    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#isPluginInstalled(java.lang.String)
     */
    public boolean isPluginInstalled(String pluginKey)
    {
        return pluginAccessor.getPlugin(pluginKey) != null;
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#isSafeMode ()
     */
    public boolean isSafeMode()
    {
        return txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                try
                {
                    return configurationStore.getSavedConfiguration() != null;
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Error encountered while retrieving saved configuration", e);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#enterSafeMode()
     */
    public boolean enterSafeMode()
    {
        auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.start");
        return txTemplate.execute(new TransactionCallback<Boolean>()
        {
            public Boolean doInTransaction()
            {
                try
                {
                    configurationStore.saveConfiguration(getCurrentConfiguration());

                    // successful means that we are in safe mode (successfully saved the configuration) and have successfully
                    // disabled all user installed plugins
                    boolean successful = isSafeMode() && disableAllUserInstalledPlugins();

                    if (successful)
                    {
                        auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.success");
                    }
                    else
                    {
                        // if not successful, make sure to remove the saved configuration to tell system that we have failed
                        // to enter into safe mode.
                        configurationStore.removeSavedConfiguration();
                        auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.failure");
                    }

                    return successful;
                }
                catch (IOException ioe)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.failure");
                    throw new RuntimeException(ioe);
                }
                catch (IllegalPluginStateException ipse)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.failure");
                    throw new EnterSafeModePreconditionNotMetException(ipse);
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.enter.failure");
                    throw re;
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see com.atlassian.upm.PluginAccessorAndController#exitSafeMode(boolean)
     */
    public void exitSafeMode(final boolean keepState)
    {
        auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.start");
        txTemplate.execute(new TransactionCallback<Object>()
        {
            public Object doInTransaction()
            {
                try
                {
                    if (!keepState)
                    {
                        Configuration savedConfiguration = configurationStore.getSavedConfiguration();

                        if (savedConfiguration == null)
                        {
                            throw new MissingSavedConfigurationException();
                        }
                        setPluginSystemsConfiguration(savedConfiguration);
                    }
                    // if system was successfully restored or the state was kept, then we can remove the
                    // configuration to tell the system that we have successfully exited safe mode. If
                    // setPluginSystemsConfiguration failed, we don't remove the saved configuration, so we stay 
                    // in safe mode.
                    configurationStore.removeSavedConfiguration();
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.success");
                }
                catch (IOException ioe)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.failure");
                    throw new RuntimeException(ioe);
                }
                catch (MissingSavedConfigurationException msce)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.failure.missing.configuration");
                    throw msce;
                }
                catch (PluginStateUpdateException psue)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.failure.restoring.plugin.state", psue.getPlugin().getName(), psue.getPlugin().getKey());
                    throw psue;
                }
                catch (PluginModuleStateUpdateException pmsue)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.failure.restoring.plugin.module.state",
                        pmsue.getPluginModule().getName(), pmsue.getPlugin().getName(), pmsue.getPluginModule().getCompleteKey());
                    throw pmsue;
                }
                catch (RuntimeException re)
                {
                    auditLogger.logI18nMessage("upm.auditLog.safeMode.exit.failure");
                    throw re;
                }
                return null;
            }
        });
    }

    public Change getRestartRequiredChange(Plugin plugin)
    {
        checkNotNull(plugin, "plugin");
        PluginRestartState restartState = pluginAccessor.getPluginRestartState(plugin.getKey());
        if (restartState == PluginRestartState.NONE)
        {
            return null;
        }
        return new Change(plugin, restartState);
    }

    public Change getRestartRequiredChange(String pluginKey)
    {
        final Plugin plugin = getPlugin(pluginKey);
        // UPM-986 - we want to get a null Change when the plugin does not exist, not NPE
        if (plugin == null)
        {
            return null;
        }
        return getRestartRequiredChange(plugin);
    }

    public PluginRestartState getRestartState(Plugin plugin)
    {
        return pluginAccessor.getPluginRestartState(plugin.getKey());
    }

    public Iterable<Change> getRestartRequiredChanges()
    {
        Set<Change> restartChanges = new HashSet<Change>();
        for (Plugin plugin : getPlugins())
        {
            Change change = getRestartRequiredChange(plugin);
            if (change != null)
            {
                restartChanges.add(change);
            }
        }
        return restartChanges;
    }

    public boolean requiresRestart(Plugin plugin)
    {
        if (plugin == null)
        {
            return false;
        }
        return getRestartRequiredChange(plugin) != null;
    }

    public boolean hasChangesRequiringRestart()
    {
        for (Plugin plugin : getPlugins())
        {
            if (requiresRestart(plugin))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isUserInstalled(final Plugin plugin)
    {
        return (pluginMetadataManager.isUserInstalled(plugin.getPlugin()) ||
               Iterables.contains(getOverriddenUserInstalledPluginKeys(), plugin.getKey()))
            && !LICENSE_STORAGE_PLUGIN_KEY.equals(plugin.getKey());
    }

    public boolean isOptional(final Plugin plugin)
    {
        return pluginMetadataManager.isOptional(plugin.getPlugin());
    }

    public boolean isOptional(final Module module)
    {
        return pluginMetadataManager.isOptional(module.getModuleDescriptor());
    }

    /**
     * Gets the 'current' configuration which represents the current state of the plugins system.
     *
     * @return the {@code Configuration} object representing the 'current' configuration
     */
    private Configuration getCurrentConfiguration()
    {
        return (new Configuration.Builder(getCurrentPluginsConfigurationState())
            .title("Current Configuration"))
            .build();
    }

    /**
     * Transforms a collection of {@code Plugin} to a collection of {@code PluginConfiguration}
     *
     * @param plugins collection of {@code Plugin}
     * @return the collection of {@code PluginConfiguration} transformed from {@code plugins}
     */
    private Iterable<PluginConfiguration> transformPluginToPluginConfigurations(final Iterable<Plugin> plugins)
    {
        final PluginAccessorAndController accessor = this;

        return transform(plugins, new Function<Plugin, PluginConfiguration>()
        {
            public PluginConfiguration apply(@Nullable Plugin plugin)
            {
                return (new PluginConfiguration.Builder(plugin, accessor)).build();
            }
        });
    }

    /**
     * Disables all the user installed plugins in the system. Called when safe-mode is triggered. If any
     * of the plugin disablement failed, it rollbacks all the previous disablement, to restore the system
     * to its original state before the method was called.
     */
    private boolean disableAllUserInstalledPlugins()
    {
        Iterable<PluginConfiguration> previousPluginsState = getCurrentPluginsConfigurationState();

        for (Plugin plugin : getPlugins())
        {
            // check if plugin is a user-installed plugin and is enabled
            // make sure to not disable the UPM plugin
            if (!isUpmPlugin(plugin) && isUserInstalled(plugin) && PluginState.ENABLED.equals(plugin.getPluginState()))
            {
                // if plugin disable failed, revert to the original plugins state
                if (!disablePlugin(plugin.getKey()))
                {
                    revertToPluginsState(previousPluginsState);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets the plugin systems state similar to the provided configuration. If any of the plugin
     * disablement/enablement failed, it rolls back all the previous disablement/enablement, to restore the
     * system to its original state before the method was called.
     */
    private void setPluginSystemsConfiguration(Configuration configuration)
    {
        Iterable<PluginConfiguration> previousPluginsState = getCurrentPluginsConfigurationState();

        for (PluginConfiguration pluginConfiguration : configuration.getPlugins())
        {
            // If setting the plugin state fails, rollback all the changes we have made
            if (!setPluginState(pluginConfiguration))
            {
                revertToPluginsState(previousPluginsState);
                throw new PluginStateUpdateException(pluginConfiguration);
            }

            for (PluginModuleConfiguration pluginModuleConfiguration : pluginConfiguration.getModules())
            {
                if (!setPluginModuleState(pluginModuleConfiguration))
                {
                    revertToPluginsState(previousPluginsState);
                    throw new PluginModuleStateUpdateException(pluginConfiguration, pluginModuleConfiguration);
                }
            }
        }
    }

    public String getUpmPluginKey()
    {
        return upmPluginKey;
    }
    
    public Version getUpmVersion()
    {
        return upmVersion;
    }
    
    public boolean isUpmPlugin(Plugin plugin)
    {
        return getUpmPluginKey().equals(plugin.getKey());
    }

    public void revertRestartRequiredChange(String pluginKey)
    {
        try
        {
            pluginController.revertRestartRequiredChange(pluginKey);
            auditLogger.logI18nMessage("upm.auditLog.cancelChange.success", pluginKey);
        }
        catch (RuntimeException re)
        {
            auditLogger.logI18nMessage("upm.auditLog.cancelChange.failure", pluginKey);
            throw re;
        }
    }
    
    public boolean usesLicensing(Plugin plugin)
    {
        // This is true for plugins that are on our legacy support list OR have the
        // new-style "atlassian-licensing-enabled" property OR both.
        return isLegacyLicensePlugin(plugin.getKey()) || hasLicensingEnabledParam(plugin);
    }

    public boolean isLicenseReadOnly(Plugin plugin)
    {
        // This is true for plugins that are on our legacy support list AND DO NOT have
        // the new-style licensing property, i.e. they do not know about the UPM 2.0 API,
        // so we can read their license but we can't modify it.
        return isLegacyLicensePlugin(plugin.getKey()) && !hasLicensingEnabledParam(plugin);
    }

    public boolean isLegacyLicensePlugin(String pluginKey)
    {
        return contains(ATLASSIAN_LICENSED_PLUGINS, pluginKey);
    }
    
    public Option<URI> getLicenseAdminUri(Plugin plugin)
    {
        for (String uriPath : hostLicenseProvider.getPluginLicenseAdminUriPath(plugin.getKey()))
        {
            try
            {
                return some(URI.create(applicationProperties.getBaseUrl() + uriPath));
            }
            catch (Exception e)
            {
                return none();
            }
        }
        return none();
    }
    
    private boolean hasLicensingEnabledParam(Plugin plugin)
    {
        String licensingParam = plugin.getPluginInformation().getParameters().get(PLUGIN_INFO_USES_LICENSING_PARAM);
        return Boolean.valueOf(licensingParam).booleanValue();
    }

    @Override
    public Option<InputStream> getPluginIconInputStream(Plugin plugin)
    {
        return getInputStreamForResource(plugin, PLUGIN_INFO_ICON_PARAM);
    }

    @Override
    public Option<InputStream> getPluginLogoInputStream(Plugin plugin)
    {
        return getInputStreamForResource(plugin, PLUGIN_INFO_LOGO_PARAM);
    }

    @Override
    public Option<InputStream> getPluginBannerInputStream(Plugin plugin)
    {
        return getInputStreamForResource(plugin, PLUGIN_INFO_BANNER_PARAM);
    }

    @Override
    public Option<InputStream> getVendorIconInputStream(Plugin plugin)
    {
        return getInputStreamForResource(plugin, PLUGIN_INFO_VENDOR_ICON_PARAM);
    }

    @Override
    public Option<InputStream> getVendorLogoInputStream(Plugin plugin)
    {
        return getInputStreamForResource(plugin, PLUGIN_INFO_VENDOR_LOGO_PARAM);
    }

    private Option<InputStream> getInputStreamForResource(Plugin plugin, String resourceItem)
    {
        if (plugin != null && plugin.getPlugin() instanceof OsgiPlugin)
        {
            String locationString = plugin.getPluginInformation().getParameters().get(resourceItem);
            OsgiPlugin pluginOsgi = (OsgiPlugin) plugin.getPlugin();

            if(locationString != null && pluginOsgi != null)
            {
                InputStream is = pluginOsgi.getResourceAsStream(locationString);
                return option(is);
            }
        }

        return none();
    }
    
    private Iterable<PluginConfiguration> getCurrentPluginsConfigurationState()
    {
        return transformPluginToPluginConfigurations(getPlugins());
    }

    private void revertToPluginsState(Iterable<PluginConfiguration> plugins)
    {
        for (PluginConfiguration pluginConfiguration : plugins)
        {
            setPluginState(pluginConfiguration);
            for (PluginModuleConfiguration pluginModuleConfiguration : pluginConfiguration.getModules())
            {
                setPluginModuleState(pluginModuleConfiguration);
            }
        }
    }

    private boolean setPluginState(PluginConfiguration pluginConfiguration)
    {
        if (pluginConfiguration.isEnabled() != isPluginEnabled(pluginConfiguration.getKey()))
        {
            if (pluginConfiguration.isEnabled())
            {
                return enablePlugin(pluginConfiguration.getKey());
            }
            else
            {
                return disablePlugin(pluginConfiguration.getKey());
            }
        }
        return true;
    }

    private boolean setPluginModuleState(PluginModuleConfiguration pluginModule)
    {
        if (pluginModule.isEnabled() != isPluginModuleEnabled(pluginModule.getCompleteKey()))
        {
            if (pluginModule.isEnabled())
            {
                return enablePluginModule(pluginModule.getCompleteKey());
            }
            else
            {
                return disablePluginModule(pluginModule.getCompleteKey());
            }
        }
        return true;
    }

}
