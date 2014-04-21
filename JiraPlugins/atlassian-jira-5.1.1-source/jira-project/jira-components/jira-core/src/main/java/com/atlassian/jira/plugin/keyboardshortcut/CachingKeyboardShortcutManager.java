package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.jira.plugin.keyboardshortcut.CachingKeyboardShortcutManager.ShortcutCacheKey.makeCacheKey;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

@EventComponent
public class CachingKeyboardShortcutManager implements KeyboardShortcutManager
{
    private static Logger log = LoggerFactory.getLogger(CachingKeyboardShortcutManager.class);
    private static final String REST_PREFIX = "/rest/api/1.0/shortcuts/";
    private static final String REST_RESOURCE = "/shortcuts.js";

    private static final String REQUEST_CACHE_KEY = "keyboard.shortcuts.contexts";

    private final Map<String, KeyboardShortcut> shortcuts = new ConcurrentHashMap<String, KeyboardShortcut>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final BuildUtilsInfo buildUtilsInfo;
    private final PluginAccessor pluginAccessor;
    private final WebResourceIntegration webResourceIntegration;
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;

    /**
     * To speed up performance, we sort the shortcuts in a lazyreference which will only get reset if a keyboard
     * shortcut is registered/unregistered.
     */
    private final ResettableLazyReference<List<KeyboardShortcut>> allShortcuts = new ResettableLazyReference<List<KeyboardShortcut>>()
    {
        @Override
        protected List<KeyboardShortcut> create() throws Exception
        {
            final List<KeyboardShortcut> ret = new ArrayList<KeyboardShortcut>(shortcuts.values());
            Collections.sort(ret);
            return ret;
        }
    };

    /**
     * Generating the md5 hash for keyboard shortcuts can also be expensive so in order to make this as performant as
     * possible, we use a resettable lazy reference to ensure we only have to calculate it after a keyboard shortcut was
     * registered/unregistered.
     */
    private final ResettableLazyReference<String> allShortcutsMD5 = new ResettableLazyReference<String>()
    {
        @Override
        protected String create() throws Exception
        {
            MessageDigest messageDigest = getMessageDigest("MD5");
            if (messageDigest == null)
            {
                messageDigest = getMessageDigest("SHA");
            }
            if (messageDigest == null)
            {
                throw new RuntimeException("Unable to retrieve a valid message digest!");
            }
            messageDigest.update(getAllShortcuts().toString().getBytes());
            final byte[] digest = messageDigest.digest();
            final BigInteger bigInt = new BigInteger(1, digest);
            final String hash = bigInt.toString(16);
            //include the buildnumber as well as the hash of the keyboard shortcuts, to ensure that when JIRA is upgraded
            //a new keyboard shortcut js file will be served regardless of if the hashcode has changed.
            return new StringBuilder().append(REST_PREFIX).append(buildUtilsInfo.getCurrentBuildNumber()).
                    append("/").append(hash).append(REST_RESOURCE).toString();
        }

        private MessageDigest getMessageDigest(String digestName)
        {
            try
            {
                return MessageDigest.getInstance(digestName);
            }
            catch (NoSuchAlgorithmException e)
            {
                return null;
            }
        }
    };


