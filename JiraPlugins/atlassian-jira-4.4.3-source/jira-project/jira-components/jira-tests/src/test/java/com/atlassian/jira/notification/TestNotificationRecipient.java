package com.atlassian.jira.notification;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.TestAbstractJiraHome;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.core.util.collection.EasyList;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;

import java.util.Iterator;
import java.util.List;

public class TestNotificationRecipient extends AbstractUsersTestCase
{
    private User bob = null;
    private User notBob = null;
    private NotificationRecipient notificationRecipient;
    private NotificationRecipient notificationRecipient2;
    private NotificationRecipient notificationRecipientBobAlias;
    private NotificationRecipient notBobNR;
    private Group bobsGroup1;
    private Group bobsGroup2;
    private User bobAlias;

    public TestNotificationRecipient(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        bob = UtilsForTests.getTestUser("This is Bob");
        bob.setEmail("bobs@email.address");
        bob.addToGroup(bobsGroup1);
        bob.addToGroup(bobsGroup2);

        notBob = UtilsForTests.getTestUser("NotReallyBob");
        notBob.setEmail("not@bob.com");
        bobsGroup1 = UtilsForTests.getTestGroup("Bob's Group 1");
        bobsGroup2 = UtilsForTests.getTestGroup("Bob's Group 2");

        bobAlias = UtilsForTests.getTestUser("This is Bob's alias");
        bobAlias.setEmail("bobs@email.address");

        notificationRecipient = new NotificationRecipient(bob);
        notificationRecipient2 = new NotificationRecipient(bob);
        notificationRecipientBobAlias = new NotificationRecipient(bobAlias);

        notBobNR = new NotificationRecipient(notBob);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetters()
    {
        assertEquals(bob.getEmail(), notificationRecipient.getEmail());

        List bobsGroups = bob.getGroups();

        for (Iterator bobsGroupIter = bobsGroups.iterator(); bobsGroupIter.hasNext();)
        {
            String group = (String) bobsGroupIter.next();
            assertTrue(notificationRecipient.isInGroup(group));
        }
    }

    public void testGetUserNotNull() throws Exception
    {
        assertEquals(bob, notificationRecipient.getUser());
        assertEquals(bob, notificationRecipient2.getUser());
        assertEquals(bobAlias, notificationRecipientBobAlias.getUser());
        assertEquals(notBob, notBobNR.getUser());
    }

    public void testGetUserNull() throws Exception
    {
        assertNull(new NotificationRecipient("test@example.com").getUser());
    }

    public void testEquals()
    {
        //notification recipients with the same user are the same
        assertTrue(notificationRecipient.equals(notificationRecipient));
        assertTrue(notificationRecipient.equals(notificationRecipient2));

        //notification recipients with same email but different users are different
        assertFalse(notificationRecipient.equals(notificationRecipientBobAlias));

        //notification recipients with the differe user are the different
        assertFalse(notificationRecipient.equals(notBobNR));
    }

    public void testHashCode()
    {
        //notification recipients with the same user are the same
        assertEquals(notificationRecipient.hashCode(), notificationRecipient2.hashCode());

        //notification recipients with same email but different users are different
        assertFalse(notificationRecipient.hashCode() == notificationRecipientBobAlias.hashCode());

        //notification recipients with the differe user are the different
        assertFalse(notificationRecipient.hashCode() == notBobNR.hashCode());
    }

    public void testListEntry()
    {
        List addresses = EasyList.build(notificationRecipient, notBobNR);
        assertTrue(addresses.contains(new NotificationRecipient(bob)));
    }

    public void testFormat()
    {
        // Default to text
        NotificationRecipient recipient = new NotificationRecipient(bob);
        assertEquals(NotificationRecipient.MIMETYPE_TEXT, recipient.getFormat());

        ApplicationProperties ap = new ApplicationPropertiesImpl(
                new ApplicationPropertiesStore(PropertiesManager.getInstance(),
                new TestAbstractJiraHome.FixedHome()));


        ap.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
        recipient = new NotificationRecipient(bob);
        assertEquals(NotificationRecipient.MIMETYPE_HTML, recipient.getFormat());

        ap.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "stuff");
        recipient = new NotificationRecipient(bob);
        assertEquals(NotificationRecipient.MIMETYPE_TEXT, recipient.getFormat());

        ap.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, null);
        recipient = new NotificationRecipient(bob);
        assertEquals(NotificationRecipient.MIMETYPE_TEXT, recipient.getFormat());
    }

}
