package com.atlassian.jira.whatsnew;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.welcome.WelcomeUserPreferenceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWhatsNewManager extends TestCase
{
    private static final String DONT_SHOW_VERSION = "jira.user.whats.new.dont.show.version";
    private WhatsNewManager whatsNewManager;
    private User user = new MockUser("user");

    @Mock private UserPropertyManager mockUserPropertyManager;
    @Mock private PropertySet mockPS;
    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private WelcomeUserPreferenceManager mockWelcomeManager;

    public void setUp() throws Exception
    {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        when(mockUserPropertyManager.getPropertySet(user)).thenReturn(mockPS);
        when(mockWelcomeManager.isShownForUser(user)).thenReturn(false);

        whatsNewManager = new WhatsNewManager(mockApplicationProperties, mockUserPropertyManager, mockWelcomeManager);
    }

    public void tearDown() throws Exception
    {
        whatsNewManager = null;
        mockApplicationProperties = null;
        mockUserPropertyManager = null;
        mockPS = null;
        user = null;
        super.tearDown();
    }

    public void testShownForUserIfNoPreference()
    {
        // This mock just making it clear that the user pref has not been set
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn(null);
        when(mockApplicationProperties.getVersion()).thenReturn("3.5");
        assertTrue(whatsNewManager.isShownForUser(user, false));
    }

    public void testShownForUserIfPreferenceUnset()
    {
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn("UNSET");
        when(mockApplicationProperties.getVersion()).thenReturn("3.5");
        assertTrue(whatsNewManager.isShownForUser(user, false));
    }

    public void testNotShownForUserIfPreferenceMatches() throws AtlassianCoreException
    {
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn("3.5");
        when(mockApplicationProperties.getVersion()).thenReturn("3.5");
        assertFalse(whatsNewManager.isShownForUser(user, false));
    }

    public void testNotShownForUserIfPreferenceMatchesMinorVersion() throws AtlassianCoreException
    {
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn("3.5");
        when(mockApplicationProperties.getVersion()).thenReturn("3.5.1");
        assertFalse(whatsNewManager.isShownForUser(user, false));
    }

    public void testShownForUserIfPreferenceDifferent() throws AtlassianCoreException
    {
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn("3.5");
        when(mockApplicationProperties.getVersion()).thenReturn("3.4.0");
        assertTrue(whatsNewManager.isShownForUser(user, false));
    }

    public void testSetShownTrue() throws Exception
    {
        when(mockPS.getString(DONT_SHOW_VERSION)).thenReturn("3.5");
        whatsNewManager.setShownForUser(user, true);
        verify(mockPS).setString(DONT_SHOW_VERSION, "UNSET");
    }

    public void testSetShownFalse() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5-SNAPSHOT");
        whatsNewManager.setShownForUser(user, false);
        verify(mockPS).setString(DONT_SHOW_VERSION, "3.5");
    }
}
