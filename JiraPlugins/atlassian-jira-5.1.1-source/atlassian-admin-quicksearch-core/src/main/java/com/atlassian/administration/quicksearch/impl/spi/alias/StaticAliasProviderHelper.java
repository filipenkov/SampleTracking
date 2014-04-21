package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * <p/>
 * Re-usable component for all alias providers that use application i18n resources to retrieve the aliases.
 * The value of the resource is supposed to contain comma-separated list of aliases.
 *
 * <p/>
 * This component maintains a cache of keys/locales vs. alias sets that gets reloaded as i18n resources are
 * added/removed from the system. The decision whether a given module/plugin event is requiring cache reload
 * may be customized by subclasses.
 *
 * <p/>
 * NOTE: for the cache to be refreshed upon appropriate plugin events instances of this class have to be registered
 * with plugin event manager.
 *
 * @since 1.0
 */
public class StaticAliasProviderHelper
{

    private static final String KEYWORDS_SEPARATOR = ",";

    public static final Set<String> EMPTY_KEYWORDS = Collections.emptySet();

    private static final String I18N = "i18n";

    private final ConcurrentMap<CacheKey, Set<String>> cache = new MapMaker().expiration(1, TimeUnit.DAYS).makeMap();

    public Set<String> aliasesFor(String key, UserContext context)
    {
        final CacheKey cacheKey = new CacheKey(key, context.getLocale());
        if (!cache.containsKey(cacheKey))
        {
            Set<String> aliases = loadAliases(cacheKey, context);
            cache.putIfAbsent(cacheKey, aliases);
        }
        return cache.get(cacheKey);
    }

    private Set<String> loadAliases(CacheKey cacheKey, UserContext context)
    {
        String keywords = context.getI18nResolver().getText(cacheKey.key);
        if (StringUtils.isEmpty(keywords) || keywords.equals(cacheKey.key))
        {
            return EMPTY_KEYWORDS;
        }
        return splitAliases(keywords);
    }

    private Set<String> splitAliases(final String keywords)
    {
        String[] split = keywords.split(KEYWORDS_SEPARATOR);
        return ImmutableSortedSet.copyOf(asList(split));
    }

    public void resetCache()
    {
        cache.clear();
    }



    @PluginEventListener
    public void onPluginModuleEnabled(PluginModuleEnabledEvent pluginModuleEnabledEvent)
    {
        onPluginModuleEvent(pluginModuleEnabledEvent.getModule());
    }

    @PluginEventListener
    public void onPluginModuleDisabled(PluginModuleDisabledEvent pluginModuleDisabledEvent)
    {
        onPluginModuleEvent(pluginModuleDisabledEvent.getModule());
    }

    private void onPluginModuleEvent(ModuleDescriptor<?> module)
    {
        if (isI18nAffecting(module))
        {
            resetCache();
        }
    }

    @PluginEventListener
    public void onPluginEnabled(PluginEnabledEvent pluginEnabledEvent)
    {
        onPluginEvent(pluginEnabledEvent.getPlugin());
    }

    @PluginEventListener
    public void onPluginDisabled(PluginDisabledEvent pluginDisabledEvent)
    {
        onPluginEvent(pluginDisabledEvent.getPlugin());
    }

    private void onPluginEvent(Plugin plugin)
    {
        if (isI18nAffecting(plugin))
        {
            resetCache();
        }
    }

    protected boolean isI18nAffecting(Plugin changedPlugin)
    {
        return Iterables.any(changedPlugin.getResourceDescriptors(), new Resources.TypeFilter(I18N));
    }

    protected boolean isI18nAffecting(ModuleDescriptor<?> moduleDescriptor)
    {
        return Iterables.any(moduleDescriptor.getResourceDescriptors(), new Resources.TypeFilter(I18N));
    }

    private static final class CacheKey
    {
        private final String key;
        private final Locale locale;
        private final int hashCode;

        public CacheKey(String key, Locale locale)
        {
            this.key = key;
            this.locale = locale;
            this.hashCode = 37 * key.hashCode() + locale.hashCode(); // worth caching
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey that = (CacheKey) o;
            return new EqualsBuilder().append(this.key, that.key).append(this.locale, that.locale).isEquals();
        }
    }
}
