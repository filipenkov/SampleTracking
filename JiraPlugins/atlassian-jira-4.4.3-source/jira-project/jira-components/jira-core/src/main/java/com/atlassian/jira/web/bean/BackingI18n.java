package com.atlassian.jira.web.bean;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.unmodifiableSet;

/**
 * DEVSPEED-34: We wanted to properly cache the I18nBean. To make that happen we turned the I18nBean into a flyweight
 * during JIRA 4.3. All of the original logic was moved into this class.
 * <p/>
 * Looking up translations in plugins happens lazily. This (shouldn't) have any real world impact but it makes certain
 * unit tests easier. (You don't need to worry about mocking out PluginAccessor.)
 * <p/>
 * Note that this class is cached by the {@link com.atlassian.jira.web.bean.I18nBean.CachingFactory} per Locale and will
 * be re-created during plugin reload events.
 *
 * @see http://en.wikipedia.org/wiki/Flyweight_pattern
 * @since 4.3
 */
@Immutable
class BackingI18n implements I18nHelper
{
    private static final Logger log = Logger.getLogger(I18nHelper.class);

    private static final char START_HIGHLIGHT_CHAR = '\uFEFF';  // BOM
    private static final char MIDDLE_HIGHLIGHT_CHAR = '\u26A1'; // lightning
    private static final char END_HIGHLIGHT_CHAR = '\u2060';    // zero width word joiner

    private static final String PLUGIN_RESOURCE_TYPE_I18N = "i18n";
    private static final ResourceNotFound NOT_FOUND = new ResourceNotFound();

    private final Locale locale;
    private final Map<String, Set<String>> prefixKeysCache = new MapMaker().makeComputingMap(new PrefixFunction());
    private final Iterable<ResourceBundle> resourceBundlesInEffect;
    private final I18nTranslationMode i18nTranslationMode;
    private final InvolvedPluginsTracker involvedPluginsTracker;

    /**
     * Construct an I18nBean in the given Locale.
     *
     * @param locale the Locale
     * @param i18nTranslationMode whether the magic translate mode is on or not
     */
    public BackingI18n(final Locale locale, final I18nTranslationMode i18nTranslationMode, final InvolvedPluginsTracker involvedPluginsTracker)
    {
        this.involvedPluginsTracker = notNull("pluginsInvolvedTracker", involvedPluginsTracker);
        this.i18nTranslationMode = notNull("i18nTranslationMode", i18nTranslationMode);
        this.locale = notNull("locale", locale);
        this.resourceBundlesInEffect = loadV1AndV2LanguagePacksAndPluginsBundles(locale);
    }

    public Locale getLocale()
    {
        return locale;
    }

    public ResourceBundle getDefaultResourceBundle()
    {
        return DefaultResourceBundle.getDefaultResourceBundle(locale);
    }

    /**
     * Subclasses should override this and return null if they don't want to touch the ComponentManager
     *
     * @return a {@link PluginAccessor} if its null then plugin resource loading wont happen
     */
    protected PluginAccessor getPluginAccessor()
    {
        return ComponentAccessor.getPluginAccessor();
    }

    public Set<String> getKeysForPrefix(final String prefix)
    {
        return prefixKeysCache.get(prefix);
    }

