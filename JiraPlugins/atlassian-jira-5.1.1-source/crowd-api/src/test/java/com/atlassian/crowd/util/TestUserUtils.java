package com.atlassian.crowd.util;

import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import junit.framework.TestCase;

public class TestUserUtils extends TestCase
{
    public void testIsValidEmail_success()
    {
        assertTrue(UserUtils.isValidEmail("test@example.com"));
    }

    public void testIsValidEmail_failure()
    {
        assertFalse(UserUtils.isValidEmail(""));
    }

    // RFC-2822 allows these
    public void testIsValidEmail_invalidTLD()
    {
        assertTrue(UserUtils.isValidEmail("john@aol.com.nospam"));
    }

    public void testIsValidEmail_plusSign()
    {
        assertTrue(UserUtils.isValidEmail("john+@aol.com.nospam"));
    }

    public void testIsValidEmail_ipAddress()
    {
        assertTrue(UserUtils.isValidEmail("don@[18.138.9.10]"));
    }

    public void testIsValidEmail_fullName()
    {
        assertTrue(UserUtils.isValidEmail("\"Don Somebody\" <don@[18.138.9.10]>"));
        assertTrue(UserUtils.isValidEmail("\u201cDon Somebody\u201d <don@[18.138.9.10]>"));
    }

    public void testIsValidEmail_null()
    {
        assertFalse(UserUtils.isValidEmail(null));
    }

    public void testGetFirstNameLastName_simple()
    {
        String[] fnln = UserUtils.getFirstNameLastName("Jon Bob");
        assertEquals("Jon", fnln[0]);
        assertEquals("Bob", fnln[1]);
    }

    public void testGetFirstNameLastName_longLastName()
    {
        String[] fnln = UserUtils.getFirstNameLastName("Jon Bob Smithson");
        assertEquals("Jon", fnln[0]);
        assertEquals("Bob Smithson", fnln[1]);
    }

    public void testGetFirstNameLastName_spaceyLastName()
    {
        String[] fnln = UserUtils.getFirstNameLastName("Jon   Smithson  Bob ");
        assertEquals("Jon", fnln[0]);
        assertEquals("Smithson  Bob", fnln[1]);
    }

    public void testGetFirstNameLastName_leadingSpace()
    {
        String[] fnln = UserUtils.getFirstNameLastName("  Jon   Smithson  Bob ");
        assertEquals("Jon", fnln[0]);
        assertEquals("Smithson  Bob", fnln[1]);
    }

    public void testGetFirstNameLastName_noFirstName()
    {
        String[] fnln = UserUtils.getFirstNameLastName("Jon");
        assertEquals("", fnln[0]);
        assertEquals("Jon", fnln[1]);
    }

    public void testGetFirstNameLastName_blank()
    {
        String[] fnln = UserUtils.getFirstNameLastName("");
        assertEquals("", fnln[0]);
        assertEquals("", fnln[1]);
    }

    public void testGetFirstNameLastName_null()
    {
        String[] fnln = UserUtils.getFirstNameLastName(null);
        assertEquals("", fnln[0]);
        assertEquals("", fnln[1]);
    }

    public void testGetFirstName()
    {
        assertEquals("Tom", UserUtils.getFirstName("Tom", null));
        assertEquals("", UserUtils.getFirstName("", "Tom"));
        assertEquals("Tom", UserUtils.getFirstName("", "Tom Jones"));
        assertEquals("", UserUtils.getFirstName("", ""));
        assertEquals("", UserUtils.getFirstName(null, null));
    }

    public void testGetLastName()
    {
        assertEquals("Jones", UserUtils.getLastName("Jones", "blah"));
        assertEquals("Jones", UserUtils.getLastName("", "Tom Jones"));
        assertEquals("Tom", UserUtils.getLastName("", "Tom"));
        assertEquals("displayname", UserUtils.getLastName("", "displayname"));
        assertEquals("displayname", UserUtils.getLastName(null, "displayname"));
    }

    public void testGetDisplayNameFromDisplayName()
    {
        assertEquals("Tom Jones", UserUtils.getDisplayName("Tom Jones", "Something", "Different", "tjones"));
        assertEquals("Tom Jones", UserUtils.getDisplayName("Tom Jones", "", "", "tjones"));
    }

    public void testGetDisplayNameFromFirstNameAndLastName()
    {
        assertEquals("Tom Jones", UserUtils.getDisplayName(" ", "Tom", "Jones", "tjones"));
        assertEquals("Tom Jones", UserUtils.getDisplayName(null, "Tom", "Jones", "tjones"));
    }

    public void testGetDisplayNameFromFirstName()
    {
        assertEquals("Tom", UserUtils.getDisplayName(" ", "Tom", " ", "tjones"));
        assertEquals("Tom", UserUtils.getDisplayName(null, "Tom", null, "tjones"));
    }

