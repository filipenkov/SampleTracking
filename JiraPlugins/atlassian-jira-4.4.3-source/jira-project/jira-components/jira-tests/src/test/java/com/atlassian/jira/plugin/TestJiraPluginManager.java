package com.atlassian.jira.plugin;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.startup.JiraStartupPluginSystemListener;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.manager.PluginPersistentStateStore;
import com.mockobjects.dynamic.Mock;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @since v3.13 */
public class TestJiraPluginManager extends ListeningTestCase
{
    Mock mockPluginStateStore;
    Mock mockModuleDescriptorFactory;
    Mock mockPluginLoaderFactory;
    Mock mockPluginEventManager;
    Mock mockPluginPath;
    JiraStartupPluginSystemListener mockPluginSystemListener;
    private File installedPluginsDirectory;

    @Before
    public void setUp()
    {
        mockPluginStateStore = new Mock(PluginPersistentStateStore.class);
        mockModuleDescriptorFactory = new Mock(ModuleDescriptorFactory.class);

        mockPluginLoaderFactory = new Mock(PluginLoaderFactory.class);
        mockPluginLoaderFactory.expectAndReturn("getPluginLoaders", Collections.EMPTY_LIST);

        mockPluginEventManager = new Mock(PluginEventManager.class);

        mockPluginPath = new Mock(PluginPath.class);
        try
        {
            installedPluginsDirectory = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "installed-plugins");
            installedPluginsDirectory.createNewFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        mockPluginPath.expectAndReturn("getInstalledPluginsDirectory", installedPluginsDirectory);
        mockPluginSystemListener = EasyMock.createNiceMock(JiraStartupPluginSystemListener.class);
    }

    @After
    public void tearDown() throws Exception
    {
        installedPluginsDirectory.delete();
    }

    @Test
    public void testGetVersionsByKey()
    {
        final Mock mockPluginVersionStore = new Mock(PluginVersionStore.class);
        final List all = EasyList.build(new PluginVersionImpl("key1", "name1", "version1", new Date()), new PluginVersionImpl("key2", "name2",
            "version2", new Date()));
        mockPluginVersionStore.expectAndReturn("getAll", all);

        final JiraPluginManager jiraPluginManager = new JiraPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
            (PluginLoaderFactory) mockPluginLoaderFactory.proxy(), (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy(),
            (PluginVersionStore) mockPluginVersionStore.proxy(), (PluginEventManager) mockPluginEventManager.proxy(), (PluginPath) mockPluginPath.proxy(), mockPluginSystemListener);

        final Map versionsByKey = jiraPluginManager.getPluginVersionsByKey();

        assertEquals(2, versionsByKey.size());
        final List keys = new ArrayList(versionsByKey.keySet());
        Collections.sort(keys);
        assertTrue("key1".equals(keys.get(0)));
        assertTrue("key2".equals(keys.get(1)));
        mockPluginVersionStore.verify();
    }

    @Test
    public void testDeletePluginVersions()
    {
        final MockControl mockPluginVersionStoreControl = MockControl.createControl(PluginVersionStore.class);
        final PluginVersionStore mockPluginVersionStore = (PluginVersionStore) mockPluginVersionStoreControl.getMock();
        mockPluginVersionStore.delete(new Long(1));
        mockPluginVersionStoreControl.setReturnValue(true, 2);
        mockPluginVersionStoreControl.replay();

        final JiraPluginManager jiraPluginManager = new JiraPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
            (PluginLoaderFactory) mockPluginLoaderFactory.proxy(), (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy(),
            mockPluginVersionStore, (PluginEventManager) mockPluginEventManager.proxy(), (PluginPath) mockPluginPath.proxy(), mockPluginSystemListener);

        final List versionsToDelete = EasyList.build(new PluginVersionImpl(new Long(1), "key1", "name1", "version1", new Date()),
            new PluginVersionImpl(new Long(1), "key2", "name2", "version2", new Date()));
        jiraPluginManager.deletePluginVersions(versionsToDelete);

        mockPluginVersionStoreControl.verify();
    }

    @Test
    public void testStorePluginVersionCreateVersion()
    {
        final PluginVersionImpl pluginVersion2 = new PluginVersionImpl("key2", "name2", "version2", new Date());

        final AtomicBoolean createCalled = new AtomicBoolean(false);

        final PluginVersionStore mockPluginVersionStore = new PluginVersionStore()
        {
            public PluginVersion create(final PluginVersion pluginVersion)
            {
                createCalled.set(true);
                assertEquals(pluginVersion.getKey(), pluginVersion2.getKey());
                assertEquals(pluginVersion.getName(), pluginVersion2.getName());
                assertEquals(pluginVersion.getVersion(), pluginVersion2.getVersion());
                return pluginVersion;
            }

            public PluginVersion update(final PluginVersion pluginVersion)
            {
                return null;
            }

            public boolean delete(final Long pluginVersionId)
            {
                return false;
            }

            public PluginVersion getById(final Long pluginVersionId)
            {
                return null;
            }

            public List /*<PluginVersion>*/getAll()
            {
                return null;
            }
        };
        final JiraPluginManager jiraPluginManager = new JiraPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
            (PluginLoaderFactory) mockPluginLoaderFactory.proxy(), (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy(),
            mockPluginVersionStore, (PluginEventManager) mockPluginEventManager.proxy(), (PluginPath) mockPluginPath.proxy(), mockPluginSystemListener);

        final Mock mockPluginLoaderFactory = new Mock(PluginLoaderFactory.class);
        mockPluginLoaderFactory.expectAndReturn("getPluginLoaders", Collections.EMPTY_LIST);

        final Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.expectAndReturn("getKey", "key2");

        final PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("version2");
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);
        mockPlugin.expectAndReturn("getName", "name2");

