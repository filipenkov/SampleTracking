package com.atlassian.jira.config;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.concat;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Default implementation of {@link com.atlassian.jira.config.FeatureManager}.
 *
 * @since v4.4
 */
public class DefaultFeatureManager implements FeatureManager
{
    public static final String FEATURE_RESOURCE_TYPE = "feature";
    public static final Resources.TypeFilter FEATURE_TYPE_FILTER = new Resources.TypeFilter(FEATURE_RESOURCE_TYPE);

    private static final String CORE_FEATURES_RESOURCE = "jira-features.properties";

    private static final Function<String,InputStream> APP_CLASS_LOADER = new Function<String, InputStream>()
    {
        @Override
        public InputStream apply(@Nullable String name)
        {
            return DefaultFeatureManager.class.getClassLoader().getResourceAsStream(name);
        }
    };

    private static Function<String,InputStream> pluginLoader(final Plugin plugin)
    {
        return new Function<String, InputStream>()
        {
            @Override
            public InputStream apply(@Nullable String name)
            {
                return plugin.getResourceAsStream(name);
            }
        };
    }

    private final FeaturesMapHolder features;

    @VisibleForTesting
    DefaultFeatureManager(PluginAccessor pluginAccessor, PropertiesContainer properties)
    {
        this.features = new FeaturesMapHolder(pluginAccessor, properties.properties);

    }

    public DefaultFeatureManager(PluginAccessor pluginAccessor, EventPublisher eventPublisher)
    {
        this.features = new FeaturesMapHolder(pluginAccessor);
        eventPublisher.register(features);
    }

    private static Properties loadCoreProperties()
    {
        return loadProperties(CORE_FEATURES_RESOURCE, APP_CLASS_LOADER);
    }

    private static Properties loadProperties(String path, Function<String,InputStream> loader)
    {
        final InputStream propsStream = checkNotNull(loader.apply(path), String.format("Resource %s not found", path));
        try
        {
            final Properties props = new Properties();
            props.load(propsStream);
            return props;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load properties from " + path, e);
        }
        finally
        {
            closeQuietly(propsStream);
        }
    }

    @Override
    public boolean isEnabled(String featureKey)
    {
        return Boolean.TRUE.equals(features.features().get(featureKey));
    }

    @Override
    public boolean isEnabled(CoreFeatures coreFeature)
    {
        return isEnabled(coreFeature.featureKey());
    }

    public static class FeaturesMapHolder
    {
        private final PluginAccessor pluginAccessor;
        private final ResettableLazyReference<Map<String,Boolean>> featuresReference;

        FeaturesMapHolder(PluginAccessor pluginAccessor)
        {
            this.pluginAccessor = pluginAccessor;
            this.featuresReference = new ResettableLazyReference<Map<String, Boolean>>()
            {
                @Override
                protected Map<String, Boolean> create() throws Exception
                {
                    return initFeatures(concat(ImmutableList.of(loadCoreProperties()), loadPluginFeatureProperties()));
                }
            };
        }

        FeaturesMapHolder(PluginAccessor pluginAccessor, final Iterable<Properties> properties)
        {
            this.pluginAccessor = pluginAccessor;
            this.featuresReference = new ResettableLazyReference<Map<String, Boolean>>()
            {
                @Override
                protected Map<String, Boolean> create() throws Exception
                {
                    return initFeatures(properties);
                }
            };
        }

        Map<String,Boolean> features()
        {
            return featuresReference.get();
        }

        private Map<String, Boolean> initFeatures(Iterable<Properties> properties)
        {
            final Map<String,Boolean> collector = Maps.newHashMap();
            for (Properties singleProperties : properties)
            {
                for (Map.Entry<Object,Object> entry : singleProperties.entrySet())
                {
                    collector.put(entry.getKey().toString(), parseBoolean(entry.getValue()));
                }
            }
            return ImmutableMap.copyOf(collector);
        }

        private Boolean parseBoolean(Object value)
        {
            // we expect 'true'
            return value instanceof String && Boolean.valueOf((String)value);
        }

        private List<Properties> loadPluginFeatureProperties()
        {
            List<Properties> features = Lists.newArrayList();
            for (Plugin plugin : pluginAccessor.getEnabledPlugins())
            {
                for (ResourceDescriptor featureDescriptor : getFeatureResources(plugin))
                {
                    features.add(loadProperties(featureDescriptor.getLocation(), pluginLoader(plugin)));
                }
            }
            return features;
        }

        private Collection<ResourceDescriptor> getFeatureResources(Plugin plugin)
        {
            return filter(plugin.getResourceDescriptors(), FEATURE_TYPE_FILTER);
        }

        @EventListener
        public void onPluginEnabled(PluginEnabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        @EventListener
        public void onPluginDisabled(PluginDisabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        private void onPluginEvent(Plugin plugin)
        {
            if (!getFeatureResources(plugin).isEmpty())
            {
                featuresReference.reset();
            }
        }
    }

    @VisibleForTesting
    static final class PropertiesContainer
    {
        final Iterable<Properties> properties;

        PropertiesContainer(Iterable<Properties> properties)
        {
            this.properties = properties;
        }
    }
}
