package com.atlassian.upm.spi;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.license.entity.PluginLicense;

public interface Plugin
{
    /**
     * Gets the underlying plugin
     *
     * @return the underlying plugin
     */
    com.atlassian.plugin.Plugin getPlugin();

    /**
     * @see com.atlassian.plugin.Plugin#getName()
     */
    String getName();

    /**
     * @see com.atlassian.plugin.Plugin#getKey()
     */
    String getKey();

    /**
     * @see com.atlassian.plugin.Plugin#getModuleDescriptors()
     */
    Iterable<Module> getModules();

    /**
     * @see com.atlassian.plugin.Plugin#getModuleDescriptor(String)
     */
    Module getModule(String key);

    /**
     * @see com.atlassian.plugin.Plugin#isEnabledByDefault()
     */
    boolean isEnabledByDefault();

    /**
     * @see com.atlassian.plugin.Plugin#getPluginInformation()
     */
    PluginInformation getPluginInformation();

    /**
     * @see com.atlassian.plugin.Plugin#getPluginState()
     */
    PluginState getPluginState();

    /**
     * Returns true if the plugin is static, false if not
     *
     * @return true if the plugin is static, false if not
     */
    boolean isStaticPlugin();

    /**
     * @see com.atlassian.plugin.Plugin#isBundledPlugin()
     */
    boolean isBundledPlugin();

    /**
     * @see com.atlassian.plugin.Plugin#isUninstallable()
     */
    boolean isUninstallable();

    /**
     * @see com.atlassian.plugin.PluginInformation.getVersion()
     */
    String getVersion();

    /**
     * Returns true if the plugin has an available update, false if not, or none() if UPM did not check for updates.
     * Not all plugin contexts require the knowledge of whether or not a plugin update is available, therefore we
     * only set this value if we are in such a context (e.g. viewing the Manage Existing tab).
     * 
     * @return true if the plugin has an available update, false if not, or none() if UPM did not check for updates.
     */
    Option<Boolean> isUpdateAvailable();

    /**
     * Returns the {@link PluginLicense} for this plugin if one exists or {@code none()} otherwise.
     *
     * @return the {@link PluginLicense} for this plugin if one exists or {@code none()} otherwise.
     */
    Option<PluginLicense> getLicense();

    /**
     * Returns true if one or more of this plugin's module desciptors is of an unrecognized module type, false if not.
     *
     * @return true if one or more of this plugin's module desciptors is of an unrecognized module type, false if not.
     * @see com.atlassian.upm.spi.Plugin.Module#hasRecognisableType()
     */
    public boolean hasUnrecognisedModuleTypes();

    public interface Module
    {
        /**
         * @return the module descriptor this module wraps.
         */
        ModuleDescriptor<?> getModuleDescriptor();

        /**
         * @see ModuleDescriptor#getCompleteKey()
         */
        String getCompleteKey();

        /**
         * @see ModuleDescriptor#getPluginKey()
         */
        String getPluginKey();

        /**
         * @see ModuleDescriptor#getKey()
         */
        String getKey();

        /**
         * @see ModuleDescriptor#getName()
         */
        String getName();

        /**
         * @see ModuleDescriptor#getDescription()
         */
        String getDescription();

        /**
         * @see ModuleDescriptor#getPlugin()
         */
        Plugin getPlugin();

        /**
         * @return true if the plugins system will not allow this module to be disabled, false otherwise.
         */
        boolean canNotBeDisabled();

        /**
         * Returns true if the module descriptor type is recognizable and supported, false if not.
         *
         * @return true if the module descriptor type is recognizable and supported, false if not.
         */
        public boolean hasRecognisableType();
    }
}