    public void testGetDisplayNameFromLastName()
    {
        assertEquals("Jones", UserUtils.getDisplayName(" ", " ", "Jones", "tjones"));
        assertEquals("Jones", UserUtils.getDisplayName(null, null, "Jones", "tjones"));
    }

    public void testGetDisplayNameFromUsername()
    {
        assertEquals("tjones", UserUtils.getDisplayName("", "", "", "tjones"));
        assertEquals("tjones", UserUtils.getDisplayName(null, null, null, "tjones"));
    }

    public void testGetDisplayNameEmptyUsername()
    {
        try
        {
            UserUtils.getDisplayName("", "", "", " ");
            fail("Should not accept blank username");
        }
        catch (IllegalArgumentException e)
        {
            // Success
        }
    }

    public void testGetDisplayNameNullUsername()
    {
        try
        {
            UserUtils.getDisplayName("", "", "", null);
            fail("Should not accept null username");
        }
        catch (IllegalArgumentException e)
        {
            // Success
        }
    }

    private void assertNamesMatch(User expected, User actual)
    {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
    }

    private void assertNamesConsistent(User user)
    {
        assertEquals(UserUtils.getFirstName("", user.getDisplayName()), user.getFirstName());
        assertEquals(UserUtils.getLastName("", user.getDisplayName()), user.getLastName());
        assertEquals(UserUtils.getDisplayName("", user.getFirstName(), user.getLastName(), user.getName()), user.getDisplayName());
    }

    public void testPopulateNames_alreadyPrePopulatedUser()
    {
        User user = new UserTemplate("jsmith", "John", "Smith", "JohnSmith");
        User populatedUser = UserUtils.populateNames(user);

        assertNamesMatch(user, populatedUser);
    }

    public void testPopulateNames_displayNamePrePopulated()
    {
        User user = new UserTemplate("jsmith", "", "", "John Smith");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "John", "Smith", "John Smith");

        assertNamesMatch(expectedUser, populatedUser);
        assertNamesConsistent(populatedUser);
    }

    public void testPopulateNames_firstNameAndLastNamePrePopulated()
    {
        User user = new UserTemplate("jsmith", "John", "Smith", "");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "John", "Smith", "John Smith");

        assertNamesMatch(expectedUser, populatedUser);
        assertNamesConsistent(populatedUser);
    }

    public void testPopulateNames_displayNamePrePopulatedWithSpaces()
    {
        User user = new UserTemplate("jsmith", "", "", "  John  Smith");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "John", "Smith", "  John  Smith");

        assertNamesMatch(expectedUser, populatedUser);
        // is not consistent because the generated display name is not a concat of first and last names,
        // but this is ok, because it's the best way to come up with a first and last name
        // assertNamesConsistent(populatedUser);
    }

    public void testPopulateNames_lastNamePrePopulated()
    {
        User user = new UserTemplate("jsmith", "", "Smith", "");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "", "Smith", "Smith");

        assertNamesMatch(expectedUser, populatedUser);
        assertNamesConsistent(populatedUser);
    }

    /**
     * When a user has only a last name we may generate a displayName but we shouldn't
     * then split it down into a firstName + lastName pair.
     */
    public void testPopulateNames_onlyUsernameAndLastNameDoesNotResultInAFirstNameBeingSet()
    {
        UserTemplate template = new UserTemplate("user", null, "Van Buren", "");
        
        User user = UserUtils.populateNames(template);
        
        assertEquals("Van Buren", user.getLastName());
        assertEquals("", user.getFirstName());
        assertEquals("Van Buren", user.getDisplayName());
    }

    public void testPopulateNames_firstNamePrePopulated()
    {
        User user = new UserTemplate("jsmith", "John", "", "");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "John", "John", "John");

        assertNamesMatch(expectedUser, populatedUser);

        // is not consistent because the generated display name is not a concat of first and last names,
        // but this is ok, because we are only given the first name and we must populate the surname and displayName
        //assertNamesConsistent(populatedUser);
    }

    public void testPopulateNames_blankPrePopulated()
    {
        User user = new UserTemplate("jsmith", "", "", "");
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "", "jsmith", "jsmith");

        assertNamesMatch(expectedUser, populatedUser);
        assertNamesConsistent(populatedUser);
    }

    public void testPopulateNames_nullPrePopulated()
    {
        User user = new UserTemplate("jsmith", null, null, null);
        User populatedUser = UserUtils.populateNames(user);
        User expectedUser = new UserTemplate("jsmith", "", "jsmith", "jsmith");

        assertNamesMatch(expectedUser, populatedUser);
        assertNamesConsistent(populatedUser);
    }
}
