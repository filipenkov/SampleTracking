package com.atlassian.jira.web.bean;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableMap;
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
@Internal
@VisibleForTesting
public class BackingI18n implements I18nHelper
{
    private static final Logger log = Logger.getLogger(I18nHelper.class);

    /**
     * The default behavior is to fallback on Locale.getDefault() if the requested
     * locale is not the default.  This leads to surprising results that we do not
     * want, and this <tt>ResourceBundle.Control</tt> prevents it.
     */
    private static final ResourceBundle.Control NO_FALLBACK_CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    /** Slight optimization to avoid spurious Object[0] creations for messages
     * with no args.
     */
    private static final Object[] EMPTY_ARRAY = {};
    private static final char START_HIGHLIGHT_CHAR = '\uFEFF';  // BOM
    private static final char MIDDLE_HIGHLIGHT_CHAR = '\u26A1'; // lightning
    private static final char END_HIGHLIGHT_CHAR = '\u2060';    // zero width word joiner

    /** Tunable - Currently I see about 14k keys on a base system.
     * Better to err on the small side to avoid wasting mem, but the default
     * map size of 16 is certainly never going to be adequate and would make
     * the map build slower than it has to be.
     */
    private static final int INITIAL_FLATTENED_MAP_SIZE = 8192;
    private static final String PLUGIN_RESOURCE_TYPE_I18N = "i18n";
    private static final ResourceBundleLocaleSorter RESOURCE_BUNDLE_LOCALE_SORTER = new ResourceBundleLocaleSorter();

    private final Locale locale;
    private final Map<String, Set<String>> prefixKeysCache = new MapMaker().makeComputingMap(new PrefixFunction());
    private final Map<String, String> translationMap;
    private final I18nTranslationMode i18nTranslationMode;
    private final InvolvedPluginsTracker involvedPluginsTracker;
    private final List<TranslationTransform> translationTransforms;

