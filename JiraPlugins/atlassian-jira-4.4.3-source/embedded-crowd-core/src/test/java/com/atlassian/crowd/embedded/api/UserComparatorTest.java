package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.embedded.impl.ImmutableUser;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserComparatorTest
{
    @Test
    public void testComparator() throws Exception
    {
        List<User> users = new ArrayList<User>(3);
        users.add(ImmutableUser.newUser().name("Batman").toUser());
        users.add(ImmutableUser.newUser().name("west").toUser());
        users.add(ImmutableUser.newUser().name("adam").toUser());

        // test sorting is case-insensitive
        Collections.sort(users, UserComparator.USER_COMPARATOR);
        assertEquals("adam", users.get(0).getName());
        assertEquals("Batman", users.get(1).getName());
        assertEquals("west", users.get(2).getName());
    }

    @Test
    public void testComparatorWithDirectoryIDs() throws Exception
    {
        List<User> users = new ArrayList<User>(3);
        users.add(ImmutableUser.newUser().name("Batman").directoryId(3).toUser());
        users.add(ImmutableUser.newUser().name("Batman").directoryId(1).toUser());
        users.add(ImmutableUser.newUser().name("Batman").directoryId(2).toUser());
        users.add(ImmutableUser.newUser().name("west").directoryId(1).toUser());
        users.add(ImmutableUser.newUser().name("adam").toUser());

        // test sorting is case-insensitive and uses directoryID as tie-breaker
        Collections.sort(users, UserComparator.USER_COMPARATOR);
        // adam
        assertEquals("adam", users.get(0).getName());
        // Batman 1
        assertEquals("Batman", users.get(1).getName());
        assertEquals(1, users.get(1).getDirectoryId());
        // Batman 2
        assertEquals("Batman", users.get(2).getName());
        assertEquals(2, users.get(2).getDirectoryId());
        // Batman 3
        assertEquals("Batman", users.get(3).getName());
        assertEquals(3, users.get(3).getDirectoryId());
        // west
        assertEquals("west", users.get(4).getName());
    }

    @Test
    public void testEquals() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").emailAddress(null).active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();
        User user3 = ImmutableUser.newUser().directoryId(2L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();
        User user4 = ImmutableUser.newUser().directoryId(2L).name("Baron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();

        // Case-insensitive equals
        assertTrue(UserComparator.equal(user1, user2));
        // Different directories
        assertFalse(UserComparator.equal(user2, user3));
        // Different names
        assertFalse(UserComparator.equal(user3, user4));
        // Null tests
        assertTrue(UserComparator.equal(null, null));
        assertFalse(UserComparator.equal(null, user1));
        assertFalse(UserComparator.equal(user1, null));
    }

    @Test
    public void testHashCode() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").emailAddress(null).active(true).toUser();

        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Aaron Applebaum").emailAddress(null).active(false).toUser();

        // TODO: We should have a full definition of HashCode in the javadoc for the interface that all implementors must adhere to for compatibility
        assertEquals("aaron".hashCode() * 31 + 1, UserComparator.hashCode(user1));
        // Hashcode for user2 MUST be the same as user1
        assertEquals(UserComparator.hashCode(user1), UserComparator.hashCode(user2));
    }

    @Test
    public void testCompareToDifferentName() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Baron").displayName("Baron Aardvark").active(true).toUser();
        User user3 = ImmutableUser.newUser().directoryId(1L).name("karen").displayName("Karen Aardvark").active(true).toUser();

        // Case-insensitive compare
        assertTrue(UserComparator.compareTo(user1, user2) < 0);
        assertTrue(UserComparator.compareTo(user2, user1) > 0);
        assertTrue(UserComparator.compareTo(user2, user3) < 0);
        assertTrue(UserComparator.compareTo(user3, user2) > 0);
    }

    @Test
    public void testCompareToSameName() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(1L).name("Aaron").displayName("Fred").active(true).toUser();

        // Case-insensitive compare
        assertTrue(UserComparator.compareTo(user1, user2) == 0);
    }

    @Test
    public void testCompareToSameNameDifferentDirectory() throws Exception
    {
        User user1 = ImmutableUser.newUser().directoryId(1L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user2 = ImmutableUser.newUser().directoryId(2L).name("Aaron").displayName("Aaron Aardvark").active(true).toUser();
        User user3 = ImmutableUser.newUser().directoryId(3L).name("aaron").displayName("Aaron Aardvark").active(true).toUser();

        // Should use the directoryId as the tie-breaker
        assertTrue(UserComparator.compareTo(user1, user2) < 0);
        assertTrue(UserComparator.compareTo(user2, user1) > 0);
        assertTrue(UserComparator.compareTo(user2, user3) < 0);
        assertTrue(UserComparator.compareTo(user3, user2) > 0);
    }

    /**
     * Tests that {@link UserComparator#equalsObject(User, Object)} returns true when the username and directory ID
     * are equal.
     */
    @Test
    public void testEqualsObject() throws Exception {
        final String USER_NAME = "username";
        final long DIRECTORY_ID = 2L;

        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);
        com.atlassian.crowd.embedded.api.User user2 = mock(com.atlassian.crowd.embedded.api.User.class);

        when(user1.getName()).thenReturn(USER_NAME);
        when(user1.getDirectoryId()).thenReturn(DIRECTORY_ID);

        when(user2.getName()).thenReturn(USER_NAME);
        when(user2.getDirectoryId()).thenReturn(DIRECTORY_ID);

        assertTrue(UserComparator.equalsObject(user1, user2));
        verify(user1, Mockito.never()).getEmailAddress();
        verify(user1, Mockito.never()).getDisplayName();
        verify(user2, Mockito.never()).getEmailAddress();
        verify(user2, Mockito.never()).getDisplayName();
    }

    @Test
    public void testEqualsObject_DifferentDirectoryId() throws Exception {
        final String USER_NAME = "username";
        final long DIRECTORY_ID = 2L;
        final long OTHER_DIRECTORY_ID = 3L;

        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);
        com.atlassian.crowd.embedded.api.User user2 = mock(com.atlassian.crowd.embedded.api.User.class);

        when(user1.getName()).thenReturn(USER_NAME);
        when(user1.getDirectoryId()).thenReturn(DIRECTORY_ID);

        when(user2.getName()).thenReturn(USER_NAME);
        when(user2.getDirectoryId()).thenReturn(OTHER_DIRECTORY_ID);

        assertFalse(UserComparator.equalsObject(user1, user2));
        verify(user1, Mockito.never()).getEmailAddress();
        verify(user1, Mockito.never()).getDisplayName();
        verify(user2, Mockito.never()).getEmailAddress();
        verify(user2, Mockito.never()).getDisplayName();
    }

    @Test
    public void testEqualsObject_DifferentName() throws Exception {
        final String USER_NAME = "username";
        final String OTHER_USER_NAME = "othername";
        final long DIRECTORY_ID = 2L;
        final long OTHER_DIRECTORY_ID = 3L;

        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);
        com.atlassian.crowd.embedded.api.User user2 = mock(com.atlassian.crowd.embedded.api.User.class);

        when(user1.getName()).thenReturn(USER_NAME);
        when(user1.getDirectoryId()).thenReturn(DIRECTORY_ID);

        when(user2.getName()).thenReturn(OTHER_USER_NAME);
        when(user2.getDirectoryId()).thenReturn(DIRECTORY_ID);

        assertFalse(UserComparator.equalsObject(user1, user2));
        verify(user1, Mockito.never()).getEmailAddress();
        verify(user1, Mockito.never()).getDisplayName();
        verify(user2, Mockito.never()).getEmailAddress();
        verify(user2, Mockito.never()).getDisplayName();
    }

    /**
     * Tests that an embedded User can be equivalent to a model User.
     */
    @Test
    public void testEqualsObject_ModelUser() throws Exception {
        final String USER_NAME = "username";
        final long DIRECTORY_ID = 2L;

        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);
        com.atlassian.crowd.model.user.User user2 = mock(com.atlassian.crowd.model.user.User.class);

        when(user1.getName()).thenReturn(USER_NAME);
        when(user1.getDirectoryId()).thenReturn(DIRECTORY_ID);

        when(user2.getName()).thenReturn(USER_NAME);
        when(user2.getDirectoryId()).thenReturn(DIRECTORY_ID);

        assertTrue(UserComparator.equalsObject(user1, user2));
        verify(user1, Mockito.never()).getEmailAddress();
        verify(user1, Mockito.never()).getDisplayName();
        verify(user2, Mockito.never()).getEmailAddress();
        verify(user2, Mockito.never()).getDisplayName();
    }

    /**
     * Tests that comparing a null object will return false
     */
    @Test
    public void testEqualsObject_Null() throws Exception {
        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);

        assertFalse(UserComparator.equalsObject(user1, null));
        assertFalse(UserComparator.equalsObject(null, user1));
    }

    /**
     * Tests that comparing against an object that is not a User will return false.
     */
    @Test
    public void testEqualsObject_NonUser() throws Exception {
        final String USER_NAME = "username";
        final long DIRECTORY_ID = 2L;

        com.atlassian.crowd.embedded.api.User user1 = mock(com.atlassian.crowd.embedded.api.User.class);

        assertFalse(UserComparator.equalsObject(user1, new Object()));
    }
}
