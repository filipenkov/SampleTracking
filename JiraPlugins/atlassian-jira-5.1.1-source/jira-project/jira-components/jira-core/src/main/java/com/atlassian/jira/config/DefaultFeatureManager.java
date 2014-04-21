package com.atlassian.jira.config;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    /**
     * Logger for this DefaultFeatureManager instance.
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultFeatureManager.class);

    private static final String USER_DARKFEATURE_PREFERENCE_KEY = "user.features.enabled";
    private static final boolean DARK_FEATURES_DISABLED_SYSTEM_WIDE = Boolean.getBoolean("atlassian.darkfeature.disabled");

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
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;
    private final EventPublisher eventPublisher;

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
    DefaultFeatureManager(PropertiesContainer properties, EventPublisher eventPublisher, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            UserPreferencesManager userPreferencesManager)
    {
        this.eventPublisher = eventPublisher;
        this.features = new FeaturesMapHolder(properties.properties);
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    public DefaultFeatureManager(PluginAccessor pluginAccessor, EventPublisher eventPublisher,
            PermissionManager permissionManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            UserPreferencesManager userPreferencesManager)
    {
        this.eventPublisher = eventPublisher;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;

        this.features = new FeaturesMapHolder(pluginAccessor);
        eventPublisher.register(features);
    }

    private static Properties loadCoreProperties()
    {
        Properties properties = loadProperties(CORE_FEATURES_RESOURCE, APP_CLASS_LOADER);

        // add the system properties
        for (String key : System.getProperties().stringPropertyNames())
        {
            if (key.startsWith(FeatureManager.SYSTEM_PROPERTY_PREFIX))
            {
                String featureKey = key.substring(FeatureManager.SYSTEM_PROPERTY_PREFIX.length(), key.length());
                String featureValue = System.getProperty(key);

                log.trace("Feature '{}' is set to {} by system properties", featureKey, featureValue);
                properties.setProperty(featureKey, featureValue);
            }
        }

        return properties;
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
        boolean enabled = Boolean.TRUE.equals(features.features().get(featureKey));
        if (!enabled) {
            // Might be enabled via Dark Features
            enabled = getDarkFeatures().isFeatureEnabled(featureKey);
        }
        return enabled;
    }

    @Override
    public boolean isEnabled(CoreFeatures coreFeature)
    {
        return isEnabled(coreFeature.featureKey());
    }

    @Override
    public Set<String> getEnabledFeatureKeys()
    {
        return features.enabledFeatures();
    }

    public DarkFeatures getDarkFeatures()
    {
        if (DARK_FEATURES_DISABLED_SYSTEM_WIDE)
        {
            return new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet());
        }

        User user = authenticationContext.getLoggedInUser();
        return new DarkFeatures(getEnabledFeatureKeys(), getSiteEnabledFeatures(), getUserEnabledFeatures(user));
    }

    @Override
    public void enableUserDarkFeature(User user, String feature)
    {
        changeUserDarkFeature(user, feature, true);
    }

    @Override
    public void disableUserDarkFeature(User user, String feature)
    {
        changeUserDarkFeature(user, feature, false);
    }

    @Override
    public void enableSiteDarkFeature(String feature)
    {
        changeSiteDarkFeature(feature, true);
    }

    @Override
    public void disableSiteDarkFeature(String feature)
    {
        changeSiteDarkFeature(feature, false);
    }

    private void changeUserDarkFeature(User user, String feature, boolean enable)
    {
        try
        {
            // Check 'dark feature' key against CoreFeatures - users should not attempt to
            // enable or disable them.
            CoreFeatures coreFeature = CoreFeatures.valueOf(feature);
            if (coreFeature != null && !coreFeature.isDevFeature())
                throw new IllegalStateException("User cannot set feature '" + feature + "' at runtime. It must be set by an admin via properties.");
        }
        catch (IllegalArgumentException e)
        {
            // This is fine - just means that the feature is not in the enum list of core features.
        }

        Set<String> enabledFeatures = Sets.newHashSet(getUserEnabledFeatures(user));
        if (enable == enabledFeatures.contains(feature))
        {
            // No change to make - feature is already enabled or disabled.
            return;
        }

        if (enable)
            enabledFeatures.add(feature);
        else
            enabledFeatures.remove(feature);

        try
        {
            Preferences prefs = userPreferencesManager.getPreferences(user);
            prefs.setString(USER_DARKFEATURE_PREFERENCE_KEY, serialize(enabledFeatures));
        }
        catch (AtlassianCoreException e)
        {
            throw new IllegalStateException(e);
        }

        eventPublisher.publish(enable ? new FeatureEnabledEvent(feature, user) : new FeatureDisabledEvent(feature, user));
    }

    private void changeSiteDarkFeature(String feature, boolean enable)
    {
        User loggedInUser = authenticationContext.getLoggedInUser();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
            throw new IllegalStateException("User " + loggedInUser + " does not have permission to change site dark features");

        Set<String> enabledFeatures = getSiteEnabledFeatures();
        if (enable == enabledFeatures.contains(feature))
        {
            // No change to make - feature is already enabled or disabled.
            return;
        }

        if (enable)
            enabledFeatures.add(feature);
        else
            enabledFeatures.remove(feature);

        applicationProperties.setString(APKeys.JIRA_OPTION_ENABLED_DARK_FEATURES, serialize(enabledFeatures));
        eventPublisher.publish(enable ? new FeatureEnabledEvent(feature) : new FeatureDisabledEvent(feature));
    }

    private static String serialize(Set<String> features)
    {
        return StringUtils.join(features, ",");
    }

    private static Set<String> deserialize(String features)
    {
        if (StringUtils.isBlank(features))
            return Sets.newHashSet();

        String[] featureKeys = features.split(",");

        return Sets.newHashSet(featureKeys);
    }

    private Set<String> getUserEnabledFeatures(User user)
    {
        Preferences preferences = userPreferencesManager.getPreferences(user);
        String features = preferences.getString(USER_DARKFEATURE_PREFERENCE_KEY);
        return deserialize(features);
    }

    private Set<String> getSiteEnabledFeatures()
    {
        String features = applicationProperties.getString(APKeys.JIRA_OPTION_ENABLED_DARK_FEATURES);
        return deserialize(features);
    }

    private static List<Properties> loadPluginFeatureProperties(PluginAccessor pluginAccessor)
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

    private static Collection<ResourceDescriptor> getFeatureResources(Plugin plugin)
    {
        return filter(plugin.getResourceDescriptors(), FEATURE_TYPE_FILTER);
    }

    public static class FeaturesMapHolder
    {
        private final ResettableLazyReference<ImmutableMap<String,Boolean>> features;
        private final ResettableLazyReference<ImmutableSet<String>> enabledFeatures = new EnabledFeaturesRef();

        FeaturesMapHolder(final PluginAccessor pluginAccessor)
        {
            this.features = new ResettableLazyReference<ImmutableMap<String, Boolean>>()
            {
                @Override
                protected ImmutableMap<String, Boolean> create() throws Exception
                {
                    return initFeatures(concat(ImmutableList.of(loadCoreProperties()), loadPluginFeatureProperties(pluginAccessor)));
                }
            };
        }

        FeaturesMapHolder(final Iterable<Properties> properties)
        {
            this.features = new ResettableLazyReference<ImmutableMap<String, Boolean>>()
            {
                @Override
                protected ImmutableMap<String, Boolean> create() throws Exception
                {
                    return initFeatures(properties);
                }
            };
        }

        ImmutableMap<String,Boolean> features()
        {
            return features.get();
        }

        ImmutableSet<String> enabledFeatures()
        {
            return enabledFeatures.get();
        }

        private ImmutableMap<String, Boolean> initFeatures(Iterable<Properties> properties)
        {
            HashMap<String, Boolean> collector = Maps.newHashMap();
            for (Properties singleProperties : properties)
            {
                for (String property : singleProperties.stringPropertyNames())
                {
                    collector.put(property, Boolean.valueOf(singleProperties.getProperty(property)));
                }
            }

            return ImmutableMap.copyOf(collector);
        }

        private ImmutableSet<String> initEnabledFeatures(ImmutableMap<String, Boolean> allFeatures)
        {
            ImmutableSet.Builder<String> collector = ImmutableSet.builder();
            for (Map.Entry<String, Boolean> featureToggle : allFeatures.entrySet())
            {
                if (featureToggle.getValue())
                {
                    collector.add(featureToggle.getKey());
                }
            }

            return collector.build();
        }

        @EventListener
        @SuppressWarnings ({ "UnusedDeclaration" })
        public void onPluginEnabled(PluginEnabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        @EventListener
        @SuppressWarnings ({ "UnusedDeclaration" })
        public void onPluginDisabled(PluginDisabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        private void onPluginEvent(Plugin plugin)
        {
            if (!getFeatureResources(plugin).isEmpty())
            {
                features.reset();
                enabledFeatures.reset();
            }
        }

        private class EnabledFeaturesRef extends ResettableLazyReference<ImmutableSet<String>>
        {
            @Override
            protected ImmutableSet<String> create() throws Exception
            {
                return initEnabledFeatures(features.get());
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
