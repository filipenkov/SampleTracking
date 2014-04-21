package com.atlassian.upm;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

public interface PluginAccessorAndController
{
    String installPlugin(PluginArtifact pluginArtifact);

    Set<String> installPlugins(Iterable<PluginArtifact> pluginArtifacts);

    /**
     * Uninstalls a plugin, and returns {@code true} if the plugin was successfully uninstalled, false otherwise.
     *
     * @param plugin the {@code Plugin} object to uninstall
     * @throws SafeModeException if trying to uninstall a plugin when system is in safe mode
     */
    void uninstallPlugin(Plugin plugin);

    Plugin getPlugin(String pluginKey);

    Module getPluginModule(String pluginKey, String moduleKey);

    Iterable<Plugin> getPlugins();

    /**
     * Get all installed plugins. Plugin representations will contain information about available plugin updates.
     *
     * This method takes an {@code Iterable<PluginVersion>} parameter to avoid a circular dependency between
     * {@link PluginAccessorAndController} and {@link com.atlassian.upm.pac.PacClient}.
     *
     * @see #getPlugins()
     * @param availablePluginUpdates available plugin updates
     * @return all installed plugins
     */
    Iterable<Plugin> getPlugins(Iterable<PluginVersion> availablePluginUpdates);

    boolean isPluginEnabled(String key);

    boolean isPluginModuleEnabled(String completeKey);

    /**
     * Enables a plugin, and returns {@code true} if the plugin was successfully enabled,
     * false otherwise.
     *
     * @param key the key of the {@code Plugin} to enable
     * @return {@code true} if the plugin was successfully enabled, false otherwise.
     */
    boolean enablePlugin(String key);

    /**
     * Disables a plugin, and returns {@code true} if the plugin was successfully disabled,
     * false otherwise.
     *
     * @param key the key of the {@code Plugin} to disable
     * @return {@code true} if the plugin was successfully disabled, false otherwise.
     */
    boolean disablePlugin(String key);

    /**
     * Enables a plugin module, and returns {@code true} if the plugin module was successfully enabled,
     * false otherwise.
     *
     * @param completeKey the complete key of the {@code ModuleDescriptor} to enable
     * @return {@code true} if the plugin module was successfully enabled, false otherwise.
     */
    boolean enablePluginModule(String completeKey);

    /**
     * Disables a plugin module, and returns {@code true} if the plugin module was successfully disabled,
     * false otherwise.
     *
     * @param completeKey the complete key of the {@code ModuleDescriptor} to disable
     * @return {@code true} if the plugin module was successfully disabled, false otherwise.
     */
    boolean disablePluginModule(String completeKey);

    /**
     * Returns {@code true} if a plugin with the given key is installed, {@code false} otherwise.
     *
     * @param pluginKey key of the plugin to check
     * @return {@code true} if a plugin with the given key is installed, {@code false} otherwise
     */
    boolean isPluginInstalled(String pluginKey);

    /**
     * Returns {@code true} if the system is currently in safe mode, {@code false} otherwise.
     *
     * @return {@code true} if the system is currently in safe mode, {@code false} otherwise.
     */
    boolean isSafeMode();

    /**
     * Triggers the "Safe Mode", and disables all the user installed plugins in the system. Returns {@code true}
     * if entering safe mode was successful, {@code false} otherwise.
     * <p/>
     * If any of the plugin disablement failed, it rollbacks all the previous disablement, to restore the system
     * to its original state before the method was called.
     *
     * @return {@code true} if entering safe mode was successful, {@code false} otherwise.
     * @throws ConfigurationStoreException when there is a previously saved {@code Configuration}, which means that
     * we are already in safe mode
     */
    boolean enterSafeMode();

    /**
     * Exits the "Safe Mode", and either restores the saved configuration or keep the current plugins system state.
     * Returns {@code true} if the system successfully exited safe mode, {@code false} otherwise.
     * <p/>
     * If any of the plugin disablement/enablement failed, it rollbacks all the previous disablement/enablement to
     * restore the system to its original state before the method was called.
     *
     * @param keepState A flag used to indicate if the current state of the plugins system will be kept, or if the
     * saved configuration will be restored when exiting from safe mode.
     * @throws MissingSavedConfigurationException thrown if {@code keepState} is {@code false} and there is no saved
     * configuration to restore
     * @throws PluginStateUpdateException thrown if {@code keepState} is {@code false} and a plugin could not be
     * reenabled
     * @throws PluginModuleStateUpdateException thrown if {@code keepState} is {@code false} and a plugin module could
     * not be reenabled
     */
    void exitSafeMode(boolean keepState);

