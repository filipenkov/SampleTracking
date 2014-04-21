package com.atlassian.jira.admin.quicknav;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Composite implementation of {@link SimpleLinkAliasProvider}. Retrieves keywords from delegate providers and caches
 * them by i18n context.
 *
 * @since v4.4
 */
public class CompositeFragmentAliasProvider implements SimpleLinkAliasProvider
{
    private final Iterable<SimpleLinkAliasProvider> providers = ImmutableList.of(
            new StaticSimpleLinksAliasProvider(),
            new ProjectKeyAliasProvider(),
            new SectionNameAliasProvider()
    );

    private final ResettableLazyKeyToAliasesCache resettableLazyKeyToAliasesCache = new ResettableLazyKeyToAliasesCache();

    private static abstract class AbstractKey
    {
        protected final Locale locale;

        protected AbstractKey(Locale locale)
        {
            this.locale = checkNotNull(locale);
        }
    }

    private static final class LinkKey extends AbstractKey
    {
        // hopefully this is an unique thing distinguishing given link!
        private final String url;

        public LinkKey(SimpleLink link, Locale locale)
        {
            super(locale);
            this.url = checkNotNull(checkNotNull(link, "link can't be null").getUrl());
        }


        @Override
        public int hashCode()
        {
            return (url.hashCode() * 37) + locale.hashCode();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            final LinkKey that = (LinkKey) o;
            return this.url.equals(that.url) && this.locale.equals(that.locale);
        }
    }

    /**
     * Resettable, lazy cache for link keys to results.
     */
    public static final class ResettableLazyKeyToAliasesCache extends ResettableLazyReference<ConcurrentMap<AbstractKey,Set<String>>>
    {
        // TODO unbounded?
        @Override
        protected ConcurrentMap<AbstractKey, Set<String>> create() throws Exception
        {
            return new ConcurrentHashMap<AbstractKey,Set<String>>();
        }

        /**
         * Clears the keyword cache when a PluginModuleDisabledEvent is fired.
         * @param event PluginModuleDisabledEvent
         */
        @EventListener
        public void clearCacheOnDisabled(final PluginModuleDisabledEvent event)
        {
            if (isCacheInvalidatingModule(event.getModule()))
            {
                reset();
            }
        }

        /**
         * Clears the keyword cache when a PluginModuleEnabledEvent is fired.
         * @param event PluginModuleEnabledEvent
         */
        @EventListener
        public void clearCacheOnEnabled(final PluginModuleEnabledEvent event)
        {
            if (isCacheInvalidatingModule(event.getModule()))
            {
                reset();
            }
        }

        private boolean isCacheInvalidatingModule(final ModuleDescriptor moduleDescriptor)
        {
            return (moduleDescriptor instanceof WebItemModuleDescriptor ||
                    moduleDescriptor instanceof WebSectionModuleDescriptor);
        }
    }

    public CompositeFragmentAliasProvider(final EventPublisher eventPublisher)
    {
        eventPublisher.register(resettableLazyKeyToAliasesCache);
    }

    @Override
    public Set<String> aliasesFor(SimpleLinkSection section, SimpleLink link, JiraAuthenticationContext ctx)
    {
        AbstractKey theKey = buildKeyForLink(link, ctx);
        return getForKey(section, link, ctx, theKey);
    }

    private Set<String> getForKey(SimpleLinkSection section, SimpleLink link, JiraAuthenticationContext ctx, AbstractKey key)
    {
        Set<String> answer = getFromCache(key);
        if (answer == null)
        {
            answer = getFromProviders(section, link, ctx);
            intoCache(key, answer);
        }
        return new TreeSet<String>(answer);
    }

    private LinkKey buildKeyForLink(SimpleLink link, JiraAuthenticationContext ctx)
    {
        return new LinkKey(link, ctx.getLocale());
    }

    private Set<String> getFromCache(AbstractKey key)
    {
        return resettableLazyKeyToAliasesCache.get().get(key);
    }

    private Set<String> getFromProviders(SimpleLinkSection section, SimpleLink link, JiraAuthenticationContext ctx)
    {
        ImmutableSortedSet.Builder<String> builder = ImmutableSortedSet.naturalOrder();
        for (SimpleLinkAliasProvider provider : providers)
        {
            builder.addAll(provider.aliasesFor(section, link, ctx));
        }
        return builder.build();
    }

    private void intoCache(AbstractKey key, Set<String> answer)
    {
        resettableLazyKeyToAliasesCache.get().putIfAbsent(key, answer);
    }
}
