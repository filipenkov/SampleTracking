package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockUser;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link UserMatcherPredicate}
 *
 * @since v5.0
 * @see UserMatcherPredicate
 */
public class TestUserMatcherPredicate extends ListeningTestCase
{
    @Test
    public void testUserMatch()
    {
        final User testUser = new MockUser("Tester", "FirstTest LastTest", "this.tester@atlassian.com");

        // These queries should match any user part
        assertTrue(matches(testUser, "Test", true));
        assertTrue(matches(testUser, "Tester", true));
        assertTrue(matches(testUser, "test", true));
        assertTrue(matches(testUser, "TEST", true));
        assertTrue(matches(testUser, "First", true));
        assertTrue(matches(testUser, "Last", true));
        assertTrue(matches(testUser, "LastTest", true));
        assertTrue(matches(testUser, "Lasttest", true));
        assertTrue(matches(testUser, "FirstTest LastTest", true));

        // These queries should not match any user part
        assertFalse(matches(testUser, "Testing", true));
        assertFalse(matches(testUser, "not", true));
        assertFalse(matches(testUser, "tester@atlassian.com", true));
        assertFalse(matches(testUser, "atlassian.com", true));
        assertFalse(matches(testUser, ".com", true));
        assertFalse(matches(testUser, "@", true));
        assertFalse(matches(testUser, "atlas", true));

        // These queries should match the email address only, and fail if it isn't searched
        assertTrue(matches(testUser, "this", true));
        assertFalse(matches(testUser, "this", false));
        assertTrue(matches(testUser, "this.tester", true));
        assertFalse(matches(testUser, "this.tester", false));
        assertTrue(matches(testUser, "this.tester@atlassian.com", true));
        assertFalse(matches(testUser, "this.tester@atlassian.com", false));
    }
    
    private boolean matches(User user, String query, boolean canMatchEmailAddress)
    {
        UserMatcherPredicate matcher = new UserMatcherPredicate(query, canMatchEmailAddress);
        return matcher.apply(user);
    }
}
