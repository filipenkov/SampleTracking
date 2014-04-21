package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.user.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CachingKeyboardShortcutManager implements KeyboardShortcutManager, Startable
{
    private static final String REST_PREFIX = "/rest/api/1.0/shortcuts/";
    private static final String REST_RESOURCE = "/shortcuts.js";

    private static final String REQUEST_CACHE_KEY = "keyboard.shortcuts.contexts";

    private final Map<String, KeyboardShortcut> shortcuts = new ConcurrentHashMap<String, KeyboardShortcut>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final BuildUtilsInfo buildUtilsInfo;
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;
    private final WebResourceIntegration webResourceIntegration;
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;

    /**
     * To speed up performance, we sort the shortcuts in a lazyreference which will only get reset if a keyboard
     * shortcut is registered/unregistered.
     */
    private final ResettableLazyReference<List<KeyboardShortcut>> ref = new ResettableLazyReference<List<KeyboardShortcut>>()
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
    private final ResettableLazyReference<String> resourceRef = new ResettableLazyReference<String>()
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


    public CachingKeyboardShortcutManager(final BuildUtilsInfo buildUtilsInfo, final EventPublisher eventPublisher,
            final PluginAccessor pluginAccessor, final WebResourceIntegration webResourceIntegration, final JiraAuthenticationContext authenticationContext, final UserPreferencesManager userPreferencesManager)
    {
        this.buildUtilsInfo = buildUtilsInfo;
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
        this.webResourceIntegration = webResourceIntegration;
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        lock.writeLock().lock();
        try
        {
            //first clear any cache
            shortcuts.clear();
            ref.reset();
            resourceRef.reset();

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
            ref.reset();
            resourceRef.reset();
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
            ref.reset();
            resourceRef.reset();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public List<KeyboardShortcut> getAllShortcuts()
    {
        lock.readLock().lock();
        try
        {
            return ref.get();
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
            url = new UrlBuilder(resourceRef.get());
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
        final User user = authenticationContext.getUser();
        if (user == null)
        {
            return true;
        }

        Preferences userPrefs = userPreferencesManager.getPreferences(user);
        return !userPrefs.getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED);
    }
}
