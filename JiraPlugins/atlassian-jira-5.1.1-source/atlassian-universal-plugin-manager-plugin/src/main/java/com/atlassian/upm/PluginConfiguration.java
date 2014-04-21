package com.atlassian.upm;

import java.util.Collection;

import javax.annotation.Nullable;

import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * Domain model representing the plugins in a configuration.
 * <p/>
 * {@code PluginConfiguration} objects should be created using the {@code PluginConfiguration.Builder} class.
 */
public final class PluginConfiguration
{
    @JsonProperty private final String key;
    @JsonProperty private final boolean enabled;
    @JsonProperty private final String name;
    @JsonProperty private final Collection<PluginModuleConfiguration> modules;

    @JsonCreator
    public PluginConfiguration(@JsonProperty("key") String key,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("name") String name,
        @JsonProperty("modules") Collection<PluginModuleConfiguration> modules)
    {
        this.key = key;
        this.enabled = enabled;
        this.name = name;
        this.modules = ImmutableList.copyOf(modules);
    }

    private PluginConfiguration(Builder builder)
    {
        this.key = builder.key;
        this.enabled = builder.enabled;
        this.name = builder.name;
        this.modules = ImmutableList.copyOf(builder.modules);
    }

    /**
     * The key for this plugin.
     *
     * @return The key for this plugin
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns whether or not the plugin is enabled
     *
     * @return {@code true} if the plugin is enabled, {@code false} otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Returns the name of this plugin if defined.
     *
     * @return the name of this plugin if defined, or null.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the list of {@code PluginModuleConfiguration} modules.
     *
     * @return the modules contained by this plugin
     */
    public Iterable<PluginModuleConfiguration> getModules()
    {
        return modules;
    }

    /**
     * A builder that facilitates construction of {@code PluginConfiguration} objects. The final
     * {@code PluginConfiguration} is created by calling the {@link PluginConfiguration.Builder#build()} method
     */
    public static final class Builder
    {
        private String key;
        private boolean enabled;
        private String name;
        private Collection<PluginModuleConfiguration> modules;

        /**
         * Constructor
         *
         * @param plugin the {@code Plugin} object containing information about the plugin
         * @param pluginAccessorAndController the {@code PluginAccessorAndController} to use
         */
        public Builder(Plugin plugin, PluginAccessorAndController pluginAccessorAndController)
        {
            checkNotNull(plugin, "plugin");
            checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");

            this.key = plugin.getKey();
            this.enabled = pluginAccessorAndController.isPluginEnabled(plugin.getKey());
            this.name = plugin.getName();
            this.modules = ImmutableList.copyOf(getModuleConfigurations(plugin, pluginAccessorAndController));
        }

        /**
         * Constructor
         *
         * @param key the Plugin key
         * @param enabled whether or not the plugin is enabled
         * @param name the name of the plugin
         * @param modules the modules contained by the plugin
         */
        public Builder(String key, boolean enabled, String name, Iterable<PluginModuleConfiguration> modules)
        {
            this.key = checkNotNull(key, "key");
            this.enabled = enabled;
            this.name = checkNotNull(name, "name");
            this.modules = ImmutableList.copyOf(modules);
        }

        /**
         * Sets whether the {@code PluginConfiguration} is enabled or not.
         *
         * @param enabled whether the {@code PluginConfiguration} is enabled or not
         * @return this builder to allow for further construction
         */
        public Builder enabled(boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the list of {@code PluginModuleConfiguration} modules.
         *
         * @param modules the list of {@code PluginModuleConfiguration} modules
         * @return this builder to allow for further construction
         */
        public Builder modules(Iterable<PluginModuleConfiguration> modules)
        {
            this.modules = ImmutableList.copyOf(modules);
            return this;
        }

        /**
         * Returns the final constructed {@code PluginConfiguration}.
         *
         * @return the {@code PluginConfiguration}
         */
        public PluginConfiguration build()
        {
            return new PluginConfiguration(this);
        }

        private Iterable<PluginModuleConfiguration> getModuleConfigurations(final Plugin plugin,
            final PluginAccessorAndController pluginAccessorAndController)
        {
            return transform(plugin.getModules(), new Function<Module, PluginModuleConfiguration>()
            {

                public PluginModuleConfiguration apply(@Nullable Module module)
                {
                    return new PluginModuleConfiguration.Builder(module, pluginAccessorAndController).build();
                }
            });
        }
    }
}
