package com.atlassian.upm;

import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;

/**
 * Contains statuses of specified plugins with respect to a particular product version update
 */
public final class ProductUpdatePluginCompatibility
{
    private final Iterable<Plugin> compatible;
    private final Iterable<Plugin> updateRequired;
    private final Iterable<Plugin> updateRequiredAfterProductUpdate;
    private final Iterable<Plugin> incompatible;
    private final Iterable<Plugin> unknown;

    private ProductUpdatePluginCompatibility(Builder builder)
    {
        compatible = builder.compatible.build();
        updateRequired = builder.updateRequired.build();
        updateRequiredAfterProductUpdate = builder.updateRequiredAfterProductUpdate.build();
        incompatible = builder.incompatible.build();
        unknown = builder.unknown.build();
    }

    /**
     * Get plugin info of the plugins that are compatible with the product update.
     *
     * @return the plugin info of the plugins that are compatible with the product update
     */
    public Iterable<Plugin> getCompatible()
    {
        return compatible;
    }

    /**
     * Get the plugin info of the plugins that must be updated to work with the product update.
     *
     * @return the plugin info of the plugins that must be updated to work with the product update
     */
    public Iterable<Plugin> getUpdateRequired()
    {
        return updateRequired;
    }

    /**
     * Get the plugin info of the plugins that must be updated to work with the product update, but the updated version is not compatible with the current product version.
     *
     * @return the plugin info of the plugins that must be updated to work with the product update, but the updated version is not compatible with the current product version
     */
    public Iterable<Plugin> getUpdateRequiredAfterProductUpdate()
    {
        return updateRequiredAfterProductUpdate;
    }

    /**
     * Get the plugin info of the plugins that are incompatible with the product update.
     *
     * @return the plugin info of the plugins that are incompatible with the product update
     */
    public Iterable<Plugin> getIncompatible()
    {
        return incompatible;
    }

    /**
     * Get the plugin info of the plugins that are unknown to the plugin data server.
     *
     * @return the plugin info of the plugins that are unknown to the plugin data server
     */
    public Iterable<Plugin> getUnknown()
    {
        return unknown;
    }

    public static class Builder
    {
        private ImmutableList.Builder<Plugin> compatible;
        private ImmutableList.Builder<Plugin> updateRequired;
        private ImmutableList.Builder<Plugin> updateRequiredAfterProductUpdate;
        private ImmutableList.Builder<Plugin> incompatible;
        private ImmutableList.Builder<Plugin> unknown;


        public Builder()
        {
            compatible = new ImmutableList.Builder<Plugin>();
            updateRequired = new ImmutableList.Builder<Plugin>();
            updateRequiredAfterProductUpdate = new ImmutableList.Builder<Plugin>();
            incompatible = new ImmutableList.Builder<Plugin>();
            unknown = new ImmutableList.Builder<Plugin>();
        }

        public Builder addCompatible(Plugin compatiblePlugin)
        {
            this.compatible.add(compatiblePlugin);
            return this;
        }

        public Builder addUpdateRequired(Plugin updateRequiredPlugin)
        {
            this.updateRequired.add(updateRequiredPlugin);
            return this;
        }

        public Builder addUpdateRequiredAfterProductUpdate(Plugin updateRequiredAfterProductUpdatePlugin)
        {
            this.updateRequiredAfterProductUpdate.add(updateRequiredAfterProductUpdatePlugin);
            return this;
        }

        public Builder addIncompatible(Plugin incompatiblePlugin)
        {
            this.incompatible.add(incompatiblePlugin);
            return this;
        }

        public Builder addUnknown(Plugin unknownPlugin)
        {
            this.unknown.add(unknownPlugin);
            return this;
        }

        public ProductUpdatePluginCompatibility build()
        {
            return new ProductUpdatePluginCompatibility(this);
        }
    }
}
