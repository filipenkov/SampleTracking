package com.atlassian.jira.portal;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import org.easymock.MockControl;

import java.util.Collections;
import java.util.List;
import java.net.URI;

/**
 * Test for the {@link PortletConfigurationStore}.
 *
 * @since 4.0
 */
public class TestCachingPortletConfigurationStore extends ListeningTestCase
{
    private static final Long CONFIG1_ID = new Long(10);
    private static final Long PAGE1_ID = new Long(99);
    private static final String CONFIG1_KEY = "portlet_key";
    private static final String CONFIG2_KEY = "portlet_key2";
    private static final Long CONFIG2_ID = new Long(11);
    private static final Long PAGE2_ID = new Long(991);

    private MockControl delegateStoreControl;
    private PortletConfigurationStore delegateStore;
    private PortletConfigurationImpl config1;
    private PortletConfigurationImpl config2;

    @Before
    public void setUp() throws Exception
    {
        delegateStoreControl = MockControl.createStrictControl(PortletConfigurationStore.class);
        delegateStore = (PortletConfigurationStore) delegateStoreControl.getMock();
        config1 = new PortletConfigurationImpl(CONFIG1_ID, PAGE1_ID, CONFIG1_KEY, null, new Integer(1), new Integer(2), null, null, null, Collections.<String, String>emptyMap());
        config2 = new PortletConfigurationImpl(CONFIG2_ID, PAGE2_ID, CONFIG2_KEY, null, new Integer(1), new Integer(2), null, null, null, Collections.<String, String>emptyMap());
    }

    /**
     * Make sure the cache returns the correct portlet configuration.
     */
    @Test
    public void testGetByPortletId()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortletId(CONFIG2_ID);
        delegateStoreControl.setReturnValue(config2);

        PortletConfigurationStore store = createCachingStore();