        jiraPluginManager.storePluginVersion((Plugin) mockPlugin.proxy(), null);

        assertTrue(createCalled.get());
        mockPlugin.verify();
    }

    @Test
    public void testStorePluginVersionUpdateVersion()
    {
        final PluginVersionImpl pluginVersion1 = new PluginVersionImpl(new Long(1), "key1", "name1", "version1", new Date());

        final PluginVersionImpl pluginVersionUpdated = new PluginVersionImpl(new Long(1), "key1", "name1", "version3", new Date());

        final AtomicBoolean updateCalled = new AtomicBoolean(false);

        final PluginVersionStore mockPluginVersionStore = new PluginVersionStore()
        {
            public PluginVersion create(final PluginVersion pluginVersion)
            {
                return null;
            }

            public PluginVersion update(final PluginVersion pluginVersion)
            {
                updateCalled.set(true);
                assertEquals(pluginVersion.getKey(), pluginVersionUpdated.getKey());
                assertEquals(pluginVersion.getName(), pluginVersionUpdated.getName());
                assertEquals(pluginVersion.getVersion(), pluginVersionUpdated.getVersion());
                return pluginVersion;
            }

            public boolean delete(final Long pluginVersionId)
            {
                return false;
            }

            public PluginVersion getById(final Long pluginVersionId)
            {
                return null;
            }

            public List /*<PluginVersion>*/getAll()
            {
                return null;
            }
        };
        final JiraPluginManager jiraPluginManager = new JiraPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
            (PluginLoaderFactory) mockPluginLoaderFactory.proxy(), (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy(),
            mockPluginVersionStore, (PluginEventManager) mockPluginEventManager.proxy(), (PluginPath) mockPluginPath.proxy(), mockPluginSystemListener);
        final Mock mockPluginLoaderFactory = new Mock(PluginLoaderFactory.class);
        mockPluginLoaderFactory.expectAndReturn("getPluginLoaders", Collections.EMPTY_LIST);

        final Mock mockPlugin = new Mock(Plugin.class);
        mockPlugin.expectAndReturn("getKey", "key1");

        final PluginInformation pluginInfo = new PluginInformation();
        pluginInfo.setVersion("version3");
        mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);
        mockPlugin.expectAndReturn("getName", "name1");

        jiraPluginManager.storePluginVersion((Plugin) mockPlugin.proxy(), pluginVersion1);

        // Make sure the store's update was called
        assertTrue(updateCalled.get());
        mockPlugin.verify();
    }

    @Test
    public void testStorePluginVersions()
    {
        final AtomicBoolean deletePluginVersionsCalled = new AtomicBoolean(false);
        final AtomicBoolean getPluginsCalled = new AtomicBoolean(false);
        final AtomicBoolean getPluginsVersionsByKeyCalled = new AtomicBoolean(false);
        final AtomicBoolean storePluginVersionCalled = new AtomicBoolean(false);

        final JiraPluginManager jiraPluginManager = new JiraPluginManager((PluginPersistentStateStore) mockPluginStateStore.proxy(),
            (PluginLoaderFactory) mockPluginLoaderFactory.proxy(), (ModuleDescriptorFactory) mockModuleDescriptorFactory.proxy(), null,
            (PluginEventManager) mockPluginEventManager.proxy(), (PluginPath) mockPluginPath.proxy(), mockPluginSystemListener)
        {
            @Override
            void deletePluginVersions(final Collection pluginVersionsToDelete)
            {
                deletePluginVersionsCalled.set(true);
            }

            @Override
            public Collection getPlugins()
            {
                getPluginsCalled.set(true);
                final Mock mockPlugin = new Mock(Plugin.class);
                mockPlugin.expectAndReturn("getKey", "key2");
                final PluginInformation pluginInfo = new PluginInformation();
                pluginInfo.setVersion("version3");
                mockPlugin.expectAndReturn("getPluginInformation", pluginInfo);
                mockPlugin.expectAndReturn("getName", "name2");
                return EasyList.build(mockPlugin.proxy());
            }

            @Override
            Map getPluginVersionsByKey()
            {
                getPluginsVersionsByKeyCalled.set(true);
                final PluginVersionImpl pluginVersion1 = new PluginVersionImpl(new Long(1), "key1", "name1", "version1", new Date());
                final PluginVersionImpl pluginVersion2 = new PluginVersionImpl(new Long(1), "key2", "name2", "version2", new Date());
                return EasyMap.build("key1", pluginVersion1, "key2", pluginVersion2);
            }

            @Override
            void storePluginVersion(final Plugin plugin, final PluginVersion pluginVersion)
            {
                assertEquals("key2", plugin.getKey());
                storePluginVersionCalled.set(true);
            }
        };

        jiraPluginManager.storePluginVersions();
        assertTrue((deletePluginVersionsCalled.get()));
        assertTrue(getPluginsCalled.get());
        assertTrue(getPluginsVersionsByKeyCalled.get());
        assertTrue(storePluginVersionCalled.get());
    }
}
