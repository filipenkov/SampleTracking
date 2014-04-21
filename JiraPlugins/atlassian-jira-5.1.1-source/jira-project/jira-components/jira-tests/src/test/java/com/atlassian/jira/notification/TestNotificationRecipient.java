package com.atlassian.jira.notification;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestNotificationRecipient
{
    private final static String DEFAULT_FORMAT = NotificationRecipient.MIMETYPE_TEXT;

    @Mock private UserPropertyManager userPropertyManager;

    @Before
    public void setUp()
    {
        MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(UserPropertyManager.class, userPropertyManager);
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void testEmail()
    {
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(null));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertEquals(user.getEmailAddress(), notificationRecipient.getEmail());
    }

    @Test
    public void testValidFormat()
    {
        final String format = NotificationRecipient.MIMETYPE_HTML;
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(format));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertEquals(format, notificationRecipient.getFormat());
    }

    @Test
    public void testInvalidFormat()
    {
        final String format = "blahblah";
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(format));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertEquals(DEFAULT_FORMAT, notificationRecipient.getFormat());
    }

    @Test
    public void testNullFormat()
    {
        final String format = null;
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(format));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertEquals(DEFAULT_FORMAT, notificationRecipient.getFormat());
    }

    @Test
    public void testIsHtml()
    {
        final String format = NotificationRecipient.MIMETYPE_HTML;
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(format));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertTrue(notificationRecipient.isHtml());
    }

    @Test
    public void testIsNotHtml()
    {
        final String format = NotificationRecipient.MIMETYPE_TEXT;
        final User user = createMockUser();
        when(userPropertyManager.getPropertySet(user)).thenReturn(createPropertySet(format));
        final NotificationRecipient notificationRecipient = new NotificationRecipient(user);
        assertFalse(notificationRecipient.isHtml());
    }

    private User createMockUser()
    {
        return new MockUser("bob", "Bob", "bob@bob.com");
    }

    private PropertySet createPropertySet(final String format)
    {
        final MapPropertySet ps = new MapPropertySet();
        ps.setMap(EasyMap.build(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, format));
        return ps;
    }
}
