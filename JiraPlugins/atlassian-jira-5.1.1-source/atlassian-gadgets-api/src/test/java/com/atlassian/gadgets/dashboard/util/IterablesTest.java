package com.atlassian.gadgets.dashboard.util;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IterablesTest
{
    @Test
    public void assertElementsEqualWithTwoEmptyIterablesReturnsTrue()
    {
        assertTrue(Iterables.elementsEqual(emptySet(), emptySet()));
    }

    @Test
    public void assertElementsEqualWithTwoNullIterablesReturnsTrue()
    {
        assertTrue(Iterables.elementsEqual(null, null));
    }

    @Test
    public void assertElementsEqualWithTwoIterablesWithEqualElementsReturnsTrue()
    {
        assertTrue(Iterables.elementsEqual(asList("One", "Two", "Three"), asList("One", "Two", "Three")));
    }

    @Test
    public void assertElementsEqualWithTwoIterablesWithNonEqualElementsReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(asList("One", "Two", "Three"), asList("Uno", "Dos", "Tres")));
    }

    @Test
    public void assertElementsEqualWithFirstEmptySecondNonEmptyReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(emptySet(), asList("One", "Two", "Three")));
    }

    @Test
    public void assertElementsEqualWithFirstNonEmptySecondEmptyReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(asList("One", "Two", "Three"), emptySet()));
    }

    @Test
    public void assertElementsEqualWithFirstLongerThanSecondReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(asList("One", "Two", "Three"), asList("One", "Two")));
    }

    @Test
    public void assertElementsEqualWithFirstShorterThanSecondReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(asList("One", "Two"), asList("One", "Two", "Three")));
    }

    @Test
    public void assertElementsEqualWithFirstNullAndSecondNonNullReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(null, asList("One", "Two", "Three")));
    }

    @Test
    public void assertElementsEqualWithFirstNonNullAndSecondNullReturnsFalse()
    {
        assertFalse(Iterables.elementsEqual(asList("One", "Two", "Three"), null));
    }

    @Test
    public void assertCheckContentsNotNullWithEmptyIterableSucceeds()
    {
        Iterables.checkContentsNotNull(emptySet());
    }

    @Test
    public void assertCheckContentsNotNullWithIterableContainingNonNullSucceeds()
    {
        Iterables.checkContentsNotNull(asList("non-null"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertCheckContentsNotNullWithNullIterableThrowsIllegalArgumentException()
    {
        Iterables.checkContentsNotNull(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertCheckContentsNotNullWithIterableContainingNullThrowsIllegalArgumentException()
    {
        Iterables.checkContentsNotNull(asList(new Object[]{null}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertCheckContentsNotNullWithIterableContainingNonNullThenNullThrowsIllegalArgumentException()
    {
        Iterables.checkContentsNotNull(asList("non-null", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertCheckContentsNotNullWithIterableContainingNullThenNonNullThrowsIllegalArgumentException()
    {
        Iterables.checkContentsNotNull(asList(null, "non-null"));
    }
}
