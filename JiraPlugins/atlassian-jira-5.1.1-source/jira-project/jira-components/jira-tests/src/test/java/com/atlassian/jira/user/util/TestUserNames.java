package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;

public class TestUserNames extends TestCase
{
    public void testUsernameUser()
    {
        assertTrue(UserNames.equal(null, null));
        assertFalse(UserNames.equal("dude", null));
        assertFalse(UserNames.equal(null, new MockUser("fred")));
        assertFalse(UserNames.equal("bob", new MockUser("fred")));
        assertTrue(UserNames.equal("fred", new MockUser("fred")));
    }
    
    public void testCaseInsensitive()
    {
        assertTrue(UserNames.equal("fred", new MockUser("fred")));
        assertTrue(UserNames.equal("Fred", new MockUser("Fred")));
        assertTrue(UserNames.equal("FRED", new MockUser("fred")));
        assertTrue(UserNames.equal("fred", new MockUser("FRED")));
        assertTrue(UserNames.equal("fRed", new MockUser("frEd")));
    }

    public void testToKey() throws Exception
    {
        assertNull(UserNames.toKey(null));
        assertEquals(IdentifierUtils.toLowerCase("Something"),  UserNames.toKey("Something"));
        assertEquals(IdentifierUtils.toLowerCase("something"),  UserNames.toKey("something"));
    }
}
