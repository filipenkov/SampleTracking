package com.atlassian.jira.web.bean;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.language.TranslationTransformModuleDescriptor;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.Collections.sort;

/**
 * In an attempt to make use of our I18n-cache this was turned into a flyweight that delegates to the properly
 * BackingI18n that lives in the caching factory.
 * <p/>
 * Unit tests should never use this. Use the MockI18nHelper or BackingI18n (if you must).
 * <p/>
 * You should not be dealing with this Concrete Class - see {@link I18nHelper} instead.
 *
 * @since v4.3 this become a flyweight that wraps the cached version of the Real Thing.
 *
 * @see I18nHelper
 * @see com.atlassian.jira.security.JiraAuthenticationContext#getI18nHelper()
 * @see I18nHelper.BeanFactory
 */
public class I18nBean implements I18nHelper
{
    /**
     * @param user the user
     * @return the user's specified {@link Locale}
     * @since 4.1
     */
    public static Locale getLocaleFromUser(final User user)
    {
        if (user != null)
        {
            PropertySet propertySet = getUserPropertyManager().getPropertySet(user);
            if (propertySet == null)
            {
                return getDefaultLocale();
            }
            final String localeStr = propertySet.getString(PreferenceKeys.USER_LOCALE);

            if (!StringUtils.isBlank(localeStr))
            {
                return LocaleParser.parseLocale(localeStr);
            }
        }
        return getDefaultLocale();
    }

    private static Locale getDefaultLocale()
    {
        return ComponentAccessor.getApplicationProperties().getDefaultLocale();
    }

    private static UserPropertyManager getUserPropertyManager()
    {
        return ComponentAccessor.getUserPropertyManager();
    }

    private final I18nHelper delegate;

    public I18nBean()
    {
        delegate = getFactory().getInstance(ComponentAccessor.getApplicationProperties().getDefaultLocale());
    }

    public I18nBean(final Locale locale)
    {
        delegate = getFactory().getInstance(locale);
    }

    public I18nBean(final User user)
    {
        this(I18nBean.getLocaleFromUser(user));
    }

