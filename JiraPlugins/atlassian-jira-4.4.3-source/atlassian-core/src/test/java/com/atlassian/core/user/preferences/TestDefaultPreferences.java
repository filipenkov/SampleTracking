/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 11:05:07 AM
 * CVS Revision: $Revision: 1.6 $
 * Last CVS Commit: $Date: 2003/09/30 07:05:53 $
 * Author of last CVS Commit: $Author: mcannon $
 */
package com.atlassian.core.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import junit.framework.TestCase;

public class TestDefaultPreferences extends TestCase
{
    private final long SET_USER_ISSUES_PER_PAGE_VALUE = 1000;

    public TestDefaultPreferences(String s)
    {
        super(s);
    }

    public void testPreferencesNotNull()
    {
        assertNotNull(DefaultPreferences.getPreferences());
    }

    public void testGettingLongPreference()
    {
        Preferences preferences = DefaultPreferences.getPreferences();

        assertEquals(20, preferences.getLong("test.long.preferences"));
    }

    public void testGettingStringPreference()
    {
        Preferences preferences = DefaultPreferences.getPreferences();

        assertEquals("aString", preferences.getString("test.string.preferences"));
    }

    public void testSettingDefaultMaxIssuesPreference()
    {
        Preferences preferences = DefaultPreferences.getPreferences();
        boolean exceptionThrown = false;

        try
        {
            preferences.setLong("test.needs.fixing", SET_USER_ISSUES_PER_PAGE_VALUE);
        }
        catch (AtlassianCoreException e)
        {
            exceptionThrown = true;
        }

        assertTrue("The exception should have been thrown because you are not allowed to set a default property", exceptionThrown);
    }

    public void testGettingNonExistentPreference()
    {
        Preferences preferences = DefaultPreferences.getPreferences();

        assertEquals(0, preferences.getLong("this preference does not exist"));
    }
}