    /**
     * Returns a {@code Change} object describing a change in the plugins system that requires restart
     *
     * @param plugin a plugin that requires restart, must not be null
     * @return A {@code Change} object describing a change in the plugins system that requires restart, or {@code null} if
     *         the plugin does not have a requires restart change
     */
    Change getRestartRequiredChange(Plugin plugin);

    /**
     * Returns a {@code Change} object describing a change in the plugins system that requires restart
     *
     * @param pluginKey a key for a plugin that requires restart
     * @return A {@code Change} object describing a change in the plugins system that requires restart, or {@code null} if
     *         the plugin does not have a requires restart change
     */
    Change getRestartRequiredChange(String pluginKey);

    /**
     * Get the restart state for the plugin
     *
     * @param plugin the plugin for the restart state
     * @return the restart state for the plugin
     */
    PluginRestartState getRestartState(Plugin plugin);

    /**
     * Returns all of the changes in the system that require a product restart
     *
     * @return All of the changes in the system that require a product restart
     */
    Iterable<Change> getRestartRequiredChanges();

    /**
     * Returns {@code true} if the plugin requires a product restart, {@code false} otherwise.
     *
     * @param plugin the plugin to check
     * @return {@code true} if the plugin requires a product restart, {@code false} otherwise.
     */
    boolean requiresRestart(Plugin plugin);

    /**
     * Returns {@code true} if there are changes in the plugins system that requires restart, {@code false} otherwise
     *
     * @return {@code true} if there are changes in the plugins system that requires restart, {@code false} otherwise
     */
    boolean hasChangesRequiringRestart();

    /**
     * Returns the plugin key of the UPM plugin.
     * 
     * @return The plugin key of the UPM plugin
     */
    String getUpmPluginKey();
    
    /**
     * Returns the current UPM version.
     * 
     * @return The current UPM version
     */
    Version getUpmVersion();
    
    /**
     * Returns {@code true} if the plugin is the UPM plugin, {@code} false otherwise
     *
     * @param plugin the plugin to check
     * @return {@code true} if the plugin is the UPM plugin, {@code} false otherwise
     */
    boolean isUpmPlugin(Plugin plugin);

    /**
     * Removes the change requiring restart for the plugin with the specified key.
     *
     * @param pluginKey key of the plugin whose change should be removed
     */
    void revertRestartRequiredChange(String pluginKey);

    /**
     * @param plugin the plugin to check
     * @return {@code true} if the plugin is a user installed plugin {@code false} otherwise.
     */
    boolean isUserInstalled(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return {@code true} if the plugin is deemed not required for the system to run correctly, {@code false} otherwise.
     */
    boolean isOptional(Plugin plugin);

    /**
     * @param module the module to check
     * @return {@code true} if the module is deemed not required for the system to run correctly, {@code false} otherwise.
     */
    boolean isOptional(Module module);
    
    /**
     * Returns true if the plugin uses licensing, false otherwise. Does NOT execute a check to verify
     * whether or not the current user has permissions to manage this plugin's license.
     * 
     * @param plugin the plugin to check
     * @return {@code true} if the plugin can make use of a license
     */
    boolean usesLicensing(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return {@code true} if the plugin's licensing mechanism <i>cannot</i> be updated by UPM
     */
    boolean isLicenseReadOnly(Plugin plugin);

    /**
     * Returns true for plugins with Atlassian legacy licensing, false if not
     * @param pluginKey the plugin key
     * @return true for plugins with Atlassian legacy licensing, false if not
     */
    boolean isLegacyLicensePlugin(String pluginKey);

    /**
     * @param plugin the plugin to check
     * @return the URI of the license admin page for this plugin, if it is not accessible through the
     * standard UPM license UI
     */
    Option<URI> getLicenseAdminUri(Plugin plugin);
    
    /**
     * @param plugin the plugin to check
     * @return a option with an InputSteam for the plugin icon
     */
    Option<InputStream> getPluginIconInputStream(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return a option with an InputSteam for the plugin logo
     */
    Option<InputStream> getPluginLogoInputStream(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return a option with an InputSteam for the plugin banner
     */
    Option<InputStream> getPluginBannerInputStream(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return a option with an InputSteam for the vendor icon
     */
    Option<InputStream> getVendorIconInputStream(Plugin plugin);

    /**
     * @param plugin the plugin to check
     * @return a option with an InputSteam for the vendor logo
     */
    Option<InputStream> getVendorLogoInputStream(Plugin plugin);
}
