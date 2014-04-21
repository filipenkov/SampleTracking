package com.atlassian.security.auth.trustedapps;

import junit.framework.TestCase;

public class TestUIDGenerator extends TestCase
{
    public void testUidSanity()
    {
        String uid1 = UIDGenerator.generateUID();
        String uid2 = UIDGenerator.generateUID();

        assertEquals("UID length", 32,  uid1.length());
        assertEquals("UID length", 32,  uid2.length());

        assertFalse("UIDs are unique", uid1.equals(uid2));
    }
}