    private class PrefixFunction implements com.google.common.base.Function<String, Set<String>>
    {
        public Set<String> apply(final String prefix)
        {
            final Set<String> ret = new HashSet<String>();
            //loop through all resource bundles, and find all keys with the given prefix.
            for (final ResourceBundle bundle : resourceBundlesInEffect)
            {
                final Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements())
                {
                    final String key = keys.nextElement();
                    if (key.startsWith(prefix))
                    {
                        ret.add(key);
                    }
                }
            }
            return unmodifiableSet(ret);
        }
    }

    /**
     * Get the raw property value, complete with {0}'s.
     *
     * @param key Non-null key to look up
     * @return Unescaped property value for the key, or the key itself if no property with the specified key is found
     */
    public String getUnescapedText(final String key)
    {
        try
        {
            return getRawMsg(key);
        }
        catch (final ResourceNotFound e)
        {
            // if the key was not found, escape it in order to thwart XSS attacks. see JRA-21360
            // This has been backed out as it causes double escaping problems. See JRADEV-2996
            return key;
        }
    }

    public String getText(final String key)
    {
        return formatI18nMsg(key);
    }

    public String getText(final String key, final String value1)
    {
        return formatI18nMsg(key, value1);
    }

    public String getText(final String key, final String value1, final String value2)
    {
        return formatI18nMsg(key, value1, value2);
    }

    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return formatI18nMsg(key, value1, value2, value3);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3)
    {
        return formatI18nMsg(key, value1, value2, value3);
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return formatI18nMsg(key, value1, value2, value3, value4);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4)
    {
        return formatI18nMsg(key, value1, value2, value3, value4);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6);
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7, final Object value8)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }

    public String getText(final String key, final Object parameters)
    {
        final Object[] substitutionParameters;
        if (parameters instanceof Object[])
        {
            substitutionParameters = (Object[]) parameters;
        }
        else if (parameters instanceof Iterable)
        {
            substitutionParameters = Iterables.toArray((Iterable<?>) parameters, Object.class);
        }
        else if (parameters == null)
        {
            substitutionParameters = new Object[0];
        }
        else
        {
            substitutionParameters = asArray(parameters);
        }
        return formatI18nMsg(key, substitutionParameters);
    }

    private String formatI18nMsg(final String key, final Object... substitutionParameters)
    {
        String message;
        boolean doFormat = true;
        try
        {
            message = getRawMsg(key);
        }
        catch (final ResourceNotFound e)
        {
            // if we did not find a value, return 'key' as message and don't apply MessageFormat
            message = key;
            doFormat = false;
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Could not find i18n key: %s", key));
            }
        }

        if (!doFormat)
        {
            return hilightMsg(key, message, message);
        }

        final MessageFormat mf;
        try
        {
            mf = new MessageFormat(message, locale);
        }
        catch (final IllegalArgumentException e)
        {
            log.error("Error rendering '" + message + "': " + e.getMessage(), e);
            throw e;
        }
        final String formattedMsg = mf.format(substitutionParameters == null ? new Object[0] : substitutionParameters);
        return hilightMsg(key, message, formattedMsg);
    }

    private String hilightMsg(String key, String rawMessage, String formattedMsg)
    {
        if (i18nTranslationMode.isTranslationMode())
        {
            if (formattedMsg.equals(rawMessage))
            {
                // slight network traffic optimisation
                return String.format("%c%s%c%s%c%c", START_HIGHLIGHT_CHAR, formattedMsg, MIDDLE_HIGHLIGHT_CHAR, key, MIDDLE_HIGHLIGHT_CHAR, END_HIGHLIGHT_CHAR);
            }
            else
            {
                return String.format("%c%s%c%s%c%s%c", START_HIGHLIGHT_CHAR, formattedMsg, MIDDLE_HIGHLIGHT_CHAR, key, MIDDLE_HIGHLIGHT_CHAR, rawMessage, END_HIGHLIGHT_CHAR);
            }
        }
        else
        {
            return formattedMsg;
        }
    }

    private Object[] asArray(Object... values)
    {
        return values == null ? new Object[0] : values;
    }

    /**
     * Get the raw property value, complete with {0}'s.
     *
     * @param key Non-null key to look up
     * @return Unescaped property value for the key
     * @throws ResourceNotFound if the key does not have a value in any of the available resource locations
     */
    private String getRawMsg(String key) throws ResourceNotFound
    {
        if (key.startsWith("'") && key.endsWith("'"))
        {
            key = key.substring(1, key.length() - 1);
        }

        // Loop through all the i18n resources
        for (final ResourceBundle bundle : resourceBundlesInEffect)
        {
            try
            {
                if (bundle.containsKey(key))
                {
                    return bundle.getString(key);
                }
            }
            // on exceptions we just want to continue on to the next bundle
            catch (MissingResourceException ignored) {}
            catch (ClassCastException ignored) {}
            catch (NullPointerException ignored) {}
        }

        // if we did not find anything, throw an exception here
        throw NOT_FOUND;
    }


    private Iterable<ResourceBundle> loadV1AndV2LanguagePacksAndPluginsBundles(final Locale locale)
    {
        //
        // this is the JiraWebActionSupport.properties class path language packs
        //
        Iterable<ResourceBundle> v1ClassPathJiraWebActionSupportBundles = Iterables.unmodifiableIterable(Collections.singletonList(getDefaultResourceBundle()));

        final PluginAccessor pluginAccessor = getPluginAccessor();

        // unit tests can be in this state and hence we quickly return if this is the case
        // in production this is never true and if it was we have bigger problems
        if (pluginAccessor == null)
        {
            return v1ClassPathJiraWebActionSupportBundles;
        }

        Iterable<ResourceBundle> v2LanguagePackSourceBundles = loadV2LanguagePackBundles(locale, pluginAccessor);

        Iterable<ResourceBundle> pluginSourcedBundles = loadPluginSourcedBundles(locale, pluginAccessor);

        //
        // The ordering here is important.  We want V2 language packs to be before v1 language packs
        // and we want plugin i18n resources to be last of all
        //
        return Iterables.unmodifiableIterable(
                Iterables.concat(
                        v2LanguagePackSourceBundles,
                        v1ClassPathJiraWebActionSupportBundles,
                        pluginSourcedBundles));
    }

    private Iterable<ResourceBundle> loadV2LanguagePackBundles(Locale targetLocale, PluginAccessor pluginAccessor)
    {
        final List<ResourceBundle> v2LanguagePacks = new ArrayList<ResourceBundle>();
        List<LanguageModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(LanguageModuleDescriptor.class);
        for (LanguageModuleDescriptor descriptor : descriptors)
        {
            String location = descriptor.getResourceBundleName();
            if (!descriptor.getModule().getLocale().equals(targetLocale))
            {
                continue;
            }

            try
            {
                ResourceBundle resourceBundle = ResourceBundle.getBundle(location, targetLocale, descriptor.getPlugin().getClassLoader());
                v2LanguagePacks.add(resourceBundle);

                involvedPluginsTracker.trackInvolvedPlugin(descriptor);
            }
            catch (final MissingResourceException mre)
            {
                // JRA-18831 we don't want a missing resource exception to prevent us from loading other bundles.
                if (log.isDebugEnabled())
                {
                    log.debug(String.format("Failed to get resource bundle %s from module descriptor %s", location, descriptor.getName()), mre);
                }
            }
        }
        return v2LanguagePacks;
    }

    private Iterable<ResourceBundle> loadPluginSourcedBundles(Locale targetLocale, PluginAccessor pluginAccessor)
    {
        final List<ResourceBundle> pluginBundles = new ArrayList<ResourceBundle>();
        for (final Plugin plugin : pluginAccessor.getEnabledPlugins())
        {
            for (final ResourceDescriptor resourceDescriptor : getResourceBundleLocations(plugin))
            {
                try
                {
                    String location = resourceDescriptor.getLocation();
                    ResourceBundle resourceBundle = ResourceBundle.getBundle(location, targetLocale, plugin.getClassLoader());
                    pluginBundles.add(resourceBundle);

                    involvedPluginsTracker.trackInvolvedPlugin(plugin);

                }
                catch (final MissingResourceException mre)
                {
                    // JRA-18831 we don't want a missing resource exception to prevent us from loading other bundles.
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Failed to get resource bundle %s from resource descriptor %s", resourceDescriptor.getLocation(),
                                resourceDescriptor.getName()), mre);
                    }
                }
            }
        }
        return pluginBundles;
    }

    private Collection<ResourceDescriptor> getResourceBundleLocations(final Plugin plugin)
    {
        final List<ResourceDescriptor> locations = new ArrayList<ResourceDescriptor>();
        Iterables.addAll(locations, Iterables.filter(plugin.getResourceDescriptors(), new Resources.TypeFilter(PLUGIN_RESOURCE_TYPE_I18N)));
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            Iterables.addAll(locations, Iterables.filter(moduleDescriptor.getResourceDescriptors(), new Resources.TypeFilter(PLUGIN_RESOURCE_TYPE_I18N)));
        }
        return locations;
    }

    /**
     * efficient exception that has no stacktrace; we use this for flow-control.
     */
    @Immutable
    static final class ResourceNotFound extends Exception
    {
        ResourceNotFound()
        {}

        @Override
        public Throwable fillInStackTrace()
        {
            return this;
        }
    }
}
