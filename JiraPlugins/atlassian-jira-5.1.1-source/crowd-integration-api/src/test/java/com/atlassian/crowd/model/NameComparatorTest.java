package com.atlassian.crowd.model;

import java.util.Comparator;

import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;

import com.google.common.base.Function;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NameComparatorTest
{
    @Test
    public void comparatorOfReturnsComparatorForStrings()
    {
        Comparator<String> sc = NameComparator.of(String.class);
        assertEquals(0, sc.compare("a", "a"));
        assertEquals(-1, sc.compare("a", "b"));
        assertEquals(1, sc.compare("b", "a"));
    }

    @Test
    public void comparatorOfReturnsComparatorForUsers()
    {
        Comparator<User> nc = NameComparator.of(User.class);
        User userA = mock(User.class);
        when(userA.getName()).thenReturn("a");
        User userB = mock(User.class);
        when(userB.getName()).thenReturn("b");
        assertEquals(0, nc.compare(userA, userA));
        assertEquals(-1, nc.compare(userA, userB));
        assertEquals(1, nc.compare(userB, userA));
    }

    @Test
    public void comparatorOfReturnsComparatorForGroups()
    {
        Comparator<Group> nc = NameComparator.of(Group.class);
        Group groupA = mock(Group.class);
        when(groupA.getName()).thenReturn("a");
        Group groupB = mock(Group.class);
        when(groupB.getName()).thenReturn("b");
        assertEquals(0, nc.compare(groupA, groupA));
        assertEquals(-1, nc.compare(groupA, groupB));
        assertEquals(1, nc.compare(groupB, groupA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void comparatorOfFailsForUnknownTypes()
    {
        NameComparator.of(Object.class);
    }

    @Test
    public void normaliserOfReturnsNormalisersForStrings()
    {
        Function<String, String> ns = NameComparator.normaliserOf(String.class);
        assertEquals("a", ns.apply("A"));
    }

    @Test
    public void normaliserOfReturnsNormalisersForUsers()
    {
        Function<User, String> us = NameComparator.normaliserOf(User.class);
        User userA = mock(User.class);
        when(userA.getName()).thenReturn("A");
        assertEquals("a", us.apply(userA));
    }

    @Test
    public void normaliserOfReturnsNormalisersForGroups()
    {
        Function<Group, String> us = NameComparator.normaliserOf(Group.class);
        Group groupA = mock(Group.class);
        when(groupA.getName()).thenReturn("A");
        assertEquals("a", us.apply(groupA));
    }

    @Test(expected = IllegalArgumentException.class)
    public void normaliserOfFailsForUnknownTypes()
    {
        NameComparator.normaliserOf(Object.class);
    }
}
