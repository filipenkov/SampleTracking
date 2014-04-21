package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCachingTrustedApplicationManager extends ListeningTestCase
{

    public static void main(final String[] args)
    {
        final Collection datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        datas.add(new MockTrustedApplicationData(2, "anotherApplicationId", "name", 0, "192.168.0.*", "urlMatch"));

        //        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        final TrustedApplicationManager manager = new CachingTrustedApplicationManager(new DefaultTrustedApplicationManager(
            new MockTrustedApplicationStore(datas)), null);

        // warm up pass
        for (int i = 0; i < 100000; i++)
        {
            manager.get(1);
        }

        final long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++)
        {
            manager.get(1);
        }

        final long end = System.currentTimeMillis();
        System.out.println("Test took: " + (end - start));
    }

    class CountingTrustedApplicationManager extends MockTrustedApplicationManager
    {
        final AtomicInteger countGetAll = new AtomicInteger(0);
        final AtomicInteger countStore = new AtomicInteger(0);
        final AtomicInteger countRemove = new AtomicInteger(0);

        public CountingTrustedApplicationManager(final Collection datas)
        {
            super(datas);
        }

        @Override
        public Set getAll()
        {
            countGetAll.incrementAndGet();
            return super.getAll();
        }

        @Override
        public TrustedApplicationInfo store(final User user, final TrustedApplicationInfo data)
        {
            countStore.incrementAndGet();
            return super.store(user, data);
        }

        @Override
        public boolean delete(final User user, final long id)
        {
            countRemove.incrementAndGet();
            return super.delete(user, id);
        }
    }

    @Test
    public void testGetAllOnlyCalledOnce() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(new ArrayList());

        final CachingTrustedApplicationManager cachedManager = new CachingTrustedApplicationManager(countingManager, null);
        assertNotNull(cachedManager.getAll());

        for (int i = 0; i < 100; i++)
        {
            assertEquals(0, cachedManager.getAll().size());
        }

        assertEquals(1, countingManager.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataAdded() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(new ArrayList());

        final CachingTrustedApplicationManager cachedManager = new CachingTrustedApplicationManager(countingManager, null);
        assertNotNull(cachedManager.getAll());
        assertEquals(0, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());

        final TrustedApplicationInfo data = cachedManager.store(getUser(), new MockTrustedApplicationInfo(0, "CONF", "confluence", 1000));
        assertNotNull(data);
        assertEquals(1, data.getNumericId());

        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataUpdated() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            EasyList.build(new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000)));

        final CachingTrustedApplicationManager cachedManager = new CachingTrustedApplicationManager(countingManager, null);
        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());

        cachedManager.store(getUser(), new MockTrustedApplicationInfo(1, "CONF", "confluence", 7654));

        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());

        final TrustedApplicationInfo data = cachedManager.get(1);
        assertEquals(1, data.getNumericId());
        assertEquals("confluence", data.getName());
        assertEquals(7654, data.getTimeout());
    }

    @Test
    public void testGetAllOnlyCalledTwiceWhenDataRemoved() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            EasyList.build(new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000)));

        final CachingTrustedApplicationManager cachedManager = new CachingTrustedApplicationManager(countingManager, null);
        assertNotNull(cachedManager.getAll());
        assertEquals(1, cachedManager.getAll().size());
        assertEquals(1, countingManager.countGetAll.get());
        assertEquals(0, countingManager.countStore.get());

        cachedManager.delete(getUser(), 1);

        assertNotNull(cachedManager.getAll());
        assertEquals(0, cachedManager.getAll().size());
        assertEquals(2, countingManager.countGetAll.get());
        assertEquals(0, countingManager.countStore.get());
        assertEquals(1, countingManager.countRemove.get());

        assertNull(cachedManager.get("CONF"));
    }

    @Test
    public void testAlternateGetsReturnSameObject() throws Exception
    {
        final CountingTrustedApplicationManager countingManager = new CountingTrustedApplicationManager(
            EasyList.build(new MockTrustedApplicationInfo(1, "CONF", "confluencing", 1000)));

        final CachingTrustedApplicationManager cachedManager = new CachingTrustedApplicationManager(countingManager, null);

        final TrustedApplicationInfo info1 = cachedManager.get(1);
        final TrustedApplicationInfo info2 = cachedManager.get("CONF");
        assertSame(info1, info2);
    }

    private User getUser()
    {
        return new MockUser("TestCachingTrustedApplicationManager");
    }
}
