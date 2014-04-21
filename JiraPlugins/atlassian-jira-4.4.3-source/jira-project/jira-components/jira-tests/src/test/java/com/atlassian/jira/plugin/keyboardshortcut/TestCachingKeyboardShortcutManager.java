package com.atlassian.jira.plugin.keyboardshortcut;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestCachingKeyboardShortcutManager extends ListeningTestCase
{
    @Test
    public void testClearCacheEvent() throws Exception
    {
        final BuildUtilsInfo mockBuildUtilsInfo = createMock(BuildUtilsInfo.class);
        final EventPublisher mockEventPublisher = createMock(EventPublisher.class);
        final PluginAccessor mockPluginAccessor = createMock(PluginAccessor.class);
        final JiraAuthenticationContext mockAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final UserPreferencesManager mockUserPrefsManager = createMock(UserPreferencesManager.class);
        final CachingKeyboardShortcutManager manager = new CachingKeyboardShortcutManager(mockBuildUtilsInfo,
                mockEventPublisher, mockPluginAccessor, null, mockAuthenticationContext, mockUserPrefsManager);
        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andReturn("500").anyTimes();
        mockEventPublisher.register(manager);

        final MockKeyboardShortcutModuleDescriptor mockKeyboardShortcutModuleDescriptor = new MockKeyboardShortcutModuleDescriptor(null, null);
        expect(mockPluginAccessor.getEnabledModuleDescriptorsByClass(KeyboardShortcutModuleDescriptor.class)).
                andReturn(CollectionBuilder.<KeyboardShortcutModuleDescriptor>newBuilder(mockKeyboardShortcutModuleDescriptor).asList());

        replay(mockBuildUtilsInfo, mockEventPublisher, mockPluginAccessor);

        manager.start();

        List<KeyboardShortcut> list = manager.getAllShortcuts();
        assertEquals(0, list.size());
        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut(KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);
        manager.registerShortcut("some.key", shortcut);
        final List<KeyboardShortcut> shortcuts = manager.getAllShortcuts();
        assertEquals(1, shortcuts.size());
        assertEquals(shortcut, shortcuts.get(0));

        //now clear the cache
        manager.onClearCache(null);

        final List<KeyboardShortcut> shortcuts2 = manager.getAllShortcuts();
        assertEquals(1, shortcuts2.size());
        assertEquals(mockKeyboardShortcutModuleDescriptor.getSecondShortcut(), shortcuts2.get(0));

        verify(mockBuildUtilsInfo, mockEventPublisher, mockPluginAccessor);
    }

    /**
     * Test registering and unregistering of shortcuts and that this action also updates the hashcode for the URL used
     * to include shortcuts.
     */
    @Test
    public void testAddAndRemoveShortcuts()
    {
        final BuildUtilsInfo mockBuildUtilsInfo = createMock(BuildUtilsInfo.class);
        final WebResourceIntegration mockWebResourceIntegration = createMock(WebResourceIntegration.class);
        final JiraAuthenticationContext mockAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final UserPreferencesManager mockUserPrefsManager = createMock(UserPreferencesManager.class);
        final HashMap<String, Object> requestCache = new HashMap<String, Object>();

        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andReturn("500").anyTimes();
        expect(mockWebResourceIntegration.getRequestCache())
                .andReturn(requestCache)
                .anyTimes();

        replay(mockBuildUtilsInfo, mockWebResourceIntegration);
        CachingKeyboardShortcutManager manager = new CachingKeyboardShortcutManager(mockBuildUtilsInfo, null, null, mockWebResourceIntegration, mockAuthenticationContext, mockUserPrefsManager);
        List<KeyboardShortcut> list = manager.getAllShortcuts();
        assertEquals(0, list.size());
        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut(KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);
        final KeyboardShortcut shortcut2 = new KeyboardShortcut(KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link2", 10, keys, "blah2", false);
        final String url = manager.includeShortcuts();
        assertNotNull(url);
        manager.registerShortcut("some.key", shortcut);
        final String url2 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url.equals(url2));
        manager.registerShortcut("some.key2", shortcut2);
        final String url3 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url2.equals(url3));

        final List<KeyboardShortcut> sortedList = manager.getAllShortcuts();
        assertEquals(2, sortedList.size());
        //due to the order attribute, shortcut 2 should come first
        assertEquals(shortcut2, sortedList.get(0));
        assertEquals(shortcut, sortedList.get(1));

        manager.unregisterShortcut("some.key");
        final String url4 = manager.includeShortcuts();
        //url should have changed after registering another shortcut
        assertFalse(url3.equals(url4));

        final List<KeyboardShortcut> allShortcuts = manager.getAllShortcuts();
        assertEquals(1, allShortcuts.size());
        assertEquals(shortcut2, allShortcuts.get(0));

        verify(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    @Test
    public void testRequiringContextsDoesntChangeUrlHash()
    {
        final BuildUtilsInfo mockBuildUtilsInfo = createMock(BuildUtilsInfo.class);
        final WebResourceIntegration mockWebResourceIntegration = createMock(WebResourceIntegration.class);
        final JiraAuthenticationContext mockAuthenticationContext = createMock(JiraAuthenticationContext.class);
        final UserPreferencesManager mockUserPrefsManager = createMock(UserPreferencesManager.class);
        final HashMap<String, Object> requestCache = new HashMap<String, Object>();

        expect(mockBuildUtilsInfo.getCurrentBuildNumber()).andReturn("500").anyTimes();
        expect(mockWebResourceIntegration.getRequestCache())
                .andReturn(requestCache)
                .anyTimes();

        replay(mockBuildUtilsInfo, mockWebResourceIntegration);
        CachingKeyboardShortcutManager manager = new CachingKeyboardShortcutManager(mockBuildUtilsInfo, null, null, mockWebResourceIntegration, mockAuthenticationContext, mockUserPrefsManager);
        final Set<List<String>> keys = new HashSet<List<String>>();
        keys.add(Arrays.asList("g", "h"));
        final KeyboardShortcut shortcut = new KeyboardShortcut(KeyboardShortcutManager.Context.global,
                KeyboardShortcutManager.Operation.click, "#some_link", 30, keys, "blah", false);

        final String urlWithHash = manager.includeShortcuts();
        assertFalse(urlWithHash.contains("?"));

        manager.requireShortcutsForContext(KeyboardShortcutManager.Context.issueaction);
        assertTrue(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));

        manager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        assertTrue(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issuenavigation.toString()));

        // Now make the hash change.
        manager.registerShortcut("some.key", shortcut);
        assertFalse(manager.includeShortcuts().startsWith(urlWithHash));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issueaction.toString()));
        assertTrue(manager.includeShortcuts().contains("context=" + KeyboardShortcutManager.Context.issuenavigation.toString()));

        verify(mockBuildUtilsInfo, mockWebResourceIntegration);
    }

    private static class MockKeyboardShortcutModuleDescriptor extends KeyboardShortcutModuleDescriptor
    {
        private KeyboardShortcut secondShortcut;

        public MockKeyboardShortcutModuleDescriptor(final JiraAuthenticationContext authenticationContext, final KeyboardShortcutManager keyboardShortcutManager)
        {
            super(authenticationContext, keyboardShortcutManager, null);
            final Set<List<String>> keys = new HashSet<List<String>>();
            keys.add(Arrays.asList("c"));
            secondShortcut = new KeyboardShortcut(KeyboardShortcutManager.Context.issueaction,
                    KeyboardShortcutManager.Operation.click, "#create_issue", 10, keys, "dude", false);
        }

        @Override
        public String getCompleteKey()
        {
            return "some.plugin.module.key";
        }

        @Override
        protected KeyboardShortcut createModule()
        {
            return secondShortcut;
        }

        public KeyboardShortcut getSecondShortcut()
        {
            return secondShortcut;
        }
    }
}