    public CachingKeyboardShortcutManager(final BuildUtilsInfo buildUtilsInfo,
            final PluginAccessor pluginAccessor, final WebResourceIntegration webResourceIntegration, final JiraAuthenticationContext authenticationContext, final UserPreferencesManager userPreferencesManager)
    {
        this.buildUtilsInfo = buildUtilsInfo;
        this.pluginAccessor = pluginAccessor;
        this.webResourceIntegration = webResourceIntegration;
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        lock.writeLock().lock();
        try
        {
            //first clear any cache
            shortcuts.clear();
            allShortcuts.reset();
            allShortcutsMD5.reset();

            //then re-init the cache
            final List<KeyboardShortcutModuleDescriptor> descriptors =
                    pluginAccessor.getEnabledModuleDescriptorsByClass(KeyboardShortcutModuleDescriptor.class);
            for (KeyboardShortcutModuleDescriptor descriptor : descriptors)
            {
                shortcuts.put(descriptor.getCompleteKey(), descriptor.getModule());
            }
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void registerShortcut(final String pluginModuleKey, final KeyboardShortcut shortcut)
    {
        lock.writeLock().lock();
        try
        {
            shortcuts.put(pluginModuleKey, shortcut);
            allShortcuts.reset();
            allShortcutsMD5.reset();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void unregisterShortcut(final String pluginModuleKey)
    {
        lock.writeLock().lock();
        try
        {
            shortcuts.remove(pluginModuleKey);
            allShortcuts.reset();
            allShortcutsMD5.reset();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<KeyboardShortcut> getActiveShortcuts()
    {
        return listActiveShortcutsUniquePerContext(Collections.<String, Object>emptyMap());
    }

    @Override
    public List<KeyboardShortcut> listActiveShortcutsUniquePerContext(final Map<String, Object> userContext)
    {
        
        final Collection<KeyboardShortcut> filter = filter(getAllShortcuts(), new Predicate<KeyboardShortcut>()
        {
            @Override
            public boolean apply(final KeyboardShortcut keyboardShortcut)
            {
                try
                {
                    return keyboardShortcut.shouldDisplay(userContext);
                }
                catch (RuntimeException e)
                {
                    log.warn("failed to evaluate the conditions of a keyboard shortcut: " + keyboardShortcut, e);
                    return false;
                }
            }
        });
        return eliminateDuplicateShortcutsPerContext(new LinkedList<KeyboardShortcut>(filter));
    }

    public List<KeyboardShortcut> getAllShortcuts()
    {
        lock.readLock().lock();
        try
        {
            return allShortcuts.get();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public void requireShortcutsForContext(final Context context)
    {
        getRequiredContexts().add(context);
    }

    private List<KeyboardShortcut> eliminateDuplicateShortcutsPerContext(final Collection<KeyboardShortcut> keyboardShortcuts)
    {
        // eliminate duplicate shortcuts from the "all shortcuts" list by stuffing them into a map. this works
        // because getAllShortcuts() returns an ordered list, so the last shortcut present on the list "wins".
        Map<ShortcutCacheKey, KeyboardShortcut> active = Maps.newHashMapWithExpectedSize(keyboardShortcuts.size());
        for (KeyboardShortcut shortcut : keyboardShortcuts)
        {
            ShortcutCacheKey key = makeCacheKey(shortcut);
            KeyboardShortcut overridden = active.put(key, shortcut);
            if (overridden != null)
            {
                log.debug("Keyboard shortcut '{}' overrides '{}' in context {} with keys: {}", new Object[] { shortcut.getDescriptionI18nKey(), overridden.getDescriptionI18nKey(), key.context, key.shortcuts });
            }
        }

        ArrayList<KeyboardShortcut> activeShortcuts = Lists.newArrayList(active.values());
        Collections.sort(activeShortcuts);
        return activeShortcuts;
    }

    private Set<Context> getRequiredContexts()
    {
        final Map<String, Object> requestCache = webResourceIntegration.getRequestCache();
        @SuppressWarnings ("unchecked")
        Set<Context> requiredContexts = (Set<Context>) requestCache.get(REQUEST_CACHE_KEY);
        if (requiredContexts == null)
        {
            requiredContexts = new LinkedHashSet<Context>();
            requestCache.put(REQUEST_CACHE_KEY, requiredContexts);
        }
        return requiredContexts;
    }

    public String includeShortcuts()
    {
        UrlBuilder url;
        lock.readLock().lock();
        try
        {
            url = new UrlBuilder(allShortcutsMD5.get());
        }
        finally
        {
            lock.readLock().unlock();
        }

        for (Context context : getRequiredContexts())
        {
            url.addParameterUnsafe("context", context.toString());
        }
        return url.asUrlString();
    }

    public boolean isKeyboardShortcutsEnabled()
    {
        final User user = authenticationContext.getLoggedInUser();
        if (user == null)
        {
            return true;
        }

        Preferences userPrefs = userPreferencesManager.getPreferences(user);
        return !userPrefs.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }
    
    final static class ShortcutCacheKey
    {
        public static ShortcutCacheKey makeCacheKey(@Nonnull KeyboardShortcut shortcut)
        {
            checkNotNull(shortcut, "shortcut");
            return new ShortcutCacheKey(shortcut.getContext(), shortcut.getShortcuts());
        }
        
        final Context context;
        final ImmutableSet<ImmutableList<String>> shortcuts;

        private ShortcutCacheKey(Context context, Set<List<String>> shortcuts)
        {
            this.context = context;
            this.shortcuts = makeImmutable(shortcuts);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ShortcutCacheKey that = (ShortcutCacheKey) o;

            if (context != that.context) { return false; }
            if (!shortcuts.equals(that.shortcuts)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = context.hashCode();
            result = 31 * result + shortcuts.hashCode();
            return result;
        }

        private ImmutableSet<ImmutableList<String>> makeImmutable(Set<List<String>> keys)
        {
            return ImmutableSet.copyOf(transform(keys, new Function<List<String>, ImmutableList<String>>()
            {
                @Override
                public ImmutableList<String> apply(@Nullable List<String> input)
                {
                    return ImmutableList.copyOf(input);
                }
            }));
        }
    }
}