        //this should call through.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should call through.
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        verifyMocks();
    }

    /**
     * Make sure it can handle invalid portletIds
     */
    @Test
    public void testGetByPortletIdBadId()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(null);

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(null);

        PortletConfigurationStore store = createCachingStore();

        //this should call through.
        assertNull(store.getByPortletId(CONFIG1_ID));
        assertNull(store.getByPortletId(CONFIG1_ID));

        verifyMocks();
    }

    /**
     * Does the delete work. A delete should clear the cache of any related entities, including the page cache.
     */
    @Test
    public void testDelete()
    {
        final List expectedList = EasyList.build(config2);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(EasyList.build(config1, config2));

        delegateStore.delete(config1);

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortalPage(PAGE1_ID);

        store.delete(config1);

        //this should call through.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should call through.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));


        verifyMocks();
    }

    /**
     * Does the store work. A store should clear the cache of any related entities, including the page cache.
     */
    @Test
    public void testStore()
    {
        final List expectedList = EasyList.build(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        delegateStore.store(config2);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortalPage(PAGE1_ID);

        store.store(config2);

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));

        //this should still be cached because PAGE1 did not contain the portlet configuration
        //that was deleted.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        verifyMocks();
    }

    /**
     * Tests moving a portlet from one page to another.  Both page ids should be removed from cache.
     */
    @Test
    public void testMove()
    {
        final List expectedList = EasyList.build(config1, config2);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        delegateStore.getByPortalPage(PAGE2_ID);
        delegateStoreControl.setReturnValue(null);

        delegateStore.store(config2);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        delegateStore.getByPortalPage(PAGE2_ID);
        delegateStoreControl.setReturnValue(null);


        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);

        store.store(config2);

        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(Collections.EMPTY_LIST, store.getByPortalPage(PAGE2_ID));

        //these should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(Collections.EMPTY_LIST, store.getByPortalPage(PAGE2_ID));

        verifyMocks();
    }


    @Test
    public void testAdd()
    {
        final List expectedList = EasyList.build(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        addConfiguration(config1, delegateStore);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        addConfiguration(config2, delegateStore);
        delegateStoreControl.setReturnValue(config2);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortalPage(PAGE1_ID);

        assertEqualsButNotSame(config1, addConfiguration(config1, store));

        //this should call through to the store since we added a page 
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this call to store should not delete the cache for PAGE1 since the added
        //portlet is not on that PAGE.
        assertEqualsButNotSame(config2, addConfiguration(config2, store));

        //these should be cached.
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config1, store.getByPortletId(CONFIG1_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));

        verifyMocks();
    }

    @Test
    public void testGetByPortalPage()
    {
        final List expectedList = EasyList.build(config1, config2);

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(config2, store.getByPortletId(CONFIG2_ID));

        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        verifyMocks();
    }

    @Test
    public void testGetByPortalPageNullFromDB()
    {
        final List expectedList = Collections.EMPTY_LIST;

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(null);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        verifyMocks();
    }

    @Test
    public void testGetByPortalPageStoreReturn()
    {
        final List expectedList = EasyList.build(config1, config2);

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1);

        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(expectedList);

        PortletConfigurationStore store = createCachingStore();

        //prime the cache.
        store.getByPortletId(CONFIG1_ID);

        //this should call through to the store.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        //this should be cached.
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));
        assertEqualsButNotSame(expectedList, store.getByPortalPage(PAGE1_ID));

        verifyMocks();
    }  

    @Test
    public void testUpdateGadgetColor()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);
        delegateStore.updateGadgetColor(CONFIG1_ID, Color.color3);

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);

        PortletConfigurationStore store = createCachingStore();
        //first warm up the cache
        PortletConfiguration config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());
        //second time all should come from the cache
        config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());

        //then change color.  The cached entry should have been removed
        store.updateGadgetColor(CONFIG1_ID, Color.color3);

        //this will be the second call to the delegate store.
        config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());

        verifyMocks();
    }

    @Test
    public void testUpdateGadgetUserPrefs()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);
        delegateStore.updateUserPrefs(CONFIG1_ID, MapBuilder.<String, String>newBuilder().add("pref1", "value1").toMap());

        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);

        PortletConfigurationStore store = createCachingStore();
        //first warm up the cache
        PortletConfiguration config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());
        //second time all should come from the cache
        config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());

        //then change user prefs.  The cached entry should have been removed
        store.updateUserPrefs(CONFIG1_ID, MapBuilder.<String, String>newBuilder().add("pref1", "value1").toMap());

        //this will be the second call to the delegate store.
        config1 = store.getByPortletId(CONFIG1_ID);
        assertEquals(config1.getId(), config1.getId());

        verifyMocks();
    }

    @Test
    public void testUpdateGadgetPositionNotCached()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);
        delegateStore.updateGadgetPosition(CONFIG1_ID, 0, 0, PAGE1_ID);

        PortletConfigurationStore store = createCachingStore();

        store.updateGadgetPosition(CONFIG1_ID, 0, 0, PAGE1_ID);

        verifyMocks();
    }

    @Test
    public void testUpdateGadgetPositionCached()
    {
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);
        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(CollectionBuilder.newBuilder(config1).asList(), 1);
        delegateStore.getByPortalPage(PAGE2_ID);
        delegateStoreControl.setReturnValue(CollectionBuilder.newBuilder(config2).asList(), 1);
        delegateStore.updateGadgetPosition(CONFIG1_ID, 0, 2, PAGE2_ID);
        delegateStore.getByPortletId(CONFIG1_ID);
        delegateStoreControl.setReturnValue(config1, 1);
        delegateStore.getByPortalPage(PAGE1_ID);
        delegateStoreControl.setReturnValue(CollectionBuilder.newBuilder().asList(), 1);
        delegateStore.getByPortalPage(PAGE2_ID);
        delegateStoreControl.setReturnValue(CollectionBuilder.newBuilder(config1, config2).asList(), 1);

        PortletConfigurationStore store = createCachingStore();

        //first warm up the cache
        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);
        //second time round all should come from the cache
        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);

        //now move the gadget to another dashboard, all caches should have been cleared
        store.updateGadgetPosition(CONFIG1_ID, 0, 2, PAGE2_ID);

        store.getByPortletId(CONFIG1_ID);
        store.getByPortalPage(PAGE1_ID);
        store.getByPortalPage(PAGE2_ID);

        verifyMocks();
    }

    private void verifyMocks()
    {
        delegateStoreControl.verify();
    }

    private void assertEqualsButNotSame(List /*<PortletConfiguration>*/ expectedList, List /*<PortletConfiguration>*/ actualList)
    {
        assertEquals("Configuration lists have diferent size.", expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++)
        {
            assertEqualsButNotSame((PortletConfiguration) expectedList.get(i), (PortletConfiguration) actualList.get(i));
        }
    }

    private void assertEqualsButNotSame(PortletConfiguration configuration1, PortletConfiguration configuration2)
    {
        assertNotSame(configuration1, configuration2);

        assertEquals(configuration1.getColumn(), configuration2.getColumn());
        assertEquals(configuration1.getId(), configuration2.getId());
        assertEquals(configuration1.getDashboardPageId(), configuration2.getDashboardPageId());
        assertEquals(configuration1.getKey(), configuration2.getKey());
        assertEquals(configuration1.getRow(), configuration2.getRow());
    }

    private PortletConfigurationStore createCachingStore()
    {
        delegateStoreControl.replay();

        return new CachingPortletConfigurationStore(delegateStore, null)
        {
            ///CLOVER:OFF
            PropertySet clonePropertySet(PropertySet srcPropertySet)
            {
                return srcPropertySet;
            }
        };
    }

    private static PortletConfiguration addConfiguration(PortletConfiguration configuration, PortletConfigurationStore store)
    {
        return store.addLegacyPortlet(configuration.getDashboardPageId(), null, configuration.getColumn(), configuration.getRow(), URI.create("rest/legacy/" + configuration.getKey() + ".xml"), Color.color1, Collections.<String,String>emptyMap(), configuration.getKey());
    }
}