    /**
     * Construct an I18nBean in the given Locale.
     *
     * @param locale the Locale
     * @param i18nTranslationMode whether the magic translate mode is on or not
     * @param involvedPluginsTracker keeps track of which plugins have I18N information
     *      so that we can avoid flushing the translation cache when unrelated modules
     * @param translationTransforms
     */
    public BackingI18n(final Locale locale, final I18nTranslationMode i18nTranslationMode, final InvolvedPluginsTracker involvedPluginsTracker, List<TranslationTransform> translationTransforms)
    {
        this.involvedPluginsTracker = notNull("pluginsInvolvedTracker", involvedPluginsTracker);
        this.i18nTranslationMode = notNull("i18nTranslationMode", i18nTranslationMode);
        this.locale = notNull("locale", locale);
        this.translationMap = loadV1AndV2LanguagePacksAndPluginsBundles(locale);
        this.translationTransforms = notNull("translationTransforms", translationTransforms);
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
            for (String key : translationMap.keySet())
            {
                if (key.startsWith(prefix))
                {
                    ret.add(key);
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
    public String getUnescapedText(String key)
    {
        // if the key was not found, escape it in order to thwart XSS attacks. see JRA-21360
        // This has been backed out as it causes double escaping problems. See JRADEV-2996
        key = cleanKey(key);
        final String value = getTranslation(key);
        return (value != null) ? value : key;
    }

    public String getUntransformedRawText(String key)
    {
        key = cleanKey(key);
        String rawMessage = translationMap.get(key);
        if (rawMessage != null)
        {
            return rawMessage;
        }
        return key;
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return translationMap.containsKey(cleanKey(key));
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
            substitutionParameters = EMPTY_ARRAY;
        }
        else
        {
            substitutionParameters = new Object[] { parameters };
        }
        return formatI18nMsg(key, substitutionParameters);
    }

    // Ugly kludge to allow caller to ask for 'key' instead of just the key.
    private String cleanKey(String key)
    {
        return (key != null && key.length() >= 2 && key.charAt(0) == '\'' && key.charAt(key.length() - 1) == '\'')
                ? key.substring(1, key.length()-1)
                : key;
    }

    private String formatI18nMsg(String key, final Object... substitutionParameters)
    {
        key = cleanKey(key);
        String rawMessage = getTranslation(key);
        if (rawMessage == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Could not find i18n key: " + key);
            }
            return hilightMsg(key, key, key);
        }

        final MessageFormat mf;
        try
        {
            mf = new MessageFormat(rawMessage, locale);
        }
        catch (final IllegalArgumentException e)
        {
            log.error("Error rendering '" + rawMessage + "': " + e.getMessage(), e);
            throw e;
        }
        final String formattedMsg = mf.format((substitutionParameters == null) ? EMPTY_ARRAY : substitutionParameters);
        return hilightMsg(key, rawMessage, formattedMsg);
    }

    private String getTranslation(String key)
    {
        String rawMessage = translationMap.get(key);
        if (rawMessage != null)
        {
            rawMessage = processTranslationTransforms(key, rawMessage);
        }
        return rawMessage;
    }

    private String processTranslationTransforms(String key, String rawMessage)
    {
        String result = rawMessage;
        for (TranslationTransform translationTransform : translationTransforms)
        {
            result = translationTransform.apply(this.locale, key, result);
        }
        return result;
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


    private Map<String,String> loadV1AndV2LanguagePacksAndPluginsBundles(final Locale locale)
    {
        //
        // this is the JiraWebActionSupport.properties class path language packs
        //
        List<ResourceBundle> v1ClassPathJiraWebActionSupportBundles = Arrays.asList(getDefaultResourceBundle());
        if (log.isDebugEnabled())
        {
            log.debug("Loaded v1 support bundles; targetLocale=" + locale + "; resource locale=" + v1ClassPathJiraWebActionSupportBundles.get(0).getLocale());
        }
        final PluginAccessor pluginAccessor = getPluginAccessor();

        // unit tests can be in this state and hence we quickly return if this is the case
        // in production this is never true and if it was we have bigger problems
        if (pluginAccessor == null)
        {
            return flattenResourceBundlesToMap(v1ClassPathJiraWebActionSupportBundles);
        }

        Iterable<ResourceBundle> v2LanguagePackSourceBundles = loadV2LanguagePackBundles(locale, pluginAccessor);
        Iterable<ResourceBundle> pluginSourcedBundles = loadPluginSourcedBundles(locale, pluginAccessor);

        //
        // The ordering here is important.  We want V2 language packs to be before v1 language packs
        // and we want plugin i18n resources to be last of all.  They need to be listed in the
        // reverse order, here (see flattenResourceBundlesToMap for an explanation).
        //
        return flattenResourceBundlesToMap(
                Iterables.concat(
                        pluginSourcedBundles,
                        v1ClassPathJiraWebActionSupportBundles,
                        v2LanguagePackSourceBundles));
    }

    /**
     * Returns whether or not the <tt>providedLocale</tt> is suitable for
     * use in the desired <tt>targetLocale</tt>.  It is suitable if the
     * provided locale is an exact match or an appropriate fallback for
     * the target locale.  Specifically, this means that:
     * <ol>
     * <li>The provided locale does not specify a language (it is the root locale); or</li>
     * <li>The provided locale does not specify a country and the language matches; or</li>
     * <li>The provided locale specifies both a language and a country, and they both match.</li>
     * </ol>
     * Any locale "variant" information is ignored, here.
     *
     * @param providedLocale the locale provided by the resource under consideration
     * @param targetLocale the target locale
     * @return <tt>true</tt> if the resource should be included; <tt>false</tt>
     *      if it does not provide translations for the target locale
     */
    static boolean providedLocaleMatches(Locale providedLocale, Locale targetLocale)
    {
        if (providedLocale.getLanguage().length() == 0)
        {
            return true;
        }
        if (!providedLocale.getLanguage().equals(targetLocale.getLanguage()))
        {
            return false;
        }
        if (providedLocale.getCountry().length() == 0)
        {
            return true;
        }
        return providedLocale.getCountry().equals(targetLocale.getCountry());
    }


    private Iterable<ResourceBundle> loadV2LanguagePackBundles(Locale targetLocale, PluginAccessor pluginAccessor)
    {
        final List<ResourceBundle> v2LanguagePacks = new ArrayList<ResourceBundle>();
        final List<LanguageModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(LanguageModuleDescriptor.class);
        for (LanguageModuleDescriptor descriptor : descriptors)
        {
            final String location = descriptor.getResourceBundleName();
            final Locale providedLocale = descriptor.getModule().getLocale();
            if (!providedLocaleMatches(providedLocale, targetLocale))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("IGNORING v2 lang pack; targetLocale=" + targetLocale + "; descriptor=" + descriptor.getName() + "; providedLocale=" + providedLocale);
                }
                continue;
            }

            try
            {
                ResourceBundle resourceBundle = ResourceBundle.getBundle(location, providedLocale, descriptor.getPlugin().getClassLoader(), NO_FALLBACK_CONTROL);
                v2LanguagePacks.add(resourceBundle);
                involvedPluginsTracker.trackInvolvedPlugin(descriptor);
                if (log.isDebugEnabled())
                {
                    log.debug("Accepted v2 lang pack; targetLocale=" + targetLocale + "; descriptor=" + descriptor.getName() + "; providedLocale=" + providedLocale);
                }
            }
            catch (final MissingResourceException mre)
            {
                // JRA-18831 we don't want a missing resource exception to prevent us from loading other bundles.
                if (log.isDebugEnabled())
                {
                    log.debug("Failed to get resource bundle " + location + " from module descriptor " + descriptor.getName() + ": " + mre);
                }
            }
        }
        sort(v2LanguagePacks, RESOURCE_BUNDLE_LOCALE_SORTER);
        return v2LanguagePacks;
    }

