/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.opensymphony.user.User;

import java.security.*;
import java.util.*;

public class TestDefaultTrustedApplicationManager extends ListeningTestCase
{
    @Test
    public void testNullInCtorThrows()
    {
        try
        {
            new DefaultTrustedApplicationManager(null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testGetAllForMultiples()
    {
        Collection datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        datas.add(new MockTrustedApplicationData(2, "anotherApplicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        final Set allInfos = manager.getAll();
        assertEquals(2, allInfos.size());
        for (Iterator it = allInfos.iterator(); it.hasNext();)
        {
            TrustedApplicationInfo info = (TrustedApplicationInfo) it.next();
            assertNotNull(info);
            assertNotNull(info.getPublicKey());
            assertNotNull(info.getID());
        }
    }

    @Test
    public void testGetBy()
    {
        Collection datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        TrustedApplicationInfo info = manager.get(1);
        assertNotNull(info);
        assertNotNull(info.getPublicKey());
        assertEquals("name", info.getName());
        assertEquals("applicationId", info.getID());
        assertEquals("192.168.0.*", info.getIpMatch());
        assertEquals("urlMatch", info.getUrlMatch());
        assertEquals(0, info.getTimeout());

        info = manager.get("applicationId");
        assertNotNull(info);
        assertNotNull(info.getPublicKey());
        assertEquals("name", info.getName());
        assertEquals("applicationId", info.getID());
        assertEquals("192.168.0.*", info.getIpMatch());
        assertEquals("urlMatch", info.getUrlMatch());
        assertEquals(0, info.getTimeout());
    }

    @Test
    public void testGetNotExists()
    {
        Collection datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        TrustedApplicationInfo info = manager.get(2);
        assertNull(info);

        info = manager.get("someApplicationId");
        assertNull(info);
    }

    @Test
    public void testDelete()
    {
        Collection datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));

        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(new MockTrustedApplicationStore(datas));
        assertEquals(1, manager.getAll().size());
        assertTrue(manager.delete(new User("test", new MockProviderAccessor(), new MockCrowdService()), 1));
        assertEquals(0, manager.getAll().size());
    }

    @Test
    public void testStore()
    {
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(Collections.EMPTY_LIST);
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        User createUser = new User("createUser", new MockProviderAccessor(), new MockCrowdService());
        Date testStart = new Date();

        TrustedApplicationInfo info = manager.store(createUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(0, "appId", "name", 1000)).toInfo());
        assertNotNull(info);
        assertEquals(1, manager.getAll().size());
        assertEquals(1, store.getAll().size());

        AuditLog created = store.getByApplicationId("appId").getCreated();
        assertNotNull(created);
        assertEquals("createUser", created.getWho());
        assertNotNull(created.getWhen());
        assertTrue(testStart.getTime() <= created.getWhen().getTime());

        AuditLog updated = store.getByApplicationId("appId").getUpdated();
        assertNotNull(updated);
        assertEquals("createUser", updated.getWho());
        assertNotNull(updated.getWhen());
        assertEquals(created.getWhen().getTime(), updated.getWhen().getTime());

        sleep(16);

        User updateUser = new User("updateUser", new MockProviderAccessor(), new MockCrowdService());
        TrustedApplicationInfo newInfo = manager.store(updateUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(info.getNumericId(), "appId", "name", 1000)).toInfo());
        assertNotNull(newInfo);
        assertEquals(1, manager.getAll().size());
        assertEquals(1, store.getAll().size());

        created = store.getByApplicationId("appId").getCreated();
        assertNotNull(created);
        assertEquals("createUser", created.getWho());
        assertNotNull(created.getWhen());
        assertTrue(testStart.getTime() <= created.getWhen().getTime());

        updated = store.getByApplicationId("appId").getUpdated();
        assertNotNull(updated);
        assertEquals("updateUser", updated.getWho());
        assertNotNull(updated.getWhen());
        assertTrue(created.getWhen().getTime() < updated.getWhen().getTime());
    }

    @Test
    public void testStoreNonExistentApp()
    {
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(Collections.EMPTY_LIST);
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        User createUser = new User("createUser", new MockProviderAccessor(), new MockCrowdService());
        try
        {
            manager.store(createUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(1, "appId", "name", 1000)).toInfo());
            fail("Can't update a nonexistent entity, should have thrown IllegalArg");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testStoreExistingAppWithDifferentAppId()
    {
        List datas = new ArrayList();
        datas.add(new MockTrustedApplicationData(1, "applicationId", "name", 0, "192.168.0.*", "urlMatch"));
        final MockTrustedApplicationStore store = new MockTrustedApplicationStore(datas);
        TrustedApplicationManager manager = new DefaultTrustedApplicationManager(store);
        User createUser = new User("createUser", new MockProviderAccessor(), new MockCrowdService());

        try
        {
            manager.store(createUser, new TrustedApplicationBuilder().set(new MockTrustedApplicationData(1, "appId", "name", 1000)).toInfo());
            fail("Can't change an existing entity's applicationId, should have thrown IllegalArg");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    static void sleep(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}

class MockTrustedApplicationData extends TrustedApplicationData
{
    MockTrustedApplicationData(long id, String applicationId, String name, int timeout)
    {
        this(id, applicationId, name, timeout, null, null);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, int timeout, String ipMatch, String urlMatch)
    {
        this(id, applicationId, name, timeout, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), ipMatch, urlMatch);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, int timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        this(id, applicationId, name, KeyUtil.generateNewKeyPair("RSA").getPublic(), timeout, created, updated, ipMatch, urlMatch);
    }

    MockTrustedApplicationData(long id, String applicationId, String name, PublicKey publicKey, int timeout, AuditLog created, AuditLog updated, String ipMatch, String urlMatch)
    {
        super(id, applicationId, name, KeyFactory.encode(publicKey), timeout, created, updated, ipMatch, urlMatch);
    }
}
