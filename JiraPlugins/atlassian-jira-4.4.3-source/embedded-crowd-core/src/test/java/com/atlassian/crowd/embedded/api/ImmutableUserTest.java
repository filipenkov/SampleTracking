package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import org.junit.Test;

import static org.junit.Assert.*;

public class ImmutableUserTest
{
    @SuppressWarnings ({ "EqualsBetweenInconvertibleTypes", "ObjectEqualsNull" })
    @Test
    public void testEquals() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").emailAddress(null).active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();
        User user3 = ImmutableUser.newUser().directoryId(2L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();
        User user4 = ImmutableUser.newUser().directoryId(2L).name("Baron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();

        // Case-insensitive equals
        assertTrue(user1.equals(user2));
        // Different directories
        assertFalse(user2.equals(user3));
        // Different names
        assertFalse(user3.equals(user4));
        // Different Objects
        assertFalse(user1.equals(null));
        assertFalse(user1.equals(new Integer(1)));
        assertFalse(user1.equals(new ImmutableGroup("aaron")));
    }

    @Test
    public void testHashCode() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").emailAddress(null).active(true).toUser();

        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();

        assertEquals("aaron".hashCode() * 31 + 1, user1.hashCode());
        // Hashcode for user2 MUST be the same as user1
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testCompareToDifferentName() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Baron").displayName("Baron Aardvark").active(true).toUser();
        User user3 = ImmutableUser.newUser().directoryId(1L).name("karen").displayName("Karen Aardvark").active(true).toUser();

        // Case-insensitive compare
        assertTrue(user1.compareTo(user2) < 0);
        assertTrue(user2.compareTo(user1) > 0);
        assertTrue(user2.compareTo(user3) < 0);
        assertTrue(user3.compareTo(user2) > 0);
    }

    @Test
    public void testCompareToSameName() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Fred").active(true).toUser();

        // Case-insensitive compare
        assertTrue(user1.compareTo(user2) == 0);
    }

    @Test
    public void testCompareToSameNameDifferentDirectory() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(2L).name("Aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user3 = ImmutableUser.newUser().directoryId(3L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();

        // Should use the directoryId as the tie-breaker
        assertTrue(user1.compareTo(user2) < 0);
        assertTrue(user2.compareTo(user1) > 0);
        assertTrue(user2.compareTo(user3) < 0);
        assertTrue(user3.compareTo(user2) > 0);
    }

    @Test
    public void testNewUser() throws Exception
    {
        User user = ImmutableUser.newUser().name("hsmith").active(true).displayName("Hayley Smith")
                .emailAddress("hayley@example.com").directoryId(20L).toUser();
        assertEquals(20, user.getDirectoryId());
        assertEquals("hsmith", user.getName());
        assertEquals(true, user.isActive());
        assertEquals("Hayley Smith", user.getDisplayName());
        assertEquals("hayley@example.com", user.getEmailAddress());
    }

    @Test
    public void testNewUserClone() throws Exception
    {
        // Create a user
        User user1 = ImmutableUser.newUser().name("hsmith").active(true).displayName("Hayley Smith")
                .emailAddress("hayley@example.com").directoryId(20L).toUser();
        // Clone the user
        User user2 = ImmutableUser.newUser(user1).toUser();

        assertEquals(20, user2.getDirectoryId());
        assertEquals("hsmith", user2.getName());
        assertEquals(true, user2.isActive());
        assertEquals("Hayley Smith", user2.getDisplayName());
        assertEquals("hayley@example.com", user2.getEmailAddress());
    }
}