    public I18nBean(final I18nHelper delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Construct an I18nBean in the given Locale.
     *
     * @param localeString The locale String. eg "fr_CA"
     * @deprecated use {@link #I18nBean(java.util.Locale)} instead
     */
    @Deprecated
    public I18nBean(final String localeString)
    {
        this(LocaleParser.parseLocale(localeString));
    }

    protected BeanFactory getFactory()
    {
        return ComponentAccessor.getI18nHelperFactory();
    }

    /**
     * Small delegate around the CachingI18nHelperFactory that is registered in
     * pico as the actual I18nHelper.BeanFactory
     */
    public static class AccessorFactory implements I18nHelper.BeanFactory {
        private final I18nHelper.BeanFactory delegate;

        public AccessorFactory(CachingFactory delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public I18nHelper getInstance(Locale locale) {
            return delegate.getInstance(locale);
        }

        @Override
        public I18nHelper getInstance(User user) {
            return delegate.getInstance(user);
        }
    }
    /**
     * As the name implies: a factory that caches I18nBeans. With the advent of Plugins-2 we need to iterate through all
     * enabled plugins and get their i18n resources when an I18nBean is constructed.
     * <p/>
     * This factory was primarily created to make SAL's I18nResolver avoid having to do that iteration for every single
     * string that needs to be internationalized. It can, obviously, be used in other contexts as well.
     */
    static public class CachingFactory implements I18nHelper.BeanFactory, Startable
    {
        private static final String I18N_RESOURCE_TYPE = "i18n";
        private final Cache<Locale, I18nHelper> cache;
        private final JiraLocaleUtils jiraLocaleUtils;
        private final I18nTranslationMode i18nTranslationMode;
        private final InvolvedPluginsTracker involvedPluginsTracker;
        private final EventPublisher eventPublisher;
        private final ResettableLazyReference<List<TranslationTransform>> translationTransformsRef = new ResettableLazyReference<List<TranslationTransform>>()
        {
            @Override
            protected List<TranslationTransform> create() throws Exception
            {
                return loadTranslationTransforms();
            }
        };

        private static final String CACHE_NAME = "I18nBeans";

        public CachingFactory(
                final CacheFactory factory,
                final JiraLocaleUtils jiraLocaleUtils,
                final I18nTranslationMode i18nTranslationMode,
                final EventPublisher eventPublisher
        )
        {
            this.jiraLocaleUtils = jiraLocaleUtils;
            this.i18nTranslationMode = i18nTranslationMode;
            this.involvedPluginsTracker = new InvolvedPluginsTracker();
            this.eventPublisher = eventPublisher;
            cache = factory.getCache(CACHE_NAME, Locale.class, I18nHelper.class);
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        @EventListener
        public void pluginModuleDisabled(final PluginModuleDisabledEvent event)
        {
            if (involvedPluginsTracker.isPluginInvolved(event.getModule()))
            {
                clearCaches();
            }
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        @EventListener
        public void pluginModuleEnabled(final PluginModuleEnabledEvent event)
        {
            if (involvedPluginsTracker.isPluginWithModuleDescriptor(event.getModule(), LanguageModuleDescriptor.class) ||
                    involvedPluginsTracker.isPluginWithResourceType(event.getModule(), I18N_RESOURCE_TYPE) ||
                    involvedPluginsTracker.isPluginWithModuleDescriptor(event.getModule(), TranslationTransformModuleDescriptor.class))
            {
                clearCaches();
            }
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        @EventListener
        public void pluginRefreshed(final PluginRefreshedEvent event)
        {
            if (involvedPluginsTracker.isPluginInvolved(event.getPlugin()))
            {
                clearCaches();
            }
        }

        private void clearCaches()
        {
            involvedPluginsTracker.clear();             // <-- copy on write set under the covers
            cache.removeAll();                          // <-- concurrent hashmap under the covers
            jiraLocaleUtils.resetInstalledLocales();    // <-- synchronised method
            translationTransformsRef.reset();
        }

        public I18nHelper getInstance(final Locale locale)
        {
            I18nHelper i18nHelper;
            if (DefaultResourceBundle.isDefaultResourceBundleStale(locale))
            {
                clearCaches();
            }
            else
            {
                i18nHelper = cache.get(locale);
                if (i18nHelper != null)
                {
                    return i18nHelper;
                }
            }
            i18nHelper = new BackingI18n(locale, i18nTranslationMode, involvedPluginsTracker, translationTransformsRef.get());
            cache.put(locale, i18nHelper);
            return i18nHelper;
        }

        public I18nHelper getInstance(final User user)
        {
            return getInstance(getLocaleFromUser(user));
        }
        /**
         * An opaque string that changes whenever the underlying i18n bundles change
         * (e.g. when a new translation pack in installed)
         */
        public String getStateHashCode()
        {
            return Integer.toString(involvedPluginsTracker.hashCode(), Character.MAX_RADIX);
        }

        // Note: We can not use @EventComponent to take care of this for us because we
        // need to clear the cache after we've registered to make sure we haven't missed
        // anything.
        public void start()
        {
            eventPublisher.register(this);
            clearCaches();
        }

        private PluginAccessor getPluginAccessor()
        {
            return ComponentAccessor.getPluginAccessor();
        }

        private List<TranslationTransform> loadTranslationTransforms()
        {
            PluginAccessor pluginAccessor = getPluginAccessor();

            final List<TranslationTransform> translationTransforms = new ArrayList<TranslationTransform>();
            final List<TranslationTransformModuleDescriptor> descriptors = new ArrayList<TranslationTransformModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(TranslationTransformModuleDescriptor.class));
            sort(descriptors, ModuleDescriptorComparator.COMPARATOR);

            for (TranslationTransformModuleDescriptor descriptor : descriptors)
            {
                final TranslationTransform translationTransform = descriptor.getModule();
                involvedPluginsTracker.trackInvolvedPlugin(descriptor);
                translationTransforms.add(translationTransform);
            }
            return translationTransforms;
        }
    }

    /////////////////////////////////////
    // A bunch of IDE-generated delegates
    /////////////////////////////////////

    public Locale getLocale()
    {
        return delegate.getLocale();
    }

    public ResourceBundle getDefaultResourceBundle()
    {
        return delegate.getDefaultResourceBundle();
    }

    public Set<String> getKeysForPrefix(String prefix)
    {
        return delegate.getKeysForPrefix(prefix);
    }

    public String getUnescapedText(String key)
    {
        return delegate.getUnescapedText(key);
    }

    @Override
    public String getUntransformedRawText(String key)
    {
        return delegate.getUntransformedRawText(key);
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return delegate.isKeyDefined(key);
    }

    public String getText(String key)
    {
        return delegate.getText(key);
    }

    public String getText(String key, String value1)
    {
        return delegate.getText(key, value1);
    }

    public String getText(String key, Object parameters)
    {
        return delegate.getText(key, parameters);
    }

    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return delegate.getText(key, value1, value2, value3);
    }

    public String getText(String key, String value1, String value2)
    {
        return delegate.getText(key, value1, value2);
    }

    public String getText(String key, String value1, String value2, String value3)
    {
        return delegate.getText(key, value1, value2, value3);
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return delegate.getText(key, value1, value2, value3, value4);
    }

    public String getText(String key, String value1, String value2, String value3, String value4)
    {
        return delegate.getText(key, value1, value2, value3, value4);
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5);
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6);
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }

}
