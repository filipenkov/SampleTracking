package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.ofbiz.core.entity.GenericDelegator;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestDefaultTrustedApplicationStore extends LegacyJiraMockTestCase
{
    protected void tearDown() throws Exception
    {
        // make sure our table is empty when done
        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final List all = genericDelegator.findAll(TrustedApplicationStore.ENTITY_NAME);
        genericDelegator.removeAll(all);
        super.tearDown();
      }

    public void testStore()
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        final TrustedApplicationStore store = new DefaultTrustedApplicationStore(ofBizDelegator);

        TrustedApplicationData input = new TrustedApplicationData("someAppId", "someName", "somePublicKey", 54321, new AuditLog("me", new Date(10)), new AuditLog("you", new Date(20)), "192.*", "/some/other/url");
        store.store(input);
        TrustedApplicationData output = store.getByApplicationId("someAppId");
        assertTrustedApplicationInfoEquivalent(input, output);

        // test updating
        input = new TrustedApplicationData(output.getId(), "someAppId", "newName", "newPublicKey", 98765, new AuditLog("newMe", new Date(30)), new AuditLog("newYou", new Date(40)), "255.*", "/new/url");
        store.store(input);
        output = store.getByApplicationId("someAppId");
        assertTrustedApplicationInfoEquivalent(input, output);
        output = store.getById(output.getId());
        assertTrustedApplicationInfoEquivalent(input, output);

        input = new TrustedApplicationData("someOtherAppId", "anotherName", "someOtherPublicKey", 12345, new AuditLog("oldMe", new Date(50)), new AuditLog("newYou", new Date(60)), "251.*", "/old/url");

        store.store(input);
        output = store.getByApplicationId("someOtherAppId");
        assertTrustedApplicationInfoEquivalent(input, output);
        output = store.getById(output.getId());
        assertTrustedApplicationInfoEquivalent(input, output);

        Set all = store.getAll();
        assertEquals(2, all.size());
        for (Iterator it = all.iterator(); it.hasNext();)
        {
            assertTrue(it.next() instanceof TrustedApplicationData);
        }

        assertFalse(store.delete(99));

        assertTrue(store.delete(1));
        all = store.getAll();
        assertEquals(1, all.size());
        output = store.getByApplicationId("someAppId");
        assertNull(output);
        output = store.getById(1);
        assertNull(output);

        assertTrue(store.delete(2));
        all = store.getAll();
        assertEquals(0, all.size());
        output = store.getByApplicationId("someOtherAppId");
        assertNull(output);
        output = store.getById(2);
        assertNull(output);
    }

    private void assertTrustedApplicationInfoEquivalent(TrustedApplicationData input, TrustedApplicationData output)
    {
        assertNotNull(input);
        assertNotNull(output);
        assertEquals(input.getApplicationId(), output.getApplicationId());
        assertEquals(input.getName(), output.getName());
        assertEquals(input.getPublicKey(), output.getPublicKey());
        assertEquals(input.getTimeout(), output.getTimeout());
        assertEquals(input.getIpMatch(), output.getIpMatch());
        assertEquals(input.getUrlMatch(), output.getUrlMatch());
        assertNotNull(input.getCreated());
        assertNotNull(output.getCreated());
        assertEquals(input.getCreated().getWho(), output.getCreated().getWho());
        assertEquals(input.getCreated().getWhen(), output.getCreated().getWhen());
        assertNotNull(input.getUpdated());
        assertNotNull(output.getUpdated());
        assertEquals(input.getUpdated().getWho(), output.getUpdated().getWho());
        assertEquals(input.getUpdated().getWhen(), output.getUpdated().getWhen());
    }
}