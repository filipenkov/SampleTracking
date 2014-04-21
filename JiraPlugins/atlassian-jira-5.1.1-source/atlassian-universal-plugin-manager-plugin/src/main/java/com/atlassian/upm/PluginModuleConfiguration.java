package com.atlassian.upm;

import com.atlassian.upm.spi.Plugin.Module;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Domain model representing the plugin modules in a configuration.
 * <p/>
 * {@code PluginModuleConfiguration} objects should be created using the {@code PluginModuleConfiguration.Builder} class.
 */
public final class PluginModuleConfiguration
{
    @JsonProperty private final String completeKey;
    @JsonProperty private final boolean enabled;
    @JsonProperty private final String name;

    @JsonCreator
    public PluginModuleConfiguration(@JsonProperty("completeKey") String completeKey,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("name") String name)
    {
        this.completeKey = completeKey;
        this.enabled = enabled;
        this.name = name;
    }

    private PluginModuleConfiguration(Builder builder)
    {
        this.completeKey = builder.completeKey;
        this.enabled = builder.enabled;
        this.name = builder.name;
    }

    /**
     * The complete key for this module, unique within the plugin.
     *
     * @return The complete key for this module
     */
    public String getCompleteKey()
    {
        return completeKey;
    }

    /**
     * Returns whether or not the plugin module is enabled.
     *
     * @return {@code true} if the plugin module is enabled, {@code false} otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Returns the plugin module's name if defined.
     *
     * @return the plugin module's name if defined, or null
     */
    public String getName()
    {
        return name;
    }

    /**
     * A builder that facilitates construction of {@code PluginModuleConfiguration} objects. The final
     * {@code PluginModuleConfiguration} is created by calling the
     * {@link PluginModuleConfiguration.Builder#build()} method
     */
    public static final class Builder
    {
        private String completeKey;
        private boolean enabled;
        private String name;

        /**
         * Constructor
         *
         * @param module the {@code Module} containing information about the plugin module
         * @param pluginAccessorAndController the {@code PluginAccessorAndController} to use
         */
        public Builder(Module module, PluginAccessorAndController pluginAccessorAndController)
        {
            checkNotNull(module, "moduleDescriptor");
            checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");

            this.completeKey = module.getCompleteKey();
            this.enabled = pluginAccessorAndController.isPluginModuleEnabled(module.getCompleteKey());
            this.name = module.getName();
        }

        /**
         * Constructor
         *
         * @param completeKey The complete key for this module
         * @param enabled whether or not the plugin module is enabled
         * @param name the plugin module's name
         */
        public Builder(String completeKey, boolean enabled, String name)
        {
            this.completeKey = checkNotNull(completeKey, "completeKey");
            this.enabled = enabled;
            this.name = name;
        }

        /**
         * Sets whether the {@code PluginModuleConfiguration} is enabled or not.
         *
         * @param enabled whether the {@code PluginModuleConfiguration} is enabled or not
         * @return this builder to allow for further construction
         */
        public Builder enabled(boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        /**
         * Returns the final constructed {@code PluginModuleConfiguration}.
         *
         * @return the {@code PluginModuleConfiguration}
         */
        public PluginModuleConfiguration build()
        {
            return new PluginModuleConfiguration(this);
        }
    }
}
