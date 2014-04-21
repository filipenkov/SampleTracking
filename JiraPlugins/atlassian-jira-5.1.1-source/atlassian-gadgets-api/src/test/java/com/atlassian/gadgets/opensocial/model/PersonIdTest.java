package com.atlassian.gadgets.opensocial.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PersonIdTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testThatEmptyPersonIdThrowsException()
    {
        assertEquals(new PersonId("").value(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatBlankPersonIdThrowsException()
    {
        assertEquals(new PersonId("  ").value(), "  ");
    }

    @Test
    public void testThatUnderscoreIsAccepted()
    {
        assertEquals(new PersonId("foo_").value(), "foo_");
    }

    @Test
    public void testThatDotIsAccepted()
    {
        assertEquals(new PersonId("foo.").value(), "foo.");
    }

    @Test
    public void testThatDashIsAccepted()
    {
        assertEquals(new PersonId("foo-").value(), "foo-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatIllegalCharactersAreRejected()
    {
        assertEquals(new PersonId("^-^").value(), "^-^");
    }

}