    private Iterable<ResourceBundle> loadPluginSourcedBundles(Locale targetLocale, PluginAccessor pluginAccessor)
    {
        final List<ResourceBundle> pluginBundles = new ArrayList<ResourceBundle>();
        for (final Plugin plugin : pluginAccessor.getEnabledPlugins())
        {
            loadPluginSourcedBundles(targetLocale, pluginBundles, plugin);
        }
        sort(pluginBundles, RESOURCE_BUNDLE_LOCALE_SORTER);
        return pluginBundles;
    }

    private void loadPluginSourcedBundles(Locale targetLocale, List<ResourceBundle> pluginBundles, Plugin plugin)
    {
        for (final ResourceDescriptor resourceDescriptor : getResourceBundleLocations(plugin))
        {
            try
            {
                String location = resourceDescriptor.getLocation();
                ResourceBundle resourceBundle = ResourceBundle.getBundle(location, targetLocale, plugin.getClassLoader(), NO_FALLBACK_CONTROL);
                if (providedLocaleMatches(resourceBundle.getLocale(), targetLocale))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Accepted plugin resource; targetLocale=" + targetLocale + "; plugin=" + plugin.getName() + "; descriptor=" + resourceDescriptor.getName() + "; providedLocale=" + resourceBundle.getLocale());
                    }
                    pluginBundles.add(resourceBundle);
                    involvedPluginsTracker.trackInvolvedPlugin(plugin);
                }
                else if (log.isDebugEnabled())
                {
                    log.debug("IGNORING plugin resource; targetLocale=" + targetLocale + "; plugin=" + plugin.getName() + "; descriptor=" + resourceDescriptor.getName() + "; providedLocale=" + resourceBundle.getLocale());
                }
            }
            catch (final MissingResourceException mre)
            {
                // JRA-18831 we don't want a missing resource exception to prevent us from loading other bundles.
                if (log.isDebugEnabled())
                {
                    log.debug("Failed to get resource bundle " + resourceDescriptor.getLocation() + " from resource descriptor " +
                            resourceDescriptor.getName() + ": " + mre);
                }
            }
        }
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
     * Flattens a collection of <tt>ResourceBundle</tt>s into a simple
     * key-value map for efficient, exception-free lookups at a later time.
     *
     * @param bundles and iterable which will produce the resource bundles
     *      to apply in the order of lowest to highest priority.  The caller
     *      should ensure, for example, that "de" is listed before "de_CH"
     *      so that "de_CH" keys will override those from "de" by overwriting
     *      them in the map.
     * @return an unmodifiable key-value map containing the flattened mapping
     *      of all the key-value pairs from the resource bundles
     */
    private Map<String,String> flattenResourceBundlesToMap(Iterable<ResourceBundle> bundles)
    {
        final Map<String,String> map = new HashMap<String,String>(INITIAL_FLATTENED_MAP_SIZE);
        for (final ResourceBundle bundle : bundles)
        {
            if (bundle == null)
            {
                continue;
            }

            final Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
                final String key = keys.nextElement();
                try {
                    map.put(key, bundle.getString(key));
                }
                // None of these should ever happen because we're getting
                // the list of keys from the resource bundle itself, but
                // let's play it safe, anyway...
                catch (ClassCastException cce)
                {
                    logFlatteningException(key, bundle, cce);
                }
                catch (MissingResourceException mre)
                {
                    logFlatteningException(key, bundle, mre);
                }
                catch (NullPointerException npe)
                {
                    logFlatteningException(key, bundle, npe);
                }
            }
        }
        return unmodifiableMap(map);
    }

    private void logFlatteningException(String key, ResourceBundle bundle, Exception e)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Failed to resolve key " + key + "  from resource bundle " + bundle + ": " + e);
        }
    }

    /**
     * A comparator that orders resource bundles by how completely
     * specified their locales are.  Specifically:
     * <ol>
     *     <li>the root locale comes first</li>
     *     <li>locales with a language come after the root locale</li>
     *     <li>locales with a language and country come after locales with a language only</li>
     *     <li>No further ordering is imposed within each of these categories.  In
     *          particular, locale variants are not considered, here.</li>
     * </ol>
     * <p>
     * <em>Note: This comparator imposes orderings that are inconsistent with equals.</em></li>
     * </p>
     */
    @Immutable
    static final class ResourceBundleLocaleSorter implements Comparator<ResourceBundle>
    {
        ResourceBundleLocaleSorter()
        {}

        @Override
        public int compare(ResourceBundle bundle1, ResourceBundle bundle2)
        {
            final Locale locale1 = bundle1.getLocale();
            final Locale locale2 = bundle2.getLocale();
            if (locale1.getLanguage().length() == 0)
            {
                return (locale2.getLanguage().length() == 0) ? 0 : -1;
            }
            if (locale2.getLanguage().length() == 0)
            {
                return 1;
            }
            if (locale1.getCountry().length() == 0)
            {
                return (locale2.getCountry().length() == 0) ? 0 : -1;
            }
            if (locale2.getCountry().length() == 0)
            {
                return 1;
            }
            return 0;
        }
    }
}


