/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 11:27:19 AM
 * CVS Revision: $Revision: 1.10 $
 * Last CVS Commit: $Date: 2005/10/04 02:41:53 $
 * Author of last CVS Commit: $Author: nfaiz $
 */
package com.atlassian.core.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import junit.framework.TestCase;

public class TestUserPreferences extends TestCase
{
    private final long SET_USER_ISSUES_PER_PAGE_VALUE = 1000;
    private User fred;
    private User bob;

    public TestUserPreferences(String s) throws Exception
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        fred = UserManager.getInstance().createUser("fred");
        bob = UserManager.getInstance().createUser("bob");
    }

    protected void tearDown() throws Exception
    {
        fred.remove();
        bob.remove();
        super.tearDown();
    }

    public void testGettingNonExistentPreference()
    {
        Preferences preferences = DefaultPreferences.getPreferences();

        assertEquals(0, preferences.getLong("this preference does not exist"));
    }

    public void testGettingMaxIssuesPreferenceNoUser()
    {
        User user = null;
        Preferences preferences = new UserPreferences(user);

        assertEquals(DefaultPreferences.getPreferences().getLong("test.needs.fixing"), preferences.getLong("test.needs.fixing"));
    }

    public void testSettingMaxIssuesPreferenceNoUser()
    {
        User user = null;
        Preferences preferences = new UserPreferences(user);

        boolean exceptionThrown = false;

        try
        {
            preferences.setLong("test.needs.fixing", SET_USER_ISSUES_PER_PAGE_VALUE);
        }
        catch (AtlassianCoreException e)
        {
            exceptionThrown = true;
        }

        assertTrue("The exception should have been thrown because you are not allowed to set a property when there is no user", exceptionThrown);
    }

    public void testGettingMaxIssuesPreferenceWithUser() throws Exception
    {
        Preferences preferences = new UserPreferences(fred);

        assertEquals(DefaultPreferences.getPreferences().getLong("test.needs.fixing"), preferences.getLong("test.needs.fixing"));

    }

    public void testSettingMaxIssuesPreferenceWithUser() throws Exception
    {
        Preferences preferences = new UserPreferences(fred);

        try
        {
            preferences.setLong("test.needs.fixing", SET_USER_ISSUES_PER_PAGE_VALUE);
        }
        catch (AtlassianCoreException e)
        {
            assertTrue("This exception should not have been thrown", false);
        }

        assertEquals(preferences.getLong("test.needs.fixing"), SET_USER_ISSUES_PER_PAGE_VALUE);

        // Ensure the preference was persisted and can be retrieved again
        preferences = new UserPreferences(fred);
        assertEquals(preferences.getLong("test.needs.fixing"), SET_USER_ISSUES_PER_PAGE_VALUE);
    }

    public void testHashCodeEquals()
    {
        Preferences fredPrefs = new UserPreferences(fred);
        Preferences bobPrefs = new UserPreferences(bob);

        assertTrue(fredPrefs.equals(fredPrefs));
        assertTrue(!fredPrefs.equals(fred));

        Preferences guestPrefs = new UserPreferences();
        assertTrue(guestPrefs.equals(guestPrefs));

        assertEquals(fredPrefs.hashCode(), fredPrefs.hashCode());
    }
}
